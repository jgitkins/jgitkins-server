# 리팩토링 분석서

### 1. 분석 대상 개요 (Overview)
- **분석 대상**: `JobDispatchService` 및 연관 코드
  - `src/main/java/io/jgitkins/server/application/service/JobDispatchService.java`
  - `src/main/java/io/jgitkins/server/application/port/in/JobDispatchUseCase.java`
  - `src/main/java/io/jgitkins/server/presentation/api/grpc/JobDispatchGrpcController.java`
  - `src/main/java/io/jgitkins/server/application/dto/JobDispatchMessage.java`
  - `src/main/java/io/jgitkins/server/application/dto/PendingJob.java`
  - `src/main/java/io/jgitkins/server/application/dto/RunnerAssignmentCandidate.java`
  - `src/main/java/io/jgitkins/server/presentation/dto/RunnerJobFetchRequest.java`
  - `src/main/java/io/jgitkins/server/application/port/out/JobPersistencePort.java`
  - `src/main/java/io/jgitkins/server/infrastructure/adapter/persistence/JobPersistenceAdapter.java`
- **분석 배경**: Runner가 Job을 가져가는 dispatch 경로는 현재 동작 자체는 단순하지만, 입력 모델의 계층 위치, 서비스 내부의 책임 혼합, persistence adapter의 조회 정책 단순화, gRPC 어댑터와의 데이터 전달 구조가 함께 얽혀 있어 이후 네이밍 변경이나 구조 개선 시 변경 전파 범위가 넓다. 따라서 실제 구현 리팩토링 전에 현재 구조를 정리하고, 어떤 수준까지 간결화할지 기준을 먼저 문서화할 필요가 있다.

### 2. 현행 시스템 구조 및 동작 방식 (AS-IS Architecture)
- **동작 흐름**
  - gRPC 어댑터인 `JobDispatchGrpcController`가 `JobDispatchRequest.runnerToken`을 받아 `RunnerJobFetchRequest`를 생성한다.
  - `JobDispatchUseCase.fetchJob(RunnerJobFetchRequest)`가 application service인 `JobDispatchService`로 위임된다.
  - `JobDispatchService`는 `RunnerPersistencePort.findByToken(...)`으로 runner를 조회하고 `RunnerAssignmentCandidate`를 조립한다.
  - `JobPersistencePort.findPendingByCandidate(...)`가 pending job을 조회하고, `PendingJob`으로 job aggregate와 부가 정보(`organizeId`, `repositoryClonePath`)를 함께 반환한다.
  - `JobDispatchService`는 `job.publish(runnerId)`로 상태를 변경하고, `saveHistory(...)`로 최신 히스토리를 저장한 뒤 `JobDispatchMessage`를 조립하여 gRPC 응답 payload로 전달한다.
- **의존성 및 결합도**
  - `JobDispatchUseCase`가 presentation DTO인 `RunnerJobFetchRequest`에 직접 의존하고 있다.
    - 예: application input port가 `presentation.dto` 패키지 타입을 받는다.
  - `JobDispatchService`가 runner 조회, 후보 조립, job 상태 변경, history 저장, dispatch message 조립, clone URL 생성까지 모두 수행한다.
  - `JobPersistenceAdapter.findPendingByCandidate(...)`는 조회와 스코프 판정 정책을 함께 가진다.
    - 예: 최신 history 조회, pending 판별, repository 조회, organizeId/clonePath 조립을 한 메서드에서 수행한다.
  - `JobDispatchGrpcController`는 application DTO를 gRPC payload로 다시 변환한다.
    - 예: `JobDispatchMessage` -> `JobPayload`

### 3. 주요 문제점 식별 (Problem Identification)
- **코드 품질**
  - 서비스 내부 책임이 과밀하다.
    - 예: `fetchJob()` 한 유스케이스 안에 `runnerToken` 검증, runner 조회, candidate 생성, pending job 조회, 상태 전이, 응답 메시지 조립이 모두 들어 있다.
  - 메서드명과 도메인 의미가 일부 어긋난다.
    - 예: `fetchJob()`은 실제로 단순 조회가 아니라 runner 할당과 상태 전이를 포함한 dispatch 수행이다.
    - 예: `publishDispatchMessage()`는 현재 실제 publish를 하지 않고 DTO를 조립해 반환만 한다.
  - `parseJobId()`가 문자열 기반 ID 설계의 누수를 흡수하는 임시 로직처럼 보인다.
    - 예: `NumberFormatException` 발생 시 `null` 반환
  - DTO 명칭이 역할을 충분히 드러내지 못한다.
    - 예: `PendingJob`은 단순 pending job이 아니라 dispatch 시 필요한 repository clone path, organizeId를 포함한 조회 projection이다.
    - 예: `RunnerAssignmentCandidate`는 조회 조건과 runner 식별 정보를 동시에 담고 있다.
- **성능 이슈**
  - `JobPersistenceAdapter.findPendingByCandidate(...)`는 현재 전체 job 후보를 읽고, 각 job마다 latest history를 다시 조회하는 방식이다.
    - 예: pending job이 많아질수록 반복 조회 비용이 증가한다.
  - 스코프 필터링이 SQL 수준에서 충분히 걸리지 않고 adapter 로직에 남아 있다.
    - 예: 주석에 `For now, let's just find any PENDING job.`가 남아 있다.
  - dispatch 경쟁 상황을 `saveHistory()`의 낙관적 체크에 의존하고 있어, 불필요한 조회 후 실패가 발생할 수 있다.
- **구조적 한계**
  - application port가 presentation 모델에 의존한다.
    - 예: `JobDispatchUseCase.fetchJob(RunnerJobFetchRequest request)`
  - application service가 응답 전용 DTO 조립까지 맡아 inbound adapter와의 경계가 흐려져 있다.
    - 예: `JobDispatchMessage`는 사실상 gRPC payload 근접 모델이다.
  - persistence port가 "도메인 저장소"라기보다 "dispatch 전용 조회 프로젝션"까지 함께 반환하고 있어 포트 의미가 넓다.
    - 예: `findPendingByCandidate()`가 `Job`만이 아니라 clone path, organizeId를 얹은 `PendingJob`을 반환

### 4. 영향도 분석 (Impact Analysis)
- **비즈니스 영향도**
  - runner dispatch 경로는 실제 빌드 실행 시작점이므로, 구조가 불명확하면 장애 시 원인 추적과 수정 범위 판단이 느려진다.
  - pending job 조회 정책이 모호하면 runner가 의도하지 않은 job을 선점하거나, 특정 scope의 job이 지연될 수 있다.
  - 응답 모델과 내부 모델이 섞여 있으면 gRPC 스펙 변경 시 application service까지 불필요하게 흔들릴 가능성이 높다.
- **기술적 파급 효과**
  - 영향 대상은 최소 다음 범위다.
    - `JobDispatchGrpcController`
    - `JobDispatchUseCase`
    - `JobDispatchService`
    - `JobPersistencePort` / `JobPersistenceAdapter`
    - `RunnerPersistencePort`
    - `CloneUrlBuilder`
    - `JobDispatchServiceTest`
  - `JobResultReportService`와 runner 조회 패턴이 유사하므로, runner token 해석 책임을 재배치하면 후속 정리 대상이 될 수 있다.
  - `JobDispatchMessage`의 구조를 바꾸면 gRPC 응답 변환 로직과 테스트가 함께 수정된다.

### 5. 개선 방향성 (TO-BE Direction)
- **검토한 방법**
  - 방법 1. `JobDispatchService` 내부 메서드 분리만 수행
    - 장점: 변경 범위가 가장 작다.
    - 단점: 계층 경계 문제와 DTO 역할 혼합은 그대로 남는다.
  - 방법 2. application 입력/출력 모델과 port 의미를 재정렬하고, 서비스는 dispatch 오케스트레이션에 집중하도록 단순화
    - 장점: 네이밍, 책임, 테스트 경계가 함께 정리된다.
    - 단점: 포트/DTO/어댑터 시그니처 변경이 필요하다.
  - 방법 3. dispatch 조회를 전용 query service 또는 CQRS read model로 분리
    - 장점: 장기적으로 가장 명확하다.
    - 단점: 현재 코드베이스 규모 대비 과한 분리일 수 있고 변경 범위가 크다.
- **선택 방향**
  - 현재 단계에서는 **방법 2**가 가장 적합하다.
  - 이유: 단순 메서드 분리만으로는 핵심 문제인 경계 혼합을 해결하지 못하고, 반대로 CQRS 수준 분리는 현재 요구 대비 과하다.
<!-- 선택안 외 나머지방안 취소선처리 (가독성) -->
- **목표 아키텍처**
  - application input port는 presentation DTO를 받지 않고 application command를 입력으로 받는다.
    - 예: `DispatchJobCommand` 또는 `RunnerDispatchRequest`
  - application service는 "runner 적격성 확인 -> dispatch 대상 조회 -> 상태 전이 -> dispatch result 반환"에 집중한다.
  - outbound port는 저장소/조회 의도를 이름에 드러내고, dispatch 전용 projection은 의도에 맞는 이름으로 반환한다.
    - 예: `PendingDispatchJob`, `DispatchableJobView`
  - gRPC controller는 request/response 변환만 담당한다.
- **핵심 개선 전략**
  - 입력 모델 경계 정리
    - AS-IS 예시: `JobDispatchUseCase.fetchJob(RunnerJobFetchRequest request)`
    - TO-BE 예시: `JobDispatchUseCase.dispatch(DispatchJobCommand command)`
  - 메서드 네이밍을 실제 행위 중심으로 정리
    - AS-IS 예시: `fetchJob`, `publishDispatchMessage`
    - TO-BE 예시: `dispatchNextJob`, `buildDispatchResult`
  - dispatch 후보/조회 projection 명칭 정리
    - AS-IS 예시: `PendingJob`, `RunnerAssignmentCandidate`
    - TO-BE 예시: `DispatchableJob`, `RunnerDispatchContext`
  - persistence adapter의 조회 책임을 명시화
    - 예: "pending latest history 조회"와 "scope 매칭"을 한 메서드에 섞지 않고, 최소한 포트 시그니처와 명칭에서 dispatch 조회 의도를 드러내도록 정리
  - 테스트도 행위 기준으로 재구성
    - 예: "runner 없음", "dispatch 대상 없음", "경합으로 선점 실패", "dispatch 결과 반환" 시나리오를 메서드명과 동일한 용어로 정리

### 6. 위험 요소 및 고려사항 (Risk Assessment)
- **예상 리스크**
  - `JobDispatchUseCase` 시그니처 변경 시 gRPC controller와 테스트가 동시에 깨질 수 있다.
  - `PendingJob`/`RunnerAssignmentCandidate` rename 또는 구조 변경 시 `JobPersistenceAdapter` 조회 로직을 함께 수정해야 한다.
  - `fetchJob`를 `dispatch` 의미로 재정의하면 외부 호출자와 문서의 용어도 함께 맞춰야 한다.
  - `Job.publish()`가 실제로는 `IN_PROGRESS` history를 쌓는 구조이므로, 네이밍 정리 범위가 aggregate까지 확장될 가능성이 있다.
- **테스트 전략**
  - 현재 `JobDispatchServiceTest`는 빈 토큰, runner 없음, pending job 없음 정도만 커버한다. 리팩토링 전 다음 시나리오를 우선 보강해야 한다.
    - dispatch 성공 시 `saveHistory()`가 호출되고 응답이 반환되는지 검증
    - `saveHistory()`가 empty를 반환하는 경합 상황에서 empty가 반환되는지 검증
    - clone URL 조립과 응답 매핑이 기대값과 일치하는지 검증
  - gRPC 어댑터 테스트 또는 최소한 mapper 성격의 단위 테스트가 있으면 시그니처 변경 회귀를 줄일 수 있다.

### 결론 (추후 작성)
- 본 분석 기준으로 `JobDispatchService` 리팩토링은 단순 메서드 정리 수준이 아니라, `application input model`, `dispatch projection`, `service method naming`, `grpc adapter mapping`을 함께 정리하는 범위로 보는 것이 타당하다.
- 구현 단계에서는 "작은 구조 개선"을 우선 적용한다.
  - 1단계: 입력 모델과 메서드명 정리
  - 2단계: dispatch projection 명칭/반환 구조 정리
  - 3단계: persistence 조회 책임과 테스트 보강

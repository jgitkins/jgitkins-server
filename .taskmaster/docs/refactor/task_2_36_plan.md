# 리팩토링 계획서

### 제목
- **리팩토링 계획**: `JobDispatchService` 연관 코드 간결화 리팩토링 계획 수립하였음

### 배경 (왜?)
- 현재 `JobDispatchService`는 runner token 검증, runner 조회, dispatch 후보 조립, job 상태 전이, history 저장, 응답 DTO 조립까지 함께 수행하고 있어 응집도가 낮은 상태임을 확인하였음
- `JobDispatchUseCase`가 `presentation.dto.RunnerJobFetchRequest`를 직접 받는 구조이므로 application 계층이 presentation 모델에 의존하고 있음을 확인하였음
- `PendingJob`, `RunnerAssignmentCandidate`, `JobDispatchMessage`는 각자 역할이 있으나, 현재 명칭만으로는 dispatch 조회 projection인지, application command인지, 외부 응답용 결과인지 구분이 쉽지 않음을 확인하였음
- `JobPersistenceAdapter.findPendingByCandidate(...)`는 조회, latest history 판별, repository 보조 정보 조립을 한 곳에서 수행하고 있어 향후 scope 정책 강화나 쿼리 개선 시 수정 지점이 커질 위험이 있음을 확인하였음
- 따라서 이번 리팩토링은 단순 rename 작업이 아니라, dispatch 경로의 입력 모델 경계와 오케스트레이션 책임을 간결하게 재정렬하는 것을 목표로 수립함

### 목표 (Goals)
- `JobDispatchService`의 책임을 dispatch 오케스트레이션 중심으로 축소함
- application input port가 presentation DTO에 의존하지 않도록 정리함
- dispatch 관련 DTO 및 메서드 네이밍을 실제 역할 기준으로 명확히 정리함
- gRPC controller는 요청/응답 변환 책임으로 한정되도록 구조를 단순화함
- dispatch 성공, 경쟁 선점 실패, 후보 없음 시나리오를 테스트로 명확히 고정함

### 범위 (Scope)
- **수정 대상**
  - `src/main/java/io/jgitkins/server/application/service/JobDispatchService.java`
  - `src/main/java/io/jgitkins/server/application/port/in/JobDispatchUseCase.java`
  - `src/main/java/io/jgitkins/server/presentation/api/grpc/JobDispatchGrpcController.java`
  - `src/main/java/io/jgitkins/server/application/dto/JobDispatchMessage.java`
  - `src/main/java/io/jgitkins/server/application/dto/PendingJob.java`
  - `src/main/java/io/jgitkins/server/application/dto/RunnerAssignmentCandidate.java`
  - `src/main/java/io/jgitkins/server/presentation/dto/RunnerJobFetchRequest.java`
  - `src/main/java/io/jgitkins/server/application/port/out/JobPersistencePort.java`
  - `src/main/java/io/jgitkins/server/infrastructure/adapter/persistence/JobPersistenceAdapter.java`
  - `src/test/java/io/jgitkins/server/application/service/JobDispatchServiceTest.java`
- **수정 제외 대상**
  - Job dispatch 기능 자체의 비즈니스 정책 변경은 제외함
  - Runner 스코프 정책의 신규 기능 추가는 제외함
  - gRPC proto 스펙 자체 변경은 원칙적으로 제외함
  - Job aggregate의 라이프사이클 정책 변경은 제외하고, 필요한 경우 네이밍 검토 수준으로만 다룸

### 계획 (Plan)
- **단계 1**: 입력 모델 경계 재정렬함
  - `JobDispatchUseCase` 입력 타입을 presentation DTO에서 application command로 변경함
  - `JobDispatchGrpcController`는 gRPC request를 application command로 변환하는 역할만 수행하도록 정리함
  - 예시를 아래와 같이 정의함

```java
// BEFORE
public interface JobDispatchUseCase {
    Optional<JobDispatchMessage> fetchJob(RunnerJobFetchRequest request);
}

// AFTER
public interface JobDispatchUseCase {
    Optional<JobDispatchResult> dispatch(DispatchJobCommand command);
}
```

- **단계 2**: 서비스 메서드명과 내부 책임을 행위 기준으로 정리함
  - `fetchJob()`는 실제로 조회가 아니라 dispatch 수행이므로 행위형 메서드명으로 변경함
  - `publishDispatchMessage()`는 실제 publish를 하지 않으므로 결과 생성 의미가 드러나는 이름으로 변경함
  - runner token 검증, runner context 조립, dispatch 결과 생성은 private helper 또는 support 성격으로 정리함
  - 예시를 아래와 같이 정의함

```java
// BEFORE
public Optional<JobDispatchMessage> fetchJob(RunnerJobFetchRequest request) { ... }
private Optional<RunnerAssignmentCandidate> resolveRunnerCandidate(String runnerToken) { ... }
private JobDispatchMessage publishDispatchMessage(...) { ... }

// AFTER
public Optional<JobDispatchResult> dispatch(DispatchJobCommand command) { ... }
private Optional<RunnerDispatchContext> resolveRunnerContext(String runnerToken) { ... }
private JobDispatchResult buildDispatchResult(...) { ... }
```

- **단계 3**: dispatch projection과 DTO 명칭을 정리함
  - `PendingJob`은 dispatch 조회 projection임을 이름에 드러내도록 변경 검토함
  - `RunnerAssignmentCandidate`는 runner 조회 결과와 scope 정보를 담는 문맥이므로 context 성격 명칭으로 조정함
  - `JobDispatchMessage`는 이벤트 publish가 아니라 응답 결과 모델이므로 `Result` 또는 `Payload` 의미로 재정의함
  - 예시를 아래와 같이 정의함

```java
// BEFORE
PendingJob
RunnerAssignmentCandidate
JobDispatchMessage

// AFTER
DispatchableJob
RunnerDispatchContext
JobDispatchResult
```

- **단계 4**: persistence port와 adapter의 의도를 명확히 함
  - `findPendingByCandidate(...)`는 단순 저장소 조회라기보다 dispatch 대상 선택이므로 포트 메서드명도 의도 중심으로 정리함
  - adapter 내부 주석 기반 임시 구현 지점은 유지 여부를 점검하고, 최소한 TODO 수준이 아니라 의도가 드러나는 명칭으로 정리함
  - 조회 projection 반환 구조도 새 명칭에 맞춰 일관되게 조정함
  - 예시를 아래와 같이 정의함

```java
// BEFORE
Optional<PendingJob> findPendingByCandidate(RunnerAssignmentCandidate candidate);

// AFTER
Optional<DispatchableJob> findNextDispatchableJob(RunnerDispatchContext context);
```

- **단계 5**: 테스트 및 회귀 검증 보강함
  - 기존 `JobDispatchServiceTest`는 실패 경로 일부만 검증하고 있으므로, dispatch 성공 및 경쟁 상황을 포함하도록 보강함
  - 시그니처 변경에 따라 gRPC controller 또는 변환 로직 회귀도 함께 확인함
  - 예시를 아래와 같이 정의함

```java
// 추가 검증 대상 예시
dispatch_returnsResult_whenRunnerAndPendingJobExist()
dispatch_returnsEmpty_whenHistorySaveFailsBecauseAnotherRunnerWon()
dispatch_buildsCloneUrlIntoResult()
```

- **단계 6**: 문서와 task 기록을 동기화함
  - 리팩토링 완료 후 task 2.36 상태와 상세 내용을 taskmaster에 반영함
  - 필요 시 `JobDispatch` 용어 변경 사항을 business 또는 architecture 문서에 후속 반영함

### 기대효과 (Expected Benefits)
- dispatch 경로의 입력/출력 경계가 선명해져 gRPC 어댑터와 application service의 역할 구분이 명확해질 것으로 예상함
- 메서드명과 DTO명이 실제 행위를 반영하게 되어 코드 독해 비용이 낮아질 것으로 예상함
- persistence 조회 포트가 dispatch 의도를 드러내게 되어 추후 쿼리 최적화 또는 scope 정책 강화 시 수정 지점 파악이 쉬워질 것으로 예상함
- dispatch 성공/실패/경합 시나리오가 테스트로 고정되어 리팩토링 회귀 위험이 낮아질 것으로 예상함

### 예시 (선택 방안 기준 코드 스니펫)

#### AS-IS (현재 구조)
```java
public interface JobDispatchUseCase {
    Optional<JobDispatchMessage> fetchJob(RunnerJobFetchRequest request);
}

@Transactional
public Optional<JobDispatchMessage> fetchJob(RunnerJobFetchRequest request) {
    Optional<RunnerAssignmentCandidate> candidateOptional = resolveRunnerCandidate(request.getRunnerToken());
    if (candidateOptional.isEmpty()) {
        return Optional.empty();
    }

    RunnerAssignmentCandidate candidate = candidateOptional.get();
    Optional<PendingJob> pendingJob = jobPort.findPendingByCandidate(candidate);
    if (pendingJob.isEmpty()) {
        return Optional.empty();
    }
    return assignRunner(candidate, pendingJob.get());
}

private JobDispatchMessage publishDispatchMessage(...) {
    return JobDispatchMessage.builder()
            .jobId(parseJobId(job))
            .cloneUrl(cloneUrlBuilder.build(pendingJob.getRepositoryClonePath()))
            .build();
}
```

#### TO-BE (개선 제안 구조)
```java
public interface JobDispatchUseCase {
    Optional<JobDispatchResult> dispatch(DispatchJobCommand command);
}

@Transactional
public Optional<JobDispatchResult> dispatch(DispatchJobCommand command) {
    Optional<RunnerDispatchContext> context = resolveRunnerContext(command.runnerToken());
    if (context.isEmpty()) {
        return Optional.empty();
    }

    Optional<DispatchableJob> dispatchableJob = jobPort.findNextDispatchableJob(context.get());
    if (dispatchableJob.isEmpty()) {
        return Optional.empty();
    }

    return assignRunner(context.get(), dispatchableJob.get());
}

private JobDispatchResult buildDispatchResult(...) {
    return JobDispatchResult.builder()
            .jobId(parseJobId(job))
            .cloneUrl(cloneUrlBuilder.build(dispatchableJob.getRepositoryClonePath()))
            .build();
}
```

### 주의사항
- **포맷팅 금지**: 리팩토링 과정에서 코드 포맷팅은 수행하지 않고, 네이밍·구조·책임 조정에만 집중함
- **기존 기능 보장**: runner가 job을 정상적으로 수신하는 기존 기능은 반드시 유지되어야 하며, 회귀 테스트 통과를 구현 완료 조건으로 설정함
- **계획우선**: 본 문서 작성 단계에서는 구현을 진행하지 않으며, 실제 코드 수정은 별도 구현 단계에서 수행함
- **예시전체나열**: 변경 대상 메서드명, DTO명, 포트 시그니처의 BEFORE / AFTER 예시를 문서에 모두 명시하였음
- **점진적 변경 유지**: aggregate 정책 변경이나 gRPC proto 변경까지 한 번에 확장하지 않고, application 경계와 네이밍 정리에 우선 집중함

### 결론 (추후작성)
- 본 리팩토링은 `JobDispatchService` 단일 클래스 정리가 아니라 dispatch 유스케이스 경계 전체를 간결하게 만드는 작업으로 정의하였음
- 구현 우선순위는 `입력 모델 정리 -> 메서드/DTO 네이밍 정리 -> persistence 조회 의도 정리 -> 테스트 보강` 순으로 수립하였음
- 본 계획에 따라 후속 구현을 진행하면 구조 정리는 이루어지되, 정책 변경 범위는 과도하게 확장되지 않도록 통제할 수 있을 것으로 판단함

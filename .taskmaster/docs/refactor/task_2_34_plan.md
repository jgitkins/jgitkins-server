# 리팩토링 계획서

### 제목
- **리팩토링 계획**: `PushEventHandleService` 헥사고날 경계 재정렬 및 Job 도메인 이벤트 흐름 정리

### 배경 (왜?)
- 현재 [PushEventHandleService](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/PushEventHandleService.java)는 push 이벤트 처리의 애플리케이션 오케스트레이션을 담당하고 있으나, 입력 모델과 후속 처리 경계에 인프라 세부사항이 일부 유입되어 있음.
- `PushEventCommand` 가 JGit의 `ReceiveCommand` 와 `Constants` 를 직접 참조하고 있어 application 계층이 JGit 전송 프로토콜에 결합되어 있음.
- `PushEventHandleService` 는 저장소 식별을 `gitDirPath` 기반으로 수행하고 있어, 코어가 물리 파일시스템 경로를 직접 해석하는 구조를 일부 수용하고 있음.
- `PushHook` 에서 전달되는 외부 이벤트는 실제로 복수 ref 변경일 수 있으나, 현재 입력 모델은 마지막 branch 명령 하나만 선택하는 단일 이벤트 구조로 축소되어 있음.
- 후속 동작인 Job 생성이 `JobCreateUseCase` 직접 호출로 이어져 있어, push 처리와 CI 후속 반응 사이의 포트 경계가 명시적으로 드러나지 않음.
- Job 관련 문서도 현재는 상태 흐름과 구조 문서가 분리되어 있지 않아, 리팩토링 목표 구조를 공통 언어로 논의하기 어려운 상태임.

### 목표 (Goals)
- `PushEventHandleService` 의 책임을 애플리케이션 오케스트레이션으로 한정하고, 인프라 입력 해석 책임을 어댑터 계층으로 되돌림.
- JGit 타입 의존을 application DTO 에서 제거할 수 있는 목표 구조를 설계함.
- push 입력 모델을 단일 branch 이벤트가 아닌 ref 업데이트 단위 또는 배치 단위로 재정의할 수 있도록 설계함.
- 후속 Job 생성 트리거를 use case 직접 호출이 아닌 명시적 포트 또는 이벤트 기반 구조로 분리하는 방향을 정리함.
- `job-event-storming.md` 와 `job-domain-model.md` 에 Job 도메인 흐름과 구조를 분리하여 정리하고, 후속 구현과 리뷰 기준 문서로 활용 가능하게 함.

### 범위 (Scope)
- **수정 대상**:
    - `PushEventHandleService`, `PushHook`, `PushEventCommand` 관련 구조 분석 및 설계 문서
    - push 입력 모델, 저장소 식별 전략, 후속 Job 트리거 방식에 대한 리팩토링 계획
    - `docs/business/job.md`, `docs/business/job-event-storming.md`, `docs/business/job-domain-model.md` 문서 정리
    - Taskmaster Task 2 하위 서브태스크 문서화
- **수정 제외 대상**:
    - 실제 Java 코드 구현 및 패키지 이동
    - Port/Adapter 인터페이스 시그니처 변경
    - Push 이벤트 배치 처리 로직의 실제 반영
    - Job 생성 플로우의 런타임 동작 변경

### 계획 (Plan)
- **단계 1: 현행 구조 및 위반 지점 분석**
    - `PushHook` 에서 `PushEventHandleService` 로 이어지는 흐름을 기준으로, inbound adapter, application service, outbound port 간 책임 경계를 재정리함.
    - 현재 문제를 해결하기 위한 3가지 접근 방식을 검토함.
    - ~~**방안 1**: 현재 구조를 유지하되 `PushEventCommand` 내부 정적 팩토리만 adapter 로 이동하는 최소 수정안을 검토함.~~
    - **방안 2**: push 입력 모델을 `PushRefUpdate` 또는 `PushEventBatchCommand` 형태로 재정의하고, JGit 타입 해석을 전부 inbound adapter 로 이동하는 구조 재정렬안을 검토함.
    - ~~**방안 3**: Push 처리 이후 Job 생성까지 포함하여 domain event publisher 기반으로 전체 플로우를 이벤트 중심으로 재구성하는 확장안을 검토함.~~
    - 범위 대비 효과, 현재 코드베이스 수용성, 회귀 위험을 비교한 결과 이번 리팩토링 설계는 **방안 2**를 기준으로 수립함.
    - **예시**:
    - AS-IS: `PushEventCommand.from(gitDirPath, requesterId, commands)` 가 application DTO 내부에서 JGit `ReceiveCommand` 를 직접 해석함.
    - TO-BE: `PushHook` 또는 별도 mapper adapter 가 `ReceiveCommand` 를 `PushRefUpdate` 목록으로 변환한 뒤, application 에는 순수 command 만 전달함.

- **단계 2: 입력 포트 및 모델 구조 재설계**
    - `PushEventCommand` 는 application 계층 순수 모델로 유지하되, JGit 타입 및 git 경로 문자열 해석 책임은 제거하는 방향으로 설계함.
    - 입력 포트는 단건 branch 이벤트보다 `ref update` 집합을 표현할 수 있는 모델을 우선 검토함.
    - 저장소 식별은 `gitDirPath` 직접 전달 대신 `repositoryId` 또는 `RepositoryKey` 를 application 진입값으로 사용하는 구조를 목표로 설정함.
    - 어댑터는 `ReceivePack`, `ReceiveCommand` 를 해석하여 순수 command/event 로 변환하는 역할만 담당하도록 정리함.
    - **예시**:
    - AS-IS 입력 모델: `PushEventCommand { gitDirPath, branchName, branchCreated, branchDeleted, commitHash, triggeredBy }`
    - TO-BE 입력 모델 예시:
```java
public record PushRefUpdate(
        Long repositoryId,
        String branchName,
        PushRefAction action,
        String commitHash,
        Long triggeredBy
) {}
```
    - TO-BE 포트 예시:
```java
public interface PushEventHandleUseCase {
    void handle(PushEventBatchCommand command);
}
```

- **단계 3: 후속 Job 트리거 경계 재정의**
    - 현재 `PushEventHandleService -> JobCreateUseCase` 직접 호출 구조를 다음 3가지로 비교 검토함.
    - ~~**방안 A**: 현 구조 유지 후 메서드명만 명확히 하는 방안을 검토함.~~
    - **방안 B**: `JobTriggerPort` 또는 `CiTriggerPort` 를 도입하여 application service 가 후속 반응 의도를 outbound port 로 표현하는 방안을 검토함.
    - ~~**방안 C**: domain event publisher 를 도입하여 `PushHandledEvent`, `BranchUpdatedEvent`, `JobRequestedEvent` 로 완전히 분리하는 방안을 검토함.~~
    - 현재 시스템 복잡도와 적용 난이도를 고려할 때, 단기적으로는 **방안 B**가 가장 적합하다고 판단함.
    - 장기적으로는 방안 C 로 확장 가능하도록 event vocabulary 를 문서에 함께 정의함.
    - **예시**:
    - AS-IS:
```java
if (shouldTriggerJob(command)) {
    jobCreateUseCase.create(buildJobCommand(command, repository));
}
```
    - TO-BE:
```java
if (shouldTriggerJob(command)) {
    jobTriggerPort.requestJob(JobTriggerRequest.from(command, repository));
}
```
    - 장기 확장 예시:
```java
eventPublisher.publish(new JobRequestedFromPushEvent(repositoryId, branchName, commitHash, triggeredBy));
```

- **단계 4: Job 도메인 이벤트 흐름 문서화**
    - `docs/business/job.md` 는 인덱스 문서로 축소하고, 세부 다이어그램은 `docs/business/job-event-storming.md` 와 `docs/business/job-domain-model.md` 로 분리하여 작성함.
    - 문서에는 다음 항목을 포함함.
    - `job-event-storming.md`: Push 수신, 브랜치 반영, Jenkinsfile 확인, Job 생성 요청, Job 실행 라이프사이클의 상태 변화
    - `job-domain-model.md`: Job aggregate, JobHistory, RunnerAssignment, JobTrigger 등 핵심 객체의 경계와 관계
    - `job.md`: 두 문서를 안내하는 인덱스 및 참조 포인트
    - 장기적으로 고려할 domain/application event 명칭 예시
    - 문서는 현재 구현 상태가 아닌 목표 구조와 해석 기준을 설명하는 문서로 작성함.
    - **예시**:
    - 이벤트 스토밍 상태 예시: `브랜치갱신반영됨 -> Jenkinsfile확인됨 -> Job생성요청됨 -> Job생성됨`
    - 도메인 모델 예시: `Job` aggregate root 가 `JobHistory`, `RunnerAssignment`, `JobTrigger` 를 내부 경계로 포함함.

- **단계 5: 후속 구현 기준선 정리**
    - 실제 구현 착수 전 변경 대상 클래스, 예상 포트, 예상 테스트 범위를 체크리스트 형태로 정리함.
    - 구현 단계에서는 본 계획 문서를 기준으로 inbound adapter, application service, outbound trigger 경계가 의도대로 유지되는지 검증함.
    - 본 단계에서는 계획 및 문서만 작성하고, 구현은 절대 진행하지 않음.
    - **예시 체크리스트**:
    - `PushEventCommand` 의 JGit import 제거 여부 확인
    - `PushHook` 에서 command 변환 책임 보유 여부 확인
    - `PushEventHandleService` 가 `gitDirPath` 대신 도메인 식별자를 입력받는지 확인
    - `JobCreateUseCase` 직접 호출 제거 또는 의도적 유지 여부 확인
    - Push 관련 테스트에서 복수 ref update 시나리오가 검토되었는지 확인

### 기대효과 (Expected Benefits)
- application 계층에서 JGit 타입과 물리 경로 의존을 제거할 수 있는 구조적 기준이 명확해짐.
- push 입력과 후속 Job 트리거의 책임 경계가 분리되어 유지보수성과 테스트 용이성이 향상됨.
- 복수 ref 업데이트와 같은 실제 push 이벤트 특성을 반영할 수 있는 확장 방향이 확보됨.
- `job-event-storming.md` 와 `job-domain-model.md` 가 각각 상태 흐름과 구조 관점의 공통 참조 문서 역할을 수행하여 후속 리팩토링과 리뷰 효율이 향상됨.

### 예시 (방안 2 기준 코드 스니펫)

#### AS-IS (현재 구조)
```java
public void onPostReceive(ReceivePack receivePack, Collection<ReceiveCommand> commands) {
    String gitDirPath = receivePack.getRepository().getDirectory().getAbsolutePath();
    Long requesterId = pushEventRequestResolver.resolveRequesterId().orElse(null);

    PushEventCommand pushEventCommand = PushEventCommand.from(gitDirPath, requesterId, commands);
    if (pushEventCommand == null) {
        return;
    }

    pushEventHandleUseCase.handle(pushEventCommand);
}
```

#### TO-BE (개선 제안 구조)
```java
public void onPostReceive(ReceivePack receivePack, Collection<ReceiveCommand> commands) {
    RepositoryKey repositoryKey = pushRepositoryResolver.resolve(receivePack.getRepository());
    Long requesterId = pushEventRequestResolver.resolveRequesterId().orElse(null);

    PushEventBatchCommand batchCommand = pushCommandMapper.toBatchCommand(
            repositoryKey,
            requesterId,
            commands
    );

    if (batchCommand.isEmpty()) {
        return;
    }

    pushEventHandleUseCase.handle(batchCommand);
}
```

### 주의사항
- **포맷팅 금지**: 리팩토링 계획 수립 과정에서 코드 포맷팅이나 불필요한 문서 정리는 수행하지 않음.
- **기존 기능 보장**: 후속 구현 단계에서는 현재 push 처리와 Job 생성의 기능적 동등성을 우선 보장해야 함.
- **계획우선**: 본 문서 작성 단계에서는 실제 Java 코드 구현을 절대 진행하지 않음.
- **예시전체나열**: 현재 구조와 목표 구조를 흐름 단위로 비교 가능하도록 AS-IS / TO-BE 를 전체 형태로 제시함.
- **문서체규약**:
    - 모든 문장은 공식 문서체로 작성함.
    - 문장 끝은 `~~하였음` 또는 `~~함` 형태로 유지함.
    - 구조적 문제와 목표 구조를 혼동하지 않도록 현재 상태와 목표 상태를 분리하여 서술함.

### 결론 (추후작성)
- 본 문서는 `PushEventHandleService` 관련 헥사고날 경계 개선과 Job 도메인 이벤트 흐름 정리를 위한 실행 기준으로 작성함.
- 현재 단계에서는 서브태스크 추가, 계획 문서 작성, Job 관련 문서 분리 및 보강만 수행하였으며 구현은 진행하지 않았음.

# 리팩토링 계획서

### 제목
- **리팩토링 계획**: `Push` 후처리 Job 생성 경계 재정리

### 배경 (왜?)
- 현재 [PushEventHandleService](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/PushEventHandleService.java)는 branch 상태 반영, 생성 가능 여부 판단, rule 기반 skip 판단, job 생성까지 함께 담당하고 있어 규칙 판단 책임이 분산되어 있음.
- 현재 `repositoryId == null` 예외는 mapper 단계의 입력 보장과 중복될 수 있어 service 계층에 남길지 재검토가 필요함.
- 현재 `canCreateJob()`와 rule 기반 `skip` 결과는 모두 job 생성 여부 판단 규칙이나 서로 다른 위치에 분산되어 있어 읽기 흐름이 끊기고 있음.
- 현재 [PushHook](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/infrastructure/config/git/hook/push/PushHook.java)의 `pushEventCommandMapper`는 단순 mapper 라기보다 JGit 명령 해석과 저장소 식별을 함께 수행하고 있어 역할 명칭이 실제 책임과 어긋나 있음.
- 현재 hook translation 컴포넌트는 infrastructure 에 위치하면서 application port 를 사용하고 있어, 구조 위반 여부보다 적절한 계층 위치와 역할 명칭 정리가 더 중요함.
- infrastructure adapter 가 outgoing port 를 직접 의존하는 것은 허용 가능하나, 현재 클래스는 JGit 해석 책임과 저장소 식별 책임이 한곳에 섞여 있어 재분리가 필요함.

### 목표 (Goals)
- `PushEventHandleService`를 오케스트레이션 중심으로 단순화함.
- service 는 유효한 command 를 받아 흐름만 조정하는 방향으로 정리함.
- Job 생성 가능 여부 판단 규칙을 별도 validator 또는 결과 모델로 분리함.
- precondition skip 과 rule 기반 skip 을 같은 job 생성 판단 흐름으로 읽히게 정리함.
- `PushHook` 주변 번역 컴포넌트의 역할을 명확히 하여 inbound adapter 경계를 분명히 함.
- `PushEventCommandMapper`는 유지보다 `PushHookCommandTranslator` 와 `PushEventCommandResolver` 분리 방향을 우선 검토함.
- `PushEventHandleService`는 이름 변경보다 책임 분리를 우선 검토함.

### 범위 (Scope)
- **수정 대상**:
    - `PushEventHandleService`
    - `PushHook`
    - `PushEventCommandMapper` 명칭 및 책임 표현
    - push 후처리 validator/결과 모델 구조
    - service 의 중복 입력 검증 처리 기준
- **수정 제외 대상**:
    - `.jgitkins/ci.yml` 스키마 변경
    - pipeline rule 매칭 정책 변경
    - Job 도메인 및 persistence 구조 변경

### 계획 (Plan)
- **단계 1**: 현재 책임 분리를 재정의함.
    - `PushHook`는 PostReceiveHook inbound adapter 로 한정함.
    - command 생성 책임은 `PushHookCommandTranslator` 성격의 hook translation 컴포넌트로 한정함.
    - translator 는 JGit 입력을 raw request 로 변환하는 역할까지만 담당함.
    - `PushEventCommandResolver`는 저장소 path 기반 식별, namespace 결정, 최종 `PushEventCommand` 조립을 담당함.
    - job 생성 여부 판단은 service 내부 boolean 분기 대신 별도 validator/결과 모델로 모음.
    - mapper 가 입력 무결성을 어디까지 보장하는지와 service 의 null repositoryId 방어가 필요한지 함께 정리함.
    - **예시**:
```java
PushHook -> PushHookCommandTranslator -> PushEventCommandResolver -> PushEventHandleService
```
    - 위 흐름에서 `PushHook`는 유지하고, translator 가 `PushEventHandleService` 호출 직전까지 command 생성을 담당하는 구조를 목표로 함.

- **단계 2**: 구조 대안을 비교함.
    - ~~**방안 1**: `PushEventHandleService` 내부 private method 확장으로 유지함.~~
    - **방안 2**: `JobCreationValidator`, `PushHookCommandTranslator`, `PushEventCommandResolver`로 입력/판단 책임을 분리하고, rule 판단 컴포넌트의 `Policy` 명칭을 채택함.
    - ~~**방안 3**: rule 판단 컴포넌트가 precondition 까지 모두 흡수하도록 확장함.~~
    - 책임 경계와 확장성을 고려해 **방안 2**를 채택함.
    - 이 단계에서 `Mapper`, `Resolver`, `Translator` 중 어떤 명칭이 역할과 가장 잘 맞는지도 함께 결정함.
    - **예시**:
```java
PushHookCommandTranslator + PushEventCommandResolver + JobCreationValidator + PushJobCreationPolicy
```
    - hook translation 은 `Translator`, 저장소 식별 및 command 확정은 `Resolver`, rule 판단은 `Policy` 명칭을 우선 채택함.

- **단계 3**: 리팩토링 작업을 수행함.
    - `PushEventHandleService`의 입력 무결성 검증과 생성 가능 여부 판단을 별도 validator 또는 skip 결과 반환 구조로 분리함.
    - `canCreateJob()`와 rule 기반 skip 판단을 동일한 “job 생성 판단 흐름”으로 읽히도록 정리함.
    - `PushEventCommandMapper`는 유지하지 않고 translator/resolver 분리 방향을 우선 검토함.
    - JGit 의 `ReceiveCommand`, ref 해석, gitDirPath 수집은 infrastructure 의 `PushHookCommandTranslator`가 담당함.
    - 저장소 path 로 repository 를 식별하고 namespace 를 계산하여 `PushEventCommand`를 만드는 책임은 application 의 `PushEventCommandResolver`로 이동함.
    - rule 기반 판단 컴포넌트 명칭은 `Planner` 대신 `Policy`를 채택하는 방향으로 정리함.
    - **예시**:
```java
PushHookRequest request = pushHookCommandTranslator.translate(gitDirPath, triggeredBy, commands);
PushEventCommand command = pushEventCommandResolver.resolve(request);
JobCreationDecision decision = jobCreationValidator.validate(command);
JobPlan jobPlan = pushJobCreationPolicy.plan(PushJobPlanRequest.from(command));
```
    - application 계층으로 이동 가능한 책임은 “JGit 타입 해석”이 아니라 “저장소 식별 규칙과 command 생성”임을 명확히 함.
    
- **단계 4**: 테스트 및 검증을 수행함.
    - branch delete, commit hash 없음, triggeredBy 없음, rule 미매칭, pipeline file 미존재, rule 판단 예외 시나리오를 유지 검증함.
    - hook -> raw request 변환 테스트, raw request -> command resolver 테스트, service 오케스트레이션 테스트를 분리 유지함.
    - **예시**:
```java
handle_skipsJobWhenTriggeredByMissing()
translate_buildsPushHookRequestFromReceiveCommand()
resolve_buildsPushEventCommandFromPushHookRequest()
```

- **단계 5**: 문서화를 수행함.
    - application service, validator, hook translator 의 책임 경계를 문서로 정리함.
    - `mapper`와 `resolver` 용어 기준을 함께 기록함.
    - application service 이름에는 `PostReceive` 같은 infrastructure 세부 용어를 직접 올리지 않는 기준도 함께 기록함.
    - **예시**:
```text
application service 는 PostReceive 구현 세부를 직접 드러내지 않음
```
    - 위 기준은 application service 가 JGit hook 종류를 알지 않고, inbound adapter 가 해당 세부 구현을 흡수하는 구조를 의미함.

### 기대효과 (Expected Benefits)
- push 후처리 구조가 단순해져 확장 포인트가 명확해짐.
- skip 판단 규칙이 한곳에 모여 유지보수성이 향상됨.
- hook translation 컴포넌트의 역할이 분명해져 헥사고날 경계 해석 비용이 줄어듦.
- service 명칭보다 책임 분리에 집중할 수 있어 후속 리네이밍 비용이 줄어듦.
- infrastructure adapter 가 outgoing port 를 참조하더라도, JGit 해석과 application 수준 저장소 식별 규칙이 분리되어 코드 의도가 선명해짐.


### 예시 (방안 2 기준 코드 스니펫)

#### AS-IS (기존 구조)
```java
if (!legacyCanCreateJob(command)) {
    return;
}

JobPlan jobPlan = pushJobCreationPlanner.plan(PushJobPlanRequest.from(command));
if (jobPlan.isSkipped()) {
    return;
}
```

#### TO-BE (개선 제안 구조)
```java
PushHookRequest request = pushHookCommandTranslator.translate(gitDirPath, triggeredBy, commands);
PushEventCommand command = pushEventCommandResolver.resolve(request);
JobCreationDecision decision = jobCreationValidator.validate(command);
if (decision.isSkipped()) {
    return;
}

JobPlan jobPlan = pushJobCreationPolicy.plan(PushJobPlanRequest.from(command));
if (jobPlan.isSkipped()) {
    return;
}

jobCreateUseCase.create(buildJobCommand(command, jobPlan.getPipelineFilePath()));
```

#### 책임 분리 예시
```java
// infrastructure
public class PushHookCommandTranslator {

    public Optional<PushHookRequest> translate(
            String gitDirPath,
            Long triggeredBy,
            Collection<ReceiveCommand> commands
    ) {
        ReceiveCommand branchCommand = findLastBranchCommand(commands).orElse(null);
        if (branchCommand == null) {
            return Optional.empty();
        }

        String branchName = extractBranchName(branchCommand.getRefName()).orElse(null);
        if (branchName == null) {
            return Optional.empty();
        }

        return Optional.of(new PushHookRequest(
                gitDirPath,
                triggeredBy,
                branchName,
                branchCommand.getType() == ReceiveCommand.Type.CREATE,
                branchCommand.getType() == ReceiveCommand.Type.DELETE,
                branchCommand.getNewId() != null ? branchCommand.getNewId().getName() : null
        ));
    }
}
```

```java
// application
public class PushEventCommandResolver {

    private final RepositoryPersistencePort repositoryPort;

    public PushEventCommand resolve(PushHookRequest request) {
        Repository repository = resolveRepository(request.gitDirPath());
        String namespace = extractNamespace(repository);

        return PushEventCommand.builder()
                .repositoryId(repository.getId().getValue())
                .namespace(namespace)
                .repoName(repository.getName().getValue())
                .branchName(request.branchName())
                .branchCreated(request.branchCreated())
                .branchDeleted(request.branchDeleted())
                .commitHash(request.commitHash())
                .triggeredBy(request.triggeredBy())
                .build();
    }
}
```

```text
PushHookCommandTranslator
- JGit ReceiveCommand 해석
- branch ref 필터링
- gitDirPath 와 hook 입력을 raw request 로 변환

PushEventCommandResolver
- repository 조회 전략 결정
- git path 기반 저장소 식별
- namespace 계산
- PushEventCommand 생성
```
### 주의사항
- **포맷팅 금지**: 리팩토링과 무관한 정렬 변경은 수행하지 않음.
- **기존 기능 보장**: push 후 branch 반영 및 job skip 정책은 그대로 유지함.
- **계획우선**: 본 문서는 구현 전에 구조 정리를 위한 계획만 작성함.
- **문서체규약**:
    - 모든 문장은 `~~함` 또는 `~~하였음`으로 마무리함.
    - 간결하게 작성하되 핵심 책임 변화는 빠짐없이 포함함.

### 구현 반영 현황
- `PushEventCommandMapper`는 제거하고 `PushHookCommandTranslator`와 `PushEventCommandResolver`로 책임을 분리하여 반영하였음.
- `PushHook`는 translator 와 resolver 를 순차 호출하는 구조로 단순화하였음.
- `PushEventHandleService`의 precondition 판단은 `JobCreationValidator`로 분리하여 반영하였음.
- rule 기반 계획 수립 컴포넌트는 `PushJobCreationPolicy` 명칭으로 정리하여 반영하였음.
- translator 테스트와 resolver 테스트를 분리 추가하여 hook 입력 해석과 command 생성 책임을 각각 검증하였음.
- validator 테스트와 policy 테스트를 추가하여 precondition 판단과 rule 기반 판단을 각각 검증하였음.

### 결론
- 본 문서는 `2.37`의 후속 구조 리팩토링 범위를 짧게 재정의하기 위해 작성함.
- 현재 단계에서는 translator/resolver 분리, validator/policy 분리, 관련 테스트까지 구현 반영하였음.

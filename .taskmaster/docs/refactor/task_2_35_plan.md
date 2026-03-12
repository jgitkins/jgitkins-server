# 리팩토링 계획서

### 제목
- **리팩토링 계획**: Git push 후처리 원자성 보장 전략 및 `PreReceiveHook` / `PostReceiveHook` 정책 정리

### 배경 (왜?)
- 현재 `jgitkins-server` 의 Git push 처리 경로는 `ReceivePack` 에 `PostReceiveHook` 를 연결하여, Git ref 업데이트 이후 `PushHook` 와 `PushEventHandleService` 를 통해 branch 반영 및 Job 생성 후처리를 수행하고 있음.
- 이 구조에서는 Git 저장소 업데이트와 DB의 논리 엔트리 반영, Job 생성 요청이 하나의 기술 트랜잭션으로 묶여 있지 않음.
- 따라서 후처리 단계에서 예외가 발생할 경우, Git push 는 이미 성공하였으나 DB branch 상태 반영 또는 Job 생성이 실패하는 정합성 불일치가 발생할 수 있음.
- 반대로 모든 비즈니스 로직을 `PreReceiveHook` 로 이동하여 push 이전에 수행하더라도, Git ref 업데이트와 DB 저장을 단일 트랜잭션으로 묶을 수 없으므로 완전한 원자성을 보장하기는 어려움.
- 현재 구조에는 “어떤 검증은 pre 단계에서 차단할지”, “어떤 후속 작업은 post 단계에서 수행할지”, “실패 시 어떻게 복구 또는 재처리할지”에 대한 정책 문서가 부족한 상태임.

### 목표 (Goals)
- Git 저장소 업데이트와 논리 엔트리(DB/Job)의 원자성 한계를 명시적으로 정리함.
- `PreReceiveHook` 와 `PostReceiveHook` 의 역할 분리 기준을 수립함.
- push 후처리 실패 시 보상, 재시도, 운영 대응 전략을 문서화함.
- 향후 구현 시 “사전 검증은 어디까지”, “후처리 실패는 어떻게 다룰지”에 대한 기준선을 제공함.
- Git 을 정본(source of truth)으로 둘지, DB 논리 엔트리를 정본으로 둘지에 대한 운영 원칙을 명확히 함.

### 범위 (Scope)
- **수정 대상**:
    - Git push 처리 경로의 훅 시점(`PreReceiveHook`, `PostReceiveHook`) 분석
    - Git 업데이트와 DB/Job 후처리 간 원자성 한계 정리
    - 실패 보상, 재처리, 운영 알림 정책 문서화
    - 후속 리팩토링 기준이 되는 계획 문서 작성
- **수정 제외 대상**:
    - 실제 `PreReceiveHook` 구현 또는 `PostReceiveHook` 제거
    - DB outbox, retry queue, failure log persistence 의 실제 개발
    - Git rollback 자동화 구현
    - 운영 배치 및 재처리 API 실제 구현

### 계획 (Plan)
- **단계 1: 현재 Git push 처리 시점과 원자성 한계 분석**
    - 현재 `ReceivePackFactory -> ReceivePack -> PostReceiveHook` 흐름을 기준으로, Git ref 업데이트가 언제 반영되고 후처리가 언제 실행되는지 정리함.
    - 다음 3가지 접근 방식을 검토함.
    - ~~**방안 1**: 현 구조를 유지하되 예외만 로깅하고 운영 개입에 의존하는 방안을 검토함.~~
    - **방안 2**: `PreReceiveHook` 는 사전 검증 전용, `PostReceiveHook` 는 후처리 전용으로 분리하고 후처리 실패는 보상/재처리 대상으로 다루는 정책을 검토함.
    - ~~**방안 3**: 모든 비즈니스 로직을 `PreReceiveHook` 로 이동하여 push 이전에 DB까지 선반영하는 방안을 검토함.~~
    - 기술적 원자성, 운영 안정성, 구현 난이도를 비교한 결과 **방안 2**를 기준으로 수립함.
    - **예시**:
    - AS-IS: Git push 성공 후 `PushEventHandleService` 예외가 발생하면 Git 은 반영되었으나 Job 생성은 실패할 수 있음.
    - TO-BE: Git push 성공 후 후처리 실패는 `PushPostProcessFailed` 로 기록하고 재처리 대상으로 분리함.

- **단계 2: `PreReceiveHook` 역할 정의**
    - `PreReceiveHook` 는 “push 자체를 허용할지 여부”를 판단하는 사전 검증 단계로 한정함.
    - 이 단계에는 권한 검증, 브랜치 보호 정책, 금지된 ref update 차단, 필수 파일 존재 여부 등 즉시 거부 가능한 정책만 배치하는 방향을 검토함.
    - DB 변경이나 Job 생성 요청 같은 파생 작업은 이 단계에서 수행하지 않는 것을 원칙으로 함.
    - **예시**:
    - 허용 가능한 pre 검증 예시: `main` 브랜치 direct push 금지, 인증되지 않은 사용자 push 차단, 필수 `Jenkinsfile` 부재 시 특정 정책에 따라 push 차단
    - 지양할 pre 처리 예시: Job 저장, branch 테이블 저장, 외부 API 호출, 재시도 필요한 부가 로직

- **단계 3: `PostReceiveHook` 역할 및 실패 정책 정의**
    - `PostReceiveHook` 는 Git 반영 이후 발생하는 파생 작업을 처리하는 단계로 정의함.
    - branch 동기화, Job 생성 요청, 도메인 이벤트 발행, 감사 로그 축적 등은 이 단계에 위치시킴.
    - 이 단계 예외는 “push 실패”가 아닌 “후처리 실패”로 분리해서 다루는 정책을 수립함.
    - **예시**:
    - post 처리 예시: `PushEventCommandMapper` 를 통한 command 생성, branch 상태 동기화, `JobTriggerPort.requestJob(...)`
    - 실패 정책 예시: 예외 catch 후 `repository/branch/commit/user` 식별자를 포함한 오류 로그 기록, 실패 엔트리 저장, 재처리 큐 적재

- **단계 4: 보상 및 재처리 전략 수립**
    - Git rollback 자동화는 위험성이 높으므로 기본 전략으로 채택하지 않음.
    - 후처리 실패는 다음 3가지 방식을 비교 검토함.
    - ~~**방안 A**: 실패 시 즉시 Git rollback 수행~~
    - **방안 B**: 실패 이벤트 저장 후 운영 재처리 또는 배치 재시도**
    - ~~**방안 C**: 실패를 무시하고 로그만 남김~~
    - 기본 전략은 **방안 B**로 설정함.
    - **예시**:
    - 재처리 엔트리 예시: `repositoryId`, `branchName`, `commitHash`, `triggeredBy`, `failedAt`, `failureReason`
    - 운영 대응 예시: 관리자 화면에서 “Push 후처리 재실행” 버튼 제공 또는 배치 재시도 수행

- **단계 5: 정본(Source of Truth) 및 운영 기준 문서화**
    - Git 저장소를 정본으로 둘지, DB의 branch/job 상태를 정본으로 둘지 명시적으로 선언함.
    - 본 계획에서는 Git ref 상태를 정본으로 간주하고, branch/job 테이블은 Git 상태로부터 재구성 가능한 파생 데이터로 보는 방향을 우선 채택함.
    - 이에 따라 후처리 실패는 rollback 대상이 아니라 “정합성 복구 대상”으로 정의함.
    - **예시**:
    - Git push 성공 + Job 생성 실패 시: 사용자 입장에서는 push 성공, 시스템 입장에서는 후처리 실패 이벤트 발생으로 기록
    - 복구 방법 예시: 동일 `repositoryId/branch/commitHash` 기준으로 Job 재생성 또는 branch 상태 재동기화 수행

### 기대효과 (Expected Benefits)
- Git push 처리 시점별 책임이 명확해져 설계 혼선이 줄어듦.
- `PreReceiveHook` 와 `PostReceiveHook` 의 역할이 분리되어 정책 배치 기준이 명확해짐.
- 후처리 실패를 rollback 문제로 오해하지 않고, 재처리 가능한 운영 문제로 다룰 수 있게 됨.
- 향후 outbox, retry queue, failure event 저장 구조를 도입할 때 기준 문서로 활용 가능해짐.

### 예시 (방안 2 기준 코드 스니펫)

#### AS-IS (현재 구조)
```java
public void onPostReceive(ReceivePack receivePack, Collection<ReceiveCommand> commands) {
    PushEventCommand command = pushEventCommandMapper.map(gitDirPath, requesterId, commands)
            .orElse(null);
    if (command == null) {
        return;
    }

    pushEventHandleUseCase.handle(command);
}
```

#### TO-BE (개선 제안 구조)
```java
public void onPreReceive(ReceivePack receivePack, Collection<ReceiveCommand> commands) {
    pushPolicyValidator.validate(receivePack, commands); // 차단 가능한 정책만 수행
}

public void onPostReceive(ReceivePack receivePack, Collection<ReceiveCommand> commands) {
    try {
        PushEventCommand command = pushEventCommandMapper.map(gitDirPath, requesterId, commands)
                .orElse(null);
        if (command == null) {
            return;
        }
        pushEventHandleUseCase.handle(command);
    } catch (Exception ex) {
        pushPostProcessFailureRecorder.record(gitDirPath, commands, requesterId, ex);
    }
}
```

### 주의사항
- **포맷팅 금지**: 계획 문서 작성 과정에서 코드 포맷팅이나 무관한 리팩토링은 수행하지 않음.
- **기존 기능 보장**: 후속 구현 단계에서도 현재 push 허용/차단 정책과 사용자 체감 동작이 급격히 변하지 않도록 검토해야 함.
- **계획우선**: 본 문서 작성 단계에서는 실제 `PreReceiveHook` 구현이나 예외 보상 로직 구현을 진행하지 않음.
- **예시전체나열**: 현재 구조와 목표 구조를 단계별로 비교 가능한 예시와 함께 제시함.
- **문서체규약**:
    - 모든 문장은 공식 문서체로 작성함.
    - 문장 끝은 `~~하였음` 또는 `~~함` 형태로 유지함.
    - 기술적 원자성과 운영적 정합성 복구 개념을 혼동하지 않도록 구분하여 서술함.

### 결론 (추후작성)
- 본 문서는 Git push 후처리의 원자성 한계와 `PreReceiveHook` / `PostReceiveHook` 정책 분리를 위한 기준 문서로 작성함.
- 현재 단계에서는 계획 문서 작성만 수행하였으며, 구현은 진행하지 않았음.

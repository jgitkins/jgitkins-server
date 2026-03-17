# 리팩토링 계획서

### 제목
- **기능 변경 계획**: `Push`로 인한 `Job` 생성 규칙 변경

### 배경 (왜?)
- 현재 [PushEventHandleService](/Users/alzar/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/PushEventHandleService.java)는 push 이벤트 후 branch 상태 반영과 `JobCreateUseCase` 호출만 담당하고 있으며, branch별 pipeline 선택 규칙을 해석하는 단계가 부재함.
- 현재 [JobService](/Users/alzar/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/JobService.java)는 `Jenkinsfile` 고정 경로를 기준으로만 파일 존재 여부를 확인하고 있어, 브랜치별 상이한 pipeline file 선택 요구사항을 수용할 수 없음.
- 참조 문서 [jgitkins-branch-pipeline.md](/Users/alzar/task/sources/jgitkins/jgitkins-server/docs/jgitkins-branch-pipeline.md)는 branch별 규칙 기반 선택, 규칙 미매칭 시 skip, pipeline file 미존재 시 skip 정책을 요구하고 있음.
- 문서 내부 TODO 기준으로 설정 파일 경로와 pipeline file 위치 정책도 함께 변경되어야 함.
- 최종 목표는 기존 `jgitkins.yml` 및 repository root `Jenkinsfile` 중심 구조를 `.jgitkins/ci.yml`과 `.jgitkins/` 하위 pipeline 파일 구조로 전환하는 것임.

### 목표 (Goals)
- push 후처리 시 `.jgitkins/ci.yml`의 `on.push.rules`를 기준으로 branch별 `Job` 생성 규칙을 선택하는 구조를 설계함.
- 규칙 미매칭 시 `SKIPPED_NO_RULE`, pipeline file 미존재 시 `SKIPPED_PIPELINE_NOT_FOUND`로 정상 종료하는 정책을 명확히 정의함.
- pipeline file의 기준 경로를 repository root가 아니라 `.jgitkins/` 하위 상대 경로 체계로 정리함.
- `PushEventHandleService`는 오케스트레이션만 담당하고, 설정 조회/파싱/규칙 매칭 책임은 별도 support/port 계층으로 분리하는 방향을 확정함.
- `JobCreateCommand`와 `JobService`가 선택된 pipeline file 경로를 명시적으로 전달받는 구조로 전환하는 계획을 수립함.

### 범위 (Scope)
- **수정 대상**:
    - `PushEventHandleService`, `JobService`, `PushEventCommand`, `JobCreateCommand` 관련 기능 변경 계획 수립
    - `.jgitkins/ci.yml` 조회/파싱/규칙 매칭 구조 설계
    - `.jgitkins/` 하위 pipeline file 존재 검증 규칙 설계
    - Taskmaster `2.37` 하위 계획 문서 정리
- **수정 제외 대상**:
    - Java 코드 구현
    - 실제 `.jgitkins/ci.yml` 스키마 파서 구현
    - 실제 `Job` 도메인 또는 persistence 구조 변경
    - 실제 Git adapter 구현 변경

### 계획 (Plan)
- **단계 1: 요구사항 및 경로 정책 재정의**
    - 참조 문서 `docs/jgitkins-branch-pipeline.md`를 기준으로 현재 요구사항을 재해석하되, 문서 TODO를 반영하여 설정 파일 기준 경로를 `jgitkins.yml`이 아닌 `.jgitkins/ci.yml`로 고정함.
    - pipeline file 또한 repository root 직접 경로가 아니라 `.jgitkins/` 하위에 위치해야 하는 정책으로 재정의함.
    - 이 단계에서 아래 3가지 접근 방식을 비교 검토함.
    - ~~**방안 1**: 기존 `jgitkins.yml` 구조를 유지하고 file 경로만 부분 수정하는 최소 변경안을 검토함.~~
    - **방안 2**: `.jgitkins/ci.yml` 설정 조회와 `.jgitkins/` 하위 pipeline file 규칙을 함께 반영하는 구조 변경안을 검토함.
    - ~~**방안 3**: pipeline 설정을 YAML이 아닌 DB 구성으로 전환하는 확장안을 검토함.~~
    - 현재 요구사항, 기존 코드 수용성, 변경 범위를 비교한 결과 이번 계획은 **방안 2**를 기준으로 수립함.
    - **예시**:
    - AS-IS:
```yaml
on:
  push:
    rules:
      - branches: [main]
        file: Jenkinsfile
```
    - TO-BE:
```yaml
on:
  push:
    rules:
      - branches: [main]
        file: pipelines/main.Jenkinsfile
```
    - 위 예시에서 실제 설정 파일 위치는 `.jgitkins/ci.yml`, 실제 pipeline file 해석 경로는 `.jgitkins/pipelines/main.Jenkinsfile`가 됨.

- **단계 2: 현재 구조와 기능 공백 분석**
    - [PushEventHandleService](/Users/alzar/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/PushEventHandleService.java)는 branch 상태 반영 이후 단순 조건 검증만 거쳐 `JobCreateUseCase`를 호출하고 있음.
    - [JobService](/Users/alzar/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/JobService.java)는 `Jenkinsfile` 상수를 통해 파일 존재 여부를 검사하고 있음.
    - [PushEventCommand](/Users/alzar/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/dto/command/PushEventCommand.java)는 어떤 규칙이 매칭되었는지, 어떤 pipeline file이 선택되었는지, 왜 skip 되었는지를 표현하지 못함.
    - [FileGitPort](/Users/alzar/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/port/out/FileGitPort.java)는 파일 존재 여부만 검증 가능하고, `.jgitkins/ci.yml` blob 내용을 읽어오는 기능은 제공하지 않음.
    - 따라서 이번 기능은 단순 상수 교체가 아니라 입력 모델, 설정 조회 방식, 선택 결과 모델을 함께 재설계하는 작업으로 판단함.

- **단계 3: 설정 조회 및 매칭 책임 분리**
    - `.jgitkins/ci.yml` 조회와 YAML 파싱 책임은 `PushEventHandleService`에 직접 넣지 않고 별도 outbound port 및 support 컴포넌트로 분리하는 방안을 채택함.
    - 이 단계에서 아래 3가지 방식을 비교 검토함.
    - ~~**방안 A**: `PushEventHandleService` 내부에서 Git 파일 조회와 YAML 파싱을 모두 직접 수행함.~~
    - **방안 B**: `.jgitkins/ci.yml` 조회는 config 전용 port가 담당하고, 규칙 매칭은 application support에서 담당함.
    - ~~**방안 C**: `JobService`가 설정 조회부터 pipeline 선택까지 모두 수행함.~~
    - 유지보수성과 확장성을 고려할 때 **방안 B**를 채택함.
    - **예시**:
```java
PipelineSelectionResult selection = pipelineSelector.select(
        repositoryId,
        taskCd,
        repoName,
        branchName,
        commitHash
);
```
    - 위 selection 내부에서 `.jgitkins/ci.yml`을 읽고 규칙을 해석하되, `PushEventHandleService`는 결과만 소비하는 방향으로 설계함.

- **단계 4: 목표 입력/출력 모델 설계**
    - `PushEventCommand`는 push 사실 자체를 전달하는 순수 입력 모델로 유지하고, selection 결과는 별도 모델로 분리함.
    - 신규 개념은 아래와 같음.
    - `PushPipelineConfig`: `.jgitkins/ci.yml`의 `on.push.rules` 전체를 표현하는 읽기 모델
    - `PushPipelineRule`: `branches`, `file`, 향후 `paths`를 담는 규칙 모델
    - `PipelineSelectionResult`: `matched`, `pipelineFilePath`, `skipReason`를 담는 결과 모델
    - `PipelineSkipReason`: 최소 `SKIPPED_NO_RULE`, `SKIPPED_PIPELINE_NOT_FOUND`
    - `JobCreateCommand`에는 `pipelineFilePath` 필드를 추가하는 방향으로 계획함.
    - `pipelineFilePath`는 `.jgitkins/` 기준 상대 경로를 담도록 설계함.
    - **예시**:
```java
JobCreateCommand.builder()
        .repositoryId(command.getRepositoryId())
        .branchName(command.getBranchName())
        .commitHash(command.getCommitHash())
        .pipelineFilePath(".jgitkins/pipelines/main.Jenkinsfile")
        .triggeredBy(command.getTriggeredBy())
        .build();
```

- **단계 5: PushEventHandleService 오케스트레이션 재정의**
    - `PushEventHandleService`의 목표 역할은 아래 순서로 재정의함.
    - `repositoryId` 검증
    - branch 상태 반영
    - branch delete 여부, commit hash, triggeredBy 검증
    - `.jgitkins/ci.yml` 기반 pipeline selection 수행
    - 규칙 미매칭 시 `SKIPPED_NO_RULE`로 정상 종료
    - file 미존재 시 `SKIPPED_PIPELINE_NOT_FOUND`로 정상 종료
    - 규칙이 선택된 경우에만 `JobCreateUseCase.create()` 호출
    - **예시**:
```java
if (!validateCanCreateJob(command)) {
    return;
}

PipelineSelectionResult selection = pipelineSelector.select(...);
if (selection.isSkipped()) {
    log.info("push event job skipped: reason={}", selection.getSkipReason());
    return;
}

jobCreateUseCase.create(buildJobCommand(command, selection));
```

- **단계 6: JobService 계약 변경**
    - `JobService`는 더 이상 `JENKINS_FILE_PATH = "Jenkinsfile"` 상수를 기준으로 동작하지 않음.
    - 입력 command가 전달한 `.jgitkins/...` 경로를 기준으로 실제 pipeline file 존재 여부를 검증하는 구조로 단순화함.
    - 이로써 `JobService`는 정책 해석이 아니라 "이미 선택된 pipeline file을 가진 Job 생성"만 담당하도록 경계를 정리함.
    - 장기적으로는 file 존재 검증도 `PushEventHandleService` 쪽에서 완료하고, `JobService`는 저장만 담당하는 구조까지 확장 가능함.

- **단계 7: 테스트 계획 수립**
    - 필수 테스트 시나리오는 아래와 같음.
    - `.jgitkins/ci.yml` 규칙과 branch가 매칭되고, `.jgitkins/` 하위 pipeline file이 존재하면 `Job` 생성
    - `.jgitkins/ci.yml`이 없거나 rules가 비어 있으면 `SKIPPED_NO_RULE`
    - matching rule은 있으나 대상 pipeline file이 `.jgitkins/` 하위에 없으면 `SKIPPED_PIPELINE_NOT_FOUND`
    - branch delete 이벤트면 selection 이전에 `Job` 생성 생략
    - `triggeredBy` 없음, commit hash 없음은 기존처럼 skip
    - 여러 규칙이 있을 때 첫 번째 매칭 규칙이 우선 선택됨
    - 향후 `release/*`, `paths` 확장을 고려해 matcher 테스트는 exact match와 확장 포인트를 분리하여 설계함.

### 기대효과 (Expected Benefits)
- `.jgitkins/ci.yml`과 `.jgitkins/` 하위 pipeline file 체계를 기준으로 push 후처리 규칙이 명확해짐.
- branch별 서로 다른 pipeline 선택 요구사항을 수용할 수 있게 됨.
- `PushEventHandleService`, 설정 조회/파싱, `JobService`의 책임 경계가 분리되어 유지보수성이 향상됨.
- 규칙 미매칭과 file 미존재가 예외가 아닌 정상적인 skip 결과로 표준화되어 운영 로그 해석이 쉬워짐.
- 향후 `release/*`, `paths` 확장 규칙을 추가하더라도 matcher 구조를 재사용할 수 있게 됨.

### 예시 (방안 2 기준 코드 스니펫)

#### AS-IS (현재 구조)
```java
if (validateCanCreateJob(command)) {
    jobCreateUseCase.create(buildJobCommand(command));
}
```

#### TO-BE (개선 제안 구조)
```java
if (!validateCanCreateJob(command)) {
    return;
}

PipelineSelectionResult selection = pipelineSelector.select(
        command.getRepositoryId(),
        command.getTaskCd(),
        command.getRepoName(),
        command.getBranchName(),
        command.getCommitHash()
);

if (selection.isSkipped()) {
    return;
}

jobCreateUseCase.create(
        buildJobCommand(command, selection.getPipelineFilePath())
);
```

### 주의사항
- **구현 금지**: 본 문서는 기능 변경 계획만 다루며, 실제 Java 코드 구현은 절대 진행하지 않음.
- **경로 정책 일관성 유지**: 설정 파일은 `.jgitkins/ci.yml`, pipeline file은 `.jgitkins/` 하위 경로라는 기준을 문서 전체에서 일관되게 유지해야 함.
- **skip은 정상 종료**: `SKIPPED_NO_RULE`, `SKIPPED_PIPELINE_NOT_FOUND`는 예외가 아니라 정상 후처리 결과로 취급해야 함.
- **문서체 규약**:
    - 모든 문장은 공식 문서체로 작성함.
    - 문장 끝은 `~~함` 또는 `~~하였음` 형태를 유지함.
    - 현재 상태와 목표 상태를 혼동하지 않도록 AS-IS / TO-BE를 분리하여 서술함.

### 결론
- 본 문서는 Task `2.37`의 새 제목인 `Push로 인한 Job 생성 규칙 변경`을 기준으로 작성한 계획 문서임.
- 본 계획은 `.jgitkins/ci.yml`과 `.jgitkins/` 하위 pipeline file 구조를 기준으로 push 후처리 규칙을 재정의하는 방향을 확정하였음.
- 현재 단계에서는 계획 문서 이동 및 내용 정리만 수행하였으며, 구현은 진행하지 않았음.

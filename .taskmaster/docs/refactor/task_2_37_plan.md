# 리팩토링 계획서

### 제목
- **기능 변경 계획**: `Push`로 인한 `Job` 생성 규칙 변경

### 배경 (왜?)
- 현재 [PushEventHandleService](/Users/alzar/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/PushEventHandleService.java)는 push 이벤트 후 branch 상태 반영과 `JobCreateUseCase` 호출만 담당하고 있으며, branch별 pipeline 선택 규칙을 해석하는 단계가 부재함.
- 현재 [JobService](/Users/alzar/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/JobService.java)는 `Jenkinsfile` 고정 경로를 기준으로만 파일 존재 여부를 확인하고 있어, 브랜치별 상이한 pipeline file 선택 요구사항을 수용할 수 없음.
- 참조 문서 [jgitkins-branch-pipeline.md](/Users/alzar/task/sources/jgitkins/jgitkins-server/docs/jgitkins-branch-pipeline.md)는 branch별 규칙 기반 선택, 규칙 미매칭 시 skip, pipeline file 미존재 시 skip 정책을 요구하고 있음.
- 요구사항 변경 기준으로 설정 파일 경로와 pipeline file 위치 정책도 함께 변경되어야 함.
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
    - 참조 문서 `docs/jgitkins-branch-pipeline.md`를 기준으로 현재 요구사항을 재해석하되, 설정 파일 기준 경로를 `jgitkins.yml`이 아닌 `.jgitkins/ci.yml`로 고정함.
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
    - [JobService](/Users/alzar/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/JobService.java)는 현재 `pipelineFilePath`를 전달받아 `Job` aggregate 생성과 persistence 저장만 담당함.
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
JobPlan jobPlan = pushJobCreationPlanner.plan(
        repositoryId,
        taskCd,
        repoName,
        branchName,
        commitHash
);
```
    - 위 `plan(...)` 내부에서 `.jgitkins/ci.yml`을 읽고 규칙을 해석하되, `PushEventHandleService`는 결과만 소비하는 방향으로 설계함.

- **단계 4: 목표 입력/출력 모델 설계**
    - `PushEventCommand`는 push 사실 자체를 전달하는 순수 입력 모델로 유지하고, `Job` 생성 계획 결과는 별도 모델로 분리함.
    - 신규 개념은 아래와 같음.
    - `PipelineConfig`: `.jgitkins/ci.yml`의 `on.push.rules` 전체를 표현하는 읽기 모델
    - `PipelineRule`: `branches`, `file`, 향후 `paths`를 담는 규칙 모델
    - `JobPlan`: `creatable`, `pipelineFilePath`, `skipReason`를 담는 결과 모델
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
    - `.jgitkins/ci.yml` 기반 `Job` 생성 계획 수립 수행
    - 규칙 미매칭 시 `SKIPPED_NO_RULE`로 정상 종료
    - file 미존재 시 `SKIPPED_PIPELINE_NOT_FOUND`로 정상 종료
    - 규칙이 선택된 경우에만 `JobCreateUseCase.create()` 호출
    - **예시**:
```java
if (!canCreateJob(command)) {
    return;
}

JobPlan jobPlan = pushJobCreationPlanner.plan(...);
if (jobPlan.isSkipped()) {
    log.info("push event job skipped: reason={}", jobPlan.getSkipReason());
    return;
}

jobCreateUseCase.create(buildJobCommand(command, jobPlan));
```

- **단계 6: JobService 계약 변경**
    - `JobService`는 더 이상 `JENKINS_FILE_PATH = "Jenkinsfile"` 상수를 기준으로 동작하지 않음.
    - `.jgitkins/ci.yml` 해석, branch 규칙 매칭, pipeline file 존재 여부 검증은 모두 `PushJobCreationPlanner`가 전담함.
    - 입력 command가 전달한 `pipelineFilePath`는 이미 planner가 검증을 완료한 결과로 취급함.
    - 이로써 `JobService`는 정책 해석이나 중복 검증 없이 "이미 생성 가능하다고 판단된 Job 저장"만 담당하도록 경계를 정리함.
    - 즉 최종 책임 분리는 아래와 같음.
    - `PushJobCreationPlanner`: 규칙 판단, file 존재 검증, skip reason 결정
    - `JobService`: `Job` aggregate 생성 및 persistence 저장

- **단계 7: 테스트 계획 수립**
    - 필수 테스트 시나리오는 아래와 같음.
    - `.jgitkins/ci.yml` 규칙과 branch가 매칭되고, `.jgitkins/` 하위 pipeline file이 존재하면 `Job` 생성
    - `.jgitkins/ci.yml`이 없거나 rules가 비어 있으면 `SKIPPED_NO_RULE`
    - matching rule은 있으나 대상 pipeline file이 `.jgitkins/` 하위에 없으면 `SKIPPED_PIPELINE_NOT_FOUND`
    - branch delete 이벤트면 `Job` 계획 수립 이전에 생성 흐름 생략
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
if (canCreateJob(command)) {
    jobCreateUseCase.create(buildJobCommand(command));
}
```

#### TO-BE (개선 제안 구조)
```java
if (!canCreateJob(command)) {
    return;
}

JobPlan jobPlan = pushJobCreationPlanner.plan(
        command.getRepositoryId(),
        command.getTaskCd(),
        command.getRepoName(),
        command.getBranchName(),
        command.getCommitHash()
);

if (jobPlan.isSkipped()) {
    return;
}

jobCreateUseCase.create(
        buildJobCommand(command, jobPlan.getPipelineFilePath())
);
```

#### TO-BE (간결한 전체 흐름 예시)
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PushEventHandleService implements PushEventHandleUseCase {

    private final JobCreateUseCase jobCreateUseCase;
    private final BranchPersistencePort branchPort;
    private final PushJobCreationPlanner pushJobCreationPlanner;

    @Override
    @Transactional
    public void handle(PushEventCommand command) {
        validate(command);

        updateBranchState(command);

        if (!canCreateJob(command)) {
            return;
        }

        JobPlan jobPlan = pushJobCreationPlanner.plan(
                command.getTaskCd(),
                command.getRepoName(),
                command.getBranchName(),
                command.getCommitHash()
        );

        if (jobPlan.isSkipped()) {
            log.info("push job skipped: reason={}", jobPlan.getSkipReason());
            return;
        }

        jobCreateUseCase.create(
                JobCreateCommand.builder()
                        .taskCd(command.getTaskCd())
                        .repoName(command.getRepoName())
                        .repositoryId(command.getRepositoryId())
                        .branchName(command.getBranchName())
                        .commitHash(command.getCommitHash())
                        .triggeredBy(command.getTriggeredBy())
                        .pipelineFilePath(jobPlan.getPipelineFilePath())
                        .build()
        );
    }

    private boolean canCreateJob(PushEventCommand command) {
        return !command.isBranchDeleted()
                && command.getCommitHash() != null
                && !command.getCommitHash().isBlank()
                && command.getTriggeredBy() != null;
    }
}
```

```java
@Component
@RequiredArgsConstructor
public class PushJobCreationPlanner {

    private final PipelineConfigPort configPort;
    private final FileGitPort fileGitPort;

    public JobPlan plan(
            String taskCd,
            String repoName,
            String branchName,
            String commitHash
    ) {
        PipelineConfig config = configPort.read(taskCd, repoName, commitHash);

        PipelineRule rule = config.findRule(branchName);
        if (rule == null) {
            return JobPlan.skip(SKIPPED_NO_RULE);
        }

        String pipelineFilePath = ".jgitkins/" + rule.getFile();

        if (!fileGitPort.exists(taskCd, repoName, commitHash, pipelineFilePath)) {
            return JobPlan.skip(SKIPPED_PIPELINE_NOT_FOUND);
        }

        return JobPlan.create(pipelineFilePath);
    }
}
```

```java
public interface PipelineConfigPort {

    PipelineConfig read(
            String taskCd,
            String repoName,
            String commitHash
    );
}
```

```java
public record PipelineConfig(
        List<PipelineRule> rules
) {

    public PipelineRule findRule(String branchName) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }

        for (PipelineRule rule : rules) {
            if (rule.matches(branchName)) {
                return rule;
            }
        }

        return null;
    }
}
```

```java
public record PipelineRule(
        List<String> branches,
        String file
) {

    public boolean matches(String branchName) {
        if (branches == null || branches.isEmpty()) {
            return false;
        }

        for (String candidate : branches) {
            if (candidate.equals(branchName)) {
                return true;
            }

            if (candidate.endsWith("/*")) {
                String prefix = candidate.substring(0, candidate.length() - 1);
                if (branchName.startsWith(prefix)) {
                    return true;
                }
            }
        }

        return false;
    }
}
```

```java
public record JobPlan(
        boolean creatable,
        String pipelineFilePath,
        PipelineSkipReason skipReason
) {

    public static JobPlan create(String pipelineFilePath) {
        return new JobPlan(true, pipelineFilePath, null);
    }

    public static JobPlan skip(PipelineSkipReason skipReason) {
        return new JobPlan(false, null, skipReason);
    }

    public boolean isSkipped() {
        return !creatable;
    }
}
```

#### TO-BE 흐름 요약
- `PushHook`는 push 이벤트를 `PushEventCommand`로 변환하여 전달함.
- `PushEventHandleService`는 입력 검증, branch 상태 반영, 사전 skip 조건 검증까지만 담당함.
- `canCreateJob`는 branch 삭제, commit 없음, 사용자 없음처럼 pipeline 선택 전에 바로 종료해야 하는 조건을 확인함.
- `PipelineConfigPort`는 `.jgitkins/ci.yml`을 읽어 `PipelineConfig`로 반환함.
- `PipelineConfig`와 `PipelineRule`은 branch 규칙 집합과 개별 rule을 표현하며, 첫 번째 매칭 rule 탐색 책임을 가짐.
- `PushJobCreationPlanner`는 config 조회 결과를 사용해 branch 규칙 매칭 및 `.jgitkins/` 하위 pipeline file 존재 여부를 검증함.
- `PushJobCreationPlanner`가 생성 가능 여부를 이미 확정하므로, `JobService`는 동일 판단을 재수행하지 않음.
- 매칭 실패 시 `SKIPPED_NO_RULE`, file 미존재 시 `SKIPPED_PIPELINE_NOT_FOUND`로 정상 종료함.
- `JobPlan`이 생성 가능 상태를 반환하면 `pipelineFilePath`가 포함된 `JobCreateCommand`를 생성하여 `JobCreateUseCase`로 전달함.

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

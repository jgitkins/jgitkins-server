# Task ID: 1

**Title:** Feature Runner Management API

**Status:** done

**Dependencies:** None

**Priority:** medium

**Description:** Create the core pipeline/job aggregates, DTOs, and storage ports that will back CI orchestration.

**Details:**

You asked: "Please Translate Implementation Detail to korean." A more natural phrasing is: "Please translate the implementation details into Korean." 이후 구현 지침은 한국어로 정리했습니다.
`src/main/java/io/jgitkins/server/domain/model/Job.java`에서 사용하는 Aggregate/Value Object 패턴을 벤치마킹해 `application/domain` 패키지에 `PipelineJob`, `PipelineStage`, `JobStatus`, `RunnerAssignment` 레코드를 추가하고, 각 객체에 저장소 경로·taskCd·commitSha·requestedBy·Jenkinsfile digest·큐 진입/할당 시각 등 메타데이터를 포함한다. `application/dto`에는 `PipelineJobCommand` 계열 DTO를 두고 `presentation/mapper` 아래 기존 MapStruct 예제(`CreateRepositoryMapper`)처럼 `@Mapper(componentModel = "spring")` 인터페이스를 만들어 REST 요청 ↔ 도메인 변환을 담당하게 한다. `application/port/out`에는 `PipelineJobPersistencePort`를 선언하고, `infrastructure/persistence/adapter`에 ConcurrentHashMap 기반 인메모리 구현을 두며 이후 DB 어댑터로 교체할 수 있도록 Spring 구성(`src/main/java/io/jgitkins/server/config` 참고)에서 빈으로 주입한다. 저장/조회/락 동작에 대한 의사코드는 `PipelineJob job = PipelineJob.create(taskCd, repoName, commitSha, definition); pipelineJobRepository.save(job);` 형태로 문서화하고, Runner 관련 Task 4에서 사용할 수 있도록 `RunnerAssignment`가 Runner 이미지/플러그인 매니페스트 식별자를 참조할 수 있게 설계한다.

**Test Strategy:**

도메인 팩토리 및 상태 전이(pending→queued→running→succeeded) 로직은 `src/test/java/.../domain`에 단위 테스트를 추가하고, 인메모리 `PipelineJobPersistencePort` 어댑터는 Spring Boot 슬라이스 테스트로 save/find/lock 시나리오를 검증한다.

## Subtasks

### 1.1. Runner Registration API

**Status:** done  
**Dependencies:** None  

Wokring Create an API to Register Runner such as GitLab CI

### 1.2. Runner Integration API

**Status:** done  
**Dependencies:** None  

Feature Integration API to Integration Runner's Instance

### 1.3. Runner Loading API

**Status:** done  
**Dependencies:** None  

Runner 조회 API

### 1.4. Runner Delete API

**Status:** done  
**Dependencies:** None  

Runner 삭제 API

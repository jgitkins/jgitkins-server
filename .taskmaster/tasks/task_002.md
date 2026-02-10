# Task ID: 2

**Title:** Feature Integrate MQ

**Status:** cancelled

**Dependencies:** None

**Priority:** medium

**Description:** Introduce the Feature MQ message-bus so the CI coordinator can asynchronously push pipeline job definitions and runner heartbeat signals between the Spring Boot orchestrator and external JGitkins Runners, ensuring jobs created from parsed Jenkinsfiles are queued and acknowledged reliably.

**Details:**

요청 문구 개선: “Please translate the implementation details into Korean.” 이하 구현 지침은 한국어로 정리했습니다.
`src/main/java/io/jgitkins/server/application/port/out/` 패키지에 `RepositoryContentPort`(참고: RepositoryService가 사용)와 나란히 `JobQueuePort`, `RunnerHeartbeatPort`를 추가해 애플리케이션 계층이 저장소·메시징 구현에서 분리되도록 한다. 각 포트는 Jenkinsfile 파싱 이후 생성되는 잡 메타데이터(`job_id`, `repository_id`, `commit_hash`, `runner_id`, 타임스탬프)를 직렬화·enqueue/dequeue하거나 러너 하트비트를 upsert/조회하는 메서드를 정의한다. 인프라 계층에는 `src/main/java/io/jgitkins/server/infrastructure/adapter/mq/` 이하에 인메모리 구현(예: `InMemoryJobQueueAdapter`, `InMemoryRunnerHeartbeatAdapter`)을 두고, 향후 실제 MQ 프로바이더로 교체할 수 있도록 스프링 빈 구성을 `infrastructure/config/mq/MessagingConfig`에서 관리한다. 파이프라인 잡 생성 서비스(`src/main/java/io/jgitkins/server/application/port/service/` 내 예정)에는 새 포트를 주입하여 Jenkinsfile 파싱 시 직렬화 payload를 큐에 push하고 실패 시 도메인 예외를 던지며, 하트비트 어댑터를 통해 러너 생존 신호를 읽고 필요 시 갱신한다. 큐 작업 중 발생한 예외는 도메인 계층으로 전파하여 디스패처가 enqueue 실패를 감지하도록 한다.

**Test Strategy:**

Add unit tests around the new `JobQueuePort`-backed service to ensure payloads derived from job metadata (repo, commit, runner assignment) are serialized consistently before enqueue. Provide integration-style tests that spin up the in-memory MQ adapter in Spring and verify enqueue/dequeue semantics plus error propagation when the queue is unavailable.

## Subtasks

### 2.1. Define MQ application ports and payload DTOs

**Status:** pending  
**Dependencies:** None  

Add queue-specific contracts in the application layer so services can enqueue jobs and track runner heartbeats without binding to infrastructure.

**Details:**

`src/main/java/io/jgitkins/server/application/port/out/`에 `JobQueuePort`, `RunnerHeartbeatPort` 인터페이스를 추가하여 enqueue/dequeue, ack, 하트비트 조회·저장을 추상화하고, DTO(`application/dto/JobQueuePayload`, `RunnerHeartbeatSnapshot`)에는 `job_id`, `repository_id`, `commit_hash`, `runner_id`, 타임스탬프 필드를 포함한 불변 객체를 정의한다. 큐 작업 실패를 표준화하기 위해 `application/exception/JobQueueException`(새로운 패키지 위치 허용)을 만들어 서비스 계층이 명확한 메시지와 함께 예외를 전달할 수 있게 한다.

### 2.2. Implement in-memory MQ adapters

**Status:** pending  
**Dependencies:** 2.1  

Provide infrastructure adapters that satisfy the new queue ports using in-memory data structures to unblock development while keeping the design swappable for a real MQ provider later.

**Details:**

`src/main/java/io/jgitkins/server/infrastructure/adapter/mq/`에 `InMemoryJobQueueAdapter`(예: `BlockingQueue<JobQueuePayload>` 기반)와 `InMemoryRunnerHeartbeatAdapter`(예: `ConcurrentHashMap<String, RunnerHeartbeatSnapshot>`)를 구현해 각각 `JobQueuePort`, `RunnerHeartbeatPort`를 충족시키고, Spring `@Component` 혹은 구성 클래스로 빈 등록 가능한 형태로 작성한다. 큐 오퍼레이션 실패 시 `JobQueueException`을 던지며, 로그에 직렬화된 payload/runner id를 포함해 추적하기 쉽게 만든다.

### 2.3. Wire MQ adapters through Spring configuration

**Status:** pending  
**Dependencies:** 2.2  

Expose the in-memory queue adapters as beans and prepare configuration hooks so future MQ providers can replace them cleanly.

**Details:**

`src/main/java/io/jgitkins/server/infrastructure/config/mq/MessagingConfig.java`(새 디렉터리)에서 `@Configuration` 클래스를 정의하고 `@Bean` 메서드로 인메모리 어댑터를 등록하며, `@ConditionalOnMissingBean(JobQueuePort.class)` 등 조건을 붙여 실제 MQ 구현이 존재할 경우 자동으로 대체되도록 한다. 러너 하트비트 만료 처리나 재시도 스케줄러가 필요하면 같은 구성 클래스 안에서 `TaskScheduler` 또는 `Executor` 빈을 정의해 어댑터에 주입한다.

### 2.4. Extend job creation flow to emit MQ events

**Status:** pending  
**Dependencies:** 2.1, 2.3  

Hook the new ports into the service that turns parsed Jenkinsfiles into runnable jobs so every job is serialized and enqueued reliably.

**Details:**

`src/main/java/io/jgitkins/server/application/port/service/` 경로에 `PipelineJobService`(또는 기존 서비스 확장)를 두고, Jenkinsfile 파싱으로 생성된 잡 엔터티를 `JobQueuePayload`로 변환하여 `JobQueuePort.enqueue`를 호출한다. 직렬화 시 `data/ERD.md`에 맞춘 필드 네이밍을 유지하며, enqueue 실패는 `JobQueueException`으로 감싸 상위 호출자가 재시도/롤백을 결정할 수 있게 한다. 러너 배정 전 하트비트 확인이 필요하면 `RunnerHeartbeatPort`를 주입해 특정 러너가 최신 신호를 보냈는지 검사하고, 필요 시 하트비트를 갱신하는 헬퍼 메서드를 추가한다.

### 2.5. Add end-to-end tests covering queue publishing and heartbeat semantics

**Status:** pending  
**Dependencies:** 2.1, 2.2, 2.4  

Prove the MQ integration works by exercising the in-memory adapters inside a Spring test slice that simulates job creation and runner heartbeats.

**Details:**

`src/test/java/io/jgitkins/server/mq/JobQueueIntegrationTest` 등 패키지에서 스프링 테스트 슬라이스를 띄우고 `MessagingConfig`를 불러온 뒤, 페이크 파이프라인 잡을 persist/생성하고 서비스가 `JobQueuePort`를 통해 enqueue 했는지, 인메모리 큐에 직렬화 payload가 저장됐는지, `RunnerHeartbeatPort`가 러너 하트비트 타임스탬프를 최신 상태로 유지하는지 검증한다. 큐 예외를 강제로 발생시켜 도메인 예외가 상위로 전파되는 네거티브 시나리오도 포함한다.

### 2.6. Job Integration

**Status:** in-progress  
**Dependencies:** None  

Job 생성 및 메세지 발행 연계 작업 진행
 - Repository 도메인 모델링 
-`onPostReceive` 훅 로직 변경(Push 이벤트 감지후, 저장소 로딩 및 핸들링)

### 2.7. Job Message Publish

**Status:** done  
**Dependencies:** None  

1. Scheduling 작업 진행
2. Dispatch Job to Runnable Runner

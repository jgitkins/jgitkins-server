# Task ID: 3

**Title:** Feature Batch Job Publisher

**Status:** cancelled

**Dependencies:** 1 ✓, 2 ✗

**Priority:** medium

**Description:** Create a scheduled dispatcher that scans stored pipeline jobs and enqueues them only when at least one runnable runner is currently reporting heartbeats.

**Details:**

1. Follow the service layout under `src/main/java/io/jgitkins/server/application/port/service/` (see `RepositoryService` as a reference) to add a `JobDispatchService` that consumes `PipelineJobPersistencePort`, `JobQueuePort`, and `RunnerHeartbeatPort` (from Tasks 1 & 2). The service should expose `publishPendingJobs()` that: a) loads pending `PipelineJob`s flagged as runnable, b) checks runner availability via heartbeat data (only runners with recent timestamps and matching labels/architectures should qualify), c) transitions the job status to `QUEUED` and persists it before enqueueing via `JobQueuePort`, and d) skips jobs when no runner fits.
2. Implement a Spring `@Component` scheduler (e.g., `PipelineJobPublisherBatch` under `src/main/java/io/jgitkins/server/infrastructure/batch/`) using `@Scheduled(fixedDelay = …)` and enable scheduling in `JGitkinsServerApplication` if not already. The batch should call `JobDispatchService.publishPendingJobs()` and log outcomes with structured log messages (job id, runner id) using SLF4J, mirroring logging already present in adapters like `JGitRepositoryAdapter`.
3. Add configuration knobs in `application.yml` (new `jgitkins.dispatcher.*` section) for delay interval, heartbeat freshness threshold, and per-runner concurrency guard. Bind them via a `@ConfigurationProperties` class under `infrastructure/config` similar to existing configs (e.g., `DataSourceConfig`). Inject these properties into the batch component.
4. Extend the domain from Task 1 by documenting the new `QUEUED` transition and runner-selection constraints inside the `PipelineJob` aggregate (update its JavaDoc or KDoc comments once implemented) so later tasks understand invariants. Ensure the dispatch logic throws `ResourceLockedException` when concurrent updates race, matching existing exception types in `application/common/exception`.
5. Prepare the infrastructure adapter for recording runner occupancy: add a lightweight `RunnerAssignmentTracker` (in-memory Map for now) under `infrastructure/adapter/mq/` that mirrors how other adapters (e.g., `JGitBranchAdapter`) encapsulate external resources, so future MQ-backed implementations can reuse the selection logic.

**Test Strategy:**

- Add unit tests in `src/test/java/.../application/port/service/JobDispatchServiceTest` using mocks for `PipelineJobPersistencePort`, `JobQueuePort`, and `RunnerHeartbeatPort` to cover: (a) publishing succeeds when a runner is available, (b) no enqueue occurs when no runner heartbeat meets freshness criteria, and (c) concurrency guard prevents double enqueue. Use Spring’s `@ExtendWith(SpringExtension.class)` and Mockito as in existing tests.
- Create a Spring Boot slice/integration test (e.g., `PipelineJobPublisherBatchIT`) that wires the real in-memory adapters, runs the `PipelineJobPublisherBatch` via `@SpringBootTest` with `@ActiveProfiles("test")`, and verifies that jobs inserted through the persistence port are moved to the in-memory queue only when a heartbeat entry exists.
- Include a configuration binding test asserting that `jgitkins.dispatcher` properties map correctly by loading the context with a custom `@TestPropertySource`.

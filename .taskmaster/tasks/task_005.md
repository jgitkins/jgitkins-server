# Task ID: 5

**Title:** Scheduled Job Publication Flow

**Status:** cancelled

**Dependencies:** 1 ✓, 2 ✗, 3 ✗

**Priority:** medium

**Description:** Implement a scheduler-driven service that detects pending pipeline jobs, ensures a compatible runnable runner exists, updates job history, and enqueues the job for execution.

**Details:**

1. Add a Spring `@Scheduled` component (e.g., `JobSchedulePoller` under `src/main/java/io/jgitkins/server/application/scheduling/`) that invokes the existing `JobDispatchService.publishPendingJobs()` from Task 3 at configurable intervals (default 30s via `application.yml`).
2. Within `JobDispatchService`, implement logic to query `PipelineJobPersistencePort` for pending jobs, ask `RunnerHeartbeatPort` for runners whose heartbeats fall within the freshness window and match required labels/arch, and pick the best runner via `RunnerAssignment` metadata.
3. When a runner is selected, update the job’s domain aggregate: append a history entry (new `JobHistoryEvent`/value object) capturing transition `PENDING→QUEUED`, runner id, timestamp, and any queue reference.
4. Persist the updated job via `PipelineJobPersistencePort` using optimistic locking to prevent duplicate dispatch, then call `JobQueuePort.enqueue()` with a DTO mirroring Task 2’s payload specification.
5. Emit structured logs/metrics (Micrometer counter for dispatched jobs, gauge for backlog) to aid observability and ensure the scheduler is idempotent (skip processing when no runners qualify).

**Test Strategy:**

- Add `JobDispatchServiceTest` cases: (a) verifies pending job transitions to queued when a fresh runner is available—assert history entry and enqueue call; (b) ensures no enqueue when no runner passes freshness/label filters; (c) concurrency test mocking persistence lock failure to confirm retry/skip behavior.
- Create `JobSchedulePollerTest` (Spring slice) to assert the scheduler delegates to `JobDispatchService` and honors configuration (use `@ExtendWith(SpringExtension.class)` plus `@Import(JobSchedulePoller.class)` with mocked service).
- Optional integration test wiring in-memory implementations of ports to ensure full flow updates job records and publishes to queue.

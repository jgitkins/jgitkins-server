# Task ID: 14

**Title:** Implement Runner Health Check (Heartbeat)

**Status:** pending

**Dependencies:** 1 ✓, 6 ✓, 10 ✓, 11 ✓, 13 ✓

**Priority:** medium

**Description:** Develop a system for JGitkins runners to send periodic heartbeat signals to the server, enabling the server to monitor runner health and availability.

**Details:**

1.  **Domain Model Definition**: Define a new `Runner` aggregate (e.g., `io.jgitkins.server.application.domain.model.Runner.java`) following patterns established in Task 1 (`Job.java`), Task 6 (`Repository.java`), and Task 11 (`User.java`). The model should include properties such as `id` (UUID), `name` (unique identifier for the runner), `status` (e.g., `ONLINE`, `OFFLINE`, `UNRESPONSIVE`), `lastHeartbeat` (Timestamp), `capabilities` (List<String>), `version` (String), and `registrationTokenId` (UUID, linking to a managed credential from Task 13, for secure identification).
2.  **Persistence Layer**: Create a `RunnerRepository` interface (e.g., `io.jgitkins.server.application.port.out.RunnerRepository.java`) for CRUD operations on the `Runner` aggregate. Implement this repository using the existing data access patterns (e.g., Spring Data JPA).
3.  **Service Layer**: Implement a `RunnerService` (e.g., `io.jgitkins.server.application.service.RunnerService.java`) to handle business logic:
    *   `registerRunner(RunnerRegistrationCommand)`: Creates a new `Runner` entity, potentially generating a unique authentication token (handled by Task 13).
    *   `processHeartbeat(UUID runnerId, HeartbeatPayload payload)`: Updates the `lastHeartbeat` timestamp and `status` of the specified runner. The `HeartbeatPayload` can include current status and resource utilization.
    *   `getRunnerStatus(UUID runnerId)`: Retrieves the current status of a runner.
4.  **API Endpoint**: Expose a REST endpoint (e.g., `POST /api/v1/runners/{runnerId}/heartbeat`) in `io.jgitkins.server.api.RunnerController.java` for runners to send heartbeat signals. This endpoint must be secured using JWT (from Task 10), requiring runners to authenticate using a pre-shared token or a runner-specific JWT issued during registration (potentially managed via Task 13's credentials).
5.  **Scheduled Health Check Service**: Implement a background scheduled task (e.g., using `@Scheduled` in Spring) to periodically scan the `Runner` entities. For any runner where `lastHeartbeat` is older than a defined threshold (e.g., 5 minutes), update its `status` to `UNRESPONSIVE` or `OFFLINE`.
6.  **Authentication for Runners**: Leverage Task 13 for secure runner identification. During runner registration, generate a unique API token (a type of credential) that the runner will use for subsequent heartbeat calls. The server will validate this token on each heartbeat.

**Test Strategy:**

1.  **Domain Unit Tests**: Create `RunnerTest.java` under `src/test/java/.../domain/model/` to thoroughly test the `Runner` aggregate's constructor, immutability, status transitions, and `lastHeartbeat` updates.
2.  **Service Layer Tests**: Develop `RunnerServiceTest.java` to test:
    *   Successful processing of heartbeat signals, verifying `lastHeartbeat` and `status` updates.
    *   Runner registration and token generation.
    *   Handling of non-existent runner IDs during heartbeat processing.
3.  **API Integration Tests**: Implement `RunnerControllerIntegrationTest.java` to:
    *   Verify the `/api/v1/runners/{runnerId}/heartbeat` endpoint's functionality.
    *   Test successful heartbeat submission with valid authentication credentials (JWT/token).
    *   Test rejection of heartbeat requests with invalid or missing authentication.
    *   Ensure proper HTTP status codes are returned for various scenarios.
4.  **Scheduled Task Tests**: Write tests for the background health check service to verify:
    *   Runners whose `lastHeartbeat` exceeds the threshold are correctly marked as `UNRESPONSIVE` or `OFFLINE`.
    *   Runners within the threshold remain `ONLINE`.
5.  **Security Tests**: Validate that only authenticated runners can submit heartbeats, leveraging the JWT and credential management systems established in Tasks 10 and 13.

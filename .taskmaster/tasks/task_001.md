# Task ID: 1

**Title:** 신규기능

**Status:** in-progress

**Dependencies:** None

**Priority:** high

**Description:** 사용자 가치 전달을 위한 기능 개발/확장 작업

**Details:**


기존 기능별 Task를 카테고리 기반(신규기능/리팩토링/보안)으로 재구성함.

**Test Strategy:**


카테고리별 우선순위에 따라 하위 작업을 순차 수행하고 회귀 테스트를 적용한다.

## Subtasks

### 1.1. [1] Feature Runner Management API

**Status:** done  

**Dependencies:** None  


Create the core pipeline/job aggregates, DTOs, and storage ports that will back CI orchestration.

**Details:**


You asked: "Please Translate Implementation Detail to korean." A more natural phrasing is: "Please translate the implementation details into Korean." 이후 구현 지침은 한국어로 정리했습니다.
`src/main/java/io/jgitkins/server/domain/model/Job.java`에서 사용하는 Aggregate/Value Object 패턴을 벤치마킹해 `application/domain` 패키지에 `PipelineJob`, `PipelineStage`, `JobStatus`, `RunnerAssignment` 레코드를 추가하고, 각 객체에 저장소 경로·taskCd·commitSha·requestedBy·Jenkinsfile digest·큐 진입/할당 시각 등 메타데이터를 포함한다. `application/dto`에는 `PipelineJobCommand` 계열 DTO를 두고 `presentation/mapper` 아래 기존 MapStruct 예제(`CreateRepositoryMapper`)처럼 `@Mapper(componentModel = "spring")` 인터페이스를 만들어 REST 요청 ↔ 도메인 변환을 담당하게 한다. `application/port/out`에는 `PipelineJobPersistencePort`를 선언하고, `infrastructure/persistence/adapter`에 ConcurrentHashMap 기반 인메모리 구현을 두며 이후 DB 어댑터로 교체할 수 있도록 Spring 구성(`src/main/java/io/jgitkins/server/config` 참고)에서 빈으로 주입한다. 저장/조회/락 동작에 대한 의사코드는 `PipelineJob job = PipelineJob.create(taskCd, repoName, commitSha, definition); pipelineJobRepository.save(job);` 형태로 문서화하고, Runner 관련 Task 4에서 사용할 수 있도록 `RunnerAssignment`가 Runner 이미지/플러그인 매니페스트 식별자를 참조할 수 있게 설계한다.


(legacy task: 1)

### 1.2. [2] Feature Integrate MQ

**Status:** cancelled  

**Dependencies:** None  


Introduce the Feature MQ message-bus so the CI coordinator can asynchronously push pipeline job definitions and runner heartbeat signals between the Spring Boot orchestrator and external JGitkins Runners, ensuring jobs created from parsed Jenkinsfiles are queued and acknowledged reliably.

**Details:**


요청 문구 개선: “Please translate the implementation details into Korean.” 이하 구현 지침은 한국어로 정리했습니다.
`src/main/java/io/jgitkins/server/application/port/out/` 패키지에 `RepositoryContentPort`(참고: RepositoryService가 사용)와 나란히 `JobQueuePort`, `RunnerHeartbeatPort`를 추가해 애플리케이션 계층이 저장소·메시징 구현에서 분리되도록 한다. 각 포트는 Jenkinsfile 파싱 이후 생성되는 잡 메타데이터(`job_id`, `repository_id`, `commit_hash`, `runner_id`, 타임스탬프)를 직렬화·enqueue/dequeue하거나 러너 하트비트를 upsert/조회하는 메서드를 정의한다. 인프라 계층에는 `src/main/java/io/jgitkins/server/infrastructure/adapter/mq/` 이하에 인메모리 구현(예: `InMemoryJobQueueAdapter`, `InMemoryRunnerHeartbeatAdapter`)을 두고, 향후 실제 MQ 프로바이더로 교체할 수 있도록 스프링 빈 구성을 `infrastructure/config/mq/MessagingConfig`에서 관리한다. 파이프라인 잡 생성 서비스(`src/main/java/io/jgitkins/server/application/port/service/` 내 예정)에는 새 포트를 주입하여 Jenkinsfile 파싱 시 직렬화 payload를 큐에 push하고 실패 시 도메인 예외를 던지며, 하트비트 어댑터를 통해 러너 생존 신호를 읽고 필요 시 갱신한다. 큐 작업 중 발생한 예외는 도메인 계층으로 전파하여 디스패처가 enqueue 실패를 감지하도록 한다.


(legacy task: 2)

### 1.3. [3] Feature Batch Job Publisher

**Status:** cancelled  

**Dependencies:** None  


Create a scheduled dispatcher that scans stored pipeline jobs and enqueues them only when at least one runnable runner is currently reporting heartbeats.

**Details:**


1. Follow the service layout under `src/main/java/io/jgitkins/server/application/port/service/` (see `RepositoryService` as a reference) to add a `JobDispatchService` that consumes `PipelineJobPersistencePort`, `JobQueuePort`, and `RunnerHeartbeatPort` (from Tasks 1 & 2). The service should expose `publishPendingJobs()` that: a) loads pending `PipelineJob`s flagged as runnable, b) checks runner availability via heartbeat data (only runners with recent timestamps and matching labels/architectures should qualify), c) transitions the job status to `QUEUED` and persists it before enqueueing via `JobQueuePort`, and d) skips jobs when no runner fits.
2. Implement a Spring `@Component` scheduler (e.g., `PipelineJobPublisherBatch` under `src/main/java/io/jgitkins/server/infrastructure/batch/`) using `@Scheduled(fixedDelay = …)` and enable scheduling in `JGitkinsServerApplication` if not already. The batch should call `JobDispatchService.publishPendingJobs()` and log outcomes with structured log messages (job id, runner id) using SLF4J, mirroring logging already present in adapters like `JGitRepositoryAdapter`.
3. Add configuration knobs in `application.yml` (new `jgitkins.dispatcher.*` section) for delay interval, heartbeat freshness threshold, and per-runner concurrency guard. Bind them via a `@ConfigurationProperties` class under `infrastructure/config` similar to existing configs (e.g., `DataSourceConfig`). Inject these properties into the batch component.
4. Extend the domain from Task 1 by documenting the new `QUEUED` transition and runner-selection constraints inside the `PipelineJob` aggregate (update its JavaDoc or KDoc comments once implemented) so later tasks understand invariants. Ensure the dispatch logic throws `ResourceLockedException` when concurrent updates race, matching existing exception types in `application/common/exception`.
5. Prepare the infrastructure adapter for recording runner occupancy: add a lightweight `RunnerAssignmentTracker` (in-memory Map for now) under `infrastructure/adapter/mq/` that mirrors how other adapters (e.g., `JGitBranchAdapter`) encapsulate external resources, so future MQ-backed implementations can reuse the selection logic.


(legacy task: 3)

### 1.4. [4] Runner Management(Manage Plugin or Runner Configuration (Schedule cycle or.. dockerImage(Jenkinsfile Runner or Jenkinsfile Runner 에 사용되는 플러그인들을 서버에서 설정 ))

**Status:** in-progress  

**Dependencies:** None  


Centralized Runner runtime management on JGitkins Server. Maintain a catalog of Runner Docker images (including Jenkinsfile Runner) and plugin bundles, with versioning, approval, distribution (including offline mirroring), and per-runner configuration such as allowed images/plugins and execution settings.

**Details:**





(legacy task: 4)

### 1.5. [5] Scheduled Job Publication Flow

**Status:** cancelled  

**Dependencies:** None  


Implement a scheduler-driven service that detects pending pipeline jobs, ensures a compatible runnable runner exists, updates job history, and enqueues the job for execution.

**Details:**


1. Add a Spring `@Scheduled` component (e.g., `JobSchedulePoller` under `src/main/java/io/jgitkins/server/application/scheduling/`) that invokes the existing `JobDispatchService.publishPendingJobs()` from Task 3 at configurable intervals (default 30s via `application.yml`).
2. Within `JobDispatchService`, implement logic to query `PipelineJobPersistencePort` for pending jobs, ask `RunnerHeartbeatPort` for runners whose heartbeats fall within the freshness window and match required labels/arch, and pick the best runner via `RunnerAssignment` metadata.
3. When a runner is selected, update the job’s domain aggregate: append a history entry (new `JobHistoryEvent`/value object) capturing transition `PENDING→QUEUED`, runner id, timestamp, and any queue reference.
4. Persist the updated job via `PipelineJobPersistencePort` using optimistic locking to prevent duplicate dispatch, then call `JobQueuePort.enqueue()` with a DTO mirroring Task 2’s payload specification.
5. Emit structured logs/metrics (Micrometer counter for dispatched jobs, gauge for backlog) to aid observability and ensure the scheduler is idempotent (skip processing when no runners qualify).


(legacy task: 5)

### 1.6. [6] Define Repository Domain and Refine Repository Management API

**Status:** done  

**Dependencies:** None  


Establish a robust domain model for repositories and enhance the existing API for managing them, including CRUD operations and state tracking.

**Details:**


1. Define Repository Aggregate: Following the pattern established in `src/main/java/io/jgitkins/server/domain/model/Job.java` (from Task 1), introduce a `Repository` aggregate (e.g., as a Java record) within `src/main/java/io/jgitkins/server/application/domain/model/`. This aggregate should include properties such as `id` (UUID), `url` (e.g., Git URL), `name`, `branch`, `credentialId` (if applicable), `lastSyncedAt`, `status` (e.g., 'ACTIVE', 'INACTIVE', 'ERROR'), and `ownerId`. Consider `RepositoryType` (e.g., GITHUB, GITLAB) as a Value Object. 2. Refine RepositoryService: Update or extend `src/main/java/io/jgitkins/server/application/port/service/RepositoryService.java` to operate on the new `Repository` domain model. Implement use cases for creating, updating, retrieving, and deleting repositories. This service will act as the application-layer entry point for repository management. 3. Implement RepositoryPersistencePort: Define a `RepositoryPersistencePort` interface in `src/main/java/io/jgitkins/server/application/port/out/` alongside `RepositoryContentPort` (as per Task 2). Implement methods like `save(Repository repository)`, `findById(UUID id)`, `findByUrl(String url)`, and `delete(UUID id)`. 4. Create Persistence Adapter: Develop an in-memory adapter (`InMemoryRepositoryAdapter`) for `RepositoryPersistencePort` in `src/main/java/io/jgitkins/server/infrastructure/adapter/out/persistence/` for initial development and testing, mirroring the approach of other in-memory adapters. 5. Develop Repository DTOs: Create corresponding Data Transfer Objects (DTOs) in `src/main/java/io/jgitkins/server/application/dto/` for `CreateRepositoryCommand`, `UpdateRepositoryCommand`, and `RepositoryResponse` to facilitate API interactions. 6. Expose REST Endpoints: Implement or modify REST controllers (e.g., `RepositoryController` in `src/main/java/io/jgitkins/server/interfaces/rest/`) to expose endpoints for managing repositories (e.g., `POST /api/repositories`, `GET /api/repositories/{id}`, `PUT /api/repositories/{id}`, `DELETE /api/repositories/{id}`). These endpoints should use the newly defined DTOs and interact with `RepositoryService`. 7. Integrate with RepositoryContentPort: Ensure the refined `RepositoryService` can leverage or integrate with the existing `RepositoryContentPort` for operations requiring interaction with the actual repository content (e.g., fetching Jenkinsfile, although this specific part might be a follow-up task, ensure compatibility.


(legacy task: 6)

### 1.7. [7] Define Organize Domain and Refine Repository Management API

**Status:** done  

**Dependencies:** None  


Define Organize Domain and Refine Repository Management API

**Details:**





(legacy task: 7)

### 1.8. [11] Implement Admin User Management API

**Status:** done  

**Dependencies:** None  


Develop a comprehensive API for administrative user management, enabling CRUD operations on user accounts, role assignment, and permission management within the JGitkins Server.

**Details:**


1. User Domain Model: Define a `User` aggregate (e.g., `io.jgitkins.server.application.domain.model.User`) following patterns established in Task 6's `Repository.java` and Task 1's `Job.java`. It should encapsulate properties such as `id` (UUID), `username`, `email`, `hashedPassword` (for local accounts, though OAuth is primary), `roles` (list of `Role` enum or aggregate IDs), `providerId` (from OAuth, see Task 10 for details on local user records), `createdAt`, `updatedAt`, and `status` (e.g., 'ACTIVE', 'INACTIVE').2. Role/Permission Domain Model: Introduce `Role` (e.g., an enum for predefined roles like `ADMIN`, `DEVELOPER`, `GUEST`) and `Permission` domain concepts to manage access levels. Permissions should be granular (e.g., `READ_REPOSITORY`, `WRITE_REPOSITORY`, `MANAGE_USERS`).3. Database Schema: Extend the database schema to support the defined `User` properties and `User-Role` mappings. This will likely involve new `ROLES` and `USER_ROLES` tables, building upon the `USERS` table mentioned in Task 10's description.4. Application Service Layer: Implement a `UserService` (e.g., `io.jgitkins.server.application.port.service.UserService`) to encapsulate business logic for user creation, updates, deletion, role assignment, and querying. This service should interact with the `User` domain model and a `UserRepository` (persistence adapter).5. gRPC API Adapter: Following Task 8's migration to gRPC, define new `.proto` files (e.g., `user_management_service.proto` under `src/main/proto`) for the Admin User Management API. This API should expose gRPC endpoints for operations such as `CreateUser(UserRequest)`, `GetUser(GetUserRequest)`, `UpdateUser(UpdateUserRequest)`, `DeleteUser(DeleteUserRequest)`, `AssignRoles(AssignRolesRequest)`, and `ListUsers(ListUsersRequest)`. Implement the gRPC service adapter (e.g., `io.jgitkins.server.adapter.in.grpc.UserManagementServiceGrpc`) to translate gRPC requests into calls to the `UserService`.6. Security Integration: Integrate with the authentication and authorization mechanisms established in Task 10. Ensure that only authenticated users with appropriate `ADMIN` roles can access and perform sensitive operations via this API, enforcing security at the gRPC service layer.7. Error Handling: Implement robust error handling for invalid inputs, unauthorized access, and domain rule violations, returning appropriate gRPC status codes (e.g., `INVALID_ARGUMENT`, `PERMISSION_DENIED`, `NOT_FOUND`).


(legacy task: 11)

### 1.9. [12] Add Member Management API

**Status:** done  

**Dependencies:** None  


Develop an API for managing members within organizations or repositories, including roles, permissions, and membership lifecycle, building upon the existing user management and authentication infrastructure.

**Details:**


1.  **Domain Model Definition**: Define a new `Member` aggregate (e.g., `io.jgitkins.server.application.domain.model.Member.java`) in `src/main/java/.../domain/model/`. This model should link a `User` (as defined in Task 11) to an `Organization` or `Repository` (following patterns from Task 6 and 7). Include properties such as `id` (UUID), `userId` (linking to the `User` entity), `targetId` (UUID for `Organization` or `Repository`), `targetType` (enum for `ORGANIZATION`, `REPOSITORY`), `role` (enum like `OWNER`, `MAINTAINER`, `CONTRIBUTOR`, `VIEWER`), `status`, `createdAt`, `updatedAt`.
2.  **Service Layer Implementation**: Create a `MemberService` (e.g., `io.jgitkins.server.application.service.MemberService.java`) in `src/main/java/.../application/service/` that encapsulates business logic for member management. This service will orchestrate interactions with the `UserService` (from Task 11) and `RepositoryService` (from Task 6/8) or `OrganizeService` (from Task 7/8) to perform CRUD operations on members. Implement methods for:
    *   `addMember(targetType, targetId, userId, role)`: Add a user as a member to a specific organization or repository.
    *   `updateMemberRole(targetType, targetId, userId, newRole)`: Change a member's role.
    *   `removeMember(targetType, targetId, userId)`: Remove a member.
    *   `listMembers(targetType, targetId)`: Retrieve all members for a given organization or repository.
    *   Ensure robust validation and authorization checks are performed within this service, leveraging the security context provided by Task 10.
3.  **gRPC API Definition**: Following the approach of Task 8, define a new `.proto` file (e.g., `member_service.proto`) in `src/main/proto` to specify the gRPC service for member management. This will include messages for requests (e.g., `AddMemberRequest`, `UpdateMemberRoleRequest`) and responses (e.g., `MemberResponse`, `ListMembersResponse`). The gRPC service methods should correspond to the `MemberService` operations.
4.  **gRPC Adapter Implementation**: Implement the gRPC service adapter (e.g., `io.jgitkins.server.adapter.in.grpc.GrpcMemberService.java`) that translates gRPC requests into calls to the `MemberService` and converts service results back into gRPC responses. This adapter will reside in `src/main/java/.../adapter/in/grpc/`.


(legacy task: 12)

### 1.10. [14] Implement Runner Health Check (Heartbeat)

**Status:** pending  

**Dependencies:** None  


Develop a system for JGitkins runners to send periodic heartbeat signals to the server, enabling the server to monitor runner health and availability.

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


(legacy task: 14)

### 1.11. [15] Repository 소유자(owner) 추상화 및 ownerId 기반 경로 분기 도입

**Status:** done  

**Dependencies:** None  


organizeId 의존 제거, ownerType(USER|ORG) + ownerId 모델링, RepositoryCreateRequest 및 서비스 흐름에서 owner 해석, RepositoryPathResolver로 실제 경로 계산(예: /bare/users/{username}/{repo}.git, /bare/orgs/{orgSlug}/{repo}.git), 기존 organizeId 기반 생성 호환/마이그레이션 고려, 테스트 전략 포함

**Details:**





(legacy task: 15)

### 1.12. [16] Setup Git Repository Access Service

**Status:** pending  

**Dependencies:** None  


Create a foundational backend service layer for interacting with Git repositories. This service will abstract low-level Git operations, such as repository cloning, opening, and navigating through its contents.

**Details:**


Implement a Java service, e.g., `GitRepositoryService`, utilizing the JGit library. This service should provide methods for: 1. Initializing a Git repository object given a local path or URL. 2. Fetching references (branches, tags). 3. Resolving a tree object for a specific branch and path. 4. Resolving a blob object for a specific branch and path. Ensure proper resource management for Git repository objects (e.g., `Repository.close()`).

Example pseudo-code for service method:
```java
public class GitRepositoryService {
    public Repository getRepository(String owner, String repoName) { /* ... */ }
    public RevWalk getRevWalk(Repository repo) { /* ... */ }
    public ObjectId resolveTree(Repository repo, String branch, String path) { /* ... */ }
    public TreeWalk getTreeWalk(Repository repo, RevTree tree, String path) { /* ... */ }
    public byte[] getBlobContent(Repository repo, ObjectId blobId) { /* ... */ }
}
```


(legacy task: 16)

### 1.13. [17] Implement Backend API for File Tree Content

**Status:** done  

**Dependencies:** None  


Develop a REST API endpoint to retrieve the contents of a specified directory (tree) within a Git repository for a given branch and path. This will support the file tree view.

**Details:**


Create a Spring Boot REST controller, e.g., `RepositoryController`, with an endpoint like `GET /api/repositories/{ownerType}/{owner}/{repoName}/{branch}/**path`. The `**path` should capture the full path of the directory within the repository. The endpoint should:
1.  Receive `ownerType`, `owner`, `repoName`, `branch`, and `path` from the URL.
2.  Utilize `GitRepositoryService` (Task 16) to access the repository and resolve the specified tree.
3.  Iterate through the `TreeWalk` to list files and subdirectories within the current path.
4.  Return a JSON array of objects, each containing `name`, `type` ('file'/'directory'), and potentially `size` (for files) and `sha`.

Example API response:
```json
[
  {"name": "src", "type": "directory", "sha": "..."},
  {"name": "README.md", "type": "file", "size": 1234, "sha": "..."}
]
```


(legacy task: 17)

### 1.14. [18] Implement Backend API for File Blob Content

**Status:** done  

**Dependencies:** None  


Develop a REST API endpoint to retrieve the raw content of a specified file (blob) within a Git repository for a given branch and file path. This will support the file detail view.

**Details:**


Extend the `RepositoryController` (or create a new one) with an endpoint like `GET /api/repositories/{ownerType}/{owner}/{repoName}/{branch}/**filePath`. The `**filePath` should capture the full path of the file within the repository.
1.  Receive `ownerType`, `owner`, `repoName`, `branch`, and `filePath`.
2.  Utilize `GitRepositoryService` (Task 16) to access the repository and resolve the specified blob.
3.  Retrieve the raw content of the blob.
4.  Return the content directly with the appropriate `Content-Type` header (e.g., `text/plain`, `text/markdown`, `application/octet-stream`). Handle binary files appropriately.

Consider using `ResponseEntity<byte[]>` for binary content and `String` for text content, setting the `Content-Type` header dynamically based on file extension.


(legacy task: 18)

### 1.15. [19] Frontend Router Setup for Repository Views

**Status:** pending  

**Dependencies:** None  


Configure the frontend routing to handle repository-specific URLs, including dynamic parameters for owner type, owner, repository name, branch, and file/directory path.

**Details:**


Set up a main frontend component (e.g., `RepositoryView`) and define a route structure that captures all necessary parameters: `/repositories/:ownerType/:owner/:repoName/:branch/*path`. The `*path` segment should be optional and capture the remainder of the URL for file/directory paths.

Example routing configuration (e.g., React Router, Vue Router, Angular Router):
```javascript
// Assuming a React-like router setup
<Routes>
  <Route path="/repositories/:ownerType/:owner/:repoName/:branch/*path" element={<RepositoryView />} />
</Routes>
```
The `RepositoryView` component should be able to extract these parameters (e.g., `useParams()` in React Router) to pass to child components or use for API calls. Default the `branch` to 'main' or 'master' if not explicitly provided in the URL initially.


(legacy task: 19)

### 1.16. [20] Develop File Tree UI Component

**Status:** cancelled  

**Dependencies:** None  


Create a reusable frontend UI component capable of rendering a hierarchical list of files and directories within a repository.

**Details:**


Design and implement a component (e.g., `FileTree`) that takes an array of file/directory objects (as returned by Task 17) as props. The component should:
1.  Display file/directory names clearly.
2.  Visually differentiate between files and directories (e.g., using icons).
3.  Make each item clickable.
4.  Handle loading states (e.g., showing a spinner).
5.  Include a dropdown or similar UI element for branch selection.

Consider using a UI library's tree view component or building a custom list that mimics a tree structure.


(legacy task: 20)

### 1.17. [21] Integrate File Tree API with Frontend Component

**Status:** done  

**Dependencies:** None  


Connect the `FileTree` UI component to the backend API developed in Task 17 to fetch and display the contents of a specified repository path.

**Details:**


Within the `RepositoryView` (Task 19) or a child component, implement data fetching logic.
1.  When the component mounts or URL parameters (owner, repoName, branch, path) change, make an API call to `GET /api/repositories/{ownerType}/{owner}/{repoName}/{branch}/**path`.
2.  Store the fetched data in the component's state.
3.  Pass the fetched file/directory list to the `FileTree` component (Task 20) for rendering.
4.  Display loading indicators while data is being fetched and error messages if the API call fails.

Use a state management solution (e.g., React Context, Redux, Vuex, NGRX) for managing repository data if applicable.


(legacy task: 21)

### 1.18. [22] Implement Frontend Tree Navigation Logic

**Status:** pending  

**Dependencies:** None  


Add interactivity to the `FileTree` UI component to handle user clicks on directories and branch selection, updating the URL and fetching new data.

**Details:**


Modify the `FileTree` component (Task 20) to:
1.  On clicking a directory, update the current route's `path` parameter to reflect the new directory's path. Use the frontend router's navigation API (e.g., `navigate('/repositories/.../new/path')`).
2.  Implement logic for the branch selection dropdown. When a new branch is selected, update the route's `branch` parameter, triggering a re-fetch of the file tree for the new branch.

Ensure that URL changes automatically trigger the data fetching in Task 21.


(legacy task: 22)

### 1.19. [23] Develop File Content Viewer UI Component with WYSIWYG

**Status:** pending  

**Dependencies:** None  


Create a frontend UI component to display file content, integrating a WYSIWYG editor for rendering various file types (e.g., Markdown, code, plain text).

**Details:**


Implement a component (e.g., `FileContentViewer`) that accepts raw file content and its type (derived from file extension).
1.  For Markdown files (`.md`, `.markdown`), integrate a Markdown renderer library (e.g., `react-markdown`, `marked.js`) to display rendered HTML.
2.  For code files (e.g., `.java`, `.js`, `.py`, `.xml`), integrate a code highlighter/editor (e.g., Monaco Editor, CodeMirror, Prism.js) to display syntax-highlighted code.
3.  For plain text files, display the content directly.
4.  For unknown or binary files, display a message indicating content cannot be rendered directly.

The component should act as a viewer (read-only) as per the PRD's '읽을 수 있는 기능을 제공한다' (provides the ability to read).


(legacy task: 23)

### 1.20. [24] Integrate File Blob API and Navigation for File Details

**Status:** pending  

**Dependencies:** None  


Connect the `FileContentViewer` component to the backend API (Task 18) and implement navigation logic for clicking files in the tree view to display their content.

**Details:**


Extend the `RepositoryView` or a relevant child component:
1.  Modify the `FileTree` component (Task 20) to handle clicks on *files*. When a file is clicked, update the current route's `path` parameter to point to the file's path.
2.  When the URL's `path` parameter points to a file, trigger an API call to `GET /api/repositories/{ownerType}/{owner}/{repoName}/{branch}/**filePath` (Task 18).
3.  Pass the fetched raw file content and its type to the `FileContentViewer` component (Task 23) for display.
4.  Handle loading states and errors for file content fetching.


(legacy task: 24)

### 1.21. [25] Implement Robust Error Handling and Loading States

**Status:** pending  

**Dependencies:** None  


Add comprehensive error handling (e.g., repository not found, file/path not found, API errors) and loading indicators across both file tree and file detail views to improve user experience.

**Details:**


Enhance both frontend and backend:
**Frontend:**
1.  Display user-friendly error messages for failed API calls (e.g., 'Repository not found', 'File not found', 'Network error').
2.  Implement visual loading indicators (spinners, skeletons) during all data fetches (file tree, file content).
3.  Provide clear UI feedback when a branch is switched or directory is navigated.

**Backend:**
1.  Implement custom exception handling for specific Git errors (e.g., `RepositoryNotFoundException`, `BranchNotFoundException`, `PathNotFoundException`).
2.  Map these exceptions to appropriate HTTP status codes (e.g., 404 Not Found, 500 Internal Server Error) and provide informative error bodies.

Ensure that an unhandled error doesn't crash the application.


(legacy task: 25)

### 1.22. Find a file용 전체 파일 인덱스 응답 정리

**Status:** done  

**Dependencies:** None  


서버는 브랜치 기준 전체 파일 목록만 응답하고, 검색은 웹에서 수행하도록 역할을 분리한다.

**Details:**


신규 /search API를 두지 않고 기존 /repositories/{taskCd}/{repoName}/files?ref={branch} 응답을 표준 전체 인덱스로 사용한다.

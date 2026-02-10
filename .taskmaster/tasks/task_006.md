# Task ID: 6

**Title:** Define Repository Domain and Refine Repository Management API

**Status:** done

**Dependencies:** 1 ✓, 2 ✗

**Priority:** medium

**Description:** Establish a robust domain model for repositories and enhance the existing API for managing them, including CRUD operations and state tracking.

**Details:**

1. Define Repository Aggregate: Following the pattern established in `src/main/java/io/jgitkins/server/domain/model/Job.java` (from Task 1), introduce a `Repository` aggregate (e.g., as a Java record) within `src/main/java/io/jgitkins/server/application/domain/model/`. This aggregate should include properties such as `id` (UUID), `url` (e.g., Git URL), `name`, `branch`, `credentialId` (if applicable), `lastSyncedAt`, `status` (e.g., 'ACTIVE', 'INACTIVE', 'ERROR'), and `ownerId`. Consider `RepositoryType` (e.g., GITHUB, GITLAB) as a Value Object. 2. Refine RepositoryService: Update or extend `src/main/java/io/jgitkins/server/application/port/service/RepositoryService.java` to operate on the new `Repository` domain model. Implement use cases for creating, updating, retrieving, and deleting repositories. This service will act as the application-layer entry point for repository management. 3. Implement RepositoryPersistencePort: Define a `RepositoryPersistencePort` interface in `src/main/java/io/jgitkins/server/application/port/out/` alongside `RepositoryContentPort` (as per Task 2). Implement methods like `save(Repository repository)`, `findById(UUID id)`, `findByUrl(String url)`, and `delete(UUID id)`. 4. Create Persistence Adapter: Develop an in-memory adapter (`InMemoryRepositoryAdapter`) for `RepositoryPersistencePort` in `src/main/java/io/jgitkins/server/infrastructure/adapter/out/persistence/` for initial development and testing, mirroring the approach of other in-memory adapters. 5. Develop Repository DTOs: Create corresponding Data Transfer Objects (DTOs) in `src/main/java/io/jgitkins/server/application/dto/` for `CreateRepositoryCommand`, `UpdateRepositoryCommand`, and `RepositoryResponse` to facilitate API interactions. 6. Expose REST Endpoints: Implement or modify REST controllers (e.g., `RepositoryController` in `src/main/java/io/jgitkins/server/interfaces/rest/`) to expose endpoints for managing repositories (e.g., `POST /api/repositories`, `GET /api/repositories/{id}`, `PUT /api/repositories/{id}`, `DELETE /api/repositories/{id}`). These endpoints should use the newly defined DTOs and interact with `RepositoryService`. 7. Integrate with RepositoryContentPort: Ensure the refined `RepositoryService` can leverage or integrate with the existing `RepositoryContentPort` for operations requiring interaction with the actual repository content (e.g., fetching Jenkinsfile, although this specific part might be a follow-up task, ensure compatibility.

**Test Strategy:**

1. Domain Unit Tests: Add unit tests under `src/test/java/.../domain/model/RepositoryTest` to verify the `Repository` aggregate's constructor, invariants, and state transitions. 2. Service Layer Tests: Create `RepositoryServiceTest` under `src/test/java/.../application/port/service/` using mocks for `RepositoryPersistencePort` to test business logic for creating, updating, and retrieving repositories, ensuring correct data flow and error handling. 3. Persistence Adapter Tests: Implement Spring Boot slice tests for `InMemoryRepositoryAdapter` to verify `save`, `findById`, `findByUrl`, and `delete` operations correctly persist and retrieve `Repository` objects. 4. API Integration Tests: Develop integration tests for the `RepositoryController` using tools like MockMvc or TestRestTemplate to ensure REST endpoints function as expected, handle various HTTP methods, status codes, and data payloads for repository management.

## Subtasks

### 6.1. Define Repository Domain Aggregate and Value Objects

**Status:** done  
**Dependencies:** 6.1  

Create the Repository aggregate as a Java record and RepositoryType as a Value Object, mirroring the Job domain model pattern established in Task 1.

**Details:**

Introduce the `Repository` aggregate as a Java record in `src/main/java/io/jgitkins/server/application/domain/model/`. It should include properties such as `id` (UUID), `url`, `name`, `branch`, `credentialId`, `lastSyncedAt`, `status` (e.g., 'ACTIVE', 'INACTIVE', 'ERROR'), and `ownerId`. Define `RepositoryType` (e.g., GITHUB, GITLAB) as a Value Object, potentially an enum or record. Ensure consistency with the `Job.java` pattern from Task 1.

### 6.2. Implement Repository Persistence Port and In-Memory Adapter

**Status:** done  
**Dependencies:** 6.1  

Define the `RepositoryPersistencePort` interface for CRUD operations and create its in-memory implementation for initial development and testing, following existing adapter patterns.

**Details:**

Define the `RepositoryPersistencePort` interface in `src/main/java/io/jgitkins/server/application/port/out/` with methods such as `save(Repository repository)`, `findById(UUID id)`, `findByUrl(String url)`, and `delete(UUID id)`. Implement an `InMemoryRepositoryAdapter` for this port in `src/main/java/io/jgitkins/server/infrastructure/adapter/out/persistence/`, mirroring the approach of other in-memory adapters.

### 6.3. Develop Repository Management Data Transfer Objects

**Status:** done  
**Dependencies:** 6.1  

Create Data Transfer Objects (DTOs) specifically for managing repositories, including commands for creation and update, and a response DTO for API interactions.

**Details:**

Develop corresponding Data Transfer Objects (DTOs) in `src/main/java/io/jgitkins/server/application/dto/`. These DTOs should include `CreateRepositoryCommand` (for initial repository data), `UpdateRepositoryCommand` (for modifying repository details), and `RepositoryResponse` (for returning repository information via the API). Ensure these DTOs align with the `Repository` domain model.

### 6.4. Implement Repository Service Logic with CRUD Operations

**Status:** done  
**Dependencies:** 6.1, 6.2, 6.3  

Develop or extend the `RepositoryService` to handle core business logic for managing repositories, including CRUD operations, by interacting with the persistence layer.

**Details:**

Update or extend `src/main/java/io/jgitkins/server/application/port/service/RepositoryService.java` to implement use cases for creating, updating, retrieving, and deleting repositories. This service will operate on the `Repository` domain model, utilize `RepositoryPersistencePort`, and integrate with the new DTOs. Ensure its design accommodates future integration with `RepositoryContentPort` from Task 2.

### 6.5. Expose Repository Management REST Endpoints

**Status:** done  
**Dependencies:** 6.4  

Implement REST API endpoints for creating, retrieving, updating, and deleting repositories, utilizing the `RepositoryService` and newly defined DTOs.

**Details:**

Implement or modify REST controllers (e.g., `RepositoryController` in `src/main/java/io/jgitkins/server/interfaces/rest/`) to expose endpoints for managing repositories. This includes `POST /api/repositories` (create), `GET /api/repositories/{id}` (retrieve by ID), `PUT /api/repositories/{id}` (update), and `DELETE /api/repositories/{id}` (delete). These endpoints must use the newly defined DTOs and interact with `RepositoryService`.

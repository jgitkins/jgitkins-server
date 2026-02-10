# Task ID: 7

**Title:** Define Organize Domain and Refine Repository Management API

**Status:** done

**Dependencies:** 1 ✓, 6 ✓

**Priority:** medium

**Description:** Define Organize Domain and Refine Repository Management API

**Details:**

No details provided.

**Test Strategy:**

No test strategy provided.

## Subtasks

### 7.1. Define Organize Domain Aggregate and Value Objects

**Status:** done  
**Dependencies:** 7.1  

Create the Organize aggregate as a Java record and OrganizeType as a Value Object, mirroring the Job domain model pattern established in Task 1.

**Details:**

Introduce the `Organize` aggregate as a Java record in `src/main/java/io/jgitkins/server/application/domain/model/`. It should include properties such as `id` (UUID), `url`, `name`, `branch`, `credentialId`, `lastSyncedAt`, `status` (e.g., 'ACTIVE', 'INACTIVE', 'ERROR'), and `ownerId`. Define `OrganizeType` (e.g., GITHUB, GITLAB) as a Value Object, potentially an enum or record. Ensure consistency with the `Job.java` pattern from Task 1.

### 7.2. Implement Organize Persistence Port and In-Memory Adapter

**Status:** done  
**Dependencies:** 7.1  

Define the `OrganizePersistencePort` interface for CRUD operations and create its in-memory implementation for initial development and testing, following existing adapter patterns.

**Details:**

Define the `OrganizePersistencePort` interface in `src/main/java/io/jgitkins/server/application/port/out/` with methods such as `save(Organize repository)`, `findById(UUID id)`, `findByUrl(String url)`, and `delete(UUID id)`. Implement an `InMemoryOrganizeAdapter` for this port in `src/main/java/io/jgitkins/server/infrastructure/adapter/out/persistence/`, mirroring the approach of other in-memory adapters.

### 7.3. Develop Organize Management Data Transfer Objects

**Status:** done  
**Dependencies:** 7.1  

Create Data Transfer Objects (DTOs) specifically for managing repositories, including commands for creation and update, and a response DTO for API interactions.

**Details:**

Develop corresponding Data Transfer Objects (DTOs) in `src/main/java/io/jgitkins/server/application/dto/`. These DTOs should include `CreateOrganizeCommand` (for initial repository data), `UpdateOrganizeCommand` (for modifying repository details), and `OrganizeResponse` (for returning repository information via the API). Ensure these DTOs align with the `Organize` domain model.

### 7.4. Implement Organize Service Logic with CRUD Operations

**Status:** done  
**Dependencies:** 7.1, 7.2, 7.3  

Develop or extend the `OrganizeService` to handle core business logic for managing repositories, including CRUD operations, by interacting with the persistence layer.

**Details:**

Update or extend `src/main/java/io/jgitkins/server/application/port/service/OrganizeService.java` to implement use cases for creating, updating, retrieving, and deleting repositories. This service will operate on the `Organize` domain model, utilize `OrganizePersistencePort`, and integrate with the new DTOs. Ensure its design accommodates future integration with `OrganizeContentPort` from Task 2.

### 7.5. Expose Organize Management REST Endpoints

**Status:** done  
**Dependencies:** 7.4  

Implement REST API endpoints for creating, retrieving, updating, and deleting repositories, utilizing the `OrganizeService` and newly defined DTOs.

**Details:**

Implement or modify REST controllers (e.g., `OrganizeController` in `src/main/java/io/jgitkins/server/interfaces/rest/`) to expose endpoints for managing repositories. This includes `POST /api/repositories` (create), `GET /api/repositories/{id}` (retrieve by ID), `PUT /api/repositories/{id}` (update), and `DELETE /api/repositories/{id}` (delete). These endpoints must use the newly defined DTOs and interact with `OrganizeService`.

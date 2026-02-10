# Task ID: 4

**Title:** Runner Management(Manage Plugin or Runner Configuration (Schedule cycle or.. dockerImage(Jenkinsfile Runner or Jenkinsfile Runner 에 사용되는 플러그인들을 서버에서 설정 ))

**Status:** in-progress

**Dependencies:** 1 ✓

**Priority:** medium

**Description:** Centralized Runner runtime management on JGitkins Server. Maintain a catalog of Runner Docker images (including Jenkinsfile Runner) and plugin bundles, with versioning, approval, distribution (including offline mirroring), and per-runner configuration such as allowed images/plugins and execution settings.

**Details:**

No details provided.

**Test Strategy:**

No test strategy provided.

## Subtasks

### 4.1. Define RunnerImage and PluginBundle Domain Models and DTOs

**Status:** pending  
**Dependencies:** None  

Create domain aggregates/value objects for RunnerImage and PluginBundle, including properties for versioning, Docker image reference, plugin IDs, and metadata. Also, define corresponding DTOs for API interaction.

**Details:**

Model `RunnerImage` with fields like `id`, `name`, `version`, `dockerImageRef`, `status` (e.g., pending, approved), `creationDate`. Model `PluginBundle` with `id`, `name`, `version`, list of `pluginIds`, `status`, `creationDate`. Place these in `src/main/java/io/jgitkins/server/application/domain/model/runner` and DTOs in `src/main/java/io/jgitkins/server/application/dto/runner`. Benchmarking `src/main/java/io/jgitkins/server/domain/model/Job.java` for Aggregate/Value Object patterns.

### 4.2. Implement Persistence Ports for Runner Image and Plugin Bundle Catalogs

**Status:** pending  
**Dependencies:** 4.1  

Define outbound persistence ports (`RunnerImagePersistencePort`, `PluginBundlePersistencePort`) in the application layer to manage the storage and retrieval of RunnerImage and PluginBundle entities.

**Details:**

Create `RunnerImagePersistencePort` and `PluginBundlePersistencePort` interfaces in `src/main/java/io/jgitkins/server/application/port/out/runner`. These ports should define methods like `save`, `findById`, `findByVersion`, `findAll`. Implement in-memory adapters for initial testing, following the pattern of `PipelineJobPersistencePort` used in Task 1.

### 4.3. Develop Runner Configuration Management Service

**Status:** pending  
**Dependencies:** 4.1, 4.2  

Create a service layer component, `RunnerManagementService`, that orchestrates business logic related to managing the catalog of Runner Docker images and plugin bundles using the defined persistence ports.

**Details:**

Implement `RunnerManagementService` in `src/main/java/io/jgitkins/server/application/port/service/runner`. This service will expose methods such as `createRunnerImage`, `approveRunnerImage`, `getRunnerImage`, `addPluginBundle`, `updatePluginBundle`, etc. It should handle validation and status transitions.

### 4.4. Create REST API Endpoints for Runner Image and Plugin Bundle Management

**Status:** pending  
**Dependencies:** 4.1, 4.3  

Implement REST controllers and define request/response DTOs to expose API endpoints for managing Runner Docker images and plugin bundles.

**Details:**

Create `RunnerImageController` and `PluginBundleController` in `src/main/java/io/jgitkins/server/interface/rest/runner`. These controllers will expose endpoints like `POST /runner-images`, `GET /runner-images/{id}`, `PUT /runner-images/{id}/approve`, `POST /plugin-bundles`, etc., interacting with the `RunnerManagementService`. Ensure appropriate input validation and error handling.

### 4.5. Implement Per-Runner Configuration Storage and Application Logic

**Status:** pending  
**Dependencies:** 4.1, 4.2, 4.3  

Develop the data structures and persistence mechanisms to store specific configurations for individual runners, such as allowed Docker images, plugin bundles, and execution settings. Implement logic to retrieve and apply these configurations.

**Details:**

Define a new domain model `RunnerConfiguration` (or extend `Runner` if it exists) in `src/main/java/io/jgitkins/server/application/domain/model/runner` to hold settings like `runnerId`, `allowedImageVersions`, `allowedPluginBundles`, `resourceLimits`, `scheduleCycle`. Create a corresponding `RunnerConfigurationPersistencePort` and update `RunnerManagementService` to manage these per-runner settings.

### 4.6. Provide Runner runtime activation config

**Status:** done  
**Dependencies:** None  

Bind Runner runtime REST/GRPC endpoints and polling intervals from Spring properties via RunnerRuntimeConfigProvider, returning them in activate response.

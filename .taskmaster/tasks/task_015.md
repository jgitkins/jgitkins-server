# Task ID: 15

**Title:** Repository 소유자(owner) 추상화 및 ownerId 기반 경로 분기 도입

**Status:** done

**Dependencies:** None

**Priority:** medium

**Description:** organizeId 의존 제거, ownerType(USER|ORG) + ownerId 모델링, RepositoryCreateRequest 및 서비스 흐름에서 owner 해석, RepositoryPathResolver로 실제 경로 계산(예: /bare/users/{username}/{repo}.git, /bare/orgs/{orgSlug}/{repo}.git), 기존 organizeId 기반 생성 호환/마이그레이션 고려, 테스트 전략 포함

**Details:**

No details provided.

**Test Strategy:**

No test strategy provided.

## Subtasks

### 15.1. Define OwnerType Enum and Update Repository Domain/DTOs

**Status:** done  
**Dependencies:** None  

Introduce an OwnerType enum with USER and ORGANIZATION values. Add ownerType field to the Repository domain model and RepositoryCreateRequest DTO, preparing for owner-scoped path resolution. This also involves reviewing and removing organizeId if it is directly tied to the old structure in DTOs.

**Details:**

Create io.jgitkins.server.application.domain.model.OwnerType enum (e.g., in Java). Modify the Repository record (src/main/java/io/jgitkins/server/application/domain/model/Repository.java) to include OwnerType ownerType. Update RepositoryCreateRequest DTO (src/main/java/io/jgitkins/server/application/dto/RepositoryCreateRequest.java) to accept OwnerType ownerType and ensure ownerId is correctly used, decoupling from organizeId.

### 15.2. Refactor Repository Service for Owner-Type Based Creation

**Status:** done  
**Dependencies:** 15.1  

Adjust RepositoryService methods to utilize the new ownerType and ownerId fields when creating or updating repository entities. This step is crucial for transitioning the service layer's understanding of repository ownership from organizeId to the more abstract ownerType + ownerId model.

**Details:**

Modify io.jgitkins.server.application.port.service.RepositoryService (e.g., createRepository, updateRepository) to receive ownerType and ownerId directly from the DTO, mapping them to the Repository domain object. Remove any direct usage or parsing of organizeId for new repository creation flows within this service.

### 15.3. Implement RepositoryPathResolver for Dynamic Bare Repository Paths

**Status:** done  
**Dependencies:** 15.1  

Develop a dedicated component, RepositoryPathResolver, responsible for constructing the canonical bare Git repository file path based on the provided ownerType, ownerId, and repository name. This centralizes path generation logic.

**Details:**

Create a new class io.jgitkins.server.application.util.RepositoryPathResolver with a public method String resolveBareRepositoryPath(OwnerType ownerType, String ownerId, String repoName). Implement logic to return paths like /bare/users/{ownerId}/{repoName}.git or /bare/orgs/{ownerId}/{repoName}.git based on ownerType. Ensure robustness for various input cases.

### 15.4. Integrate RepositoryPathResolver into Repository Operations

**Status:** done  
**Dependencies:** 15.2, 15.3  

Integrate the newly created RepositoryPathResolver into the RepositoryService and other relevant components that manage the physical Git repositories. This ensures that all repository creation, cloning, and access operations use the standardized owner-scoped paths.

**Details:**

Modify RepositoryService methods (e.g., createRepository, getRepositoryUrl) to invoke RepositoryPathResolver.resolveBareRepositoryPath when determining the actual file system location for bare Git repositories. Update any RepositoryPersistencePort or infrastructure adapters that manage file system interactions to use the resolved path.

### 15.5. Develop Backward Compatibility and Migration Strategy for organizeId

**Status:** done  
**Dependencies:** 15.4  

Design and implement a strategy to ensure backward compatibility for repositories created with the legacy organizeId system. This includes defining how these existing repositories will be accessed and outlining a plan for their eventual migration to the new ownerType/ownerId model.

**Details:**

Within RepositoryService or RepositoryPersistencePort, implement logic to handle repositories where ownerType might be null or missing (legacy data). For such cases, infer ownerType (e.g., default to ORGANIZATION) and map organizeId to ownerId for path resolution. Propose a separate, explicit migration process (e.g., a one-time script or a background job) to populate ownerType for all existing Repository entries in the database, based on existing organizeId values. This ensures old repositories remain accessible and allows for a smooth transition.

### 15.6. remove organize_id property from repository table

**Status:** done  
**Dependencies:** None  


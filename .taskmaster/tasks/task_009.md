# Task ID: 9

**Title:** Domain Modeling Refinement and Test Case Addition

**Status:** done

**Dependencies:** 6 ✓, 7 ✓

**Priority:** medium

**Description:** Refine the existing Repository and Organize domain models to improve their robustness and extensibility, and expand the associated unit and integration test coverage to ensure correctness.

**Details:**

Based on the existing codebase, specifically drawing from the implementation patterns of Task 1's `Job.java` and the `Repository` and `Organize` aggregates from Tasks 6 and 7:

1. Review `Repository` Domain Model:
   * Examine `src/main/java/io/jgitkins/server/application/domain/model/Repository.java` (or similar path if implemented as a record).
   * Identify potential areas for refinement:
     * Value Objects: Consider if any existing properties (e.g., `url`, `branch`) would benefit from being encapsulated as dedicated Value Objects (e.g., `RepositoryUrl`, `BranchName`) to enforce invariants and improve type safety, aligning with established DDD patterns.
     * Behavior: Add any missing domain-specific behaviors or methods directly to the `Repository` aggregate to ensure business logic resides with the domain model rather than leaking into services. For example, introduce `updateStatus(RepositoryStatus newStatus)` or `assignCredential(CredentialId id)`.
     * Immutability: Ensure the aggregate remains immutable where appropriate, utilizing `with` methods or constructor-based updates for state changes, consistent with Java record patterns.
2. Review `Organize` Domain Model:
   * Perform a similar review for `src/main/java/io/jgitkins/server/application/domain/model/Organize.java`, ensuring consistency with the `Repository` and `Job` models where applicable.
3. Refine Domain Event Handling (if applicable):
   * If `Repository` or `Organize` state changes emit domain events, ensure these events are well-defined and carry sufficient context. Add new events if domain changes introduce new significant state transitions, referencing existing patterns for domain event publishing.
4. Update `RepositoryService` and `OrganizeService`:
   * Adjust `src/main/java/io/jgitkins/server/application/port/service/RepositoryService.java` and `OrganizeService.java` to accommodate any structural or behavioral changes in their respective domain models. Ensure any new Value Objects are correctly handled when interacting with persistence ports or external APIs.

**Test Strategy:**

1. Expanded Domain Unit Tests:
   * Repository Tests: Enhance `src/test/java/io/jgitkins/server/application/domain/model/RepositoryTest.java` (established in Task 6).
     * Add new test cases for any new Value Objects introduced (e.g., `RepositoryUrlTest` to verify URL format, immutability, and invariants).
     * Add tests for newly introduced domain behaviors and methods on the `Repository` aggregate.
     * Ensure all invariants are thoroughly tested, including edge cases and invalid state transitions.
   * Organize Tests: Create or enhance `src/test/java/io/jgitkins/server/application/domain/model/OrganizeTest.java` (following the pattern from Task 7).
     * Apply similar comprehensive testing principles as for `Repository` tests, covering Value Objects, behaviors, and invariants.
2. Service Layer Integration Tests:
   * RepositoryService Tests: Extend `src/test/java/io/jgitkins/server/application/port/service/RepositoryServiceTest.java` (from Task 6).
     * Verify that `RepositoryService` correctly interacts with the refined `Repository` domain model, particularly concerning new behaviors or Value Objects.
     * Use mocks for persistence and other external ports to isolate service logic and ensure robust testing of business operations.
   * OrganizeService Tests: Create or extend `src/test/java/io/jgitkins/server/application/port/service/OrganizeServiceTest.java` (from Task 7).
     * Ensure similar test coverage for `OrganizeService` interactions with its refined domain model.
3. gRPC Adapter Tests (Conditional):
   * If domain model changes necessitate modifications in the gRPC `.proto` definitions or the `GrpcRepositoryService`/`GrpcOrganizeService` implementations (from Task 8), update or add test cases in `src/test/java/.../adapter/in/grpc/GrpcRepositoryServiceTest` and `GrpcOrganizeServiceTest`. These tests should verify proper serialization/deserialization and correct mapping between gRPC messages and the refined domain objects, including handling of any new Value Objects.

## Subtasks

### 9.1. Introduce Value Objects for Repository URL and Branch

**Status:** done  
**Dependencies:** None  

Create dedicated Value Objects for 'url' and 'branch' within the Repository domain model to enforce invariants and improve type safety, following the pattern of Task 1's Job.java and its related value objects.

**Details:**

Implement `RepositoryUrl` and `BranchName` as Java records or immutable classes in `src/main/java/io/jgitkins/server/application/domain/model/`. Each should encapsulate the respective string value, provide factory methods for validation (e.g., URL format, branch naming conventions), and override `equals()`, `hashCode()`, and `toString()`. Update `Repository.java` to use these new Value Objects for its `url` and `branch` properties.

### 9.2. Refine Repository Domain Model with New Behavior and Immutability

**Status:** done  
**Dependencies:** 9.1  

Enhance the `Repository` aggregate by adding domain-specific behaviors and ensuring its immutability and consistency with DDD patterns, leveraging the new Value Objects.

**Details:**

Modify `src/main/java/io/jgitkins/server/application/domain/model/Repository.java`. Introduce methods like `updateStatus(RepositoryStatus newStatus)` and `assignCredential(CredentialId id)` to encapsulate domain logic directly within the aggregate. Ensure the `Repository` record remains immutable, using 'with' methods (e.g., `withStatus(RepositoryStatus newStatus)`) for state changes, consistent with Java record patterns. Adjust constructors and existing methods to utilize `RepositoryUrl` and `BranchName`.

### 9.3. Refine Organize Domain Model and Consider Value Objects

**Status:** done  
**Dependencies:** None  

Review and refine the `Organize` domain model, introducing specific behaviors, ensuring immutability, and evaluating potential value objects or nested structures similar to the Repository and Job models.

**Details:**

Examine `src/main/java/io/jgitkins/server/application/domain/model/Organize.java`. Identify if any properties within `Organize` (e.g., specific identifiers or configuration settings) could benefit from encapsulation as Value Objects. Introduce domain-specific methods to the `Organize` aggregate (e.g., `addRepository(RepositoryId id)`, `updateConfiguration(...)`). Ensure the aggregate maintains immutability using 'with' methods if state changes are required, aligning with the pattern used for `Job.java`.

### 9.4. Update Services and Refine Domain Event Handling

**Status:** cancelled  
**Dependencies:** 9.2, 9.3  

Adjust `RepositoryService` and `OrganizeService` to align with the refined domain models and implement or refine domain event publishing for significant state changes.

**Details:**

Modify `src/main/java/io/jgitkins/server/application/port/service/RepositoryService.java` and `OrganizeService.java`. Update method signatures and implementations to correctly handle the new Value Objects and utilize the new domain behaviors of the `Repository` and `Organize` aggregates. If applicable, introduce or refine domain event publishing mechanisms within these services when a `Repository` or `Organize` aggregate state changes significantly (e.g., `RepositoryStatusUpdatedEvent`, `OrganizeConfigurationChangedEvent`), following existing patterns for event publication.

### 9.5. Expand Comprehensive Unit and Integration Test Coverage

**Status:** cancelled  
**Dependencies:** 9.2, 9.3, 9.4  

Expand unit and integration test coverage across the refined `Repository` and `Organize` domain models, their Value Objects, and their respective services to ensure correctness and robustness.

**Details:**

Review and enhance existing test files: `src/test/java/io/jgitkins/server/application/domain/model/RepositoryTest.java`, `RepositoryUrlTest.java`, `BranchNameTest.java`, `OrganizeTest.java`, `RepositoryServiceTest.java`, and `OrganizeServiceTest.java`. Add test cases to cover all new methods, edge cases, invariants, and ensure proper interaction between components after the domain model and service refinements. Focus on thorough validation, state changes, and event emission where applicable.

### 9.6. Refine Branch Domain

**Status:** done  
**Dependencies:** None  

ReOrganize Branch Domain

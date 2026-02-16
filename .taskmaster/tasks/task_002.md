# Task ID: 2

**Title:** 리팩토링

**Status:** in-progress

**Dependencies:** None

**Priority:** high

**Description:** 구조 개선, 품질 개선, 테스트 체계 강화 작업

**Details:**


기존 기능별 Task를 카테고리 기반(신규기능/리팩토링/보안)으로 재구성함.

**Test Strategy:**


카테고리별 우선순위에 따라 하위 작업을 순차 수행하고 회귀 테스트를 적용한다.

## Subtasks

### 2.1. [8] Migrate Service Communication with gRPC

**Status:** done  

**Dependencies:** None  


Implement gRPC services for existing application APIs, starting with Repository and Organize management, to enable efficient, high-performance inter-service communication.

**Details:**


1. Add gRPC Dependencies: Update the `pom.xml` to include `grpc-netty-shaded`, `grpc-protobuf`, `grpc-stub`, and `protobuf-maven-plugin` for code generation. Ensure the `protobuf-maven-plugin` is configured to generate Java sources from `.proto` files during the build lifecycle, targeting `target/generated-sources/protobuf`.
2. Define Protocol Buffer Definitions: Create new `.proto` files (e.g., `repository_service.proto`, `organize_service.proto`) under a new `src/main/proto` directory. These files should define gRPC services and message types that mirror the domain models and DTOs used by the existing `RepositoryService` and `OrganizeService` (from Task 6 and Task 7). For instance, a `Repository` message in `repository_service.proto` should reflect `io.jgitkins.server.application.domain.model.Repository`.
3. Implement gRPC Service Adapters: Create gRPC server implementations (e.g., `GrpcRepositoryService`, `GrpcOrganizeService`) under a new package `io.jgitkins.server.adapter.in.grpc`. These classes must extend the generated `*Grpc.ServiceImplBase` classes and delegate incoming gRPC requests to the corresponding application services (e.g., `RepositoryService`, `OrganizeService`) from `io.jgitkins.server.application.port.service`. Ensure meticulous mapping between gRPC proto messages and the application's domain/DTO objects.
4. Configure gRPC Server: Integrate the gRPC server into the Spring Boot application. This can be achieved by using `grpc-spring-boot-starter` or by manually configuring and starting a `ServerBuilder` within a Spring `@Configuration` class. The server must register the implemented gRPC services.
5. Implement Error Handling and Interceptors: Develop global gRPC error handling mechanisms, possibly using a `ServerInterceptor`, to catch exceptions thrown by application services and map them to appropriate gRPC `Status` codes (e.g., `Status.NOT_FOUND`, `Status.INVALID_ARGUMENT`, `Status.INTERNAL`).


(legacy task: 8)

### 2.2. [9] Domain Modeling Refinement and Test Case Addition

**Status:** done  

**Dependencies:** None  


Refine the existing Repository and Organize domain models to improve their robustness and extensibility, and expand the associated unit and integration test coverage to ensure correctness.

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


(legacy task: 9)

### 2.3. [26] jgitkins-server JUnit 테스트 체계 구축

**Status:** in-progress  

**Dependencies:** None  


jgitkins-server의 모든 기능에 대해 JUnit 기반 테스트 코드를 작성하고 회귀 검증 체계를 구축한다.

**Details:**


컨트롤러(WebMvcTest), 서비스(Mockito 기반 단위 테스트), 핵심 인프라 통합 테스트를 기능 단위로 확장한다. 기능 목록을 SubTask로 분해해 누락 없이 작성한다.


(legacy task: 26)

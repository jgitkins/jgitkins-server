# Task ID: 8

**Title:** Migrate Service Communication with gRPC

**Status:** done

**Dependencies:** 1 ✓, 6 ✓, 7 ✓

**Priority:** medium

**Description:** Implement gRPC services for existing application APIs, starting with Repository and Organize management, to enable efficient, high-performance inter-service communication.

**Details:**

1. Add gRPC Dependencies: Update the `pom.xml` to include `grpc-netty-shaded`, `grpc-protobuf`, `grpc-stub`, and `protobuf-maven-plugin` for code generation. Ensure the `protobuf-maven-plugin` is configured to generate Java sources from `.proto` files during the build lifecycle, targeting `target/generated-sources/protobuf`.
2. Define Protocol Buffer Definitions: Create new `.proto` files (e.g., `repository_service.proto`, `organize_service.proto`) under a new `src/main/proto` directory. These files should define gRPC services and message types that mirror the domain models and DTOs used by the existing `RepositoryService` and `OrganizeService` (from Task 6 and Task 7). For instance, a `Repository` message in `repository_service.proto` should reflect `io.jgitkins.server.application.domain.model.Repository`.
3. Implement gRPC Service Adapters: Create gRPC server implementations (e.g., `GrpcRepositoryService`, `GrpcOrganizeService`) under a new package `io.jgitkins.server.adapter.in.grpc`. These classes must extend the generated `*Grpc.ServiceImplBase` classes and delegate incoming gRPC requests to the corresponding application services (e.g., `RepositoryService`, `OrganizeService`) from `io.jgitkins.server.application.port.service`. Ensure meticulous mapping between gRPC proto messages and the application's domain/DTO objects.
4. Configure gRPC Server: Integrate the gRPC server into the Spring Boot application. This can be achieved by using `grpc-spring-boot-starter` or by manually configuring and starting a `ServerBuilder` within a Spring `@Configuration` class. The server must register the implemented gRPC services.
5. Implement Error Handling and Interceptors: Develop global gRPC error handling mechanisms, possibly using a `ServerInterceptor`, to catch exceptions thrown by application services and map them to appropriate gRPC `Status` codes (e.g., `Status.NOT_FOUND`, `Status.INVALID_ARGUMENT`, `Status.INTERNAL`).

**Test Strategy:**

1. Unit Tests for gRPC Service Adapters: Create unit tests (e.g., `GrpcRepositoryServiceTest`, `GrpcOrganizeServiceTest`) within `src/test/java/.../adapter/in/grpc`. These tests should use mock objects for `RepositoryService` and `OrganizeService` to verify that gRPC requests are correctly translated into application service calls with appropriate parameters and that application service results are correctly mapped back to gRPC responses.
2. Integration Tests with Embedded gRPC Server: Develop integration tests that start an embedded gRPC server within the test environment. Utilize gRPC client stubs to send requests to the exposed gRPC endpoints (e.g., `RepositoryService/GetRepository`, `OrganizeService/CreateOrganize`) and assert the correctness of the responses. These tests should cover both successful operations and various error scenarios.
3. Contract Testing: (Optional, for future refinement) Consider implementing basic contract tests to ensure the `.proto` definitions and their server-side implementations adhere to a consistent interface, potentially using a tool like WireMock for client-side stubbing if outbound gRPC calls are introduced.

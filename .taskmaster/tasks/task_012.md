# Task ID: 12

**Title:** Add Member Management API

**Status:** done

**Dependencies:** 1 ✓, 6 ✓, 8 ✓, 10 ✓, 11 ✓

**Priority:** medium

**Description:** Develop an API for managing members within organizations or repositories, including roles, permissions, and membership lifecycle, building upon the existing user management and authentication infrastructure.

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

**Test Strategy:**

1.  **Domain Unit Tests**: Create `MemberTest.java` under `src/test/java/.../domain/model/` to thoroughly test the `Member` aggregate. Verify its constructor, immutability, role assignment logic, and any invariants. Test edge cases for different `targetType` and `role` combinations.
2.  **Service Layer Tests**: Develop `MemberServiceTest.java` under `src/test/java/.../application/service/`. Use mock objects for the `UserRepository` (or `UserService`), `RepositoryService`, `OrganizeService`, and `MemberRepository`. Test the business logic for adding, updating, and removing members, ensuring that role-based permissions are enforced and that the correct interactions with dependent services occur. Verify error handling for invalid inputs, unauthorized actions, and non-existent entities.
3.  **gRPC Integration Tests**: Implement integration tests for the `GrpcMemberService` in `src/test/java/.../adapter/in/grpc/`. These tests should use a gRPC test client to invoke the actual gRPC service endpoints. Mock the underlying `MemberService` to verify that requests are correctly deserialized, forwarded to the service layer, and responses are correctly serialized. Crucially, test the integration with the authentication mechanism from Task 10, ensuring that only authenticated and authorized users can perform member management operations. This includes testing various user roles (e.g., admin, organization owner, repository owner) and their respective permissions.

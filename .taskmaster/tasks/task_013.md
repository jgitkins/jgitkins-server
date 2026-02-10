# Task ID: 13

**Title:** Implement Credential Management API

**Status:** done

**Dependencies:** 1 ✓, 8 ✓, 10 ✓, 11 ✓

**Priority:** medium

**Description:** Develop a comprehensive API for securely managing various types of credentials (e.g., SSH keys, API tokens) associated with users, organizations, or repositories within the JGitkins Server, ensuring secure storage and controlled access.

**Details:**

1.  **Domain Model Definition**: Define a new `Credential` aggregate (e.g., `io.jgitkins.server.application.domain.model.Credential.java`) following patterns established in Task 1 (`Job.java`), Task 6 (`Repository.java`), and Task 11 (`User.java`). The model should include properties such as `id` (UUID), `name` (for identification), `type` (e.g., `SSH_KEY`, `API_TOKEN`, as an enum or value object), `value` (encrypted string), `ownerId` (UUID, linking to `User`, `Organization`, or `Repository`), `ownerType` (enum for `USER`, `ORGANIZATION`, `REPOSITORY`), `createdAt`, `updatedAt`. Ensure the `value` is always stored encrypted.
2.  **Encryption Mechanism**: Implement a robust encryption and decryption service for `Credential` values. This service should utilize existing security practices or introduce a new, secure standard (e.g., AES-256) and manage encryption keys securely, possibly integrating with a Key Management System if one is defined in the architecture, or a secure configuration property.
3.  **Service Layer**: Create a `CredentialService` (e.g., `io.jgitkins.server.application.domain.service.CredentialService.java`) to handle business logic for credential operations. This service will be responsible for creating, reading, updating, deleting credentials, and ensuring that the requesting user (from Task 10 and 11 context) has appropriate permissions to perform actions on the target `ownerId`.
4.  **Persistence Layer**: Implement a `CredentialRepository` (e.g., using Spring Data JPA) for managing `Credential` entities in the database. Ensure that the `value` field is correctly mapped to a securely stored (e.g., BLOB or encrypted VARCHAR) column.
5.  **gRPC API Definition**: Following the patterns from Task 8, define a new `credential_service.proto` file under `src/main/proto/` for managing credentials. This protobuf definition should include messages for `CreateCredentialRequest`, `GetCredentialRequest`, `UpdateCredentialRequest`, `DeleteCredentialRequest`, `ListCredentialsRequest`, and `CredentialResponse` (containing public credential details, not the encrypted value directly).
6.  **gRPC Adapter Implementation**: Create a gRPC service adapter (e.g., `io.jgitkins.server.application.adapter.in.grpc.GrpcCredentialService.java`) that implements the generated gRPC interface. This adapter will translate gRPC requests into calls to the `CredentialService` and vice versa, handling input validation and response mapping.
7.  **Security and Authorization**: Integrate with the existing authentication context from Task 10 (OAuth + JWT) and user management from Task 11 (Admin User Management API) to enforce fine-grained access control. Users should only be able to access or modify credentials they own, or for organizations/repositories where they have appropriate permissions (building on Task 12 principles, if completed for membership checking).

**Test Strategy:**

1.  **Domain Unit Tests**: Create `CredentialTest.java` under `src/test/java/.../domain/model/` to thoroughly test the `Credential` aggregate. Verify its constructor, immutability, owner type relationships, and value object encapsulation for the encrypted value. Test edge cases for different credential types and owner types.
2.  **Encryption Service Tests**: Develop unit tests for the encryption/decryption service to ensure correct and secure data transformation. Verify that encrypted values are non-deterministic (if applicable), and that decryption yields the original plaintext.
3.  **Service Layer Tests**: Create `CredentialServiceTest.java` to test the business logic of credential management. Use mock objects for `CredentialRepository` and security contexts (e.g., for user authentication from Task 10 and 11) to verify CRUD operations, permission checks, and data validation.
4.  **gRPC Integration Tests**: Develop tests for the `GrpcCredentialService` adapter within `src/test/java/.../adapter/in/grpc/`. Use an in-memory gRPC server to simulate client-server interaction. Verify that gRPC requests are correctly parsed, translated to `CredentialService` calls, and responses are correctly formed. Ensure error handling and status codes are accurate.
5.  **Security & Authorization Tests**: Implement integration tests that verify access control. For example, ensure a user cannot retrieve another user's private credential, or create a credential for an organization they are not a member of (if Task 12 is integrated for role checks). This will involve simulating authenticated user contexts from Task 10/11.

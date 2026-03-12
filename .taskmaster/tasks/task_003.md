# Task ID: 3

**Title:** 보안

**Status:** in-progress

**Dependencies:** None

**Priority:** high

**Description:** 인증/인가/자격증명/보안 회귀 강화 작업

**Details:**

기존 기능별 Task를 카테고리 기반(신규기능/리팩토링/보안)으로 재구성함.

**Test Strategy:**

카테고리별 우선순위에 따라 하위 작업을 순차 수행하고 회귀 테스트를 적용한다.

## Subtasks

### 3.1. [10] OAuth + JWT Flow (Architectural View)

**Status:** done  
**Dependencies:** None  

  1. Client Initiation: User hits your app (SPA or backend). App redirects to auth gateway/Identity Provider (IdP) with OAuth2/OIDC (authorization code flow).
  2. IdP Authentication: User signs in at IdP (Google). On success, IdP redirects back with an auth code.
  3. Token Exchange: Your backend (or auth service) exchanges the code for tokens from IdP. Typically receives an ID Token (JWT) plus Access/Refresh tokens.
  4. Local User Link: Backend extracts sub/provider info from ID Token. It creates or updates a local user record (USERS table) tied to that provider ID, ensuring domain logic references a stable internal user ID.
  5. App Token Issuance: Backend issues its own short-lived JWT (or session) containing local user ID, roles, and any scoped claims. Refresh tokens (or re-auth via IdP) handle renewal.
  6. API Access: Clients call your APIs with the app JWT. Each service validates signature/expiry, then uses the local user ID to enforce domain rules. No service directly depends on Google IDs.
  7. Logout/Revocation: Invalidate refresh tokens or server sessions; optionally call IdP revoke endpoints. Centralize audit logs for sign-ins and token lifecycle events.

### 3.2. [13] Implement Credential Management API

**Status:** done  
**Dependencies:** None  

Develop a comprehensive API for securely managing various types of credentials (e.g., SSH keys, API tokens) associated with users, organizations, or repositories within the JGitkins Server, ensuring secure storage and controlled access.

**Details:**

1.  **Domain Model Definition**: Define a new `Credential` aggregate (e.g., `io.jgitkins.server.application.domain.model.Credential.java`) following patterns established in Task 1 (`Job.java`), Task 6 (`Repository.java`), and Task 11 (`User.java`). The model should include properties such as `id` (UUID), `name` (for identification), `type` (e.g., `SSH_KEY`, `API_TOKEN`, as an enum or value object), `value` (encrypted string), `ownerId` (UUID, linking to `User`, `Organization`, or `Repository`), `ownerType` (enum for `USER`, `ORGANIZATION`, `REPOSITORY`), `createdAt`, `updatedAt`. Ensure the `value` is always stored encrypted.
2.  **Encryption Mechanism**: Implement a robust encryption and decryption service for `Credential` values. This service should utilize existing security practices or introduce a new, secure standard (e.g., AES-256) and manage encryption keys securely, possibly integrating with a Key Management System if one is defined in the architecture, or a secure configuration property.
3.  **Service Layer**: Create a `CredentialService` (e.g., `io.jgitkins.server.application.domain.service.CredentialService.java`) to handle business logic for credential operations. This service will be responsible for creating, reading, updating, deleting credentials, and ensuring that the requesting user (from Task 10 and 11 context) has appropriate permissions to perform actions on the target `ownerId`.
4.  **Persistence Layer**: Implement a `CredentialRepository` (e.g., using Spring Data JPA) for managing `Credential` entities in the database. Ensure that the `value` field is correctly mapped to a securely stored (e.g., BLOB or encrypted VARCHAR) column.
5.  **gRPC API Definition**: Following the patterns from Task 8, define a new `credential_service.proto` file under `src/main/proto/` for managing credentials. This protobuf definition should include messages for `CreateCredentialRequest`, `GetCredentialRequest`, `UpdateCredentialRequest`, `... [truncated]

### 3.3. [27] OAuth 인증 실패/요청 검증 개선

**Status:** pending  
**Dependencies:** None  

OAuth 로그인 API의 요청 검증과 예외 매핑을 강화해 오류 응답을 일관화한다.

**Details:**

컨트롤러 요청 검증(@Valid) 및 도메인 예외를 4xx 계열로 매핑해 클라이언트가 원인을 명확히 알 수 있도록 개선한다.

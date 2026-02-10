# Task ID: 10

**Title:** OAuth + JWT Flow (Architectural View)

**Status:** done

**Dependencies:** None

**Priority:** medium

**Description:**   1. Client Initiation: User hits your app (SPA or backend). App redirects to auth gateway/Identity Provider (IdP) with OAuth2/OIDC (authorization code flow).
  2. IdP Authentication: User signs in at IdP (Google). On success, IdP redirects back with an auth code.
  3. Token Exchange: Your backend (or auth service) exchanges the code for tokens from IdP. Typically receives an ID Token (JWT) plus Access/Refresh tokens.
  4. Local User Link: Backend extracts sub/provider info from ID Token. It creates or updates a local user record (USERS table) tied to that provider ID, ensuring domain logic references a stable internal user ID.
  5. App Token Issuance: Backend issues its own short-lived JWT (or session) containing local user ID, roles, and any scoped claims. Refresh tokens (or re-auth via IdP) handle renewal.
  6. API Access: Clients call your APIs with the app JWT. Each service validates signature/expiry, then uses the local user ID to enforce domain rules. No service directly depends on Google IDs.
  7. Logout/Revocation: Invalidate refresh tokens or server sessions; optionally call IdP revoke endpoints. Centralize audit logs for sign-ins and token lifecycle events.

**Details:**

No details provided.

**Test Strategy:**

No test strategy provided.

## Subtasks

### 10.1. Configure OAuth2 Client and Authorization Code Grant Flow

**Status:** done  
**Dependencies:** None  

Set up the necessary Spring Security OAuth2 client configurations for an Identity Provider (e.g., Google) to initiate the authorization code flow. This includes defining client ID, client secret, redirect URIs, and scopes within the application's configuration.

**Details:**

1. Add `spring-boot-starter-oauth2-client` dependency to the `pom.xml`.
2. Configure `application.yml` or `application.properties` with the OAuth2 client registration details: client ID, client secret, authorization URI, token URI, user info URI, and redirect URI for the chosen IdP (e.g., Google).
3. Define a Spring Security configuration class (e.g., `SecurityConfig.java`) to enable OAuth2 login.
4. Ensure the application correctly handles the initial `/oauth2/authorization/<idp-name>` redirect to the IdP.

### 10.2. Implement Authorization Code to Token Exchange

**Status:** done  
**Dependencies:** 10.1  

Develop the backend logic to receive the authorization code from the Identity Provider (IdP) and exchange it for ID, Access, and Refresh tokens using the IdP's token endpoint.

**Details:**

1. Implement an endpoint (e.g., `/login/oauth2/code/<idp-name>`) that receives the authorization code as a callback from the IdP.
2. Utilize Spring Security's `OAuth2AuthorizedClientService` or `WebClient` to programmatically make a POST request to the IdP's token endpoint, sending the authorization code, client ID, client secret, and redirect URI.
3. Parse the JSON response from the IdP to extract the `id_token` (JWT), `access_token`, and `refresh_token`.
4. Validate the `id_token`'s signature and expiration, and then parse its claims (e.g., `sub`, `email`, `name`, `picture`).

### 10.3. Manage Local User Records based on IdP Claims

**Status:** done  
**Dependencies:** 10.2  

Implement the logic to create or update a local user record in the application's `USERS` table based on the extracted 'sub' and provider information from the ID Token. This establishes a stable internal user ID for all subsequent domain logic.

**Details:**

1. Define a `User` entity (e.g., `io.jgitkins.server.application.domain.model.User.java`) with fields such as `id` (internal primary key), `providerId` (IdP's 'sub' claim), `providerName` (e.g., 'google'), `email`, and `name`.
2. Create a `UserRepository` interface (e.g., `io.jgitkins.server.application.port.out.persistence.UserRepository.java`) for persistence operations, potentially using Spring Data JPA.
3. Develop a `UserService` (e.g., `io.jgitkins.server.application.port.service.UserService.java`) method, such as `findOrCreateUser(String providerName, String providerId, String email, String name)`, which:
    a. Queries the `UserRepository` to find an existing user by `providerId` and `providerName`.
    b. If the user does not exist, creates a new `User` entity and persists it.
    c. If the user exists, updates relevant user details (e.g., name, email) if they have changed.

### 10.4. Issue Internal Application JWT for Authentication

**Status:** done  
**Dependencies:** 10.3  

After a user has been authenticated via the IdP and linked to a local record, issue a new, short-lived internal JWT (JSON Web Token) containing the local user ID, roles, and other relevant claims for use in subsequent API calls within the application.

**Details:**

1. Implement a `JwtService` (e.g., `io.jgitkins.server.security.JwtService.java`) responsible for generating and validating internal JWTs.
2. Use a Java JWT library (e.g., `jjwt` or `Auth0 JWT`) to create digitally signed JWTs.
3. Configure a robust, securely stored secret key for signing the internal JWTs.
4. Include essential claims in the JWT payload: `sub` (local internal user ID), `roles` (list of assigned roles), `exp` (expiration timestamp), and `iat` (issued at timestamp).
5. Integrate the `JwtService` into the authentication flow after local user record management (Subtask 3) to return the newly generated internal JWT to the client (e.g., in the response body or as a cookie).
6. Consider a simple refresh token strategy, possibly by requiring re-authentication via the IdP for session renewal or implementing a server-side refresh token management system for the internal JWTs.

### 10.5. Implement JWT Validation Filter/Interceptor for API Access

**Status:** done  
**Dependencies:** 10.4  

Create a Spring Security filter or gRPC interceptor that intercepts incoming API requests, validates the internal application JWT, authenticates the user based on the JWT's claims, and sets up the security context for authorization checks.

**Details:**

1. For REST APIs: Implement a `JwtAuthenticationFilter` that extends `OncePerRequestFilter`. This filter will:
    a. Extract the JWT from the `Authorization` header (e.g., 'Bearer <token>').
    b. Validate the JWT's signature and expiration using the `JwtService` developed in Subtask 4.
    c. Extract the local `userId` and `roles` from the validated JWT.
    d. Load user details (e.g., `UserDetails`) based on the `userId`.
    e. Set the authenticated user in `SecurityContextHolder.getContext().setAuthentication(authentication)`.
2. For gRPC APIs (considering Task 8: 'Migrate Service Communication with gRPC'): Implement a `ServerInterceptor` (e.g., `JwtGrpcServerInterceptor`) that performs similar JWT validation logic for gRPC requests and sets the authenticated user within the gRPC `Context`.
3. Configure the filter/interceptor to be applied to all protected API endpoints in `SecurityConfig.java`.

### 10.6. OAuth 계정 연동을 위한 사용자 스키마 확장, 도메인 모델/매퍼 반영

**Status:** done  
**Dependencies:** None  

ReOrganize Branch Domain

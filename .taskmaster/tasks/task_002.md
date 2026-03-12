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
   * Adjust `src/main/java/io/jgitkins/server/application/port/service/RepositoryService.java` and `OrganizeService.java` to accommodate any structural or behavioral changes in their respective domain mod... [truncated]

### 2.3. [26] jgitkins-server JUnit 테스트 체계 구축

**Status:** done  
**Dependencies:** None  

jgitkins-server의 모든 기능에 대해 JUnit 기반 테스트 코드를 작성하고 회귀 검증 체계를 구축한다.

**Details:**

진행 워크플로우: (1) 테스트 인벤토리 스캔 -> (2) 도메인/서비스/WebMvc 공백 식별 -> (3) 공통 fixture/유틸 정리 -> (4) 우선순위 테스트 추가 -> (5) 회귀 실행 및 안정화.

현재 스캔 결과(2026-02-17): OAuth/User/Admin/Organize/RepositoryMember/Branch/Commit/RepositoryManagement/Credential/Runner 등록 관련 테스트는 존재.
우선 보강 대상: Repository Content(Tree/Blob/File) API, File Upload 생성 경로, 공통 인증/예외 응답 포맷 회귀.

이번 사이클 목표:
- [x] RepositoryContentController/WebMvc 테스트 보강
- [x] FileUploadUseCase/서비스 단위 테스트 보강
- [x] 401/403/404 공통 응답 포맷 회귀 테스트 보강
- [x] gradle test 기준 최소 회귀 세트 확정

### 2.4. RunnerController WebMvc 테스트 추가

**Status:** done  
**Dependencies:** 2.3  

RunnerController의 register/list/get/delete/activate API 응답, 매핑, 헤더(X-Forwarded-For) 처리 검증

**Details:**

RunnerController의 register/list/get/delete/activate API 응답, 매핑, 헤더(X-Forwarded-For) 처리 검증

### 2.5. UserCredentialController WebMvc 테스트 추가

**Status:** done  
**Dependencies:** 2.3  

PAT issue/list/revoke 경로 및 인증 주체 파싱/응답 포맷 검증

**Details:**

PAT issue/list/revoke 경로 및 인증 주체 파싱/응답 포맷 검증

### 2.6. MergeController WebMvc 테스트 추가

**Status:** done  
**Dependencies:** 2.3  

merge check/perform API 정상/예외 매핑 검증

**Details:**

merge check/perform API 정상/예외 매핑 검증

### 2.7. RepositoryCommit/RepositoryFile Controller 테스트 추가

**Status:** done  
**Dependencies:** 2.3  

커밋 상세/목록 및 파일 목록 API 파라미터 매핑/응답 포맷 검증

**Details:**

커밋 상세/목록 및 파일 목록 API 파라미터 매핑/응답 포맷 검증

### 2.8. JobDispatch/Runner 서비스 단위 테스트 보강

**Status:** done  
**Dependencies:** 2.3  

JobDispatchService, RunnerReadService, RunnerWriteService 핵심 분기 및 예외 경로 검증

**Details:**

JobDispatchService, RunnerReadService, RunnerWriteService 핵심 분기 및 예외 경로 검증

진행 현황(2026-02-17): RunnerReadServiceTest/RunnerWriteServiceTest 추가 완료. JobDispatchService 테스트는 다음 커밋에서 보강 예정.

### 2.9. [R9] 미사용 코드 제거 및 헥사고날 경계 정렬

**Status:** cancelled  
**Dependencies:** None  

서버 모듈의 dead code를 제거하고 포트/어댑터 경계를 기준으로 헥사고날 위배 지점을 정리한다.

**Details:**

1) 사용되지 않는 서비스/유틸/설정/테스트 코드 제거, 2) 애플리케이션 계층에서 인프라 세부 구현 의존 제거, 3) inbound/outbound adapter 책임 재정렬, 4) 예외/권한/매핑 로직의 계층 위치 재검증, 5) 리팩토링 후 회귀 테스트 보강.

### 2.10. 도메인 중심 예외 계층 구조 (Domain-Centric Exception Hierarchy)

**Status:** cancelled  
**Dependencies:** None  

방법 2: 도메인 중심 예외 계층 구조 (Domain-Centric Exception Hierarchy) - 정의: 모든 예외를 비즈니스 규칙 위반(BusinessException)과 시스템 장애(SystemException)로 나누고, 각각에 고유한 ErrorCode를 부여합니다. - 장점: 비즈니스 로직과 인프라 장애를 명확히 분리할 수 있으며, 헥사고날 아키텍처의 핵심인 도메인 보호에 최적화되어 있습니다. - 단점: 초기 설계 시 많은 ErrorCode 정의가 필요합니다. 최종 선택: 방법 2 (도메인 중심 예외 계층 구조) + ApiResponse 통합 가장 모던하고 이상적인 방법으로 방법 2를 선택합니다. 여기에 Spring Boot 3의 기능을 결합하여 다음과 같이 구조화하겠습니다. 1. ErrorCode (Enum): 에러의 식별자, 메시지, HTTP 상태 코드를 관리합니다. 2. BaseException (Abstract): 모든 커스텀 예외의 최상위 클래스입니다. 3. Domain/Application Exception: 비즈니스 로직 에러 (4xx 계열). 4. Infrastructure/External Exception: 외부 API 호출 실패나 시스템 장애 (5xx 계열). 5. GlobalExceptionHandler: 모든 예외를 잡아 일관된 ApiResponse 형식으로 변환합니다. 6. RestClientResponseErrorHandler: 외부 API의 에러를 분석하여 적절한 내부 예외로 변환(Mapping)합니다

### 2.11. BranchService 리팩토링 (책임 분리/예외 정합성/트랜잭션 경계)

**Status:** done  
**Dependencies:** None  

BranchService 오케스트레이션 책임을 명확히 하고 검증/조회/권한/예외 매핑을 정리하여 유지보수성과 테스트 용이성을 개선한다.

**Details:**

대상: src/main/java/io/jgitkins/server/application/port/service/BranchService.java 및 연관 validator/port/adapter. 핵심: 1) 입력/정책 검증 분리, 2) IOException 누수 제거 및 JgitkinsException 일원화, 3) 브랜치 생성/삭제 플로우에서 Git-DB 일관성 보강, 4) 중복 조회/중복 매핑 제거, 5) 단위/통합 테스트 보강.
<info added on 2026-02-28T16:43:39.643Z>
BranchService 리팩토링 사전 분석 및 계획 문서 작성이 완료됨. 참조 문서: .taskmaster/docs/task_2_11_plan.md. 핵심 이슈: 책임 과밀, IOException 시그니처 누수, Git-DB 불일치 리스크, BranchJGitAdapter 오류코드 정합성 문제. 단계별 실행안(Step1~4)과 DoD를 문서화함.
</info added on 2026-02-28T16:43:39.643Z>

### 2.12. PushHook 리팩토링 및 헥사고날 아키텍처 적용

**Status:** done  
**Dependencies:** None  

JGit PushHook의 기술적 관심사와 비즈니스 로직을 분리하고 헥사고날 패턴을 적용한다.

**Details:**

참조 문서: .taskmaster/docs/task_2_12_plan.md. 1) PushEventRequestResolver 도입으로 서블릿 의존성 제거, 2) RepositoryPort.findByPath 도입으로 경로 파싱 로직 인프라 위임, 3) PushEventHandleService 도메인 중심 오케스트레이션 구현 완료.

### 2.13. RepositoryLifecycleService 리팩토링 및 책임 분리

**Status:** done  
**Dependencies:** None  

거대해진 RepositoryLifecycleService의 검증, 조회, 가시성 정책을 분리하여 응집도를 높이고 도메인 중심 설계를 강화한다.

**Details:**

참조 문서: .taskmaster/docs/task_2_13_plan.md. 1) RepositoryValidator 도입으로 검증 로직 분리, 2) RepositoryLookupService 도입으로 복잡한 경로 해석 분리, 3) VO 우선 생성(Fast-Fail) 패턴 적용.

### 2.14. RepositoryOverviewService 리팩토링 및 예외 처리 개선

**Status:** done  
**Dependencies:** None  

RepositoryOverviewService의 Checked Exception을 제거하고 오케스트레이션 로직을 정돈한다.

**Details:**

참조 문서: .taskmaster/docs/task_2_14_plan.md. 1) UseCase 인터페이스에서 IOException 제거, 2) 예외 발생 시 JgitkinsException으로 통일, 3) 조회 및 브랜치 결정 로직 가독성 개선.

### 2.15. 계층별 ErrorCode 핸들링 전략 수립 및 리팩토링

**Status:** done  
**Dependencies:** None  

헥사고날 아키텍처 각 계층에서 발생하는 오류코드의 상호작용 규칙을 정의하고 정합성을 맞춘다.

**Details:**

참조 문서: .taskmaster/docs/task_2_15_plan.md. 1) 계층별 ErrorCode(DOM/APP/INF/PRE)의 책임 정의, 2) 하위 계층 예외의 상위 계층 전파/번역 규칙 수립, 3) GlobalExceptionHandler의 통합 처리 로직 개선.

### 2.18. Application→Infrastructure 직접 의존 제거

**Status:** done  
**Dependencies:** None  

Application 계층에서 인프라 구현체/패키지를 직접 참조하는 지점을 Port 인터페이스 경유로 치환한다.

### 2.19. Inbound/Outbound Adapter 책임 재배치

**Status:** pending  
**Dependencies:** None  

요청 파싱/인증 컨텍스트/외부 I/O 책임이 섞인 지점을 어댑터 경계 기준으로 분리한다.

### 2.20. 리팩토링 회귀 테스트 세트 확정

**Status:** pending  
**Dependencies:** None  

핵심 시나리오 기반 최소 회귀 세트를 정의하고 CI 기준선을 업데이트한다.

### 2.21. GlobalExceptionHandler/Mapper 일원화 검증

**Status:** pending  
**Dependencies:** None  

예외→ErrorCode→HttpStatus 변환 규칙의 중복/누락을 점검하고 단일 경로를 강제한다.

### 2.22. 외부 응답 마스킹 및 내부 관측성 기준 확정

**Status:** pending  
**Dependencies:** None  

외부 응답 메시지 노출 범위를 제한하고, 내부 로그/메트릭에 에러코드·예외타입·어댑터명을 표준 필드로 남긴다.

### 2.23. Dead code 인벤토리 작성

**Status:** done  
**Dependencies:** None  

참조 그래프, 빈 주입 여부, 테스트 사용 여부를 기준으로 미사용 클래스/메서드/설정/테스트를 후보로 식별하고 삭제 리스크를 분류한다.

### 2.24. 미사용 코드 제거 및 빌드 검증

**Status:** done  
**Dependencies:** None  

인벤토리 결과를 기반으로 dead code를 제거하고 컴파일/테스트로 회귀를 확인한다.

### 2.25. 로깅 설정 변경

**Status:** done  
**Dependencies:** None  

Java 기반 Configuration 으로 수정 (from .xml)

**Details:**

- 로깅 레벨별 Color 부여

### 2.26. Application 서비스 로직 일관화

**Status:** done  
**Dependencies:** None  

모든 Application 서비스들을 참조해서 코드흐름 일관성을 유지하도록 변경 [Updated: 3/10/2026]

**Details:**

1. Input validating 모두 제거 및 Presentation 계층 이관 주석 (TODO) 추가
2. Application Validator 캡슐화 도입 (데이터 정합성 검증)
<info added on 2026-03-10T04:50:08.281Z>
Input Validation 이관 작업이 완료되었습니다. `jgitkins-server` 프로젝트의 `src/main/java/jgitkins/server/api/dto` 패키지 내 Request DTO (예: `UserRequestDto.java`)들에 `@NotBlank`, `@NotNull`, `@Pattern`, `@Size` 등의 Jakarta Validation 어노테이션을 적용했습니다. 또한, 해당 DTO를 사용하는 `jgitkins.server.api.controller` 패키지 내 Controller 메서드 파라미터에 `@Valid`를 추가하여 Presentation 계층에서 유효성 검사를 수행하도록 로직을 이관하고, 기존 서비스/애플리케이션 계층의 유효성 검사 로직은 모두 제거되었습니다.
</info added on 2026-03-10T04:50:08.281Z>

### 2.27. Support 클래스 재분류 및 패키지 통일

**Status:** done  
**Dependencies:** 2.26  

UseCase 가 아닌 내부 서비스 용도로 사용되는 메서드/클래스들을 도출하여 Support 클래스로 강등하고, 패키지 구조 및 빈 어노테이션을 정비한다.

**Details:**

1. UseCase 인터페이스 강등 검토: 외부(Presentation/Infrastructure) 어댑터에서 직접 사용되지 않는 UseCase 인터페이스(예: GitRepositoryAccessUseCase)를 식별하여, Application 내부 Support 클래스로 재분류한다.
2. 서비스 패키지 통일: application.port.service 패키지에 있는 모든 Service 클래스를 application.service 패키지로 이관하여 패키지 구조를 단일화한다.
3. Support 패키지 빈 어노테이션 변경: application.support 패키지 내 @Service 어노테이션을 @Component 로 일괄 변경한다 (대상: UserService, RepositoryLookupService, RepositoryNamespaceResolver 등).
참조 문서: .taskmaster/docs/task_2_27_plan.md

### 2.28. Signup activate 유즈케이스 분리 및 웹 연동 정리

**Status:** done  
**Dependencies:** None  

AdminUserService.updateUserStatus 와 UserProfileService.updateUsername 의미를 signup activate 유즈케이스로 재분류하고, 서버 SignupController/SignupUseCase/SignupService 및 jgitkins-web 호출 경로를 함께 정리한다.

**Details:**

참조 문서: .taskmaster/docs/task_2_28_plan.md. 1) 서버는 기존 UserProfileService.updateUsername 로직을 SignupService.activate 로 재배치하거나 rename 한다. 2) AdminUserService.updateUserStatus 는 signup 책임에서 분리하고, 관리자 상태 변경이 필요하면 별도 유즈케이스로 유지한다. 3) SignupController 와 signup activate API 를 신설하고, 기존 /api/users/me/username 경로는 호환 필요 시 thin endpoint 로 유지 여부를 검토한다. 4) 웹 모듈 UsernameSetupController, UserPort/JGitkinsServerAdapter/JGitkinsServerClient 도 signup activate 의미로 rename 및 경로 변경한다. 5) username 관련 3개 검증은 inner class 대신 별도 SignupActivationValidator/Policy 로 묶는 방향을 우선 검토한다.

### 2.29. 예외 처리 구조 분석 및 개선 계획 수립

**Status:** done  
**Dependencies:** None  

staged 예외 클래스 변경을 기준으로 현재 예외 처리 구조를 분석하고, docs 문서 및 후속 리팩토링 실행 계획을 정리한다.

**Details:**

<info added on 2026-03-10T09:02:50.825Z>
진행 메모(2026-03-10): staged 예외 클래스 변경을 기준으로 예외 처리 구조 분석 문서와 실행 계획 문서를 작성함. 참조 문서: docs/exception-handling-analysis.md, docs/exception-handling-plan.md. 후속 구현 범위는 계층별 예외 타입 정리, GlobalExceptionHandler 타입 기반 분리, 도메인 예외 재포장 제거, ErrorResponse 삭제 여부 반영.
</info added on 2026-03-10T09:02:50.825Z>

### 2.30. MyBatis 어댑터 예외 처리 일관성 확보

**Status:** done  
**Dependencies:** None  

RunnerMybatisAdapter, JobMybatisAdapter 등에서 발생하는 DB 예외를 InfrastructureException으로 일관되게 래핑하여 인프라 계층의 책임을 일원화합니다.

### 2.31. JgitkinsException 추상 클래스 전환 및 구조 정돈

**Status:** done  
**Dependencies:** None  

JgitkinsException을 abstract로 변경하여 직접 인스턴스화를 방지하고, 이를 반영하여 GlobalExceptionHandler의 불필요한Fallback 로직을 제거합니다.

### 2.32. Port, Adapter 클래스 Renaming (Modernly, BestPracticeful)

**Status:** done  
**Dependencies:** None  

Port 및 Adapter 클래스명을 현대적이고 모범적인 사례에 맞게 변경하여 아키텍처 가시성을 높임.

### 2.33. Port, Adapter 내 메서드명 Renaming (Modernly, BestPracticeful)

**Status:** done  
**Dependencies:** None  

Port 및 Adapter 내의 메서드명을 현대적이고 모범적인 사례에 맞게 변경하여 의도를 명확히 함.

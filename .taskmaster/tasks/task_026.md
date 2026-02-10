# Task ID: 26

**Title:** jgitkins-server JUnit 테스트 체계 구축

**Status:** pending

**Dependencies:** None

**Priority:** high

**Description:** jgitkins-server의 모든 기능에 대해 JUnit 기반 테스트 코드를 작성하고 회귀 검증 체계를 구축한다.

**Details:**

컨트롤러(WebMvcTest), 서비스(Mockito 기반 단위 테스트), 핵심 인프라 통합 테스트를 기능 단위로 확장한다. 기능 목록을 SubTask로 분해해 누락 없이 작성한다.

**Test Strategy:**

기능별 JUnit 테스트 + 전체 clean test + 핵심 API 스모크 테스트

## Subtasks

### 26.1. OAuth 로그인/JWT 발급 기능 테스트

**Status:** pending  
**Dependencies:** None  

OAuthController, OAuthLoginService, Jwt 관련 흐름 테스트

**Details:**

정상 로그인, 잘못된 provider/sub, 토큰 발급 실패, JWT claim 검증 케이스 포함

### 26.2. User 조회/프로필 업데이트 기능 테스트

**Status:** pending  
**Dependencies:** None  

UserController, UserService, PublicUserQueryService 테스트

**Details:**

내 정보 조회, 공개 사용자 조회, username 업데이트 성공/실패 검증

### 26.3. Admin User 관리 기능 테스트

**Status:** pending  
**Dependencies:** None  

AdminUserController, AdminUserService 테스트

**Details:**

관리자 사용자 목록/상태 업데이트 API의 권한 및 예외 케이스 검증

### 26.4. Organize 생성/조회/삭제 기능 테스트

**Status:** pending  
**Dependencies:** None  

OrganizeController와 관련 서비스/포트 테스트

**Details:**

조직 생성 유효성, 조회 필터링, 삭제 권한/실패 케이스 검증

### 26.5. Organize Member 관리 기능 테스트

**Status:** pending  
**Dependencies:** None  

OrganizeMemberController 및 멤버 추가/삭제/조회 테스트

**Details:**

중복 멤버 처리, 권한 검증, 잘못된 조직/사용자 입력 처리 포함

### 26.6. Repository 생성/조회/삭제 기능 테스트

**Status:** pending  
**Dependencies:** None  

RepositoryManagementController, Repository 관련 UseCase 테스트

**Details:**

owner type(USER/ORGANIZATION), 공개/비공개, 삭제 실패/권한 케이스 포함

### 26.7. Repository Member 관리 기능 테스트

**Status:** pending  
**Dependencies:** None  

RepositoryMemberController 및 멤버 추가/조회/삭제 테스트

**Details:**

역할(role) 검증, 중복 추가, 존재하지 않는 멤버 삭제 시나리오 포함

### 26.8. Branch 생성/조회/삭제 기능 테스트

**Status:** pending  
**Dependencies:** None  

BranchController 및 Branch 관련 서비스 테스트

**Details:**

브랜치명 유효성, 중복 생성, 기본 브랜치 삭제 방지 등 정책 검증

### 26.9. Repository Commit 조회 기능 테스트

**Status:** pending  
**Dependencies:** None  

RepositoryCommitController 및 CommitLoadUseCase 테스트

**Details:**

브랜치별 커밋 조회, 페이지/정렬, 비정상 ref 입력 처리 포함

### 26.10. Repository Content(Tree/Blob/File) 조회 기능 테스트

**Status:** pending  
**Dependencies:** None  

RepositoryContentController, RepositoryFileController 테스트

**Details:**

트리 탐색, blob 로드, 파일 경로 인코딩/디코딩 및 404 시나리오 검증

### 26.11. File Upload 기반 새 파일 생성 기능 테스트

**Status:** pending  
**Dependencies:** None  

FileUploadUseCase 및 업로드 API 테스트

**Details:**

multipart 업로드 성공, 파일 크기/형식 제한, 경로 충돌 처리 포함

### 26.12. Merge/Mergeability 기능 테스트

**Status:** pending  
**Dependencies:** None  

MergeController와 Merge 관련 유스케이스 테스트

**Details:**

merge 가능 여부 판단, 충돌 상황, 대상 브랜치 없음 케이스 포함

### 26.13. Runner 등록/조회/활성화/삭제 기능 테스트

**Status:** pending  
**Dependencies:** None  

RunnerController, RunnerReadService, RunnerWriteService 테스트

**Details:**

runner token/상태 전이, heartbeat 처리, 중복 등록/삭제 시나리오 포함

### 26.14. Job 생성/디스패치/결과보고/PushEvent 기능 테스트

**Status:** pending  
**Dependencies:** None  

JobService, JobDispatchService, JobResultReportService 테스트

**Details:**

job lifecycle(생성→큐잉→완료), 결과 보고 검증, push 이벤트 처리 케이스 포함

### 26.15. User Credential(PAT) 발급/조회/폐기 기능 테스트

**Status:** pending  
**Dependencies:** None  

UserCredentialController, UserCredentialService 테스트

**Details:**

토큰 발급 규칙, 만료일/설명 입력, 폐기 후 재조회 결과 검증

### 26.16. 공통 예외/보안/인증 필터 회귀 테스트

**Status:** pending  
**Dependencies:** None  

전역 예외 처리, 인증 필터, 권한 실패 응답 테스트

**Details:**

401/403/404 포맷 일관성, 예외 메시지 규격, 인증 누락 요청 처리 검증

### 26.17. 테스트 인프라 통합 및 CI 실행 전략 확정

**Status:** pending  
**Dependencies:** 26.1, 26.2, 26.3, 26.4, 26.5, 26.6, 26.7, 26.8, 26.9, 26.10, 26.11, 26.12, 26.13, 26.14, 26.15, 26.16  

테스트 유틸/픽스처/빌더 정리 및 CI 태스크 설계

**Details:**

공용 fixture, test data builder, 태그 전략(unit/integration), gradle test 분리 실행 정책 수립

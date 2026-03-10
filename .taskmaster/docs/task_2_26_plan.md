# Task 2.26: Application 서비스 로직 일관화 (Input Validation 분리)

## 1. 개요 및 설계 철학 (Architecture Overview)

* **배경:** 현재 `Application Service` 레이어(유스케이스) 내에 클라이언트 입력값에 대한 원시적인 유효성 검증(Null 체크, Blank 체크 등) 로직과 비즈니스 정합성 검증이 혼재되어 있습니다. 이는 각 계층(Layer)의 단일 책임 원칙(SRP)을 위반하며, 코드가 방대해짐에 따라 일관성이 저하되는 원인이 됩니다.
* **해결 목표:**
  1. **Input Validating 제거 및 계층 이동:** Application Service에 있는 단순 입력 검증 로직을 모두 제거하고, Presentation 계층(웹 어댑터)에 그 책임을 이관합니다.

## 2. 작업 상세 계획 및 Action Plan

### Step 1: Input Validation 제거 명세 작성 (동기성 제어)
모든 Application Service를 탐색하여 입력 파라미터(DTO, ID, 문자열 등)의 순수 유효성을 점검하는 로직을 일괄 삭제합니다.

* **삭제 대상 (예시):** 
  * `if (param == null || param.isEmpty()) throw new IllegalArgumentException();`
  * `if (email == null || !email.contains("@")) ...`
* **조치 사항:** 해당 로직이 있던 곳 상단에 `// TODO: Presentation 계층에서 Controller @Valid / @NotBlank 등으로 검증 필요` 주석을 삽입합니다.

## 3. 수정 대상 메서드 목록 및 코드 적용 예시

### 3.1. 작업 대상 애플리케이션 서비스 및 메서드 목록
전체 애플리케이션 서비스 계층을 탐색하여 웹/클라이언트 입력값(DTO, 식별자, 문자열 등)에 대해 계층 내에서 직접 `null` 혹은 `isEmpty()` 등을 검사하는 객체 및 메서드들은 다음과 같습니다.

* **`RepositoryOverviewService`**
  * `getBranches` 등 (branch, repository 필드 null 검사)
* **`RepositoryFileService`**
  * 파일 제어 메서드 (authorName, authorEmail 등 문자열 상태 검사)
* **`AdminUserService`**
  * `updateUserStatus` (status 유효성 검사 등)
* **`UsernameAllocator` / `UserProfileUpdater` / `RepositoryLookupService`** (Support 클래스들)
  * 문자열(`email`, `namespace`, `repoName` 등) 파라미터의 empty 여부 직접 제어 등

### 3.2. 각 서비스별 Before & After 명세

다음은 실제 각 애플리케이션 서비스에 존재하던 유효성 검사 코드(Before)와, 검증 책임을 Presentation 계층으로 위임하기 위해 추가한 `TODO` 주석 방향(After)입니다.

#### 1. `RepositoryOverviewService`
* **Before:**
  ```java
  public void getBranches(String branch, Repository repository) {
      if (repository == null) {
          throw new IllegalArgumentException("Repository cannot be null");
      }
      // ...
  }
  ```
* **After (Application Service):**
  ```java
  public void getBranches(String branch, Repository repository) {
      // TODO: repository 필수 값 여부는 상위 계층 호출 전 혹은 Controller 검증 단에서 처리
      // ...
  }
  ```
* **After (Presentation - Controller / Parameter Validating):**
  ```java
  @RestController
  @RequestMapping("/api/repositories/{namespace}/{repoName}")
  public class RepositoryOverviewController {
      
      private final RepositoryOverviewService overviewService;
      
      @GetMapping("/branches")
      public ResponseEntity<List<BranchDto>> getBranches(
              // @PathVariable 등 파라미터 자체에서 @NotBlank 등 필수값을 보장
              @PathVariable @NotBlank String namespace,
              @PathVariable @NotBlank String repoName,
              @RequestParam(required = false) String branch) {
          
          Repository repository = findRepository(namespace, repoName);
          // 리포지토리가 존재함이 보장된 후 서비스 호출
          overviewService.getBranches(branch, repository);
          return ResponseEntity.ok().build();
      }
  }
  ```

#### 2. `RepositoryFileService`
* **Before:**
  ```java
  public void createFile(FileRequest request) {
      if (request == null || !StringUtils.hasText(request.getAuthorName())) {
          throw new IllegalArgumentException("Author name is missing");
      }
      // ...
  }
  ```
* **After (Application Service):**
  ```java
  public void createFile(FileRequest request) {
      // TODO: request 내 AuthorName 값 존재 여부는 API 요청 단계(@Valid)에서 검증 필요
      // ...
  }
  ```
* **After (Presentation - Controller / DTO):**
  ```java
  public class FileCreateRequest {
      @NotBlank(message = "Author name is missing")
      private String authorName;
      
      @Email(message = "Invalid email format")
      private String authorEmail;
      // ...
  }

  @RestController
  @RequestMapping("/api/repositories/{repoId}")
  public class RepositoryFileController {
      private final RepositoryFileService repositoryFileService;

      @PostMapping("/file")
      public ResponseEntity<Void> createFile(
              @PathVariable Long repoId,
              // @Valid를 통해 DTO의 @NotBlank 등 형식 검증 활성화
              @Valid @RequestBody FileCreateRequest request) {
              
          repositoryFileService.createFile(repoId, request);
          return ResponseEntity.ok().build();
      }
  }
  ```

#### 3. `AdminUserService`
* **Before:**
  ```java
  public void updateUserStatus(UserStatus status) {
      if (status == null) {
          throw new IllegalArgumentException("Unsupported status");
      }
      // ...
  }
  ```
* **After (Application Service):**
  ```java
  public void updateUserStatus(UserStatus status) {
      // TODO: 상태에 대한 순수 유효성 검증은 API (@Valid) 단으로 이관
      // ...
  }
  ```
* **After (Presentation - Controller / DTO):**
  ```java
  public class UserStatusUpdateRequest {
      @NotNull(message = "Status is required")
      private UserStatus status;
  }

  @RestController
  @RequestMapping("/api/admin/users/{userId}")
  public class AdminUserController {
      private final AdminUserService adminUserService;

      @PatchMapping("/status")
      public ResponseEntity<Void> updateUserStatus(
              @PathVariable Long userId,
              @Valid @RequestBody UserStatusUpdateRequest request) {
          
          adminUserService.updateUserStatus(userId, request.getStatus());
          return ResponseEntity.ok().build();
      }
  }
  ```

#### 4. Support 영역 (`UsernameAllocator`, `UserProfileUpdater`, `RepositoryLookupService`)
* **Before:**
  ```java
  public boolean isEmailValid(String email) {
      if (email == null || email.isBlank()) {
          throw new IllegalArgumentException("Email is empty");
      }
      // ...
  }
  ```
* **After (Application Support / Service):**
  ```java
  public boolean isEmailValid(String email) {
      // TODO: 문자열 기본 검증 책임은 호출자(Controller 또는 상위 Service)로 이전
      // ...
  }
  ```
* **After (Presentation - Controller / DTO):**
  ```java
  public class UserProfileUpdateRequest {
      @NotBlank(message = "Email is empty")
      @Email(message = "Invalid email format")
      private String email;
  }

  @RestController
  @RequestMapping("/api/users/profile")
  public class UserProfileController {
      private final UserProfileUpdater userProfileUpdater;

      @PutMapping
      public ResponseEntity<Void> updateProfile(
              @Valid @RequestBody UserProfileUpdateRequest request) {
          
          // Controller 단에서 이미 @NotBlank, @Email 검증이 완료된 값이 전달됨
          userProfileUpdater.updateEmail(request.getEmail());
          return ResponseEntity.ok().build();
      }
  }
  ```


## 4. 기대 효과 및 향후 과제 (시니어 관점의 회고)

* **계층형 아키텍처(클린 아키텍처)의 이점 극대화:** Input 유효성은 프론트 엔드와 가장 맞닿은 `Spring Web(Presentation)`에게 위임하며, 순수 자바 런타임 비즈니스 로직(Usecase)과 도메인이 외부 요청 포맷에 더럽혀지지 않게 은닉됩니다.
* **단일 책임 원칙(SRP) 준수:** 서비스는 "저장소에서 무엇을 꺼내와서, 도메인에게 행위를 지시하고, 다시 집어넣는다"라는 파사드(Facade)의 역할만 담당하게 되므로 흐름 파악이 매우 용이해집니다.
* **TODO 과제:** 이후 Presentation 계층(Controller 및 DTO) 작업 태스크가 진행될 때 반드시 `jakarta.validation.constraints` (예: `@NotBlank`, `@Size`, `@Email` 등) 애노테이션을 부착하는 후속 조치가 이어져야 합니다.

# Task 2.27: Support 클래스 재분류 및 패키지 통일

## 1. 개요 및 설계 철학 (Architecture Overview)

* **배경:** 현재 `application` 계층의 패키지 구조가 `application.port.service`, `application.service`, `application.support` 세 가지로 분산되어 있으며, 그 경계가 불분명합니다. 또한 내부 협력 컴포넌트임에도 `UseCase` 인터페이스를 구현하거나 `@Service` 어노테이션을 가지고 있어 헥사고날 아키텍처의 의도와 어긋나는 지점이 존재합니다.
* **해결 목표:**
  1. **UseCase 강등:** 외부(Presentation / Infrastructure) 어댑터가 직접 의존하지 않는 `UseCase` 인터페이스·구현체를 식별하고, 내부 Support 클래스로 재분류합니다.
  2. **서비스 패키지 통일:** `application.port.service` 패키지의 모든 Service 클래스를 `application.service`로 이관하여 두 패키지를 단일화합니다.
  3. **빈 어노테이션 정비:** `application.support` 내 클래스들의 `@Service`를 의미에 맞는 `@Component`로 변경합니다.

---

## 2. 작업 대상 분석

### 2.1. UseCase 강등 후보 — `GitRepositoryAccessUseCase`

#### 현황
```
application/port/in/GitRepositoryAccessUseCase.java    ← UseCase 인터페이스
application/service/GitRepositoryAccessService.java   ← 구현체 (@Service)
```

#### 외부 참조 현황

| 참조 위치 | 종류 | 비고 |
|---|---|---|
| `RepositoryOverviewService` | Application 내부 (Service) | 내부 의존 |
| `RepositoryAccessValidator` | Application 내부 (Validator) | 내부 의존 |
| `GitSmartHttpAuthorizer` | **Infrastructure** | 외부 어댑터 |
| `GitSmartHttpAuthFilter` | **Infrastructure** | 외부 어댑터 |

> ⚠️ `GitRepositoryAccessUseCase`는 `GitSmartHttpAuthorizer`, `GitSmartHttpAuthFilter` 라는 **Infrastructure 어댑터**가 사용하고 있으므로, UseCase 포트로 남겨두는 것이 적합합니다.
> 단, **Application 내부에서의 사용 방식**(예: `RepositoryOverviewService`, `RepositoryAccessValidator`)을 점검하여 필요 시 내부 타입 분리를 검토합니다.

#### 결론
- `GitRepositoryAccessUseCase`는 Infrastructure 어댑터와의 계약으로 **UseCase 포트 유지**.
- `application.service.GitRepositoryAccessService`패키지 위치는 2.2 통일 작업에서 정리합니다.

---

### 2.2. 서비스 패키지 통일

#### 현황
현재 Service 클래스들이 두 패키지에 분산되어 있습니다.

| 현재 패키지 | 이관 대상 클래스 |
|---|---|
| `application.port.service` | `AdminUserService`, `BranchService`, `CommitService`, `MergeService`, `OAuthLoginService`, `OrganizeMemberService`, `OrganizeService`, `PublicUserQueryService`, `PushEventHandleService`, `RepositoryFileService`, `RepositoryLifecycleService`, `RepositoryMemberService`, `RepositoryOverviewService`, `UserCredentialService`, `UserProfileService` (15개) |
| `application.service` (유지) | `GitRepositoryAccessService`, `JobDispatchService`, `JobResultReportService`, `JobService`, `RunnerManagementService`, `RunnerReadService` |

#### 목표 구조
```
application/
  service/           ← 모든 Application Service 통합
    AdminUserService.java
    BranchService.java
    CommitService.java
    GitRepositoryAccessService.java
    JobDispatchService.java
    JobResultReportService.java
    JobService.java
    MergeService.java
    OAuthLoginService.java
    OrganizeMemberService.java
    OrganizeService.java
    PublicUserQueryService.java
    PushEventHandleService.java
    RepositoryFileService.java
    RepositoryLifecycleService.java
    RepositoryMemberService.java
    RepositoryOverviewService.java
    RunnerManagementService.java
    RunnerReadService.java
    UserCredentialService.java
    UserProfileService.java
  support/           ← 내부 협력 컴포넌트 (비공개 Support)
    ...
  port/
    in/              ← UseCase 인터페이스만 존재
    out/             ← Port 인터페이스
```

#### 이관 시 필수 작업
1. 각 Service 클래스의 `package` 선언 변경: `io.jgitkins.server.application.port.service` → `io.jgitkins.server.application.service`
2. 해당 Service 클래스들을 참조하는 모든 파일의 `import` 경로 업데이트
3. 컴파일 및 전체 테스트 통과 확인

---

### 2.3. Support 패키지 빈 어노테이션 정비

#### 현황 (`application.support` 내 `@Service` 사용 클래스)

| 클래스 | 현재 어노테이션 | 변경 어노테이션 |
|---|---|---|
| `UserService` | `@Service` | `@Component` |
| `RepositoryLookupService` | `@Service` | `@Component` |
| `RepositoryNamespaceResolver` | `@Service` | `@Component` |
| `UsernameAllocator` | `@Component` | 유지 |
| `UserProfileUpdater` | `@Component` | 유지 |
| `RunnerRuntimeConfigProvider` | `@Component` | 유지 |

#### 변경 이유
`@Service`는 비즈니스 서비스 계층(UseCase 오케스트레이터)을 나타내는 의미론적 어노테이션입니다. `application.support`의 클래스들은 내부 협력 컴포넌트이므로, 의미적으로 더 적합한 `@Component`를 사용하는 것이 올바릅니다.

---

## 3. Before & After 예시

### 3.1. 패키지 이관 — `AdminUserService`

* **Before:**
  ```java
  package io.jgitkins.server.application.port.service;  // 변경 전

  @Service
  public class AdminUserService implements AdminUserQueryUseCase, AdminUserUpdateUseCase {
      ...
  }
  ```

* **After:**
  ```java
  package io.jgitkins.server.application.service;  // 변경 후

  @Service
  public class AdminUserService implements AdminUserQueryUseCase, AdminUserUpdateUseCase {
      ...
  }
  ```

---

### 3.2. Support 빈 어노테이션 변경 — `RepositoryLookupService`

* **Before:**
  ```java
  package io.jgitkins.server.application.support;

  @Service   // UseCase 오케스트레이터가 아닌 내부 Support
  @RequiredArgsConstructor
  @Slf4j
  public class RepositoryLookupService {
      ...
  }
  ```

* **After:**
  ```java
  package io.jgitkins.server.application.support;

  @Component  // 내부 협력 컴포넌트로 의미 명확화
  @RequiredArgsConstructor
  @Slf4j
  public class RepositoryLookupService {
      ...
  }
  ```

---

### 3.3. Support 빈 어노테이션 변경 — `UserService`

* **Before:**
  ```java
  package io.jgitkins.server.application.support;

  @Service
  @RequiredArgsConstructor
  public class UserService {
      ...
  }
  ```

* **After:**
  ```java
  package io.jgitkins.server.application.support;

  @Component
  @RequiredArgsConstructor
  public class UserService {
      ...
  }
  ```

---

### 3.4. Support 빈 어노테이션 변경 — `RepositoryNamespaceResolver`

* **Before:**
  ```java
  package io.jgitkins.server.application.support;

  @Service
  @RequiredArgsConstructor
  public class RepositoryNamespaceResolver {
      ...
  }
  ```

* **After:**
  ```java
  package io.jgitkins.server.application.support;

  @Component
  @RequiredArgsConstructor
  public class RepositoryNamespaceResolver {
      ...
  }
  ```

---

## 4. 실행 순서 및 DoD (Definition of Done)

### Step 1: Support 빈 어노테이션 변경
- [ ] `UserService`, `RepositoryLookupService`, `RepositoryNamespaceResolver`의 `@Service` → `@Component` 변경
- [ ] `./gradlew test` 통과

### Step 2: application.port.service → application.service 이관
- [ ] 15개 Service 클래스 `package` 선언 변경
- [ ] 참조하는 모든 파일의 `import` 경로 업데이트 (Controller, Validator, 타 Service 등)
- [ ] `application.port.service` 패키지 디렉터리 삭제 (빈 패키지)
- [ ] `./gradlew test` 통과

### Step 3: GitRepositoryAccessUseCase 위상 확인
- [ ] Presentation / Infrastructure 어댑터 참조 현황 재확인
- [ ] 필요 시 내부 타입 분리 방안 별도 문서화

---

## 5. 기대 효과

* **패키지 명확성 향상:** `application.service` 단일 패키지로 Use Case 구현체 위치가 명확해집니다.
* **의미론적 일관성:** `@Component` 어노테이션으로 Support 클래스의 의도가 코드에서 드러납니다.
* **아키텍처 준수:** UseCase 포트(외부 계약)와 내부 Support(협력 컴포넌트)의 경계가 명확해져 헥사고날 아키텍처 원칙을 강화합니다.

# Signup 활성화 유즈케이스 분리 리팩토링 계획서

## 1. 배경
현재 서버의 [AdminUserService.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/AdminUserService.java) 에는 `updateUserStatus(Long userId, String status)` 메서드가 존재합니다. 이름과 위치만 보면 관리자용 상태 변경 유즈케이스처럼 보이지만, 실제 요구사항은 다릅니다.

해당 흐름은 다음 의미를 가집니다.

1. 사용자가 최초 회원가입(OAuth 로그인 포함) 이후
2. 본인의 username 을 지정하고
3. 그 결과 사용자 상태가 `PENDING -> ACTIVE` 로 전환된다

즉 이 메서드는 **관리자 상태 변경**이 아니라 **Signup 완료/계정 활성화 유즈케이스**입니다.

또한 웹 모듈 [UsernameSetupController.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/presentation/controller/UsernameSetupController.java) 는 현재 서버의 `/api/users/me/username` 만 호출하고, 성공 시 프론트 세션만 `ACTIVE` 로 바꾸고 있습니다. 따라서 서버와 웹이 모두 실제 유즈케이스 의미를 반영하도록 함께 리팩토링해야 합니다.

---

## 2. 현재 구조 분석

### 2.1 서버

#### Admin 경로
- [AdminUserController.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/presentation/api/rest/AdminUserController.java)
  - `PATCH /api/admin/users/{userId}/status`
  - `AdminUserUpdateUseCase.updateUserStatus(userId, status)` 호출

#### 실제 회원가입 완료 경로
- [UserController.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/presentation/api/rest/UserController.java)
  - `PATCH /api/users/me/username`
  - `UserProfileUpdateUseCase.updateUsername(username)` 호출
- [UserProfileService.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/UserProfileService.java)
  - username 검증
  - 현재 로그인 사용자 조회
  - 사용자명 중복/조직명 충돌/기존 저장소 보유 여부 검증
  - `user.activateWithUsername(requested)` 수행
  - 저장

즉, 이미 서버에는 **회원가입 완료 유즈케이스의 실제 구현체**가 `UserProfileService.updateUsername` 형태로 존재합니다.
즉, 이번 리팩토링은 완전히 새로운 로직을 만드는 작업이 아니라, **이미 존재하는 회원가입 완료 로직을 `activate` 유즈케이스 의미에 맞게 rename/repackage 하는 작업**으로 보는 것이 맞습니다.

### 2.2 웹

#### 온보딩 컨트롤러
- [UsernameSetupController.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/presentation/controller/UsernameSetupController.java)
  - username 입력
  - `userPort.updateUsername(form.getUsername())` 호출
  - 성공 시 세션에 username 저장
  - `sessionSupport.activateUser(request)` 호출

#### 서버 호출 어댑터
- [JGitkinsServerAdapter.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/infrastructure/adapter/JGitkinsServerAdapter.java)
  - `updateUsername(String username)`
- [JGitkinsServerClient.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/infrastructure/client/JGitkinsServerClient.java)
  - `PATCH /api/users/me/username`

즉 웹도 지금은 “username 변경”처럼 표현하고 있지만 실제 의미는 “signup activate” 입니다.

---

## 3. 문제 정의

현재 구조의 문제는 세 가지입니다.

### 3.1 유즈케이스 이름과 책임이 어긋남
- `updateUserStatus` 는 관리자 기능처럼 보이지만 실제 도메인 의미는 signup activate
- `updateUsername` 도 단순 프로필 수정처럼 보이지만 실제로는 상태 전환을 수반

### 3.2 포트 경계가 잘못 표현됨
- `AdminUserUpdateUseCase` 는 관리자용 포트처럼 보이나 signup 흐름과 무관
- `UserProfileUpdateUseCase` 는 프로필 수정으로 보이나 계정 활성화 책임을 포함

### 3.3 웹과 서버 용어가 불일치
- 서버는 `/me/username`, 웹은 `updateUsername`
- 세션은 `activateUser()` 로 바꾸고 있음
- 실제 유즈케이스 개념과 API/클라이언트 이름이 따로 놀고 있음

---

## 4. 방법 조사

### 방법 1. 기존 API 유지, 내부 메서드명만 변경
- 정의: 외부 API는 유지하고 내부 서비스/메서드명만 `activate` 로 리팩토링
- 장점: 영향 범위가 작다
- 단점: 웹/서버 용어 불일치가 그대로 남고, API 의미도 계속 흐릿하다

### 방법 2. Signup 전용 유즈케이스/서비스/컨트롤러를 신설하고 웹도 함께 변경
- 정의:
  - 서버에 `SignupController`, `SignupUseCase`, `SignupService` 신설
  - 핵심 메서드명을 `activate` 로 통일
  - 웹도 `updateUsername` 대신 signup activate 흐름으로 맞춤
- 장점: 유즈케이스 의미가 코드/패키지/API에서 일관되게 드러난다
- 단점: 서버/웹 양쪽 변경 필요, 테스트 수정 범위가 커진다

### 방법 3. Admin/UserProfile 경로를 유지하되 별도 Facade 만 추가
- 정의: 기존 유즈케이스는 유지하고 signup facade 하나를 얹는다
- 장점: 하위 호환이 쉽다
- 단점: 중복 유즈케이스가 생기고 책임이 더 모호해질 가능성이 높다

### 최종 선택
- **방법 2 선택**
- 이유: 이번 요구사항은 단순 rename 이 아니라 “이 메서드는 signup activate 유즈케이스다”라는 도메인 재분류입니다. 따라서 서버/웹 모두 이름과 경계를 다시 세우는 쪽이 맞습니다.

---

## 5. 목표 구조

### 5.1 서버

```text
presentation/api/rest
  SignupController.java

application/port/in
  SignupUseCase.java

application/service
  UserProfileService.java
```

핵심 메서드:

```java
public interface SignupUseCase {
    void activate(String username);
}
```

```java
@Service
public class UserProfileService implements SignupUseCase {
    @Override
    public void activate(String username) {
        // username 검증
        // 현재 로그인 사용자 조회
        // 중복/충돌 검증
        // activateWithUsername
        // 저장
    }
}
```

### 5.2 웹

```text
application/port/out
  SignupPort.java     // 또는 UserPort 내 signup activate 책임 분리

infrastructure/adapter
  JGitkinsServerAdapter.java

infrastructure/client
  JGitkinsServerClient.java

presentation/controller
  UsernameSetupController.java
```

웹은 “username 변경”이 아니라 “signup 활성화”를 호출해야 합니다.

예상 메서드:

```java
public interface SignupPort {
    SignupActivateResult activate(String username);
}
```

또는 최소 변경안으로는 기존 `UserPort` 유지하되 메서드명을:

```java
SignupActivateResult activate(String username);
```

로 바꿉니다.

---

## 6. 세부 리팩토링 계획

### Step 1. 서버 유즈케이스 재분류
- [ ] `AdminUserService.updateUserStatus` 의 signup 관련 책임 제거
- [ ] `AdminUserUpdateUseCase` 에서 signup 성격 로직 제거
- [ ] 진짜 관리자 상태 변경 유즈케이스가 필요한지 별도 판단

논의 포인트:
- 현재 요구사항 기준으로 `AdminUserService.updateUserStatus` 는 signup activate 와 맞지 않다
- 따라서 이 메서드는 삭제 또는 관리자 전용 상태 변경으로 축소되어야 한다
- 만약 관리자 상태 변경 API 자체가 계속 필요하다면 signup activate 와 완전히 분리된 별도 유즈케이스로 유지해야 한다

### Step 2. SignupUseCase / SignupService 도입
- [ ] `SignupUseCase.activate(String username)` 신설
- [ ] 기존 [UserProfileService.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/UserProfileService.java) 가 `SignupUseCase`를 구현하도록 정리
- [ ] `updateUsername` 메서드를 `activate` 로 rename

권장 방향:
- `UserProfileService.updateUsername` 의 구현 로직은 사실상 signup activate 유즈케이스와 동일하다
- 따라서 별도 `SignupService`를 새로 만들기보다, 기존 `UserProfileService`를 유지한 채 `activate` 유즈케이스로 재정의하는 것이 맞다

구체 원칙:
1. 로직은 새로 만들지 않는다
2. 기존 `updateUsername` 구현을 `activate` 의미로 승격한다
3. `UserProfileService` 클래스는 유지하고, 외부에 노출되는 포트/메서드 시그니처만 `activate` 의미로 정리한다
4. 하위 호환 때문에 기존 endpoint 를 잠시 유지하더라도 내부 호출 메서드는 `activate` 로 통일한다

### Step 2-1. signup 검증 캡슐화 정리
- [ ] username 관련 3개 검증을 하나의 signup activation 전용 검증 흐름으로 묶을지 결정
- [ ] `UserProfileValidator` 책임이 signup 전용으로 기울어지는지 재검토

정리 방향:
- 검증 3개는 내부 클래스보다 별도 협력 컴포넌트로 묶는 편이 적절하다
- 이번 리팩토링에서는 해당 컴포넌트 명칭을 `ActivationValidator` 로 사용한다

### Step 3. SignupController 도입
- [ ] `SignupController` 신설
- [ ] endpoint 설계

후보 API:
1. `PATCH /api/signup/activate`
2. `POST /api/signup/activate`
3. `PATCH /api/users/me/activate`

현재 권장안:
- **`POST /api/signup/activate` 우선 검토**
- 이유: username 지정 후 계정이 활성화되는 것은 단순 필드 수정이 아니라 signup completion action 에 가깝기 때문

호출지 영향:
- endpoint 가 변경되면 서버 컨트롤러만 바꾸는 것으로 끝나지 않는다
- 아래 웹 호출지까지 함께 수정되어야 한다
  - [UsernameSetupController.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/presentation/controller/UsernameSetupController.java)
  - [JGitkinsServerAdapter.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/infrastructure/adapter/JGitkinsServerAdapter.java)
  - [JGitkinsServerClient.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/infrastructure/client/JGitkinsServerClient.java)
  - `UsernameUpdateRequest`, `UsernameUpdateResult` 또는 이를 대체할 signup 전용 DTO

즉 endpoint 변경은 서버 API rename 작업이 아니라, 서버/웹 계약 변경 작업으로 다뤄야 한다.

### Step 4. 서버 기존 API 정리
- [ ] `/api/users/me/username` 의 존치 여부 결정
- [ ] 기존 `UserProfileUpdateUseCase.updateUsername` 의 deprecate 또는 제거
- [ ] `AdminUserController` 와 `AdminUserUpdateUseCase` 의 책임 재정의

논의 필요 이슈:
- 하위 호환이 중요하면 `/api/users/me/username` 를 당분간 유지하되 내부적으로 `SignupUseCase.activate` 를 호출하는 thin endpoint 로 둘 수 있다
- 그러나 장기적으로는 signup completion API 와 profile edit API 를 분리해야 한다

### Step 5. 웹 모듈 호출 경로 리팩토링
- [ ] [UsernameSetupController.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/presentation/controller/UsernameSetupController.java) 에서 `updateUsername` 호출 제거
- [ ] `SignupPort.activate` 또는 이에 준하는 포트 호출로 변경
- [ ] [JGitkinsServerAdapter.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/infrastructure/adapter/JGitkinsServerAdapter.java) / [JGitkinsServerClient.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/infrastructure/client/JGitkinsServerClient.java) API 경로 변경

### Step 6. 세션 상태 동기화 재검토
- [ ] 서버 activate 성공 후에만 세션 `ACTIVE` 반영
- [ ] username 저장과 status 활성화가 서버 응답 기준으로 일관되게 적용되는지 검증

논의 포인트:
- 현재 웹은 서버 성공 후 `storeUsername()` + `activateUser()` 를 로컬 세션에 반영한다
- 향후 서버 응답이 `username`, `status` 를 함께 반환하면, 세션도 응답 기반으로 동기화하는 편이 안전하다

---

## 7. 서버/웹 영향 파일

### 서버
- [AdminUserService.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/AdminUserService.java)
- [AdminUserUpdateUseCase.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/port/in/AdminUserUpdateUseCase.java)
- [AdminUserController.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/presentation/api/rest/AdminUserController.java)
- [UserProfileService.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/service/UserProfileService.java)
- [UserProfileUpdateUseCase.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/application/port/in/UserProfileUpdateUseCase.java)
- [UserController.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/presentation/api/rest/UserController.java)
- 관련 테스트
  - `AdminUserServiceTest`
  - `UserProfileServiceTest`
  - `AdminUserControllerTest`
  - `UserControllerTest`

### 웹
- [UsernameSetupController.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/presentation/controller/UsernameSetupController.java)
- [UserPort.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/application/port/out/UserPort.java)
- [JGitkinsServerAdapter.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/infrastructure/adapter/JGitkinsServerAdapter.java)
- [JGitkinsServerClient.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/infrastructure/client/JGitkinsServerClient.java)
- `UsernameUpdateRequest` / `UsernameUpdateResult` 의 rename 또는 signup 전용 DTO 전환
- 세션 관련
  - [SessionSupport.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-web/src/main/java/io/jgitkins/web/presentation/support/SessionSupport.java)

---

## 8. Before / After 예시

### Before
```java
// server
public interface UserProfileUpdateUseCase {
    void updateUsername(String username);
}

// web
UsernameUpdateResult updateUsername(String username);
```

### After
```java
// server
public interface SignupUseCase {
    void activate(String username);
}

// web
SignupActivateResult activate(String username);
```

---

## 9. 개선 사항 검토

### 개선안 1. Signup 응답 DTO 명확화
- 서버가 `username`, `status` 를 함께 반환하면 웹 세션 동기화가 더 명확해진다

### 개선안 2. Profile Edit 와 Signup Completion 분리
- 향후 진짜 프로필 수정 기능이 들어와도 의미 충돌이 없도록 유즈케이스를 분리한다

### 개선안 3. Admin 상태 변경 API 별도 정립
- 관리자에 의한 `BLOCKED`, `ACTIVE` 변경은 signup activate 와 완전히 다른 책임으로 둔다

### 최종 반영 우선순위
- **개선안 2 우선**
- 이유: 이번 리팩토링의 본질은 “username 수정”이 아니라 “signup completion” 임을 코드 구조에 반영하는 것이다

---

## 10. 완료 기준 (DoD)

- 서버에 `SignupUseCase`, `SignupService`, `SignupController` 가 도입된다
- signup 완료 메서드명은 `activate` 로 통일된다
- `AdminUserService.updateUserStatus` 는 signup 책임에서 분리된다
- 웹 모듈은 signup activate API 를 호출하도록 변경된다
- 서버/웹 테스트가 모두 통과한다
- username setup 플로우에서 사용자 상태가 서버/웹 모두 `ACTIVE` 로 일관되게 반영된다

---

## 11. 1차 구현 우선순위

1. 서버의 `SignupUseCase.activate` 도입
2. 기존 `UserProfileService.updateUsername` 로직 이전
3. `SignupController` 및 API 경로 정리
4. 웹 `UsernameSetupController` 와 adapter/client 메서드 rename 및 경로 변경
5. 서버/웹 테스트 갱신

이 순서가 가장 안전합니다. 서버 유즈케이스를 먼저 확정한 뒤 웹 호출을 맞추는 편이 API 표면 변경을 한 번에 정리하기 쉽습니다.

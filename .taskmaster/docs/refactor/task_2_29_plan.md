# Task 2.29: 예외처리 구조 개선

## 1. 배경

계층별 베이스 예외 클래스(`DomainException`, `ApplicationException`, `InfrastructureException`)가 도입되었으나, 실제 처리 파이프라인이 이를 반영하지 않는다.

핵심 문제:
- `GlobalExceptionHandler`가 예외 타입이 아닌 `ErrorCode instanceof` 기반으로 `source`를 추론한다.
- 서비스 계층에서 도메인 예외를 `JgitkinsException`으로 재포장하는 패턴이 남아있다.
- 일반 런타임 예외가 API 응답 경로에 노출된다.
- `ErrorResponse`가 실사용 없이 `ApiResponse`/`ApiError`와 이중화되어 있다.

---

## 2. 설계 원칙

| 예외 타입 | 역할 | 도입 여부 |
|---|---|---|
| `PresentationException` | 요청 바인딩, 인증/인가, 입력 형식 오류 | ⚠️ 검토 필요 (현재 Spring MVC 예외로 처리 중, 불필요 시 제외) |
| `ApplicationException` | 유스케이스 조합, 접근 정책, orchestration 실패 | ✅ 도입 |
| `DomainException` | 도메인 규칙 위반 | ✅ 도입 |
| `InfrastructureException` | DB, JGit, 외부 시스템 실패 | ✅ 도입 |

- **예외 타입** = 발생 계층, **에러 코드** = 문제 의미, **HTTP status** = 외부 계약
- 도메인 예외는 서비스 계층에서 재포장하지 않고 그대로 전달한다.

---

## 3. 구현 계획

### Step 1. 예외 베이스 클래스 정리

**문제 - 현재 `DomainException`에 불필요한 override 존재:**
```java
// Before
public class DomainException extends JgitkinsException {
    @Override
    public ErrorCode getErrorCode() {
        return super.getErrorCode(); // 불필요한 override
    }
}
```

**After:**
```java
// After
public class DomainException extends JgitkinsException {
    public DomainException(ErrorCode errorCode) { super(errorCode); }
    public DomainException(ErrorCode errorCode, String message) { super(errorCode, message); }
    public DomainException(ErrorCode errorCode, String message, Throwable cause) { super(errorCode, message, cause); }
    // getErrorCode() override 제거
}
```

**문제 - `UserAlreadyActivatedException` 컴파일 오류:**
```java
// Before (컴파일 불가 - DomainException에 String만 받는 생성자 없음)
public class UserAlreadyActivatedException extends DomainException {
    public UserAlreadyActivatedException() {
        super("User is already activated"); // ❌ 컴파일 오류
    }
}

// After
public class UserAlreadyActivatedException extends DomainException {
    public UserAlreadyActivatedException() {
        super(DomainErrorCode.USER_ALREADY_ACTIVATED);
    }
}
```

**`PresentationException` 도입 검토:**

현재 Presentation 계층 예외는 Spring MVC 예외(`MethodArgumentNotValidException`, `ConstraintViolationException` 등)로 이미 처리되고 있다.
`PresentationException`이 실제로 필요한 시나리오(직접 throw 지점)가 없다면 도입하지 않는다.

도입 기준:
- 직접 `throw new PresentationException(...)` 하는 지점이 1개 이상 존재하는 경우에만 추가
- 없다면 Spring MVC 예외 처리 핸들러만으로 충분

- [ ] `UserAlreadyActivatedException` 생성자 컴파일 오류 수정
- [ ] `ApplicationException`, `DomainException`, `InfrastructureException` 중복 override 제거
- [ ] `PresentationException` 도입 필요 여부 코드베이스 전수 확인 후 결정
- [ ] `JgitkinsException` abstract 전환 여부 검토
- **완료 기준:** `./gradlew compileJava testClasses` 통과

---

### Step 2. `GlobalExceptionHandler` 개편

**Before - `ErrorCode instanceof`로 source 추론:**
```java
// Before
@ExceptionHandler(JgitkinsException.class)
public ResponseEntity<ApiResponse<Void>> handleJgitkinsException(JgitkinsException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    HttpStatus status = statusMapper.map(errorCode);
    String source = inferSource(errorCode); // ErrorCode 타입으로 추론 ❌
    return buildResponse(errorCode, status, exception.getMessage(), source);
}

private String inferSource(ErrorCode errorCode) {
    if (errorCode instanceof DomainErrorCode) return SOURCE_DOMAIN;
    if (errorCode instanceof InfrastructureErrorCode) return SOURCE_INFRASTRUCTURE;
    if (errorCode instanceof ApplicationErrorCode) return SOURCE_APPLICATION;
    return SOURCE_APPLICATION;
}
```

**After - 예외 타입 기반 분기:**
```java
// After
@ExceptionHandler(DomainException.class)
public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
    HttpStatus status = statusMapper.map(ex.getErrorCode());
    log.warn("Domain exception errorCode=[{}], message=[{}]", ex.getErrorCode().getCode(), ex.getMessage());
    return buildResponse(ex.getErrorCode(), status, ex.getMessage(), SOURCE_DOMAIN);
}

@ExceptionHandler(ApplicationException.class)
public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException ex) {
    HttpStatus status = statusMapper.map(ex.getErrorCode());
    log.warn("Application exception errorCode=[{}], message=[{}]", ex.getErrorCode().getCode(), ex.getMessage());
    return buildResponse(ex.getErrorCode(), status, ex.getMessage(), SOURCE_APPLICATION);
}

@ExceptionHandler(InfrastructureException.class)
public ResponseEntity<ApiResponse<Void>> handleInfrastructureException(InfrastructureException ex) {
    HttpStatus status = statusMapper.map(ex.getErrorCode());
    log.error("Infrastructure exception errorCode=[{}], message=[{}]", ex.getErrorCode().getCode(), ex.getMessage(), ex);
    return buildResponse(ex.getErrorCode(), status, ex.getMessage(), SOURCE_INFRASTRUCTURE);
}

@ExceptionHandler(JgitkinsException.class) // fallback
public ResponseEntity<ApiResponse<Void>> handleJgitkinsException(JgitkinsException ex) {
    ...
}
```

- [ ] 계층별 `@ExceptionHandler` 메서드 구조로 개편
- [ ] `inferSource(ErrorCode)` 제거 또는 fallback 전용으로 축소
- [ ] `CompositeErrorHttpStatusMapper`는 `ErrorCode → HttpStatus` 책임만 유지
- [ ] `GlobalExceptionHandlerTest` 타입 기반 시나리오로 보강
- **완료 기준:** 각 예외 타입별 `source`와 `status`가 기대대로 반환

---

### Step 3. 예외 발생 지점 치환

**Before - `UserProfileService`의 도메인 예외 재포장 패턴:**
```java
// Before
try {
    user.activateWithUsername(requested);
} catch (UserAlreadyActivatedException ex) {
    throw new JgitkinsException(DomainErrorCode.USER_ALREADY_ACTIVATED, ex.getMessage(), ex); // ❌ 재포장
}
```

**After - 재포장 없이 도메인 예외 그대로 전파:**
```java
// After - catch 블록 제거, DomainException이 직접 GlobalExceptionHandler까지 전달됨
user.activateWithUsername(requested); // UserAlreadyActivatedException은 DomainException 하위
```

**JGit/MyBatis Adapter - `InfrastructureException` 통일:**
```java
// Before
try {
    git.push().call();
} catch (GitAPIException e) {
    throw new RuntimeException("Git push failed", e); // ❌ 일반 런타임 예외
}

// After
try {
    git.push().call();
} catch (GitAPIException e) {
    throw new InfrastructureException(InfrastructureErrorCode.GIT_OPERATION_FAILED, "Git push failed", e);
}
```

우선순위 대상:

| 대상 | 작업 |
|---|---|
| `UserProfileService` | 도메인 예외 재포장 패턴 제거 |
| `RunnerManagementService` | 도메인 예외 재포장 패턴 제거 |
| `ActivationValidator` / `BranchCreationValidator` / `RepositoryValidator` | 계층별 예외로 대체 |
| JGit adapter / MyBatis adapter | `InfrastructureException`으로 통일 |

- **완료 기준:** `catch 후 JgitkinsException 재포장` 패턴 제거, adapter 실패가 `InfrastructureException`으로 귀결

---

### Step 4. 응답 모델 정리

- [ ] `ErrorResponse` 삭제 (미사용, `ApiResponse`/`ApiError`로 단일화)
- **완료 기준:** 미사용 DTO 제거, 응답 모델이 하나의 계약으로 수렴

---

## 4. 영향 파일

- `common/exception/JgitkinsException.java`
- `application/exception/ApplicationException.java`
- `domain/exception/DomainException.java`
- `domain/exception/UserAlreadyActivatedException.java`
- `infrastructure/exception/InfrastructureException.java`
- `presentation/advice/GlobalExceptionHandler.java`
- `presentation/exception/ErrorResponse.java`
- `application/service/UserProfileService.java`
- `application/service/RunnerManagementService.java`
- 관련 validator / JGit·MyBatis adapter

---

## 5. 완료 기준 (DoD)

1. 계층별 베이스 예외가 실제 런타임 처리에 반영된다.
2. `GlobalExceptionHandler`가 예외 타입 기준으로 동작한다.
3. 대표 서비스에서 도메인 예외 재포장 패턴이 제거된다.
4. `ErrorResponse`가 제거된다.
5. `GlobalExceptionHandlerTest` 등 주요 테스트가 갱신된다.

---

## 6. 1차 구현 우선순위

1. `UserAlreadyActivatedException` 컴파일 오류 수정
2. `GlobalExceptionHandler` 타입 기반 구조로 재작성
3. `GlobalExceptionHandlerTest` 보강
4. `UserProfileService`, `RunnerManagementService` 재포장 패턴 제거
5. validator → adapter 순서로 치환
6. `ErrorResponse` 제거

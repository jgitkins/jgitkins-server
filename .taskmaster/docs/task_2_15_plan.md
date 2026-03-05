# [Task 2.15] 계층별 ErrorCode 핸들링 전략 수립 및 리팩토링 (최종)

## 1. 개요
현재 프로젝트는 `JgitkinsException`으로 예외 클래스를 단일화하고, 오류 코드를 계층별(`Domain`, `Application`, `Infrastructure`, `Presentation`)로 분리하여 관리하고 있습니다. 이 전략을 헥사고날 아키텍처에 맞게 최적화하고, 상위 계층에서 하위 계층의 예외를 어떻게 처리하고 응답할지에 대한 구체적인 메커니즘을 수립합니다.

## 2. 핵심 설계 원칙

### 2.1 예외 클래스 단일화 (Single Exception Strategy)
- **`JgitkinsException`**: 전 계층(Domain, Application, Infrastructure)에서 유일하게 사용되는 런타임 예외 클래스입니다.
- **계층 구분**: 예외 클래스 타입이 아닌, 내부의 `ErrorCode` 구현체 타입(`instanceof`)으로 발생 계층을 식별합니다.

### 2.2 ErrorCode의 순수성 유지
- **HTTP Status 비포함**: `ErrorCode` 인터페이스 및 구현체(Enum)는 HTTP 상태 코드를 가지지 않습니다.
- **Prefix 제거**: `DOM_`, `APP_` 등 물리적인 접두사를 제거하고 명확한 의미 위주의 명명 규칙을 사용합니다.

### 2.3 책임의 분리 (Separation of Concerns)
- **발생 (Inside)**: 각 계층은 자신에게 맞는 `ErrorCode`를 담아 `JgitkinsException`을 던집니다.
- **변환 (Application)**: Application Service는 필요 시 Infrastructure 예외를 잡아 Application 예외로 번역(Translation)합니다.
- **매핑 (Presentation)**: `GlobalExceptionHandler`와 `HttpStatusMapper`가 최종적으로 API 응답 코드(HttpStatus)를 결정합니다.

## 3. 계층별 ErrorCode 책임 정의

### 3.1 DomainErrorCode
- **책임**: 비즈니스 불변식(Invariant) 및 도메인 규칙 위반 검증.
- **전파**: 도메인 객체 -> 애플리케이션 서비스.

### 3.2 ApplicationErrorCode
- **책임**: 유즈케이스 흐름 제어 실패, 권한 부족, 리소스 부재 (Repository/Branch Not Found 등).
- **전파**: 애플리케이션 서비스 -> 외부 어댑터.

### 3.3 InfrastructureErrorCode
- **책임**: 외부 기술 시스템(DB, Git, FileSystem) 장애 및 I/O 오류.
- **전파**: 어댑터 -> 애플리케이션 서비스 (여기서 APP 코드로 변환되지 않으면 최종적으로 500 에러 처리).

### 3.4 PresentationErrorCode
- **책임**: HTTP 요청 파싱, 데이터 바인딩, 요청 규격 위반 (Spring MVC/Validation 관련).
- **핸들링**: `MethodArgumentNotValidException`, `HttpMessageNotReadableException` 등 Spring 표준 예외를 `PresentationErrorCode`로 변환하여 처리.
- **제외**: `IllegalArgumentException`은 발생 지점이 모호하므로 Presentation 핸들러에서 제외하고, 비즈니스 검증은 `JgitkinsException` 사용을 강제함.

## 4. 상세 리팩토링 계획

### 4.1 계층별 HttpStatusMapper 도입
- `GlobalExceptionHandler`에서 `JgitkinsException`을 잡았을 때, 내부 `ErrorCode` 타입을 분석하여 `HttpStatus`를 반환하는 매퍼 로직 구축.
- **InfrastructureErrorCode** -> 기본적으로 `500 Internal Server Error`.
- **Domain/ApplicationErrorCode** -> 비즈니스 의미에 따라 `400`, `403`, `404`, `409`, `422` 등으로 매핑.

### 4.2 로깅 정책 차별화
- **INF_ERROR**: 시스템 수준의 장애이므로 `ERROR` 레벨과 Stacktrace 기록.
- **DOM/APP_ERROR**: 비즈니스 로직 거절이므로 `WARN` 레벨로 기록하고 상세 원인만 로그에 남김.

### 4.3 Presentation 핸들러 정립
- `GlobalExceptionHandler`의 `handleBadRequest` 대상에서 Spring 기술 예외(Validation, Binding) 외의 일반 Java 예외(IllegalArgumentException 등)를 제거하여 에러 소스(Source)의 정확성 확보.

## 5. 단계별 실행 계획 (Subtasks)

### Step 1. 매핑 컴포넌트 구현
- [x] `HttpStatusMapper` 인터페이스 및 계층별 구현체(`DomainHttpStatusMapper` 등) 작성.
- [x] `GlobalExceptionHandler`에서 매퍼를 사용하도록 리팩토링.

### Step 2. ErrorCode 및 예외 발생 지점 정비
- [x] `Infrastructure` 어댑터들에서 `IOException` 등을 `JgitkinsException(InfrastructureErrorCode.*)`로 래핑하여 던지도록 수정.
- [x] 불필요한 Prefix 제거 및 중복 코드 정리.

### Step 3. 예외 번역(Translation) 적용
- [x] 서비스 계층에서 특정 `INF` 에러를 사용자에게 친숙한 `APP` 에러로 바꾸는 지점 적용.

## 6. 완료 기준 (DoD)
- 모든 `JgitkinsException`이 `GlobalExceptionHandler`를 통해 일관된 `ApiResponse`로 변환됨.
- 로그를 통해 에러가 발생한 계층을 즉시 식별 가능함.
- API 응답이 기술적 상세 정보(Infrastructure 상세)를 유출하지 않고 비즈니스적으로 의미 있는 코드를 제공함.
- `presentation` 소스로 표시되는 에러는 오직 HTTP 요청 바인딩/검증 오류로 제한됨.

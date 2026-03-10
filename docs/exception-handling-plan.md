# Exception Handling Improvement Plan

## 목적

이 문서는 [exception-handling-analysis.md](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/docs/exception-handling-analysis.md)를 기반으로 `jgitkins-server`의 예외처리 구조를 단계적으로 개선하기 위한 실행 계획 문서다.

핵심 목표는 아래 4가지다.

1. 계층별 예외 구조를 실제 동작 구조로 완성한다.
2. 도메인 예외의 의미가 서비스 계층에서 사라지지 않게 한다.
3. 응답 포맷은 `ApiResponse` 체계를 유지하면서 처리 책임을 명확히 한다.
4. 일반 런타임 예외를 점진적으로 줄여 예외 정책의 일관성을 높인다.

## 범위

이번 계획은 아래 영역을 대상으로 한다.

- `common/exception`
- `application/exception`
- `domain/exception`
- `infrastructure/exception`
- `presentation/advice`
- `presentation/common`
- 예외를 직접 발생시키는 대표 서비스와 validator
- 주요 JGit/MyBatis adapter

이번 계획에서 제외하는 범위는 아래와 같다.

- API 응답 포맷의 전면 변경
- `ProblemDetail` 기반 응답으로의 즉시 전환
- 모든 `IllegalArgumentException`의 일괄 제거

## 설계 원칙

### 1. 예외 타입은 계층을 표현한다

- `PresentationException`: 요청 바인딩, 인증/인가 진입점, 프레젠테이션 정책 오류
- `ApplicationException`: 유스케이스 조합, 접근 정책, orchestration 실패
- `DomainException`: 도메인 규칙 위반
- `InfrastructureException`: DB, 파일시스템, JGit, 외부 시스템 실패

### 2. 에러 코드는 문제 의미를 표현한다

- 예외 타입은 발생 계층
- 에러 코드는 오류 의미
- HTTP status는 외부 계약
- message는 API 소비자를 위한 설명

### 3. 도메인 예외는 가능한 한 그대로 유지한다

도메인 계층에서 발생한 예외를 서비스 계층에서 불필요하게 `JgitkinsException`으로 재포장하지 않는다. 계층 의미가 실제 런타임에도 유지되게 하는 것이 목표다.

## 구현 접근 방식

이번 계획은 아래 3단계로 진행한다.

### 1단계. 기반 정리

목표:

- staged 변경을 컴파일 가능하고 일관된 상태로 정리
- 새 베이스 예외 클래스의 형태를 안정화

작업 항목:

1. `UserAlreadyActivatedException` 생성자 오류 수정
2. `ApplicationException`, `DomainException`, `InfrastructureException`의 중복 override 제거
3. 불필요한 import 제거
4. 필요 시 `JgitkinsException`를 `abstract`로 전환할지 검토
5. `PresentationException` 도입 여부 결정 및 골격 추가

산출물:

- 예외 베이스 클래스 정리
- 컴파일 가능한 staged 변경

완료 기준:

- `./gradlew test` 또는 최소 `./gradlew compileJava testClasses` 통과
- 예외 베이스 클래스 간 역할이 문서와 코드에서 일치

### 2단계. 처리 파이프라인 정리

목표:

- `GlobalExceptionHandler`가 예외 타입 기준으로 동작하도록 변경
- source 판단 책임을 `ErrorCode`에서 예외 타입 쪽으로 이동

작업 항목:

1. `GlobalExceptionHandler`를 계층별 handler 메서드 구조로 개편
2. `inferSource(ErrorCode)` 제거 또는 fallback 전용 축소
3. 공통 응답 생성 메서드는 유지하되 source는 handler가 직접 결정
4. `CompositeErrorHttpStatusMapper`는 `ErrorCode -> HttpStatus` 책임만 유지
5. `GlobalExceptionHandlerTest`를 타입 기반 시나리오로 보강

산출물:

- 타입 기반 전역 예외 처리기
- 갱신된 테스트

완료 기준:

- `DomainException`, `ApplicationException`, `InfrastructureException` 각각에 대해 source와 status가 기대대로 반환
- 예외 처리 로직이 `instanceof ErrorCode` 중심 추론에 의존하지 않음

### 3단계. 예외 발생 지점 치환

목표:

- 실제 비즈니스 코드가 계층별 예외 체계를 사용하도록 점진 치환

우선순위 대상:

1. `UserProfileService`
2. `RunnerManagementService`
3. `ActivationValidator`
4. `BranchCreationValidator`
5. `RepositoryValidator`
6. 주요 JGit adapter
7. 주요 MyBatis adapter

작업 항목:

1. 도메인 예외 재포장 제거
2. 범용 `JgitkinsException` 사용 지점을 계층별 예외로 대체
3. 외부 시스템 실패는 `InfrastructureException`으로 통일
4. 유스케이스 정책 실패는 `ApplicationException`으로 통일
5. 잔여 `IllegalStateException` / `RuntimeException` 중 API로 전파되는 지점 선별 교체

산출물:

- 대표 서비스와 validator의 예외 구조 일관화
- adapter의 인프라 예외 표준화

완료 기준:

- 대표 서비스에서 `catch 후 JgitkinsException 재포장` 패턴이 제거 또는 최소화
- adapter 실패가 `InfrastructureException` 또는 그 하위 예외로 귀결

## 세부 작업 명세

### 작업 묶음 A. 예외 클래스 정리

- `JgitkinsException`
- `ApplicationException`
- `DomainException`
- `InfrastructureException`
- `PresentationException` 신규 여부

검토 포인트:

- 공통 생성자 시그니처
- 기본 메시지 정책
- cause 보존 방식
- abstract 여부

### 작업 묶음 B. 에러 코드와 status 정책 정리

- `ApplicationErrorCode`
- `DomainErrorCode`
- `InfrastructureErrorCode`
- `PresentationErrorCode`
- `ErrorHttpStatusMapper` 구현체

검토 포인트:

- 같은 의미의 에러 코드가 계층별로 중복되는지
- `RUNNER_ALREADY_ACTIVED` 같은 네이밍/중복 상태가 유지되어도 되는지
- 4xx/5xx 기준이 계층 의미와 맞는지

### 작업 묶음 C. 응답 모델 정리

- `ApiResponse`
- `ApiError`
- `ErrorResponse`

계획 결정:

- `ApiResponse`/`ApiError` 유지
- `ErrorResponse` 삭제

완료 기준:

- 응답 모델이 하나의 계약으로 수렴
- 미사용 예외 응답 DTO 제거

## 리스크와 대응

### 리스크 1. 예외 타입 변경으로 테스트가 대량 수정될 수 있다

대응:

- 전역 handler 테스트를 먼저 보강한다.
- 서비스 테스트는 행위 기준으로 유지하고 타입 검증은 필요한 곳만 추가한다.

### 리스크 2. 계층 분리가 과도해지면 코드가 장황해질 수 있다

대응:

- 모든 상황에 하위 예외 클래스를 남발하지 않는다.
- 공통 베이스 + 핵심 도메인 예외 중심으로 제한한다.

### 리스크 3. 기존 에러 코드 체계와 예외 타입 체계가 충돌할 수 있다

대응:

- 예외 타입은 계층, 에러 코드는 의미라는 원칙을 유지한다.
- source를 에러 코드에서 추론하지 않게 변경한다.

## 검증 계획

### 자동 검증

- `./gradlew test`
- `./gradlew compileJava testClasses`

### 테스트 보강 대상

- `GlobalExceptionHandlerTest`
- `RunnerManagementServiceTest`
- `UserProfileServiceTest`
- validator 관련 테스트
- 주요 adapter 테스트

### 수동 검증 항목

- 도메인 예외 발생 시 응답 `source=domain`
- 애플리케이션 정책 오류 시 응답 `source=application`
- 인프라 장애 시 응답 `source=infrastructure`
- validation 오류 시 응답 `source=presentation`

## 완료 정의

아래 조건을 만족하면 이번 계획은 완료로 본다.

1. 계층별 베이스 예외가 실제 런타임 처리에 반영된다.
2. 대표 서비스에서 도메인 예외 재포장 패턴이 제거된다.
3. `GlobalExceptionHandler`가 타입 중심 구조로 개편된다.
4. `ErrorResponse`가 제거되거나 미사용 상태가 해소된다.
5. 테스트가 갱신되고 주요 예외 시나리오가 검증된다.

## 권장 실행 순서

1. 예외 베이스 클래스 정리
2. `GlobalExceptionHandler` 개편
3. handler 테스트 보강
4. 대표 서비스 치환
5. validator 치환
6. adapter 치환
7. `ErrorResponse` 제거
8. 잔여 일반 런타임 예외 정리

## 다음 액션

바로 구현을 시작한다면 가장 먼저 할 작업은 아래 3가지다.

1. `UserAlreadyActivatedException` 컴파일 오류 수정
2. `GlobalExceptionHandler`를 타입 기반 구조로 재작성
3. `UserProfileService`, `RunnerManagementService`의 재포장 패턴 제거

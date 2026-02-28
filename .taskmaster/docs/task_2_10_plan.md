# [Task 2.10] 예외 처리 전략 (시니어 재검토본, 2026-03-01)

## 0. 결론 요약
현재 코드베이스와 운영 리스크를 기준으로, **예외 클래스는 단일화(`JgitkinsException`)**하고 **오류코드는 계층별(`REQ/APP/DOM/INF`)로 분리**하는 전략을 채택한다.

핵심 이유:
1. 타입(예외 클래스)과 의미(오류코드)가 이중으로 계층을 표현하면 중복 복잡도가 커진다.
2. 현재 구현처럼 `ApplicationException`이 타 계층 코드를 받을 수 있으면, 계층별 예외 클래스 분리의 실익이 낮다.
3. API 계약은 상태코드 + 오류코드로 소비되므로, 운영/관측/호환성 관점에서 코드 체계가 더 중요하다.

---

## 1. 근거 자료 (신뢰/최신 우선)
1. RFC 9457 (Problem Details for HTTP APIs, 2023)  
   https://www.rfc-editor.org/rfc/rfc9457
2. RFC 9110 (HTTP Semantics, 2022)  
   https://www.rfc-editor.org/rfc/rfc9110
3. Spring Framework Reference - REST 예외 처리/ProblemDetail  
   https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html
4. Spring `ResponseEntityExceptionHandler` Javadoc  
   https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/ResponseEntityExceptionHandler.html
5. OWASP Error Handling Cheat Sheet  
   https://cheatsheetseries.owasp.org/cheatsheets/Error_Handling_Cheat_Sheet.html

---

## 2. 대안 3가지 검토
### 대안 A: 계층별 예외 클래스 유지 (`Domain/Application/InfrastructureException`)
- 장점: 계층 경계를 타입으로 강하게 표현 가능
- 단점: 번역 코드 증가, 중복/누수 가능성 증가, 지금 구조와 충돌(`ApplicationException`이 타 계층 코드 수용)

### 대안 B: 단일 예외 + 계층별 오류코드 (채택)
- 장점: 단순/일관, 응답 계약 중심 운영에 유리, 점진 마이그레이션 용이
- 단점: 계층 경계 강제력이 약해질 수 있음(테스트/아키텍처 룰로 보완 필요)

### 대안 C: Spring ProblemDetail 전면 전환
- 장점: RFC 9457 정합성 높음, 표준화 우수
- 단점: 현재 `ApiResponse.failure(...)` 계약과 충돌, 클라이언트/테스트 대규모 변경 필요

**선택:** 대안 B  
**보완:** 내부적으로는 `ProblemDetail` 철학(구조화된 오류 메타데이터)을 참고하되, 외부 계약은 `ApiResponse` 유지.

---

## 3. 최종 아키텍처 규칙
### 3.1 예외 구조
1. 비즈니스/시스템 예외는 `JgitkinsException` 단일 계층으로 통일  
   (권장 위치: `io.jgitkins.server.common.exception.JgitkinsException`)
2. Spring 표현부 예외(`MethodArgumentNotValidException` 등)는 `@RestControllerAdvice`에서 직접 처리
3. `IllegalArgumentException`/`RuntimeException` 무분별 throw 금지 (특히 application/infrastructure)

### 3.2 오류코드 구조
`ErrorCode` 인터페이스 + prefix 규칙:  
(권장 위치: `io.jgitkins.server.common.error.ErrorCode`)
1. `REQ_*`: 요청 파싱/검증 실패 (표현부 책임)
2. `APP_*`: 유즈케이스 규칙 위반 (애플리케이션 책임)
3. `DOM_*`: 도메인 불변식/규칙 위반 (도메인 책임)
4. `INF_*`: 외부 시스템/기술 장애 (인프라 책임)

### 3.3 400 충돌(표현부 vs 유즈케이스) 해결 규칙
HTTP 상태가 같아도 코드로 구분한다.
1. 표현부 400 -> `REQ_*`
2. 유즈케이스 400 -> `APP_BAD_REQUEST` 또는 구체 `APP_*`
3. 도메인 규칙은 기본적으로 409/422 우선, 진짜 문법/입력 문제일 때만 400

### 3.4 HTTP 매핑 원칙 (RFC 9110 정렬)
1. 400: 문법/형식/요청 파라미터 문제 (`REQ_*`, 일부 `APP_*`)
2. 401/403: 인증/인가
3. 404: 리소스 부재
4. 409: 상태 충돌
5. 422: 의미는 이해했으나 처리 불가(도메인/업무 규칙 위반)
6. 500: 인프라/예상치 못한 서버 오류

---

## 4. 기존 `ErrorCode(enum)` 처리 전략 (Best Practice)
즉시 삭제하지 않고 **점진 폐기**한다.

1. 기존 enum은 `@Deprecated` + 신규 추가 금지
2. `LegacyErrorCodeBridge`로 신규 코드(`REQ/APP/DOM/INF`)를 기존 응답 코드에 매핑
3. 외부 API 응답 계약은 브리지로 호환 유지
4. 내부 신규 개발은 계층별 코드만 사용
5. 참조 0건 + 소비자 마이그레이션 완료 후 제거

---

## 5. 구현 규칙 (코드 레벨)
1. 예외 throw 시 반드시 `ErrorCode` 포함
2. `GlobalExceptionHandler`는 다음 3축 처리
   - `JgitkinsException`
   - Spring 표현부 예외군
   - fallback `Exception` (내부 코드 노출 금지)
3. 메시지 정책
   - 사용자 노출 메시지: 안전/간결
   - 내부 상세 원인: 로그(상관관계 ID 포함)
4. 로깅 정책
   - 4xx: `warn`
   - 5xx: `error` + stacktrace

---

## 6. 테스트/가드레일
1. 계약 테스트: 400/401/403/404/409/422/500 응답 포맷+코드 검증
2. 컨벤션 테스트: Controller 밖에서 `REQ_*` throw 금지
3. 아키텍처 테스트(ArchUnit 권장):
   - domain -> spring 의존 금지
   - application/infrastructure에서 표현부 예외 타입 사용 금지
4. 회귀 테스트: 레거시 코드 브리지 매핑 일관성 검증

---

## 7. 개선사항 3가지와 선택 반영
1. `source` 필드 추가 (`presentation|application|domain|infrastructure`)
2. `correlationId`를 에러 응답에 포함해 추적성 강화
3. RFC 9457 호환용 확장 필드(`type`, `instance`)를 내부적으로 준비

### 선택 반영
선택: **1번(`source` 필드 추가)**
- 이유: 현재 구조에서 가장 낮은 비용으로 “같은 400의 원인 구분”을 즉시 강화 가능
- 반영 계획: `ApiError` 확장 후 핸들러에서 소스 주입 (기존 필드와 역호환 유지)

---

## 8. 실행 체크리스트
- [x] `ErrorCode` 인터페이스 도입
- [x] `GlobalExceptionHandler`의 Spring 예외 명시 처리
- [x] 상태 매핑 분리(`*HttpStatusMapper` + Composite)
- [x] `JgitkinsException` 단일화로 예외 계층 정리 완료
- [x] `LegacyErrorCodeBridge` 도입 및 기존 enum `@Deprecated`
- [x] `source` 필드 도입 + 테스트 반영
- [~] Infrastructure adapter의 기술 예외 래핑 일괄화 (핵심 어댑터 일부 반영, 전수 적용은 후속)

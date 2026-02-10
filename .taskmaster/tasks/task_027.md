# Task ID: 27

**Title:** OAuth 인증 실패/요청 검증 개선

**Status:** pending

**Dependencies:** None

**Priority:** high

**Description:** OAuth 로그인 API의 요청 검증과 예외 매핑을 강화해 오류 응답을 일관화한다.

**Details:**

컨트롤러 요청 검증(@Valid) 및 도메인 예외를 4xx 계열로 매핑해 클라이언트가 원인을 명확히 알 수 있도록 개선한다.

**Test Strategy:**

WebMvcTest 기반 400/401/422 시나리오 및 오류 응답 포맷 검증

## Subtasks

### 27.1. OAuth 로그인 요청 DTO 검증 추가

**Status:** pending  
**Dependencies:** None  

OAuthLoginRequest 필드 유효성 검증과 @Valid 적용

**Details:**

provider/subject/email 등 필수 필드 검증을 추가하고 누락/형식 오류 시 400 응답을 보장

### 27.2. OAuth 실패 예외를 클라이언트 오류로 매핑

**Status:** pending  
**Dependencies:** 27.1  

잘못된 provider/sub 등 인증 실패를 4xx로 일관 처리

**Details:**

전역 예외 핸들러 또는 전용 예외 타입을 통해 OAuth 인증 실패를 의미 있는 오류 코드/메시지로 응답

# 리팩토링 계획서

### 제목
- **리팩토링 계획**: JgitkinsException 추상 클래스 전환 및 예외 처리 구조 정돈

### 배경 (왜?)
- `JgitkinsException`은 시스템의 최상위 커스텀 예외 클래스이나, 현재 concrete 클래스로 구현되어 있어 직접 인스턴스화가 가능한 상태임.
- 헥사고날 아키텍처 원칙에 따라 각 계층(Domain, Application, Infrastructure)은 고유의 책임을 가지며, 예외 역시 해당 계층의 특성을 반영한 하위 클래스(`DomainException`, `ApplicationException`, `InfrastructureException`)를 사용해야 함.
- 최상위 클래스의 직접 사용을 방지하여 예외 계층 구조의 엄격성을 높이고, `GlobalExceptionHandler`에서의 처리 로직을 보다 명확하게 정돈하고자 함.

### 목표 (Goals)
- `JgitkinsException`을 `abstract` 클래스로 전환하여 직접적인 인스턴스 생성을 차단함.
- `GlobalExceptionHandler` 내에서 최상위 예외에 의존하는 불필요한 로직이 있다면 이를 정비하고, 하위 계층별 예외 핸들러가 각자의 책임을 명확히 수행하도록 구조를 최적화함.
- 전반적인 예외 처리 흐름의 정합성을 확보하고 유지보수성을 향상시킴.

### 범위 (Scope)
- **수정 대상**:
    - `io.jgitkins.server.common.exception.JgitkinsException` (추상화 적용)
    - `io.jgitkins.server.presentation.advice.GlobalExceptionHandler` (구조 정돈 및 Fallback 로직 검토)
- **수정 제외 대상**:
    - 비즈니스 로직 및 외부 연동 관련 기능.
    - 예외 메시지 번역 및 HTTP 상태 코드 매핑의 세부 수치.

### 계획 (Plan)
- **단계 1**: `JgitkinsException` 클래스에 `abstract` 키워드를 추가하여 추상 클래스로 전환함.
- **단계 2**: 프로젝트 전체 소스 코드를 대상으로 `new JgitkinsException` 또는 `throw new JgitkinsException` 형태의 직접 사용 사례를 전수 조사함.
- **단계 3**: 직접 사용 사례 발견 시, 해당 로직이 속한 계층에 따라 `DomainException`, `ApplicationException`, 또는 `InfrastructureException` 중 적절한 타입으로 치환함.
- **단계 4**: `GlobalExceptionHandler`를 분석하여 `JgitkinsException`을 직접 인자로 받는 핸들러가 비효율적으로 동작하거나 하위 예외 처리를 방해하는지 확인하고 이를 최적화함.
- **단계 5**: 단위 테스트 및 API 호출 테스트를 통해 예외 발생 시 의도한 계층의 핸들러가 동작하고 일관된 응답 포맷이 반환되는지 검증함.

### 기대효과 (Expected Benefits)
- 아키텍처 원칙에 부합하는 견고한 예외 계층 구조를 확립함.
- 잘못된 예외 클래스 사용으로 인한 의미적 모호성을 사전에 방지함.
- 전역 예외 처리기의 구조가 간결해지며, 새로운 예외 타입 추가 시 확장성이 개선됨.

### 예시

#### AS-IS
```java
// 직접 인스턴스화가 가능함
public class JgitkinsException extends RuntimeException { ... }

// 사용처에서 모호하게 사용될 여지가 있음
throw new JgitkinsException(ErrorCode.INTERNAL_ERROR);
```

#### TO-BE
```java
// 추상 클래스로 선언하여 직접 생성을 차단함
public abstract class JgitkinsException extends RuntimeException { ... }

// 반드시 하위 계층 예외를 사용하도록 강제됨
throw new InfrastructureException(InfrastructureErrorCode.PERSISTENCE_OPERATION_FAILED, "...", e);
```

### 주의사항
- **포맷팅 금지**: 리팩토링 과정에서 코드의 기능적/구조적 개선에 집중하며, 단순 포맷팅 수정은 지양함.
- **기존 기능 보장**: 예외 타입 변경이 기존의 에러 응답(HttpStatus, ErrorCode)에 영향을 주지 않도록 주의 깊게 변경함.
- **계획우선**: 본 계획서가 확정된 후 구현을 시작하며, 단계별 절차를 준수함.
- **문서체규약**: 모든 문장은 "~~함", "~~함" 형태의 격식 있는 문어체로 작성하였음.

### 결론
- `JgitkinsException`을 `abstract`로 전환하여 직접 인스턴스화를 원천 차단하고 아키텍처 정합성을 확보함.
- `PresentationException`을 신설하여 프레젠테이션 계층의 독자적인 예외 표현력을 갖춤.
- `GlobalExceptionHandler`를 `JgitkinsException` 타입 기반으로 통합하여 하위 클래스에 상관없이 일관된 전처리 및 소스(Source) 판별 로직을 원격함.
- 테스트 시나리오에서 발생한 빌드 오류를 수정하고, 규약에 따라 불필요한 포맷팅을 제거하여 리팩토링 본연의 목적에 집중함.

작성 완료함.

# 핫픽스 계획서

### 제목
- **핫픽스 대상**: 조직 조회 API NullPointerException 추적 및 방어 계획서

### 배경 (현상 및 원인)
- `repositories/new` 진입 시 서버 로그에 `Unexpected exception` 과 함께 `java.lang.NullPointerException: Cannot read field "w1" because "src" is null` 오류가 기록되고 있음.
- 해당 오류가 발생하면 저장소 생성 초기화 과정에서 organization 목록 또는 owner 초기화 데이터가 정상적으로 구성되지 않아 화면 기능이 연쇄적으로 손상될 수 있음.
- 현재 분석 기준으로 `repositories/new` 초기화는 조직 목록 조회 경로를 타고 있으며, 실제 장애 지점은 `GET /api/organizes` 또는 `GET /api/internal/organizes` 응답 생성 경로일 가능성이 높다고 판단하였음.
- 다만 현재 확보된 로그는 전역 예외 처리기 기준 로그만 존재하며, 실제 예외 발생 클래스와 라인은 추가 재현이 필요한 상태임.
- 즉, 본 이슈는 증상은 확인되었으나 원인 지점이 완전히 고정되지 않은 상태의 서버 핫픽스로 분류하였음.

### 목표 (Goals)
- 조직 조회 API 호출 시 발생하는 NullPointerException 의 실제 발생 지점을 식별함.
- 동일 경로에서 null 데이터가 유입되더라도 전역 500 에러로 확대되지 않도록 최소 방어 로직을 수립함.
- 조직 조회 응답 계약이 정상적으로 유지되어 `repositories/new` 초기화 실패가 재발하지 않도록 함.

### 범위 (Scope)
- **수정 대상**:
    - 조직 조회 API 재현 경로 및 controller-service-mapper-response 흐름
    - null 유입 가능성이 있는 mapper, DTO 변환, 직렬화 경로
    - 본 이슈와 직접 연결되는 예외 재현 및 회귀 테스트
- **수정 제외 대상**:
    - 예외 처리 아키텍처 전면 재설계
    - 조직 도메인 전체 리팩토링
    - unrelated API 응답 포맷 정리

### 계획 (Plan)
- **단계 1**: 재현 경로를 고정함.
    - `repositories/new` 진입 시 서버에서 실제로 어떤 조직 조회 API 가 호출되는지 확인함.
    - `GET /api/organizes`, `GET /api/internal/organizes` 를 각각 직접 호출하여 동일 예외가 재현되는지 비교함.
    - 예외 스택의 실제 발생 클래스와 라인을 확보하여 controller, service, mapper, serializer 중 어느 단계인지 확정함.

- **단계 2**: 원인 후보를 3가지로 분류하여 비교 검토함.
    - **방안 1**: 조직 목록 데이터 내부에 null aggregate 또는 null 필드가 존재하여 mapper 접근 중 예외가 발생하는지 검토함.
    - **방안 2**: DTO 직렬화 과정에서 특정 타입의 null 값 처리 문제로 예외가 발생하는지 검토함.
    - **방안 3**: 보안/현재 사용자 연계 로직이 빈 값을 전달하여 service 결과 구성 단계에서 예외가 발생하는지 검토함.
    - 실제 스택 추적 결과를 기준으로 가장 직접적인 경로를 채택하고, 추정성 수정은 배제함.

- **단계 3**: 최소 수정 방어 전략을 수립함.
    - null aggregate 또는 null 필드가 실제 원인일 경우 mapper 또는 서비스에서 조기 필터링 또는 명시적 fallback 을 적용함.
    - 직렬화 단계가 원인일 경우 DTO 생성 시점에서 null-safe 값을 보장하도록 수정함.
    - 장애를 숨기는 포괄 try-catch 는 지양하고, 재현된 원인 경로에만 한정된 방어 로직을 적용함.

- **단계 4**: 계약 보강 테스트를 정의함.
    - 문제를 재현한 동일 데이터 조건에서 조직 목록 API 가 500 없이 정상 응답하는 테스트를 추가함.
    - null 데이터 유입이 가능한 경우를 명시적으로 테스트하여 회귀를 방지함.
    - `repositories/new` 초기화에 영향을 주는 최소 시나리오가 정상 동작하는지 연동 관점으로 확인함.

- **단계 5**: 후속 개선 항목을 정리함.
    - 본 핫픽스는 실제 예외 지점에 대한 최소 방어와 계약 복구에 집중함.
    - 개선 후보는 다음 3가지로 정리함.
    - 조직 조회 응답 DTO 생성 경로의 null-safe 정책 문서화가 필요함.
    - 전역 예외 로그에 실제 root cause 라인을 더 쉽게 확인할 수 있는 보강이 필요함.
    - 조직 조회 API 에 대한 contract test 범위 확장이 필요함.
    - 이 중 즉시 반영 가치가 가장 높은 항목은 contract test 보강이라고 판단하였음.

### 기대효과 (Expected Benefits)
- 조직 조회 API 호출 시 500 에러가 제거되어 저장소 생성 초기화 흐름이 정상화됨.
- 실제 원인 지점이 코드와 테스트로 고정되어 동일 증상의 재발 가능성이 낮아짐.
- 장애가 전역 예외로 확대되는 상황이 줄어들어 운영 안정성이 향상됨.

### 예시 (수정 전/후 코드 스니펫)

#### AS-IS (버그 발생 구조)
```java
public List<OrganizeCreationResult> getOrganizes() {
    return organizePort.findAll().stream()
            .map(organizeApplicationMapper::toDto)
            .toList();
}
```

#### TO-BE (핫픽스 적용 구조)
```java
public List<OrganizeCreationResult> getOrganizes() {
    return organizePort.findAll().stream()
            .filter(Objects::nonNull)
            .map(organizeApplicationMapper::toDto)
            .filter(Objects::nonNull)
            .toList();
}
```

### 주의사항
- **포맷팅 금지**: 예외 추적과 무관한 리팩토링 및 파일 정리는 수행하지 않음.
- **기존 기능 보장**: 조직 목록 정상 응답 외에 organization 생성, organization 조회, 저장소 생성 초기화 흐름이 함께 손상되지 않는지 확인함.
- **계획 우선**: 실제 스택을 확보하기 전 추정성 수정으로 여러 레이어를 동시에 건드리지 않음.
- **예시 전체 나열**: 문제 발생 가능 구간의 BEFORE/AFTER 흐름을 전체 구조로 제시하였음.
- **문서체 규약**:
    - 모든 문장은 공식 문어체로 작성함.
    - 문장 끝은 `~~함` 또는 `~~하였음` 형태를 유지함.
    - 근거 없는 단정은 피하고, 재현 기반 계획으로 기술함.

### 결론 (추후 작성)
- 본 문서는 조직 조회 API NullPointerException 의 실제 원인 추적과 최소 방어 전략 수립을 위해 작성함.
- 현재 단계에서는 구현을 진행하지 않았으며, 재현 및 검증 기준 정리를 완료하였음.

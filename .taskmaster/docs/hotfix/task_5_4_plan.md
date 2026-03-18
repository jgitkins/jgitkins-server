# 핫픽스 계획서

### 제목
- **핫픽스 대상**: repository 생성 화면 owner/organization 초기화 회귀 검증 계획서

### 배경 (현상 및 원인)
- 현재 장애는 단일 증상처럼 보이지만, 실제로는 owner namespace 표시, organization 목록 초기화, 초기화 실패 시 fallback 동작이 서로 연결된 복합 회귀 형태임.
- `repositories/new` 화면은 로그인 사용자 정보, 세션 username, organization 조회 결과, model attribute 계약이 동시에 맞아야 정상 동작하는 구조임.
- 따라서 owner 표시만 복구하거나 서버 예외만 막는 수준으로는 회귀를 충분히 차단하기 어렵고, 초기화 전체 흐름에 대한 검증 시나리오가 필요함.
- 현재 테스트는 저장소 생성 화면의 model attribute 계약, 개인 owner namespace 표시, 접근 가능한 organization 목록 노출 여부를 충분히 보장하지 못하는 상태로 판단하였음.

### 목표 (Goals)
- `repositories/new` 초기화 흐름의 핵심 시나리오를 테스트로 고정하여 핫픽스 이후 회귀를 차단함.
- 개인 owner namespace, organization owner 선택, 초기화 실패 fallback 동작을 각각 분리하여 검증함.
- 최소 범위 테스트 보강으로 기능 안정성을 높이되, 핫픽스 속도를 저해하지 않도록 함.

### 범위 (Scope)
- **수정 대상**:
    - `repositories/new` 진입 및 초기화 관련 controller/facade/support 테스트
    - organization 목록 조회 실패 시 fallback 또는 error message 처리 검증
    - owner namespace 및 organization 선택 관련 회귀 시나리오
- **수정 제외 대상**:
    - E2E 자동화 프레임워크 신규 도입
    - 저장소 생성 전체 기능 통합 테스트 전면 재구성
    - unrelated 화면 테스트 확대

### 계획 (Plan)
- **단계 1**: 회귀 위험 시나리오를 분해함.
    - 다음 3가지 핵심 시나리오를 기준으로 검증 대상을 확정함.
    - **시나리오 1**: 로그인 사용자 개인 namespace 가 owner 영역과 preview 영역에 정확히 반영되는지 확인함.
    - **시나리오 2**: 접근 가능한 organization 목록이 owner 전환 시 정상 노출되는지 확인함.
    - **시나리오 3**: organization 조회 실패 또는 빈 결과 시 화면이 500 없이 fallback 메시지 또는 빈 목록으로 안전하게 동작하는지 확인함.

- **단계 2**: 테스트 접근 방식을 3가지로 비교 검토함.
    - **방안 1**: controller 단 model attribute 검증 테스트 위주로 보강하는 방안을 검토함.
    - **방안 2**: facade/service 단 계산 로직 테스트 중심으로 보강하는 방안을 검토함.
    - **방안 3**: 화면 렌더링까지 포함한 MVC slice 테스트를 추가하는 방안을 검토함.
    - 핫픽스 목적상 속도와 회귀 탐지력을 함께 확보하기 위해 **방안 1 + 방안 2 조합**을 우선 채택함.

- **단계 3**: 최소 테스트 세트를 설계함.
    - `GET /repositories/new` 에 대해 owner 관련 model attribute 존재 여부를 검증함.
    - 개인 owner 기준 owner slug 가 실제 username 값인지 검증함.
    - organization owner 선택 시 owner slug 와 organization 목록이 기대값으로 반영되는지 검증함.
    - organization 조회 실패 시 `organizeError` 또는 안전한 빈 목록 처리가 동작하는지 검증함.

- **단계 4**: 회귀 테스트 실행 범위를 정의함.
    - 핫픽스 속도를 고려하여 관련 controller/facade/support 테스트만 우선 실행함.
    - 필요 시 조직 조회 API 의 최소 controller/service 테스트를 함께 수행함.
    - 실패 원인을 빠르게 분리할 수 있도록 테스트 명명과 assertion 대상을 명확히 유지함.

- **단계 5**: 후속 개선 항목을 정리함.
    - 본 핫픽스는 핵심 초기화 흐름 회귀 차단에 집중함.
    - 개선 후보는 다음 3가지로 정리함.
    - 화면 렌더링 기반 MVC 통합 테스트를 후속으로 확대할 필요가 있음.
    - owner namespace 와 display name 개념을 테스트 fixture 레벨에서 명확히 분리할 필요가 있음.
    - organization 조회 실패 fallback 정책을 사용자 메시지 규칙과 함께 문서화할 필요가 있음.
    - 이 중 즉시 반영 가치가 가장 높은 항목은 owner namespace 와 display name 분리 검증이라고 판단하였음.

### 기대효과 (Expected Benefits)
- 핫픽스 이후 `repositories/new` 관련 핵심 회귀가 테스트로 고정되어 안정성이 향상됨.
- owner 표시와 organization 목록 초기화가 다시 깨지더라도 빠르게 탐지 가능해짐.
- 화면 초기화 실패가 사용자에게 500 으로 노출되는 상황을 줄일 수 있음.

### 예시 (수정 전/후 코드 스니펫)

#### AS-IS (버그 발생 구조)
```java
@Test
void createRepository_returnsFormViewWhenBindingHasErrors() {
    RepositoryCreateForm form = new RepositoryCreateForm();
    BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.rejectValue("repoName", "NotBlank", "Repository name is required.");
    Model model = new ConcurrentModel();

    String view = controller.createRepository(form, bindingResult, null, model);

    assertEquals("repositories/new", view);
}
```

#### TO-BE (핫픽스 적용 구조)
```java
@Test
void newRepository_populatesOwnerNamespaceAndOrganizations() {
    Model model = new ConcurrentModel();
    when(repositoryFacadeUseCase.getInitData(any(), any(), any())).thenReturn(context);

    String view = controller.newRepository(authentication, model);

    assertEquals("repositories/new", view);
    assertEquals("alzar", model.getAttribute("ownerSlug"));
    assertNotNull(model.getAttribute("organizes"));
}
```

### 주의사항
- **포맷팅 금지**: 테스트 보강 과정에서 불필요한 테스트 전면 정리나 네이밍 개편은 수행하지 않음.
- **기존 기능 보장**: 핫픽스와 직접 관련된 생성 화면 초기화 경로만 최소 범위로 검증함.
- **계획 우선**: 실제 구현 전 검증 시나리오를 먼저 고정하여 임시 수정의 재발을 방지함.
- **예시 전체 나열**: 회귀 테스트의 BEFORE/AFTER 구조를 전체 흐름으로 제시하였음.
- **문서체 규약**:
    - 모든 문장은 공식 문어체로 작성함.
    - 문장 끝은 `~~함` 또는 `~~하였음` 형태를 유지함.
    - 간결하되, 회귀 검증 목적과 범위는 빠짐없이 기술함.

### 결론 (추후 작성)
- 본 문서는 repository 생성 화면 초기화 회귀 검증 범위를 정의하기 위해 작성함.
- 현재 단계에서는 구현을 진행하지 않았으며, 테스트 전략과 최소 검증 세트 수립을 완료하였음.

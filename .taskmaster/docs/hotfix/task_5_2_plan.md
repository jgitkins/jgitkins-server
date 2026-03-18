# 핫픽스 계획서

### 제목
- **핫픽스 대상**: `repositories/new` owner namespace 초기화 계약 복구 계획서

### 배경 (현상 및 원인)
- `http://localhost:8081/repositories/new` 접근 시 owner 영역에 로그인 사용자 namespace 가 표시되지 않는 현상이 발생하였음.
- 해당 화면은 저장소 생성 경로의 시작점이므로, owner namespace 가 비어 있으면 사용자는 저장소 생성 대상과 최종 URL 을 신뢰하기 어려운 상태가 됨.
- 현재 `jgitkins-web` 의 생성 화면 템플릿은 `ownerLabel`, `ownerSlug`, `organizes`, `organizeError` 를 평면 모델 속성으로 직접 참조하고 있음.
- 반면 `RepositoryViewSupport.populateCreateModel(...)` 는 리팩토링 이후 `form`, `data`, `formError` 만 모델에 주입하고 있어 템플릿과 모델 계약이 불일치하는 상태임.
- 추가로 개인 owner 의 namespace 는 표시용 이름이 아니라 실제 로그인 사용자 username 기준으로 처리되어야 하나, 현재 흐름은 OAuth 표시명과 namespace 를 분리하지 못한 상태로 판단하였음.

### 목표 (Goals)
- 저장소 생성 화면 진입 시 owner 영역에 실제 로그인 사용자 namespace 가 즉시 노출되도록 복구함.
- 템플릿과 모델 간 계약을 다시 일치시켜 owner 관련 값이 누락되지 않도록 보장함.
- 표시용 이름과 실제 namespace 소스를 분리하여 향후 동일 회귀가 재발하지 않도록 정리함.

### 범위 (Scope)
- **수정 대상**:
    - `repositories/new` 화면 렌더링에 사용되는 model 구성 경로
    - owner label 과 owner slug 산출 책임 경로
    - 개인 owner 값의 namespace 소스 결정 규칙
- **수정 제외 대상**:
    - 저장소 생성 화면 UI 전면 개편
    - owner 선택 UX 개선 및 신규 기능 추가
    - OAuth 로그인 구조 전면 개편

### 계획 (Plan)
- **단계 1**: 현상 재확인 및 모델 계약 분석을 수행함.
    - 템플릿이 참조하는 속성과 controller/view support 가 주입하는 속성을 대조하여 실제 누락 지점을 확정함.
    - 다음 3가지 수정 방안을 비교 검토함.
    - **방안 1**: 템플릿을 `data.ownerSlug` 형태로 전면 수정하는 방안을 검토함.
    - **방안 2**: view support 에서 기존 평면 속성을 다시 노출하여 템플릿 계약을 복구하는 방안을 검토함.
    - **방안 3**: 생성 화면 전용 view model 을 새로 도입하여 controller, support, template 를 함께 정리하는 방안을 검토함.
    - 장애 복구 속도와 변경 최소화 관점에서 **방안 2**를 우선 채택함.

- **단계 2**: owner namespace 소스 분리 전략을 확정함.
    - 개인 owner 는 OAuth 표시명이 아니라 세션에 저장된 실제 username 을 namespace 소스로 사용하도록 설계함.
    - ownerLabel 은 사용자 표시명 또는 보조 식별자 역할을 유지하고, ownerSlug 는 저장소 경로용 namespace 로 분리함.
    - 이 과정에서 개인 owner 와 organization owner 의 선택 로직이 혼재되지 않도록 책임 경계를 명확히 함.

- **단계 3**: 최소 수정 구현 범위를 정의함.
    - 생성 화면 진입 시 model 에 `ownerLabel`, `ownerSlug`, `organizes`, `organizeError` 가 안정적으로 주입되도록 복구함.
    - 기존 `data` 객체 유지 여부는 하위 코드 영향도를 확인한 뒤 결정하되, 핫픽스 범위에서는 템플릿이 즉시 사용하는 속성 복구를 우선함.
    - 개인 owner slug 산출 시 null 또는 빈 username 에 대한 fallback 정책도 함께 정의함.

- **단계 4**: 검증 전략을 수립함.
    - `GET /repositories/new` 진입 시 model attribute 검증 테스트를 보강함.
    - 로그인 사용자 username 이 owner slug 로 반영되는지 확인하는 회귀 테스트를 추가함.
    - organization 선택 시 owner slug 가 organization namespace 로 전환되는지 최소 시나리오를 점검함.

- **단계 5**: 후속 개선 항목을 정리함.
    - 본 핫픽스는 계약 복구와 namespace 분리에 집중하고, 구조 개선은 후속 작업으로 분리함.
    - 개선 후보는 다음 3가지로 정리함.
    - 생성 화면 전용 view model 도입이 필요함.
    - owner label/slug 네이밍을 더 명시적으로 정리할 필요가 있음.
    - controller 테스트에서 model 계약 검증 범위를 확대할 필요가 있음.
    - 이 중 즉시 반영 가치가 가장 높은 항목은 model 계약 검증 테스트 보강이라고 판단하였음.

### 기대효과 (Expected Benefits)
- 저장소 생성 화면에서 owner namespace 가 즉시 복구되어 사용자 혼란이 해소됨.
- 템플릿과 모델 계약 불일치로 인한 null 렌더링 문제가 제거됨.
- owner 표시용 값과 실제 namespace 값이 분리되어 이후 생성 경로 안정성이 향상됨.

### 예시 (수정 전/후 코드 스니펫)

#### AS-IS (버그 발생 구조)
```java
public void populateCreateModel(Model model,
        RepositoryCreateContext context,
        RepositoryCreateForm form,
        String formError) {
    model.addAttribute("form", form);
    model.addAttribute("data", context);
    model.addAttribute("formError", formError);
}
```

#### TO-BE (핫픽스 적용 구조)
```java
public void populateCreateModel(Model model,
        RepositoryCreateContext context,
        RepositoryCreateForm form,
        String formError) {
    model.addAttribute("form", form);
    model.addAttribute("data", context);
    model.addAttribute("ownerLabel", context.ownerLabel());
    model.addAttribute("ownerSlug", context.ownerSlug());
    model.addAttribute("organizes", context.organizes());
    model.addAttribute("organizeError", context.organizeError());
    model.addAttribute("formError", formError);
}
```

### 주의사항
- **포맷팅 금지**: 본 핫픽스와 무관한 리팩토링성 정렬 및 대규모 rename 은 수행하지 않음.
- **기존 기능 보장**: owner 표시 복구 외에 organization 선택, form submit, validation 흐름이 기존과 동일하게 동작하는지 확인함.
- **계획 우선**: 화면 표출 증상만 보고 임시 값 하드코딩을 적용하지 않고, 실제 namespace 소스를 분리하여 처리함.
- **예시 전체 나열**: 모델 주입 흐름의 BEFORE/AFTER 전체 구조를 제시하였음.
- **문서체 규약**:
    - 모든 문장은 공식 문어체로 작성함.
    - 문장 끝은 `~~함` 또는 `~~하였음` 형태로 유지함.
    - 추측성 표현은 줄이고, 확인된 사실과 계획 중심으로 기술함.

### 결론 (추후 작성)
- 본 문서는 `repositories/new` owner namespace 초기화 계약 복구를 위한 핫픽스 계획을 수립하기 위해 작성함.
- 현재 단계에서는 구현을 진행하지 않았으며, 범위 및 검증 전략 정의를 완료하였음.

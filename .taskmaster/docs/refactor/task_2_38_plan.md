# 리팩토링 계획서

### 제목
- **리팩토링 계획**: `jgitkins-server` 전역의 `taskCd` 명칭을 `namespace` 의미에 맞게 정렬하는 리네이밍 리팩토링 계획서

### 배경 (왜?)
- 현재 `jgitkins-server` 전역에는 `taskCd`라는 명칭이 다수 존재하나, 실제로는 업무상의 task code 가 아니라 Git 저장소의 owner namespace 또는 파일시스템 경로 세그먼트를 의미하는 경우가 많음을 확인하였음.
- 특히 Git adapter, push hook, pipeline config 조회, commit/file 조회, merge 처리 경로에서 `taskCd`는 `/Users/.../bare/{namespace}/{repo}.git` 구조의 `{namespace}`를 의미하는 값으로 사용되고 있음.
- 이 명칭 불일치로 인해 push 이벤트 처리 중 owner id 문자열이 `taskCd`로 주입되거나, namespace 와 owner id 의미가 혼재되는 버그가 실제로 발생하였음.
- 즉, 현재 구조는 코드 가독성 저하 수준을 넘어서 잘못된 값 주입과 유지보수 실수 가능성을 높이는 명명 기반 기술 부채 상태라고 판단하였음.
- 리팩토링을 통해 변수명과 도메인 의미를 일치시키지 않으면 동일 계열의 버그가 재발할 가능성이 높다고 판단하였음.

### 목표 (Goals)
- `taskCd` 명칭이 실제 의미와 다른 모든 핵심 경로를 식별하고, 내부 코드 기준으로 `namespace` 명칭으로 일관되게 정리함.
- Git 저장소 접근, push 이벤트, pipeline config, commit/file/merge 처리 경로에서 의미 혼동을 제거하여 유지보수성을 향상함.
- 외부 API 및 기존 호출자와의 호환성을 검토하여, 필요한 경우 점진적 전환 전략을 수립함.
- 테스트 코드와 문서 상의 용어도 함께 정리하여 리팩토링 후 명칭 일관성을 확보함.

### 범위 (Scope)
- **수정 대상**:
    - `application port/usecase/service` 계층의 `taskCd` 파라미터 및 지역 변수
    - `PushEventCommand`, `JobCreateCommand`, `BranchCreationContext` 등 관련 command/DTO 명칭
    - `infrastructure` 계층의 Git adapter, hook mapper, resolver 연계 코드
    - `presentation` 계층의 controller path variable 명칭 및 내부 매핑 코드
    - 해당 변경과 직접 연결되는 테스트 코드 전반
- **수정 제외 대상**:
    - 이번 리팩토링과 무관한 비즈니스 규칙 변경
    - REST API path 자체의 즉시 breaking change 적용
    - web 모듈 및 runner 모듈의 대규모 동시 리네이밍

### 계획 (Plan)
- **단계 1**: 분석 및 평가를 수행함.
    - `taskCd`가 사용되는 클래스, 메서드, DTO, 테스트를 전수 조사하여 실제 의미가 무엇인지 분류함.
    - 다음 3가지 유형으로 사용처를 분류함.
    - **유형 1**: 실제 namespace 의미로만 사용되는 경우를 식별함.
    - **유형 2**: owner id, namespace, clone path 의미가 혼재된 경우를 식별함.
    - **유형 3**: 외부 API path 변수명으로만 남아 있는 경우를 식별함.
    - 이 결과를 바탕으로 “내부 코드 우선 리네이밍, 외부 호환은 점진 처리” 전략을 채택함.

- **단계 2**: 리팩토링 전략을 수립함.
    - 다음 3가지 방안을 비교 검토함.
    - **방안 1**: 전역 일괄 rename 으로 한 번에 `taskCd -> namespace`를 적용하는 방안을 검토함.
    - **방안 2**: 내부 코드와 DTO를 먼저 rename 하고, 외부 API path variable 은 호환용 매핑을 유지하는 점진적 전환 방안을 검토함.
    - **방안 3**: 명칭은 유지하고 JavaDoc 또는 주석만 보강하는 방안을 검토함.
    - 명칭 혼동 해소 효과와 운영 안정성을 함께 확보하기 위해 **방안 2**를 채택함.

- **단계 3**: 리팩토링 작업을 수행함.
    - 내부 포트, 서비스, 어댑터 메서드 시그니처에서 `taskCd`를 `namespace`로 순차 변경함.
    - `PushEventCommand.taskCd`, `JobCreateCommand.taskCd` 등 의미상 namespace 역할인 필드는 `namespace`로 변경함.
    - controller 레이어는 외부 URL 경로의 backward compatibility 필요 여부를 검토한 뒤, 필요 시 `@PathVariable("taskCd") String namespace` 형태의 중간 호환 레이어를 둠.
    - merge/file/commit/pipeline 관련 테스트 fixture와 assertion 명칭도 함께 정리함.

- **단계 4**: 테스트 및 검증을 수행함.
    - push hook 관련 테스트, Git adapter 테스트, commit/file controller 테스트를 우선 검증 대상으로 삼음.
    - 리네이밍 이후에도 namespace 기반 bare repository 경로 해석이 동일하게 동작하는지 확인함.
    - API path variable 호환 전략을 택한 경우, 기존 요청 경로가 깨지지 않는지 별도 확인함.

- **단계 5**: 문서화를 수행함.
    - 리팩토링 이후 `namespace`, `ownerId`, `clonePath`의 의미 차이를 문서로 명확히 기록함.
    - 후속 작업이 필요한 breaking change 항목은 별도 TODO 또는 Taskmaster subtask로 분리함.
    - 테스트 전략과 API 호환 정책을 함께 정리하여 다음 변경 시 재해석 비용을 줄이도록 함.

### 기대효과 (Expected Benefits)
- `taskCd`와 `namespace`의 의미 혼동이 제거되어 코드 가독성과 유지보수성이 향상됨.
- owner id 와 namespace 혼동으로 인한 push/pipeline/Git adapter 계열 버그 재발 가능성이 감소함.
- 신규 기능 추가 시 올바른 식별자 선택이 쉬워져 설계 일관성이 향상됨.
- 테스트와 문서의 용어가 통일되어 협업 비용이 줄어들 것으로 기대함.


### 예시 (방안 2 기준 코드 스니펫)

#### AS-IS (현재 구조)
```java
public PipelineConfig read(String taskCd, String repoName, String commitHash) {
    try (Repository repository = repositoryResolver.openBareRepository(taskCd, repoName)) {
        RevTree tree = resolveCommitTree(repository, commitHash);
        String yamlText = readConfig(repository, tree);
        // ...
    }
}

PushEventCommand.builder()
        .repositoryId(repository.getId().getValue())
        .taskCd(repository.getOwnerId().toString())
        .repoName(repository.getName().getValue())
        .build();
```

#### TO-BE (개선 제안 구조)
```java
public PipelineConfig read(String namespace, String repoName, String commitHash) {
    try (Repository repository = repositoryResolver.openBareRepository(namespace, repoName)) {
        RevTree tree = resolveCommitTree(repository, commitHash);
        String yamlText = readConfig(repository, tree);
        // ...
    }
}

PushEventCommand.builder()
        .repositoryId(repository.getId().getValue())
        .namespace(extractNamespace(repository).orElseThrow())
        .repoName(repository.getName().getValue())
        .build();
```

### 주의사항
- **포맷팅 금지**: 리팩토링과 무관한 대규모 포맷팅은 수행하지 않음.
- **기존 기능 보장**: 리네이밍 이후에도 push, pipeline config 조회, commit/file/merge 처리 기능이 기존과 동일하게 동작하는지 확인이 필요함.
- **계획우선**: 본 문서 작성 단계에서는 실제 구현을 진행하지 않음.
- **예시전체나열**: 변경 핵심 흐름의 BEFORE/AFTER 구조를 전체 기준으로 제시하였음.
- **문서체규약**:
    - 모든 문장은 `~~하였음` 또는 `~~함` 형태로 작성함.
    - 구어체 표현은 사용하지 않음.
    - 중요한 정보는 빠짐없이 포함하되 간결하게 유지함.
    - 문서의 끝맺음은 공식 문어체 형식으로 유지함.

### 결론 (추후작성)
- 본 문서는 `taskCd` 명칭을 실제 의미인 `namespace`로 정렬하기 위한 전역 리네이밍 리팩토링 계획을 수립하기 위해 작성함.
- 현재 단계에서는 구현을 진행하지 않았으며, 범위, 전략, 검증 기준 정의를 완료하였음.

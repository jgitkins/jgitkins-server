# 핫픽스 계획서

### 제목
- **핫픽스 대상**: `jgitkins-web` 저장소 상세 화면의 tree 디렉토리 아이콘 및 링크 분기 오류 수정 계획서

### 배경 (현상 및 원인)
- `jgitkins-web/src/main/resources/templates/repositories/detail.html` 화면 접근 시 repository tree 내 디렉토리 엔트리가 모두 파일 아이콘으로 표시되는 현상이 발생하였음.
- 동일한 원인으로 디렉토리 행이 파일 행 분기로 처리될 가능성이 있으며, 사용자는 디렉토리와 파일을 시각적으로 구분하기 어려운 상태임.
- 저장소 브라우징 화면은 코드 탐색의 핵심 진입점이므로, 본 이슈는 사용자 경험 저하뿐 아니라 디렉토리 이동 실패로 오인될 수 있는 운영 장애 성격을 가짐.
- 현재 화면 템플릿과 검색 UI 는 엔트리 타입을 `tree` 와 `blob` 기준으로 판단하도록 구성되어 있음.
- 리팩토링 과정에서 `server` 모듈의 tree 엔트리 생성 로직이 `isDirectory` 중심으로 변경되었거나, `type` 필드 계약이 약화되면서 `web` 모듈이 기대하는 `tree` 값이 누락되었을 가능성이 높다고 판단하였음.
- 추가로 `web` 모듈에는 repository tree 캐시가 존재하므로, 잘못된 타입 정보가 캐시에 저장된 경우 원인 수정 이후에도 동일 현상이 재현될 수 있음을 확인하였음.

### 목표 (Goals)
- 디렉토리 엔트리가 화면에서 파일이 아닌 디렉토리로 정확히 렌더링되도록 복구함.
- `web` 과 `server` 사이의 repository tree 응답 계약을 명확히 하여 `type=tree/blob` 의미를 다시 일관되게 보장함.
- 캐시로 인해 기존 오염 데이터가 재사용되는 상황을 차단하여 핫픽스 배포 직후부터 정상 동작을 확보함.
- 최소 범위 수정으로 본 이슈만 해결하고, 리팩토링성 구조 변경이나 부가 기능 확장은 배제함.

### 범위 (Scope)
- **수정 대상**:
    - `jgitkins-server` 의 repository tree 엔트리 생성 경로
    - `jgitkins-web` 의 repository tree 캐시 경로 및 캐시 무효화 전략
    - 본 이슈와 직접 관련된 controller/service/adapter/template 동작 검증 테스트
- **수정 제외 대상**:
    - 저장소 상세 화면 UI 전면 개편
    - tree 엔트리 DTO 구조 전면 재설계
    - 파일 상세 조회, 커밋 히스토리, 업로드 기능 개선
    - 리팩토링 과정의 명명 정리, 포맷팅, 불필요한 클래스 이동

### 계획 (Plan)
- **단계 1: 현상 재정리 및 계약 확인**
    - `detail.html` 에서 디렉토리/파일 분기 조건이 무엇인지 우선 재확인함.
    - `web` DTO 와 `server` API 응답에서 tree 엔트리 타입 계약이 현재 어떻게 정의되어 있는지 확인함.
    - 다음 3가지 수정 방안을 비교 검토함.
    - **방안 1**: `server` 에서 `type` 값을 다시 `tree/blob` 규약으로 보장하여 API 계약을 복구하는 방안을 검토함.
    - **방안 2**: `web` 에서 `DIRECTORY`, `FILE`, `null`, `tree`, `blob` 등 다양한 타입을 모두 정규화하여 방어적으로 처리하는 방안을 검토함.
    - **방안 3**: 템플릿 조건식만 완화하여 디렉토리 추정값을 폭넓게 허용하는 화면 레이어 보정 방안을 검토함.
    - 영향 범위, 계약 일관성, 회귀 위험을 비교한 결과 **방안 1**을 1순위로 채택함.
    - 채택 사유는 `web` 템플릿, 파일 찾기 UI, tree 캐시가 모두 `tree/blob` 계약을 전제로 하고 있으므로, 서버 계약을 복구하는 것이 가장 작은 수정으로 가장 넓은 문제를 해결하기 때문임.

- **단계 2: 최소 수정 설계 확정**
    - `server` tree 엔트리 생성 로직에서 디렉토리는 `tree`, 파일은 `blob` 으로 명시되도록 설계함.
    - `isDirectory` 보조 필드는 유지하되, 화면이 실제로 의존하는 `type` 필드를 우선 보장하도록 설계함.
    - 기존 잘못된 캐시 재사용을 차단하기 위해 `web` tree 캐시 키 버전 업 또는 선택적 캐시 무효화 전략을 적용하도록 설계함.
    - 캐시 무효화 전략은 다음 3가지를 비교 검토함.
    - **방안 A**: 운영 Redis 수동 삭제를 전제로 코드 변경 없이 배포하는 방안을 검토함.
    - **방안 B**: tree 캐시 키 prefix 에 버전을 부여하여 신규 키로 재적재되도록 하는 방안을 검토함.
    - **방안 C**: 캐시 read 시 타입 보정 로직을 추가하여 기존 캐시를 재해석하는 방안을 검토함.
    - 운영 안전성과 즉시성 측면에서 **방안 B**를 채택함.

- **단계 3: 검증 전략 수립**
    - `server` 단에서는 실제 Git tree 에서 디렉토리와 파일을 함께 생성한 뒤, 디렉토리가 `tree` 로 반환되는 테스트를 작성함.
    - `controller` 단에서는 tree 응답이 정상 래핑되는 기존 테스트를 유지하며, 필요 시 타입 계약 검증을 보강함.
    - `web` 단에서는 repository detail 로딩 경로가 tree 데이터를 정상 소비하는지 기존 서비스 테스트를 회귀 검증 대상으로 삼음.
    - 배포 전 검증 명령은 모듈별 최소 테스트 세트로 제한하여 핫픽스 속도와 안정성의 균형을 맞춤.

- **단계 4: 배포 및 확인 전략 수립**
    - 핫픽스 반영 후 repository detail 화면에서 디렉토리가 폴더 아이콘과 디렉토리 링크로 노출되는지 확인함.
    - 동일 커밋 기준으로도 과거 캐시 영향 없이 즉시 정상 동작하는지 확인함.
    - 배포 직후 오류 로그, tree API 응답 샘플, 사용자 재현 경로를 중심으로 모니터링함.

- **단계 5: 후속 안정화 항목 정리**
    - 이번 핫픽스는 최소 수정으로 종료하되, 재발 방지를 위한 개선 후보를 별도로 기록함.
    - 개선 후보는 다음 3가지로 정리함.
    - `web` 레이어에 타입 정규화 방어 로직을 추가하는 방안이 있음.
    - tree API 응답 계약 테스트를 controller 수준 이상으로 확대하는 방안이 있음.
    - Redis 캐시 무효화 운영 절차를 문서화하는 방안이 있음.
    - 이 중 즉시 반영 가치가 가장 높은 항목은 캐시 무효화 전략 명시이므로, 본 핫픽스 범위에서는 캐시 키 버전 정책을 적용하는 방향으로 수립함.

### 기대효과 (Expected Benefits)
- repository detail 화면에서 디렉토리와 파일이 즉시 구분 가능하게 복구됨.
- 디렉토리 링크 분기가 정상화되어 저장소 탐색 사용성이 회복됨.
- `web` 과 `server` 간 tree 응답 계약이 다시 일치하여 동일 유형의 회귀 가능성이 낮아짐.
- 잘못된 캐시 재사용이 차단되어 배포 직후부터 안정적인 정상화가 가능해짐.

### 예시 (수정 전/후 코드 스니펫)

#### AS-IS (버그 발생 구조)
```java
private FileEntry buildEntry(Repository repo, ObjectId id, String fullPath, FileMode mode) throws IOException {
    String name = fullPath.contains("/") ? fullPath.substring(fullPath.lastIndexOf("/") + 1) : fullPath;
    boolean isDirectory = mode == FileMode.TREE;
    long size = isDirectory ? 0 : repo.open(id).getSize();

    return FileEntry.builder()
            .name(name)
            .path(fullPath)
            .isDirectory(isDirectory)
            .size(size)
            .build();
}
```

#### TO-BE (핫픽스 적용 구조)
```java
private FileEntry buildEntry(Repository repo, ObjectId id, String fullPath, FileMode mode) throws IOException {
    String name = fullPath.contains("/") ? fullPath.substring(fullPath.lastIndexOf("/") + 1) : fullPath;
    boolean isDirectory = mode == FileMode.TREE;
    long size = isDirectory ? 0 : repo.open(id).getSize();
    String type = isDirectory ? "tree" : "blob";

    return FileEntry.builder()
            .name(name)
            .path(fullPath)
            .type(type)
            .isDirectory(isDirectory)
            .size(size)
            .build();
}
```

### 주의사항
- **포맷팅 금지**: 핫픽스와 무관한 코드 정렬 및 스타일 변경은 절대 수행하지 않음.
- **기존 기능 보장**: 디렉토리 아이콘 수정 외에 파일 목록, 브랜치 선택, 파일 찾기, 업로드 경로 선택 기능이 기존과 동일하게 동작하는지 확인함.
- **계획 우선**: 본 문서 작성 단계에서는 구현을 진행하지 않으며, 범위와 전략 확정 후에만 수정에 착수함.
- **예시 전체 나열**: 실제 변경 핵심 로직의 BEFORE/AFTER 구조를 전체 흐름으로 제시하였음.
- **최소 수정 원칙**: UI 전면 수정이나 DTO 재설계 없이, API 계약 복구와 캐시 정상화에 집중함.
- **문서체 규약**:
    - 모든 문장은 공식 문서체로 작성함.
    - 문장 끝은 `~~하였음` 또는 `~~함` 형태로 유지함.
    - 구어체와 추측성 표현은 지양하고, 확인된 사실과 계획 중심으로 작성함.

### 결론 (추후 작성)
- 본 문서는 repository tree 디렉토리 아이콘 오표시 이슈에 대한 핫픽스 계획을 수립하기 위해 작성하였음.
- 현재 단계에서는 구현을 진행하지 않았으며, 템플릿 기준 계획서 작성만 완료하였음.

# 리팩토링 계획서

### 제목
- **리팩토링 계획**: Port 및 Adapter 내부 메서드명 현대화 및 의도 명확화

### 배경 (왜?)
- 현재 Port 및 Adapter 내부 메서드명에는 `create`, `delete`, `get*`, `load*`, `fetch*` 등 서로 다른 기준이 혼재되어 있어 동일 계층 내부에서도 의도 파악 비용이 발생하고 있음.
- Persistence Port/Adapter 에서는 조회 계열이 `find*`, `exists*`, `count*` 패턴을 일부 따르고 있으나, 다른 메서드는 `getBranch`, `getBranches`, `loadJob`, `create`, `delete` 등으로 혼용되어 일관성이 부족함.
- Git Port/Adapter 역시 `create`, `delete`, `getTree`, `getAllFiles`, `getCommitHistory`, `checkCanMerge` 등 반환 성격과 부수효과 성격이 메서드명만으로 즉시 구분되지 않는 구간이 존재함.
- 특히 `find*` 계열은 영속성 계층의 일반적 조회 관례로 인식되는 경우가 많으므로, Git 리소스 접근 포트까지 동일 접두사를 적용하면 관심사 경계가 오히려 흐려질 수 있음.
- `CurrentUserPort.currentUserId()` 는 조회 의미의 Optional 반환임에도 명사형에 가까워 질의 메서드인지 즉시 드러나지 않음.
- 직전 Task 2.32 에서 클래스명 규칙은 정리되었으나, 내부 메서드명은 아직 구규칙과 혼합되어 있어 아키텍처 가시성 개선이 반쪽 상태로 남아 있음.

### 목표 (Goals)
- Port 및 Adapter 계층의 메서드명을 조회/저장/삭제/검사 의도에 맞는 일관된 규칙으로 정리함.
- Persistence 계층과 Git/보안 계층의 명명 규칙을 분리하여 각 계층의 책임을 이름만으로 구분 가능하게 함.
- 메서드명만 보고도 부수효과 유무와 조회 기준을 유추할 수 있도록 개선함.
- Service 와 Test 에서 사용하는 호출부의 의미를 더 명확하게 드러내도록 함.
- 향후 Port 확장 및 Adapter 추가 시 재사용 가능한 명명 기준을 수립함.

### 범위 (Scope)
- **수정 대상**:
    - `src/main/java/io/jgitkins/server/application/port/out/` 하위 Port 인터페이스 메서드명
    - `src/main/java/io/jgitkins/server/infrastructure/adapter/` 하위 Adapter 구현 메서드명
    - 상기 메서드를 호출하는 `application/service`, `application/support`, `application/validate`, `application/event`, `src/test/java` 내 참조 지점
- **수정 제외 대상**:
    - Domain 모델의 정적 팩토리 메서드명
    - `application/port/in/` UseCase 인터페이스 메서드명
    - Presentation Controller 경로 및 API DTO 명칭
    - 비즈니스 규칙 자체와 예외 정책 변경

### 계획 (Plan)
- **단계 1: 현황 분석 및 명명 규칙 확정**
    - 현재 Port/Adapter 메서드명을 조회형, 저장형, 삭제형, 검사형, 실행형으로 분류함.
    - 다음 3가지 방안을 검토함.
    - **방안 1**: 기존 메서드명을 최대한 유지하고 일부 hotspot 만 정리하는 최소 변경 전략을 검토함.
    - **방안 2**: Persistence 계층에는 `find*`, `findAll*`, `save`, `deleteBy*`, `existsBy*` 를 적용하고, Git/보안 계층에는 `load*`, `list*`, `initialize*`, `delete*`, `preview*`, `resolve*` 등 역할 중심 동사를 적용하는 계층 분리 전략을 검토함.
    - **방안 3**: Service/Validator 헬퍼 메서드까지 포함하여 전 계층 메서드명을 광범위하게 재정렬하는 전면 정비 전략을 검토함.
    - 범위 대비 효과와 회귀 위험을 비교한 결과, 이번 Task 2.33 의 요구사항인 “Port, Adapter 내 메서드명 Renaming” 에 가장 직접적으로 부합하는 **방안 2**를 채택함.
    - 채택 기준은 다음과 같이 확정함.
    - **Persistence Port/Adapter**: 저장소 조회/저장/삭제 의미를 드러내는 `find`, `findAll`, `exists`, `count`, `save`, `deleteBy` 계열을 사용함.
    - **Git Port/Adapter**: Git 리소스 접근 및 형상 제어 의미를 드러내는 `load`, `list`, `initialize`, `delete`, `preview`, `merge` 계열을 사용함.
    - **Security / Other Infra Port**: 인증 주체 해석, 토큰 발급, 런타임 설정 등 역할 중심 동사를 사용하되, 영속성 오해를 피하는 이름을 채택함.

- **단계 2: 변경 대상 목록 및 BEFORE / AFTER 확정**
    - 변경하려는 메서드 목록을 아래와 같이 전수 확정함.

| 분류 | 클래스 | BEFORE | AFTER |
| :--- | :--- | :--- | :--- |
| **Other (Security)** | `CurrentUserPort` / `CurrentUserSecurityAdapter` | `currentUserId()` | `resolveCurrentUserId()` |
| **Persistence** | `BranchPersistencePort` / `BranchPersistenceAdapter` | `create(Branch)` | `save(Branch)` |
| **Persistence** | `BranchPersistencePort` / `BranchPersistenceAdapter` | `delete(Long repositoryId, String branchName)` | `deleteByRepositoryIdAndName(Long repositoryId, String branchName)` |
| **Persistence** | `BranchPersistencePort` / `BranchPersistenceAdapter` | `getBranch(Long repositoryId, String branch)` | `findByRepositoryIdAndName(Long repositoryId, String branchName)` |
| **Persistence** | `BranchPersistencePort` / `BranchPersistenceAdapter` | `getBranches(Long repositoryId)` | `findAllByRepositoryId(Long repositoryId)` |
| **Persistence** | `JobPersistencePort` / `JobPersistenceAdapter` | `create(Job)` | `save(Job)` |
| **Persistence** | `JobPersistencePort` / `JobPersistenceAdapter` | `fetchPendingJobFor(RunnerAssignmentCandidate candidate)` | `findPendingByCandidate(RunnerAssignmentCandidate candidate)` |
| **Persistence** | `JobPersistencePort` / `JobPersistenceAdapter` | `persistHistory(Job job, JobHistory previousHistory)` | `saveHistory(Job job, JobHistory previousHistory)` |
| **Persistence** | `JobPersistencePort` / `JobPersistenceAdapter` | `loadJob(Long jobId)` | `findById(Long jobId)` |
| **Persistence** | `OrganizePersistencePort` / `OrganizePersistenceAdapter` | `delete(OrganizeId organizeId)` | `deleteById(OrganizeId organizeId)` |
| **Persistence** | `RepositoryPersistencePort` / `RepositoryPersistenceAdapter` | `delete(RepositoryId id)` | `deleteById(RepositoryId id)` |
| **Persistence** | `RepositoryPersistencePort` / `RepositoryPersistenceAdapter` | `findRepositoryId(OwnerType ownerType, OwnerId ownerId, String repoName)` | `findIdByOwnerAndName(OwnerType ownerType, OwnerId ownerId, String repoName)` |
| **Persistence** | `OrganizeMemberPersistencePort` / `OrganizeMemberPersistenceAdapter` | `existsByOrganizeAndUser(OrganizeId organizeId, UserId userId)` | `existsByOrganizeIdAndUserId(OrganizeId organizeId, UserId userId)` |
| **Persistence** | `OrganizeMemberPersistencePort` / `OrganizeMemberPersistenceAdapter` | `findByOrganizeAndUser(OrganizeId organizeId, UserId userId)` | `findByOrganizeIdAndUserId(OrganizeId organizeId, UserId userId)` |
| **Persistence** | `OrganizeMemberPersistencePort` / `OrganizeMemberPersistenceAdapter` | `deleteByOrganizeAndUser(OrganizeId organizeId, UserId userId)` | `deleteByOrganizeIdAndUserId(OrganizeId organizeId, UserId userId)` |
| **Persistence** | `OrganizeMemberPersistencePort` / `OrganizeMemberPersistenceAdapter` | `findAllByOrganize(OrganizeId organizeId)` | `findAllByOrganizeId(OrganizeId organizeId)` |
| **Persistence** | `RepositoryMemberPersistencePort` / `RepositoryMemberPersistenceAdapter` | `existsByRepositoryAndUser(RepositoryId repositoryId, UserId userId)` | `existsByRepositoryIdAndUserId(RepositoryId repositoryId, UserId userId)` |
| **Persistence** | `RepositoryMemberPersistencePort` / `RepositoryMemberPersistenceAdapter` | `findByRepositoryAndUser(RepositoryId repositoryId, UserId userId)` | `findByRepositoryIdAndUserId(RepositoryId repositoryId, UserId userId)` |
| **Persistence** | `RepositoryMemberPersistencePort` / `RepositoryMemberPersistenceAdapter` | `deleteByRepositoryAndUser(RepositoryId repositoryId, UserId userId)` | `deleteByRepositoryIdAndUserId(RepositoryId repositoryId, UserId userId)` |
| **Persistence** | `RepositoryMemberPersistencePort` / `RepositoryMemberPersistenceAdapter` | `findAllByRepository(RepositoryId repositoryId)` | `findAllByRepositoryId(RepositoryId repositoryId)` |
| **Git** | `RepositoryGitPort` / `RepositoryGitAdapter` | `create(String taskCd, String repoName)` | `initialize(String taskCd, String repoName)` |
| **Git** | `RepositoryGitPort` / `RepositoryGitAdapter` | `delete(String taskCd, String repoName)` | `deleteRepository(String taskCd, String repoName)` |
| **Git** | `FileGitPort` / `RepositoryGitFileAdapter` | `getTree(String taskCd, String repoName, String branch, String directory)` | `listTree(String taskCd, String repoName, String branch, String directory)` |
| **Git** | `FileGitPort` / `RepositoryGitFileAdapter` | `getAllFiles(String taskCd, String repoName, String reference)` | `listAllFiles(String taskCd, String repoName, String reference)` |
| **Git** | `CommitGitPort` / `RepositoryGitCommitAdapter` | `getCommitHistory(String taskCd, String repoName, String commitHash)` | `loadCommit(String taskCd, String repoName, String commitHash)` |
| **Git** | `CommitGitPort` / `RepositoryGitCommitAdapter` | `getCommitHistories(String taskCd, String repoName, String branch)` | `listCommitHistory(String taskCd, String repoName, String branch)` |
| **Git** | `MergeGitPort` / `MergeGitAdapter` | `checkCanMerge(String taskCd, String repoName, String sourceBranch, String targetBranch)` | `previewMerge(String taskCd, String repoName, String sourceBranch, String targetBranch)` |

- **단계 3: 리팩토링 수행 순서 수립**
    - Port 인터페이스 메서드명을 먼저 변경함.
    - 해당 Port 를 구현하는 Adapter 메서드명을 동일하게 변경함.
    - Service, Support, Validator, Event, Test 의 호출 지점을 IDE Rename 또는 전수 검색 기반으로 갱신함.
    - 호출부 변경 시, 메서드 의미가 달라 보일 수 있는 구간은 검증용 테스트를 함께 확인함.

- **단계 4: 테스트 및 검증 전략 수립**
    - `compileJava` 로 메인 소스의 참조 누락을 우선 검증함.
    - `test` 로 테스트 코드의 참조 누락 및 기대 행위를 검증함.
    - 메서드명 변경 외의 기능 동작이 바뀌지 않았는지 Branch, RepositoryLifecycle, RepositoryMember, OrganizeMember, JobDispatch, Commit, Merge 관련 테스트를 우선 확인함.

- **단계 5: 문서화**
    - 계획서 기준으로 실제 변경 결과와 검증 결과를 결론 항목에 반영함.
    - 필요 시 `.taskmaster/tasks/tasks.json` 의 Task 상태를 구현 완료 후 갱신함.

### 기대효과 (Expected Benefits)
- Persistence 계층 메서드가 Spring Data 스타일과 유사한 규칙으로 정리되어 조회/저장/삭제 의도가 즉시 드러나게 됨.
- Git 계층 메서드가 `find*` 대신 `load`, `list`, `initialize`, `preview` 등 실제 책임 중심 이름을 갖게 되어 Persistence 와의 경계가 명확해지게 됨.
- Service 와 Test 코드에서 호출 의도가 더 분명해져 유지보수성과 신규 온보딩 효율이 향상됨.
- 후속 Task 에서 Port 확장 시 명명 기준을 재논의할 필요가 줄어들게 됨.

### 예시 (방안 2 기준 코드 스니펫)

#### AS-IS (현재 구조)
```java
public interface BranchPersistencePort {
    void create(Branch branch);
    void delete(Long repositoryId, String branchName);
    Optional<Branch> getBranch(Long repositoryId, String branch);
    List<Branch> getBranches(Long repositoryId);
}

public interface CurrentUserPort {
    Optional<Long> currentUserId();
}

public interface CommitGitPort {
    CommitHistory getCommitHistory(String taskCd, String repoName, String commitHash);
    List<CommitHistory> getCommitHistories(String taskCd, String repoName, String branch);
}
```

#### TO-BE (개선 제안 구조)
```java
public interface BranchPersistencePort {
    void save(Branch branch);
    void deleteByRepositoryIdAndName(Long repositoryId, String branchName);
    Optional<Branch> findByRepositoryIdAndName(Long repositoryId, String branchName);
    List<Branch> findAllByRepositoryId(Long repositoryId);
}

public interface CurrentUserPort {
    Optional<Long> resolveCurrentUserId();
}

public interface CommitGitPort {
    CommitHistory loadCommit(String taskCd, String repoName, String commitHash);
    List<CommitHistory> listCommitHistory(String taskCd, String repoName, String branch);
}
```

### 주의사항
- **포맷팅 금지**: 리팩토링 과정에서 코드 포맷팅 절대하지말것. 주로 코드의 기능과 구조를 개선하는 데 집중함.
- **기존 기능 보장**: 리팩토링 후에도 기존 기능이 정상적으로 동작하는지 확인하는 테스트가 필요함.
- **계획우선**: 계획문서 작성중에 절대로 구현을 진행하지말것.
- **예시전체나열**: 변경하려는 목록의 BEFORE AFTER를 모두 나열하였으며, 구현 시 누락 없이 동일 기준으로 반영해야 함.
- **부분최적화 금지**: 특정 Port/Adapter 만 선택적으로 적용하지 않고, 계획서에 기재된 변경 대상은 일괄 반영함.
- **의미보존 우선**: 메서드명만 정리하며, 파라미터 구조나 반환 타입, 비즈니스 정책은 본 Task 범위에서 변경하지 않음.
- **계층규칙 준수**: Persistence 계층에만 `find*`, `findAll*`, `deleteBy*`, `existsBy*` 관례를 적용하고, Git 계층에는 `load*`, `list*`, `initialize*`, `preview*` 등 역할 중심 동사를 적용함.
- **문서체규약**:
    - 문서 작성 시, 모든 문장은 **"~~하였음"** 또는 **"~~함"** 형태로 마무리함.
    - 구어체 표현은 피하고, 전문적이고 격식 있는 문어체를 사용함.
    - 문장을 간결하게 작성하되, 중요한 정보는 빠짐없이 포함함.
    - 문서의 끝맺음은 항상 "완료하였음", "수립하였음", "작성함" 등의 형식으로 마무리함.

### 결론 (추후작성)
- 템플릿 기준 계획문서를 우선 작성하였으며, 본 단계에서는 구현을 진행하지 않았음.

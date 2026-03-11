# 리팩토링 계획서

### 제목
- **리팩토링 계획**: Port 및 Adapter 클래스 명명 규칙 현대화 (Renaming)

### 배경 (왜?)
- 현재 프로젝트의 Port와 Adapter 클래스명에 기술 스택(MyBatis, JGit 등)이 직접 노출되어 있거나, 명명 규칙이 일부 혼용되고 있음.
- 헥사고날 아키텍처의 핵심은 '기술 독립성'이며, 어댑터의 이름 역시 특정 기술보다는 그 어댑터가 수행하는 '역할(Persistence, Git, Security 등)'을 더 명확히 드러내는 것이 모범 사례임.
- 클래스명을 일관성 있게 정리하여 아키텍처 가시성을 높이고 유지보수성을 향상시키고자 함.
<!-- 
현재 프로젝트에서는 1포트 N구현체 개념이 어려움
왜냐하면, Branch 를 생성하는 기능을 예시로 브랜치를 물리적으로 file system 에도 생성을 해야되고 (with jgit) 
테이블에도 논리적으로 가지고있어야하기 때문 (MariaDB) 따라서, 현재 구조에 맞는 네이밍 대안이 필요함
-->

### 전략 (Strategy)
- **전략 명칭**: **역할 기반 접미사 전략 (Concern-based Suffix Strategy)**
- **핵심 개념**: 특정 기술명(MyBatis, JGit) 대신 인프라의 책임 성격에 따라 **'논리적 영속성(Persistence)'**과 **'물리적 자원 제어(Git)'**로 이원화하여 명명함.

### 목표 (Goals)
- **논리 데이터 관리 (Logical/State)**: 
    - Port: `*PersistencePort` (기존의 단순 `*Port` 포함)
    - Adapter: `*PersistenceAdapter` (기존 `*MybatisAdapter` 또는 기술명이 없는 어댑터)
    - 책임: 데이터베이스 레코드 저장 및 조회, 도메인 상태 영속화.
- **물리적 자원 제어 (Physical/Resource)**: 
    - Port: `*GitPort` (현행 유지하되 일관성 점검)
    - Adapter: `*GitAdapter` (기존 `*JGitAdapter`)
    - 책임: Git 엔진 조작, 파일 시스템 물리적 변경, 브랜치/레포지토리 형상 관리.
- **일관성 확보**: 프로젝트 전체에서 위 명명 규칙을 통일하여 아키텍처 가시성을 극대화함.

### 범위 (Scope)
- **수정 대상**:
    - `src/main/java/io/jgitkins/server/application/port/out/` 내의 모든 Port 인터페이스
    - `src/main/java/io/jgitkins/server/infrastructure/adapter/` 내의 모든 Adapter 클래스
- **수정 제외 대상**:
    - 비즈니스 로직(Domain, Service)의 내부 구현.
    - 외부 라이브러리 클래스.

### 계획 (Plan)
- **단계 1: 현황 분석 및 규칙 확정**
    - '물리(Git) vs 논리(Persistence)' 이원화 체계를 적용한 상세 매핑 리스트를 확정함.

| 분류 | Port (AS-IS -> TO-BE) | Adapter (AS-IS -> TO-BE) |
| :--- | :--- | :--- |
| **Logic (Persistence)** | `UserPort` -> `UserPersistencePort` <br> `BranchPort` -> `BranchPersistencePort` <br> `OrganizePort` -> `OrganizePersistencePort` <br> `RepositoryPort` -> `RepositoryPersistencePort` <br> `JobPort` -> `JobPersistencePort` <br> `RunnerPort` -> `RunnerPersistencePort` <br> `UserCredentialPort` -> `UserCredentialPersistencePort` <br> `UserIdentityPort` -> `UserIdentityPersistencePort` <br> `OrganizeMemberPort` -> `OrganizeMemberPersistencePort` <br> `RepositoryMemberPort` -> `RepositoryMemberPersistencePort` | `UserMybatisAdapter` -> `UserPersistenceAdapter` <br> `BranchMybatisAdapter` -> `BranchPersistenceAdapter` <br> `OrganizeMybatisAdapter` -> `OrganizePersistenceAdapter` <br> `RepositoryMybatisAdapter` -> `RepositoryPersistenceAdapter` <br> `JobMybatisAdapter` -> `JobPersistenceAdapter` <br> `RunnerMybatisAdapter` -> `RunnerPersistenceAdapter` <br> `UserCredentialMybatisAdapter` -> `UserCredentialPersistenceAdapter` <br> `UserIdentityMybatisAdapter` -> `UserIdentityPersistenceAdapter` <br> `OrganizeMemberMybatisAdapter` -> `OrganizeMemberPersistenceAdapter` <br> `RepositoryMemberAdapter` -> `RepositoryMemberPersistenceAdapter` |
| **Physical (Git)** | `BranchGitPort` (유지) <br> `RepositoryGitPort` (유지) <br> `FileGitPort` (유지) <br> `CommitGitPort` (유지) <br> `MergeGitPort` (유지) | `BranchJGitAdapter` -> `BranchGitAdapter` <br> `RepositoryJGitAdapter` -> `RepositoryGitAdapter` <br> `RepositoryJGitFileAdapter` -> `RepositoryGitFileAdapter` <br> `RepositoryJGitCommitAdapter` -> `RepositoryGitCommitAdapter` <br> `MergeJGitAdapter` -> `MergeGitAdapter` |
| **Other (Infra)** | `CurrentUserPort` (유지) <br> `RuntimeConfigPort` (유지) | `CurrentUserAdapter` -> `CurrentUserSecurityAdapter` <br> `RunnerRuntimeConfigAdapter` (유지) |
- **단계 2: 클래스 및 파일 Rename 실행**
    - IDE의 Refactor 기능을 활용하여 클래스명을 변경하고, 관련된 모든 참조 지점(Service, Config 등)을 함께 업데이트함.
- **단계 3: Bean 설정 및 Qualifier 확인**
    - 클래스명이 변경됨에 따라 Spring Bean 이름이 자동 변경될 수 있으므로, 명시적인 `@Component`나 `@Bean` 설정에서 이름 충돌이 없는지 확인함.
- **단계 4: 테스트 및 검증**
    - 전체 프로젝트 빌드 및 기존 단위/통합 테스트를 실행하여 런타임에 빈 주입 오류나 오작동이 없는지 최종 확인함.

### 기대효과 (Expected Benefits)
- 아키텍처 설계 의도가 클래스명에 더 명확히 반영됨.
- 기술 스택 변경 시 클래스명을 대규모로 수정해야 하는 부담을 줄임.
- 신규 개발자가 프로젝트 구조를 파악하는 시간이 단축됨.

### 주의사항
- **포맷팅 금지**: 리팩토링 과정에서 코드의 기능적/구조적 개선에 집중하며, 단순 포맷팅 수정은 지양함.
- **의존성 전파 주의**: Port 이름 변경 시 이를 구현하는 모든 Adapter와 이를 사용하는 Service의 코드가 함께 수정되어야 하므로 누락이 없도록 주의함.
- **문서체규약**: 모든 문장은 "~~함", "~~함" 형태의 격식 있는 문어체로 작성함.

### 결론
- 계획서에 정의된 Port/Adapter 명명 규칙을 프로젝트 전반에 반영함.
- `*PersistencePort` / `*PersistenceAdapter`, `*GitAdapter`, `CurrentUserSecurityAdapter` 규칙에 맞춰 클래스 및 참조 지점을 정리함.
- 전체 컴파일 및 테스트를 수행하여 빈 주입/타입 참조/회귀 테스트 이상이 없음을 확인함.

# Repository 도메인 명세

`data/ERD.md`에 정의된 REPOSITORY/REPOSITORY_MEMBER/BRANCH 테이블을 기반으로 저장소 도메인을 정리한다. 서버가 제공해야 하는 저장소 관리 기능(생성, 조회, 멤버 관리, 브랜치 관리)의 공통 모델을 명확히 하는 것이 목적이다.

## 1. Aggregate & Value Objects

| 구성 요소 | 설명 |
| --- | --- |
| `Repository` | 저장소 루트 Aggregate. 조직(Organize)와 1:N 관계를 갖고 한 조직 내에서 `path` 가 unique 하다. Repository Aggregate 는 메타데이터와 동기화 정보(`lastSyncedAt`)를 함께 보관한다. |
| `RepositoryId` | DB PK (`REPOSITORY.id`)를 감싸는 VO. Long 기반이지만 도메인 계층에서 직접 숫자를 노출하지 않는다. |
| `OrganizeId` | 조직 식별자. 저장소 생성 시 반드시 필요하며 외부 시스템과 연동될 수 있으므로 별도 VO 로 추상화한다. |
| `UserId` | Owner/User auditing 을 위한 식별자. Repository 생성 시 owner 를 명시해 `REPOSITORY.owner_id` 를 채운다. |
| `RepositoryVisibility` | `PUBLIC/PRIVATE/INTERNAL` 3가지 visibility 값을 표현하는 enum. |
| `RepositoryType` | `GIT`, `GITHUB`, `GITLAB` 등의 원격 저장소 유형을 나타내는 enum. |
| `RepositoryCredentialId` | Jenkins credential 혹은 외부 시크릿 저장소 키를 참조하는 VO. null 허용. |
| `RepositoryUrl` | 원격 Git URL(예: ssh/http)을 래핑하는 값 객체. |
| `requiresInitialContent` | README/커밋 초기화가 필요한지 여부를 나타내는 불리언. `lastSyncedAt` 값으로 계산한다. |
| `RepositoryMember` | 저장소 멤버 엔터티. `role` 은 `MAINTAINER/DEVELOPER/REPORTER` 로 제한하며 동일 유저는 한 번만 추가된다. |
| `Branch` | 저장소에 속한 브랜치 엔터티. `repository_id + name` unique. 기본 브랜치(`default_branch`)는 Repository Aggregate 가 직접 참조한다. |

## 2. 속성 정의

| 필드 | 타입 | 제약 조건 | 설명 |
| --- | --- | --- | --- |
| `id` | bigint | PK, auto increment | 내부 식별자 |
| `organizeId` | bigint | FK → ORGANIZE.id, NN | 소속 조직 |
| `name` | varchar(255) | NN | 사용자 표시 이름 |
| `path` | varchar(255) | NN, unique | URL path(slug). `/{org.path}/{repo.path}` |
| `description` | text | optional | 소개 문구 |
| `defaultBranch` | varchar(255) | NN | 기본 브랜치. 초기값 `main` |
| `visibility` | varchar(32) | NN | `PUBLIC/PRIVATE/INTERNAL` |
| `repositoryType` | varchar(32) | NN | `GIT`, `GITHUB`, `GITLAB` 등 |
| `status` | varchar(32) | optional | (레거시) UI 는 default branch/커밋 존재 여부로 초기화 상태를 판별 |
| `ownerId` | bigint | FK → USERS.id | 저장소 생성자/책임자 |
| `credentialId` | varchar(128) | optional | Runner 가 사용할 자격 증명 참조 |
| `clonePath` | varchar(512) | optional | 저장된 상대 clone 경로(`/org/repo.git`). 외부에 노출할 URL은 애플리케이션이 base URL 과 결합해 계산한다. |
| `lastSyncedAt` | timestamp | nullable | 마지막 Git 동기화 시간 |
| `createdAt` | timestamp | default now | 생성 시각 |
| `updatedAt` | timestamp | default now on update | 수정 시각 |

## 3. 비즈니스 규칙

1. **경로 고유성**: `organize.path + "/" + repository.path` 조합은 전역에서 unique 하도록 보장한다. 동일 조직 내에서도 중복 불가.
2. **기본 브랜치**: 생성 시 반드시 지정되며, 이후 변경 시에는 브랜치 존재 여부를 검증해야 한다.
3. **Visibility 전파**: `PRIVATE` 저장소는 멤버십 검증이 필요하고, `INTERNAL` 은 동일 조직 사용자에 한해 접근 허용한다.
4. **멤버 역할**: `RepositoryMember.role` 은 ERD 에 정의된 값만 허용하고, ROLE 에 따라 읽기/쓰기 권한을 구분한다.
5. **브랜치 잠금**: `BRANCH.is_locked` 가 true 일 때 `locked_by/locked_at` 으로 감사 추적을 남긴다.

## 4. 초기화 판단

`lastSyncedAt` 값이 null 이면 아직 README/커밋이 없는 빈 저장소로 간주하고, 값이 존재하면 초기화가 끝난 것으로 처리한다. DB 의 `REPOSITORY.status` 컬럼은 호환성 유지를 위해 계속 기록하지만, 도메인은 더 이상 상태 enum 을 사용하지 않는다.

## 5. API 연계 요구 사항

| UseCase | 설명 | 도메인 요구 |
| --- | --- | --- |
| Repository 생성 (`POST /repositories`) | Bare repo 생성 후 optional README, metadata 저장 | 요청 시 `organizeId`, `name`, `path`, `visibility`, `defaultBranch`, `repositoryType`, `ownerId`, 자격 정보(`credentialId`), 초기 커밋 정보가 필요. clone path 는 서버가 조직/저장소 슬러그로 자동 생성 |
| Repository 조회 (`GET /repositories/{id}`) | 메타데이터 / 멤버 목록 반환 | Repository Aggregate + Members + Branch snapshot 반환. 응답에는 상대 경로(`clonePath`)와 base URL 이 결합된 전체 URL(`cloneUrl`)이 함께 노출된다. |
| Repository 업데이트 (`PUT /repositories/{id}`) | 설명, visibility, 기본 브랜치/owner/type/credential 변경 | 변경 시 브랜치 존재 확인 및 멤버 권한 체크 |
| Repository 삭제 (`DELETE /repositories/{id}`) | 논리 삭제 혹은 아카이브 | Runner/job 과의 연관성 확인 필요 |

위 명세를 기준으로 애플리케이션 계층의 DTO/서비스/포트를 정의하고, `RepositoryManagementController` 는 생성/조회/수정/삭제 API 전반에서 위 필드를 지원한다. 이를 통해 `data/ERD.md` 의 스키마와 실제 REST 계약을 일치시킨다.

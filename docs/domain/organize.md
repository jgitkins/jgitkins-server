# Organize 도메인 명세

`data/ddl.sql` 의 `ORGANIZE` 및 `ORGANIZE_MEMBER` 테이블을 기준으로 조직(Organize) 관리 도메인을 정의한다. 저장소 도메인의 상위 개념으로, 헥사고날 패턴과 Repository 관리 패턴을 그대로 적용한다.

## 1. Aggregate 구성

| 구성 요소 | 설명 |
| --- | --- |
| `Organize` | 조직 루트 Aggregate. 이름/경로/소유자/설명/타임스탬프를 보관하며 동일 경로(`path`)는 전역에서 unique 하다. |
| `OrganizeId` | DB PK(`ORGANIZE.id`)를 감싸는 VO. |
| `OrganizeName` | 표시 이름 VO. 공백을 허용하지 않는다. |
| `OrganizePath` | URL path(slug) VO. 소문자+숫자+`-`/`_` 조합만 허용한다. Repository path 검증과 동일 규칙을 따른다. |
| `OrganizeMember` | 조직 멤버 엔터티. `ORGANIZE_MEMBER` 와 매핑되며 유저/역할/가입 시각을 보관한다. (추후 멤버 API 에서 사용) |
| `OrganizeMemberRole` | 멤버 권한 enum (`OWNER`, `MAINTAINER`, `MEMBER`). OWNER 는 Organize 루트의 `ownerId` 와 동기화된다. |
| `UserId` | Owner 및 멤버 식별자 VO. |

## 2. 속성 정의

| 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | bigint | PK | Organize 식별자 |
| `name` | varchar(255) | NN | 조직 표시 이름 |
| `path` | varchar(255) | NN, unique | URL 슬러그 (`/{path}`) |
| `description` | text | optional | 소개 문구 |
| `ownerId` | bigint | FK → USERS.id | 조직 소유자 |
| `createdAt` / `updatedAt` | timestamp | default now | 감사 필드 |

## 3. 비즈니스 규칙

1. **경로 단일성**: `path` 는 시스템 전역에서 unique 하다. 변경 시에도 중복을 검사해야 한다.
2. **소유자 고정**: `ownerId` 는 Organize 생성 시 지정되며, 변경 시 권한 검증이 필요하다.
3. **멤버 역할**: `OrganizeMember.role` 은 enum 목록만 허용한다. OWNER 는 항상 멤버 목록에도 포함된다.
4. **Repository 연계**: Repository 생성 시 `OrganizeId` 를 필수로 요구하고, 동일 Organize 내에서 Repository path 를 중복 허용하지 않는다.

## 4. 헥사고날 계층 요구 사항

- **Domain**: `Organize` Aggregate 와 VO/Entity 는 `io.jgitkins.server.domain.aggregate` 및 `domain.model.vo` 아래에 위치한다.
- **Application**: DTO 는 `CreateOrganizeCommand`, `UpdateOrganizeCommand`, `OrganizeResult` 패턴을 따른다. `OrganizeService` 는 `OrganizePersistencePort` 를 통해 저장소에 접근한다.
- **Adapter**: `OrganizePersistenceAdapter` 는 MyBatis `OrganizeEntityMbgMapper` 를 사용하고, Presentation 계층에서는 `/api/organizes` REST 엔드포인트를 제공한다.

## 5. API 규격 (Repository 패턴 준수)

| Endpoint | 설명 |
| --- | --- |
| `POST /api/organizes` | Organize 생성. 요청은 `CreateOrganizeRequest` → `CreateOrganizeCommand` 로 매핑된다. |
| `GET /api/organizes` | 전체 Organize 목록 조회. |
| `GET /api/organizes/{id}` | 단건 조회. |
| `PUT /api/organizes/{id}` | 메타데이터 수정. |
| `DELETE /api/organizes/{id}` | 삭제 (향후 논리 삭제 고려). |

응답은 `ApiResponse<OrganizeResult>` 래퍼를 사용하여 Repository 관리 API 와 일관성을 유지한다.

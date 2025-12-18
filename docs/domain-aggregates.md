# 도메인 애그리게이트 정리 (Senior Review)

## Organize Aggregate
- **루트:** `Organize` (`src/main/java/io/jgitkins/server/domain/aggregate/Organize.java`).
- **의도:** 테넌트 경계를 정의하고 조직 이름·경로·소유자를 일관성 있게 관리한다.
- **구성요소:** `OrganizeId`, `OrganizeName`, `OrganizePath`, `UserId`, 설명 문자열, `OrganizeMember`(별도 엔터티지만 같은 트랜잭션에서 다룸).
- **주요 동작 및 불변 조건:**
  - `create`가 생성 시점 타임스탬프를 강제하고 경로/설명을 정규화한다.
  - `updateMetadata`는 부분 업데이트를 허용하되, 경로·이름의 전역 유일성, 유효한 소유자 여부는 애플리케이션 서비스가 검증해야 한다.
  - 조직 생성자는 OWNER 멤버로 자동 등록돼야 하며, OWNER 이상만 조직 설정을 갱신할 수 있다.
- **최근 변경:** `OrganizeService#createOrganize` 단계에서 `AddOrganizeMemberUseCase`를 호출해 OWNER 멤버십을 자동 생성하며, UseCase 내부에서 중복 여부를 검증한다.
- **설계 메모:** 경로 변경 시 하위 저장소 URL 변경 영향이 커서, 변경 사전 검증/이벤트가 필요하다.

## Repository Aggregate
- **루트:** `Repository` (`src/main/java/io/jgitkins/server/domain/aggregate/Repository.java`).
- **의도:** 저장소 메타데이터(이름, 경로, 기본 브랜치, 가시성)와 배포/동기화 속성을 캡슐화한다.
- **구성요소:** `RepositoryId`, `OrganizeId`, `RepositoryName`, `RepositoryPath`, `BranchName`, `RepositoryVisibility`, `UserId`(소유자), `repositoryType`, `credentialId`, `clonePath`, `requiresInitialContent` 등.
- **주요 동작 및 불변 조건:**
  - `create/register`는 브랜치·경로·가시성을 정규화하고 조직/소유자 존재를 요구한다.
  - `updateMetadata`는 불변 필드(생성 시점) 보호, 기본 브랜치 변경 시 실제 브랜치 존재를 전제로 한다.
  - `markSynced`는 초기 콘텐츠 요구 플래그를 해제하고 마지막 동기화 시간을 기록한다.
- **설계 메모:** 저장소 경로는 조직 내 유일해야 하며, 자격 증명은 외부 비밀 저장소 키를 참조하도록 `credentialId`만 저장한다.

## Branch Aggregate
- **루트:** `Branch` (`src/main/java/io/jgitkins/server/domain/Branch.java`) 및 ERD의 `BRANCH` 테이블.
- **의도:** 저장소 내 레퍼런스와 잠금 상태를 관리해 동시성 충돌을 막는다.
- **구성요소:** `repositoryId`, `BranchName`, 잠금 플래그(`is_locked`), `locked_by`, `locked_at`.
- **주요 동작 및 불변 조건:**
  - `create`는 저장소 존재 여부와 브랜치 이름 비어있지 않음을 검증한다.
  - 잠금/해제 로직은 아직 명시적 메서드가 없으므로 향후 `Lock`, `Unlock` 명령을 도메인 계층으로 끌어올려 감사 추적을 단일 책임으로 관리해야 한다.
- **설계 메모:** Git Hook와 동기화를 위해 브랜치 생성·삭제는 항상 실제 Git 레퍼런스와 함께 수행되어야 하며, 잠금 상태는 UI/CLI에서 즉시 확인 가능해야 한다.

## Runner Aggregate
- **루트:** `Runner` (`src/main/java/io/jgitkins/server/domain/aggregate/Runner.java`).
- **의도:** 빌드 에이전트 등록, 토큰 기반 활성화, 스코프(전역/조직/저장소)를 관리한다.
- **구성요소:** `RunnerStatus`, `RunnerScopeType`, `scopeTargetId`, `token`, `description`, IP, `lastHeartbeatAt`.
- **주요 동작 및 불변 조건:**
  - `create`는 설명/스코프/대상 검증과 토큰 자동 발급을 수행한다.
  - `activate`는 토큰 일치와 OFFLINE 상태를 요구하고, IP·하트비트를 갱신한다.
  - `withStatus`, `withLastHeartbeatAt` 등은 불변 객체 패턴으로 상태를 교체한다.
- **설계 메모:** 스코프 해석 순서(GLOBAL → ORGANIZE → REPOSITORY)는 할당 서비스가 담당하지만, enum 확장 가능성을 염두에 두고 도메인 검증을 느슨하게 유지해야 한다.

## Job Aggregate
- **루트:** `Job` (`src/main/java/io/jgitkins/server/domain/aggregate/Job.java`).
- **의도:** 특정 커밋/브랜치에 대한 실행 의도를 캡슐화하고, `JobHistory` 리스트로 상태 전이를 추적한다.
- **구성요소:** `JobId`, `RepositoryId`, `CommitHash`, `BranchName`, `UserId`, 생성시각, `List<JobHistory>`.
- **주요 동작 및 불변 조건:**
  - `create`는 자동 ID와 최초 `PENDING` 히스토리를 만든다.
  - `publish`는 현재 상태가 `PENDING`일 때만 허용하며 `IN_PROGRESS` 히스토리를 추가한다.
  - `completeSuccess/Failure`는 `IN_PROGRESS` 상태에서만 호출 가능하며, 히스토리를 append-only로 유지한다.
  - `getCurrentStatus`, `getLatestHistory`는 최소 한 개 히스토리가 존재해야 함을 강제한다.
- **설계 메모:** ERD에 있는 `QUEUED`, `CANCELED` 등 추가 상태는 `JobHistory` 팩토리 확장으로 흡수해야 하며, 러너 재시도는 새로운 히스토리로만 표현하여 감사 가능성을 확보한다.

## 교차 애그리게이트 고려사항
- 조직/저장소 멤버십은 별도 엔터티지만 권한 검증은 애그리게이트 경계(Organize → Repository) 순으로 이뤄진다.
- `OrganizeCreated`, `RepositoryProvisioned`, `RunnerActivated`, `JobQueued` 등 핵심 이벤트를 도메인에서 직접 발생시키도록 해 애플리케이션 계층과 인프라 계층 간 결합을 줄였다.
- 다중 애그리게이트 트랜잭션(예: 저장소 생성 시 기본 브랜치 생성)은 애플리케이션 서비스가 조정하되, 각 애그리게이트가 제공하는 팩토리/검증 로직을 재사용해야 한다.

## 도메인 이벤트 인프라
- `AbstractAggregateRoot`(`src/main/java/io/jgitkins/server/domain/aggregate/AbstractAggregateRoot.java`)를 추가하여 애그리게이트가 발생시킨 이벤트를 in-memory로 적재하고, 애플리케이션 서비스에서 트랜잭션 커밋 이후 발행할 수 있게 했다.
- 이벤트 정의(`src/main/java/io/jgitkins/server/domain/event/*.java`)
  - `OrganizeCreatedEvent`: `Organize.create()` 호출 시 발생. 조직 경로/소유자 정보가 외부 시스템(예: 감사 로거, notification)에 전달될 수 있다.
  - `RepositoryProvisionedEvent`: `Repository.register()` 단계에서 발생. 저장소 슬러그, 가시성, 타입 정보를 기준으로 초기 Git/CI 리소스를 준비하는 후속 파이프라인을 트리거한다.
  - `RunnerActivatedEvent`: `Runner.activate()` 성공 시 발생. Runner가 ONLINE 상태가 되었음을 알려 heartbeat 모니터와 디스패처 구성이 즉시 반영되도록 한다.
  - `JobQueuedEvent`: `Job.publish()`에서 Runner에 매핑됐을 때 발생. 커밋/브랜치/Runner 정보를 포함하므로 큐 모니터링이나 Slack 알림 등을 붙이기 쉽다.
- 각 애그리게이트의 `withIdentity`, `updateMetadata` 등 불변 객체를 반환하는 메서드는 `copyDomainEventsFrom`을 통해 기존에 기록된 이벤트를 유지한다. 이렇게 하면 persistence adapter가 새로운 도메인 객체를 반환하더라도 이벤트가 사라지지 않는다.

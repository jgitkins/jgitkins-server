# Task ID: 5

**Title:** 버그/핫픽스

**Status:** in-progress

**Dependencies:** None

**Priority:** high

**Description:** 사용자 영향 버그 및 긴급 수정 작업

**Details:**

카테고리 기반 상위 Task

**Test Strategy:**

재현/수정/회귀 테스트

## Subtasks

### 5.1. repository tree 디렉토리 오표시 핫픽스

**Status:** done  
**Dependencies:** None  

repositories/detail 화면에서 디렉토리가 파일 아이콘으로 노출되는 문제에 대해 원인 분석, server tree type 계약 복구, web 캐시 무효화, 회귀 테스트 및 배포 후 확인까지 하나의 핫픽스 단위로 처리한다.

**Details:**

<info added on 2026-03-12T03:51:33.426Z>
server의 RepositoryGitFileAdapter.java에서 Git 객체(tree/blob)를 FileEntry.type으로 매핑하는 로직을 복구하여 디렉토리/파일 타입 오표시 문제를 해결했습니다. jgitkins-web의 tree 캐시 prefix는 v2로 유지하여 기존에 잘못된 타입으로 캐시된 데이터를 재사용하는 것을 방지했습니다. 이를 검증하기 위해 jgitkins-server/src/test/java/org/jgitkins/server/adapter/git/RepositoryGitFileAdapterTest.java에 bare repository 기반의 어댑터 테스트를 추가했으며, RepositoryContentController.java의 tree 응답 타입에 대한 검증 테스트를 jgitkins-server/src/test/java/org/jgitkins/server/controller/RepositoryContentControllerTest.java에 추가했습니다. 마지막으로 jgitkins-web/src/test/js/services/RepositoryDetailServiceTest.js 회귀 테스트를 통해 기존 기능에 대한 영향이 없음을 확인했습니다.
</info added on 2026-03-12T03:51:33.426Z>

### 5.2. repositories/new owner namespace 초기화 계약 복구 핫픽스

**Status:** in-progress  
**Dependencies:** None  

repositories/new 진입 시 owner 영역에 로그인 사용자 namespace 가 노출되지 않는 문제에 대해 원인 분석, view-model 계약 복구 기준 정리, owner slug 소스 분리, 최소 변경 범위 정의를 수행하는 핫픽스 단위로 관리한다.

**Details:**

<info added on 2026-03-18T04:52:03.578Z>
repositories/new 생성 화면의 owner namespace 초기화 계약 복구를 완료했습니다. io.jgitkins.web.presentation.support.RepositoryViewSupport.populateCreateModel(...) 메서드에서 ownerLabel, ownerSlug, organizes, organizeError 속성을 평면 모델로 다시 주입하도록 수정되었습니다. io.jgitkins.web.presentation.controller.RepositoryController는 io.jgitkins.web.support.SessionSupport를 통해 세션 username을 읽어 io.jgitkins.web.facade.RepositoryFacade.getInitData(...) 메서드에 전달하도록 변경했습니다. io.jgitkins.web.facade.RepositoryFacade는 개인 owner slug 계산 시 OAuth 표시명 대신 세션 username을 namespace 소스로 사용하도록 조정되었습니다. io.jgitkins.web.presentation.controller.RepositoryControllerTest에 newRepository 진입 시 세션 username이 getInitData로 전달되는 회귀 테스트를 추가했으며, `./gradlew test --tests io.jgitkins.web.presentation.controller.RepositoryControllerTest` 검증을 통과했습니다.
</info added on 2026-03-18T04:52:03.578Z>

### 5.3. 조직 조회 API NullPointerException 추적 및 방어 핫픽스

**Status:** pending  
**Dependencies:** None  

repositories/new 초기화 과정에서 연계 호출되는 조직 조회 API 경로에서 발생한 Unexpected exception 및 'Cannot read field "w1" because "src" is null' 오류에 대해 재현 경로 확인, 실제 예외 지점 식별, null-safe 처리와 계약 검증 범위를 정의하는 핫픽스 단위로 관리한다.

**Details:**

<info added on 2026-03-18T04:58:26.306Z>
[BUGFIX] RepositoryProvisionedEventListener 경로에서 bare repository 초기 README 커밋이 실패하던 문제를 수정했습니다. 이는 src/main/java/io/jgitkins/server/infrastructure/adapter/git/RepositoryGitCommitAdapter.java 내 RepositoryGitCommitAdapter.commit(...) 메소드가 기존에 bare repository에서 staging 없이 git.commit().call()만 수행하여 초기 커밋 생성이 불가능했던 것이 원인이었습니다. 해당 로직을 JGit의 ObjectInserter, DirCache, CommitBuilder 기반 구현으로 교체하여 blob/tree/commit 객체를 실제로 생성하고 refs/heads/{branch} 갱신까지 수행하도록 복구했습니다. 추가로 src/test/java/io/jgitkins/server/infrastructure/adapter/git/RepositoryGitCommitAdapterTest.java 파일을 작성하여 bare repository initialize 후 README 초기 커밋 생성, branch log 조회, tree 조회가 모두 정상 동작함을 검증했습니다. 관련 테스트는 ./gradlew test --tests io.jgitkins.server.infrastructure.adapter.git.RepositoryGitCommitAdapterTest --tests io.jgitkins.server.infrastructure.adapter.git.RepositoryGitFileAdapterTest 로 통과했습니다.
</info added on 2026-03-18T04:58:26.306Z>

### 5.4. repository 생성 화면 owner/organization 초기화 회귀 검증 핫픽스

**Status:** pending  
**Dependencies:** None  

개인 owner namespace 표시, 접근 가능한 organization 목록 노출, 초기화 실패 시 fallback 동작까지 포함한 repositories/new 회귀 검증 시나리오와 테스트 보강 범위를 정의하는 핫픽스 단위로 관리한다.

### 5.5. 빈 저장소 커밋 목록 조회 NullPointerException 핫픽스

**Status:** done  
**Dependencies:** None  

저장소 생성 후 초기 커밋이 없는 상태에서 /repositories/{namespace}/{repo}/branches/{branch}/commits 호출 시 RepositoryGitCommitAdapter.listCommitHistory(...)가 null branch ref를 JGit log API에 전달하여 발생하는 예외를 재현, 방어, 회귀 테스트까지 포함한 핫픽스 단위로 관리한다.

### 5.6. bare repository 초기 커밋 실패로 인한 저장소 생성 장애 핫픽스

**Status:** done  
**Dependencies:** None  

저장소 생성 후 RepositoryProvisionedEventListener 경로에서 README 초기 커밋이 실패하여 저장소 생성 플로우가 손상되던 문제에 대해 RepositoryGitCommitAdapter의 bare repository commit 구현 복구, branch ref 갱신, 회귀 테스트까지 포함한 핫픽스 단위로 관리한다.

**Details:**

<info added on 2026-03-18T05:07:31.315Z>
`RepositoryProvisionedEventListener` 경로에서 bare repository 초기 README 커밋이 실패하여 저장소 생성 플로우가 손상되던 장애를 수정했습니다. 기존 `jgitkins.server.repository.RepositoryGitCommitAdapter.commit(...)` 구현은 bare repository에 대해 staging 없이 `git.commit().call()`만 수행하고 있었기 때문에 초기 커밋 생성이 불가능했습니다. 이를 JGit의 `ObjectInserter`, `DirCache`, `CommitBuilder` 기반 구현으로 교체하여 blob/tree/commit 객체를 실제로 생성하고 `refs/heads/{branch}` 갱신까지 수행하도록 복구했습니다. `jgitkins.server.repository.RepositoryGitCommitAdapterTest`를 추가하여 bare repository 초기화 후 README 초기 커밋 생성, branch log 조회, tree 조회가 모두 정상 동작함을 검증했습니다. 관련 테스트는 `RepositoryGitCommitAdapterTest` 및 `RepositoryGitFileAdapterTest` 기준으로 통과했습니다.
</info added on 2026-03-18T05:07:31.315Z>

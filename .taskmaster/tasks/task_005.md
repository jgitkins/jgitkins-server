# Task ID: 5

**Title:** 버그/핫픽스

**Status:** done

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

**Status:** pending  
**Dependencies:** None  

repositories/new 진입 시 owner 영역에 로그인 사용자 namespace 가 노출되지 않는 문제에 대해 원인 분석, view-model 계약 복구 기준 정리, owner slug 소스 분리, 최소 변경 범위 정의를 수행하는 핫픽스 단위로 관리한다.

### 5.3. 조직 조회 API NullPointerException 추적 및 방어 핫픽스

**Status:** pending  
**Dependencies:** None  

repositories/new 초기화 과정에서 연계 호출되는 조직 조회 API 경로에서 발생한 Unexpected exception 및 'Cannot read field "w1" because "src" is null' 오류에 대해 재현 경로 확인, 실제 예외 지점 식별, null-safe 처리와 계약 검증 범위를 정의하는 핫픽스 단위로 관리한다.

### 5.4. repository 생성 화면 owner/organization 초기화 회귀 검증 핫픽스

**Status:** pending  
**Dependencies:** None  

개인 owner namespace 표시, 접근 가능한 organization 목록 노출, 초기화 실패 시 fallback 동작까지 포함한 repositories/new 회귀 검증 시나리오와 테스트 보강 범위를 정의하는 핫픽스 단위로 관리한다.

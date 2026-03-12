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

**Status:** in-progress  
**Dependencies:** None  

repositories/detail 화면에서 디렉토리가 파일 아이콘으로 노출되는 문제에 대해 원인 분석, server tree type 계약 복구, web 캐시 무효화, 회귀 테스트 및 배포 후 확인까지 하나의 핫픽스 단위로 처리한다.

**Details:**

<info added on 2026-03-12T03:51:33.426Z>
server의 RepositoryGitFileAdapter.java에서 Git 객체(tree/blob)를 FileEntry.type으로 매핑하는 로직을 복구하여 디렉토리/파일 타입 오표시 문제를 해결했습니다. jgitkins-web의 tree 캐시 prefix는 v2로 유지하여 기존에 잘못된 타입으로 캐시된 데이터를 재사용하는 것을 방지했습니다. 이를 검증하기 위해 jgitkins-server/src/test/java/org/jgitkins/server/adapter/git/RepositoryGitFileAdapterTest.java에 bare repository 기반의 어댑터 테스트를 추가했으며, RepositoryContentController.java의 tree 응답 타입에 대한 검증 테스트를 jgitkins-server/src/test/java/org/jgitkins/server/controller/RepositoryContentControllerTest.java에 추가했습니다. 마지막으로 jgitkins-web/src/test/js/services/RepositoryDetailServiceTest.js 회귀 테스트를 통해 기존 기능에 대한 영향이 없음을 확인했습니다.
</info added on 2026-03-12T03:51:33.426Z>

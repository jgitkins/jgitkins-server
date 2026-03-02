# [Task 2.13] RepositoryLifecycleService 리팩토링 계획

## 1. 개요
`RepositoryLifecycleService.java`는 저장소 생성, 조회, 삭제의 오케스트레이션을 담당하고 있습니다. 현재 검증 로직, 경로 해석, 권한 체크 등이 서비스 내부에 산재해 있어 클래스 크기가 비대해지고 응집도가 낮아진 상태입니다. 이를 헥사고날 아키텍처 및 도메인 중심 설계에 맞춰 리팩토링합니다.

## 2. 현재 상태 분석 (As-Is)
- **책임 과밀**: 한 클래스에서 약 15개 이상의 프라이빗 메서드가 복잡한 비즈니스 규칙을 처리 중.
- **복잡한 경로 해석**: 네임스페이스와 저장소 이름을 바탕으로 실제 저장소를 찾는 로직(`resolveRepositoryByPath`)이 매우 복잡함.
- **유연성 부족**: 가시성 정책(`isVisibleToRequester`)과 권한 체크 로직이 서비스 내부에 하드코딩되어 재사용이 어려움.
- **입력 검증 지연**: `RepositoryCreationContext` 준비 과정이 복잡하여, 간단한 VO 생성 오류도 DB 접근(Validator 호출) 이후에 발견될 가능성이 있음.

## 3. 리팩토링 목표 (To-Be)
- **검증 로직 분리 (`RepositoryValidator`)**: 이름 중복, 소유권 유효성, 삭제 권한 체크를 별도 컴포넌트로 위임.
- **조회 로직 분리 (`RepositoryLookupService`)**: 복잡한 경로 기반 저장소 조회 및 가시성 필터링을 캡슐화.
- **Fast-Fail 패턴 (VO First)**: 메서드 최상단에서 도메인 VO를 먼저 생성하여 입력 오류를 즉시 차단.
- **서비스 경량화**: `RepositoryLifecycleService`는 유즈케이스 흐름(Orchestration)만 담당하도록 축소.

## 4. 상세 설계안

### 4.1 RepositoryValidator (신규)
- `validateCreation(OwnerType, OwnerId, RepositoryName)`: 생성 전 이름 중복 및 소유권 확인.
- `enforceDeletionPermission(Repository, Long requesterId)`: 삭제 권한 강제 검증.

### 4.2 RepositoryLookupService (신규)
- `getRepositoryByPath(String namespace, String repoName)`: 복잡한 네임스페이스 해석 및 조회.
- `filterVisibleRepositories(List<Repository>, Long requesterId)`: 가시성 정책에 따른 리스트 필터링.

### 4.3 데이터 흐름 개선 (Create Flow)
1. `RepositoryCreateCommand` 수신
2. **VO 생성**: `RepositoryName`, `OwnerType`, `InitialCommitOptions` 등 생성
3. **사전 검증**: `RepositoryValidator.validateCreation(...)`
4. **객체 생성**: `Repository.create(...)`
5. **저장 및 물리 작업**: `repositoryPort.save` -> `repositoryGitPort.create`
6. **후속 처리**: 도메인 이벤트 발행 및 DTO 반환

## 5. 단계별 실행 계획 (Subtasks)

### Step 1. RepositoryValidator 구현
- [ ] 이름 중복 체크 및 권한 검증 메서드 추출.
- [ ] `RepositoryLifecycleService`에서 검증 로직 위임.

### Step 2. RepositoryLookupService 구현
- [ ] `resolveRepositoryByPath` 로직 이전 및 테스트 보강.
- [ ] 가시성 정책(`isVisibleToRequester`) 캡슐화.

### Step 3. Create/Delete 흐름 리팩토링
- [ ] VO 우선 생성(Fast-Fail) 적용.
- [ ] `RepositoryLifecycleService`를 Orchestrator로 슬림화.

### Step 4. 테스트 및 안정화
- [ ] `RepositoryLifecycleServiceTest` 및 신규 컴포넌트 단위 테스트 수행.
- [ ] 전체 회귀 테스트 실행.

## 6. 완료 기준 (DoD)
- `RepositoryLifecycleService` 클래스 라인 수가 50% 이상 감소함.
- 모든 검증 로직이 `RepositoryValidator`로 캡슐화됨.
- 경로 기반 저장소 조회가 별도 서비스로 분리되어 테스트 가능해짐.

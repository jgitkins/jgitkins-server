# Task 2.11 - BranchService 리팩토링 계획

## 1) 목표
- 대상: `src/main/java/io/jgitkins/server/application/port/service/BranchService.java`
- 목적:
  - 서비스 오케스트레이션 책임을 명확히 분리한다.
  - 예외/에러코드 정합성을 맞춘다.
  - Git(물리 저장소) + DB(메타데이터) 처리 순서의 일관성을 높인다.
  - 테스트 용이성과 유지보수성을 개선한다.

## 2) 현재 코드 심층 분석

### 2.1 BranchService 책임 과밀
- 현재 `BranchService`는 한 클래스에서 아래를 모두 수행한다.
  - Repository/Branch 조회
  - 권한 검사
  - 생성/삭제 정책 검증
  - Git 조작 호출
  - DB 저장/삭제 호출
- 결과:
  - 플로우 이해가 어렵고 변경 영향 범위가 넓다.
  - 단위 테스트에서 mock 조합이 과도해진다.

### 2.2 예외 경계가 불명확
- `BranchService`/UseCase/Controller가 `IOException`을 시그니처로 노출한다.
- 반면 실제 구현(`BranchJGitAdapter`)은 대부분 `JgitkinsException`으로 wrapping 하고 있어 checked exception 노출의 실효성이 낮다.
- 결과:
  - 계층 경계에서 예외 정책이 불일치한다.
  - API 관점에서는 도메인/애플리케이션 예외 규칙보다 기술 예외 시그니처가 앞선다.

### 2.3 생성/삭제 플로우의 일관성 리스크
- `createBranch`: Git 생성 후 DB 저장
- `deleteBranch`: Git 삭제 후 DB 삭제
- 트랜잭션 실패/부분 실패 시 Git-DB 불일치가 발생할 수 있다.
- 보상(rollback/repair) 전략이 명시적으로 없다.

### 2.4 중복/분산된 조회 로직
- Repository/Branch 조회가 `BranchService`, `BranchCreationValidator`에 분산돼 있다.
- 소스 브랜치 검증 및 존재 검증이 분리되어 있어 책임 경계가 모호하다.

### 2.5 오류코드 정합성 이슈(연관 어댑터 포함)
- `BranchJGitAdapter`에 의미가 뒤바뀐 예외 매핑이 존재한다.
  - 신규 브랜치 중복 시 `BRANCH_NOT_FOUND` 사용
  - delete 실패 I/O에서 `BRANCH_ALREADY_EXISTS` 사용
- `BranchCreationValidator.validateNotDefaultBranch`에서 기본 브랜치 삭제 금지를 `InfrastructureErrorCode.BRANCH_DELETE_FAILED`로 표현
  - 정책 위반 성격이라 domain/application 코드가 적합

### 2.6 코드 품질 이슈
- 일부 파일에 중복 import 존재
  - `BranchCreationValidator`, `BranchJGitAdapter`
- 노이즈는 작지만 리팩토링 시 함께 정리 필요

## 3) 리팩토링 원칙
- Application Service는 유스케이스 orchestration만 담당
- 정책/규칙 검증은 명확히 분리된 컴포넌트로 위임
- Port 경계 밖 기술 예외는 `JgitkinsException`으로 통일
- 에러코드는 “원인 성격” 기준으로 매핑
  - 정책/정합성 위반: domain/application
  - 시스템/IO 장애: infrastructure

## 4) 설계 변경안

### 4.1 BranchService 경량화
- `BranchService`는 흐름 제어만 담당:
  - `loadRepositoryOrThrow`
  - `assertWritePermission`
  - `create/delete orchestration`
- 상세 조회/검증은 전용 컴포넌트로 이동:
  - (A) `BranchLookupService` 또는
  - (B) 기존 `BranchCreationValidator`를 `BranchPolicyValidator`로 확장

### 4.2 예외 경계 정리
- UseCase/Service/Controller의 `throws IOException` 제거
- Port 구현 내부에서 기술 예외는 `JgitkinsException(InfrastructureErrorCode.*)`로 변환

### 4.3 생성/삭제 일관성 전략 명시
- 단기: 현재 순서 유지 + 실패 시 명시 로그/보상 포인트 추가
- 중기: outbox 또는 보상 트랜잭션 전략 도입 후보 문서화

### 4.4 오류코드 교정
- `BranchJGitAdapter`
  - 브랜치 중복: `BRANCH_ALREADY_EXISTS`
  - 브랜치 없음: `BRANCH_NOT_FOUND`
  - I/O 장애: `InfrastructureErrorCode.BRANCH_CREATE_FAILED/BRANCH_DELETE_FAILED`
- 기본 브랜치 삭제 금지:
  - `DomainErrorCode.RULE_VIOLATION` 또는 `ApplicationErrorCode.BAD_REQUEST` 중 하나로 통일
  - (권장) `DomainErrorCode.RULE_VIOLATION`

## 5) 단계별 실행 계획

### Step 1. 시그니처/경계 정리
- `BranchCreateUseCase`, `BranchDeleteUseCase`, `BranchLoadUseCase`의 `throws IOException` 제거
- `BranchController`의 `throws IOException` 제거
- 상태: [x] 완료
  - 적용 파일:
    - `application/port/in/BranchCreateUseCase.java`
    - `application/port/in/BranchDeleteUseCase.java`
    - `application/port/in/BranchLoadUseCase.java`
    - `presentation/api/rest/BranchController.java`
    - `application/port/out/BranchPort.java`
    - `application/port/out/BranchGitPort.java`

### Step 2. 책임 분리
- 조회/검증 로직을 별도 컴포넌트로 이동
- `BranchService`는 orchestrator로 축소
- 상태: [x] 완료(경량화 범위)
  - `BranchService`에 `requireWritableRepository`, `resolveNamespace` 도입
  - 생성/삭제 플로우에서 중복 권한/namespace 로직 제거

### Step 3. 오류코드 정합성 수정
- `BranchJGitAdapter`/`BranchCreationValidator` 오류코드 재매핑
- 중복 import 및 불필요 코드 정리
- 상태: [x] 완료
  - `BranchJGitAdapter`:
    - 소스 브랜치 없음 -> `SOURCE_BRANCH_NOT_FOUND`
    - 대상 브랜치 중복 -> `BRANCH_ALREADY_EXISTS`
    - delete/create 기술 실패 -> `InfrastructureErrorCode.BRANCH_*_FAILED`
  - `BranchCreationValidator`:
    - 기본 브랜치 삭제 금지 -> `DomainErrorCode.RULE_VIOLATION`
  - 중복 import 정리 완료

### Step 4. 테스트 보강
- 서비스 단위 테스트:
  - 권한 실패, 소스 브랜치 없음, 중복 브랜치, 기본 브랜치 삭제 차단
- 컨트롤러/WebMvc:
  - 예외 응답 코드/메시지/source 검증
- 어댑터 단위(가능 시):
  - create/delete 실패 케이스 에러코드 검증
- 상태: [~] 부분 완료(환경 블로커)
  - `BranchServiceTest`, `BranchCreationValidatorTest` 시그니처 변경 반영
  - `RepositoryUploadPermissionGuardTest` 신설 및 `FileServiceTest` 참조 정리
  - `./gradlew test --tests ...Branch*` 실행 시, Branch 테스트 외 기존 테스트 컴파일 오류로 중단됨:
    - `GitAuthChallengeFilterTest` (클래스 미존재)
    - `GitSmartHttpCanonicalRedirectFilterTest` (클래스 미존재)

## 6) 완료 기준 (DoD)
- `BranchService` 메서드 복잡도 감소(조회/검증 분리)
- public API 계층에서 `IOException` 시그니처 제거
- 브랜치 관련 예외코드가 의미에 맞게 매핑
- 관련 테스트 통과 및 회귀 영향 없음

## 7) 리스크 및 대응
- 리스크: 예외 타입 변경으로 기존 테스트/클라이언트 기대값 변화
- 대응: `GlobalExceptionHandler` 기반 응답 계약 테스트를 함께 보강

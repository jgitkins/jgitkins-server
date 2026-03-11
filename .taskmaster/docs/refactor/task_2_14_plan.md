# [Task 2.14] RepositoryOverviewService 리팩토링 및 예외 처리 개선

## 1. 개요
`RepositoryOverviewService.java`는 저장소의 전반적인 상태(정보, 브랜치 목록, 파일 트리, 권한)를 조회하는 오케스트레이션 역할을 수행합니다. 현재 Checked Exception(`IOException`)을 노출하고 있으며, 다른 서비스들과의 일관성이 부족한 부분을 리팩토링합니다.

## 2. 현재 상태 분석 (As-Is)
- **예외 누수**: `RepositoryOverviewUseCase` 및 서비스가 `throws IOException`을 선언하여 인프라 계층의 기술적 예외를 상위 계층(Controller)까지 노출함.
- **불명확한 에러 처리**: `FileTreeLoadUseCase` 호출 시 발생할 수 있는 오류에 대한 도메인 예외 변환 로직이 부재함.
- **의존성 사용**: `RepositoryLoadUseCase`, `BranchLoadUseCase` 등을 적절히 사용하고 있으나, 예외 처리 및 데이터 흐름이 선언적이지 못함.

## 3. 리팩토링 목표 (To-Be)
- **`throws IOException` 제거**: 모든 기술적 예외를 `JgitkinsException`으로 래핑하여 아키텍처 규칙 준수.
- **에러 코드 적용**: 인프라 장애 시 `InfrastructureErrorCode.GIT_OPERATION_FAILED` 등을 활용.
- **인터페이스 정돈**: UseCase 인터페이스에서 Checked Exception 제거.
- **가독성 개선**: 스트림 및 Optional을 활용하여 로직을 더 간결하고 명확하게 표현.

## 4. 상세 설계안

### 4.1 UseCase 인터페이스 수정
- `RepositoryOverviewUseCase.getOverview`에서 `throws IOException` 제거.
- `FileTreeLoadUseCase.getTree`에서 `throws IOException` 제거.

### 4.2 RepositoryOverviewService 리팩토링
- `getOverview`에서 `IOException`이 발생하지 않도록 하위 UseCase 호출부의 예외 처리를 위임하거나 직접 처리.
- `resolveBranch` 등 헬퍼 메서드를 더 간결하게 다듬음.

## 5. 단계별 실행 계획 (Subtasks)

### Step 1. UseCase 인터페이스 정리
- [x] `RepositoryOverviewUseCase`, `FileTreeLoadUseCase`의 `throws IOException` 제거.

### Step 2. FileTreeLoadService (구현체) 리팩토링
- [x] `IOException`을 처리하도록 `FileTreeLoadService` 수정 (RepositoryFileService에서 구현됨).

### Step 3. RepositoryOverviewService 리팩토링
- [x] `getOverview` 흐름 개선 및 예외 래핑.
- [x] 불필요한 import 및 private 메서드 정리.

### Step 4. 테스트 검증
- [x] `RepositoryOverviewServiceTest` 작성 또는 업데이트.
- [x] 전체 빌드 및 테스트 수행.

## 6. 완료 기준 (DoD)
- 모든 관련 UseCase 및 Service에서 `throws IOException`이 제거됨.
- 저장소 오버뷰 조회 기능이 정상 작동하며, 오류 시 일관된 `JgitkinsException`이 발생함.

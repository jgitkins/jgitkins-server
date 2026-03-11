# [Task 2.12] PushHook 리팩토링 및 헥사고날 아키텍처 적용 계획 (최종)

## 1. 개요
`PushHook.java`는 Git Push 발생 시 후속 처리(Job 생성, 브랜치 갱신 등)를 트리거하는 핵심 진입점입니다. 현재 인프라 기술에 강하게 결합된 로직을 분리하고, 헥사고날 아키텍처의 포트-어댑터 패턴을 통해 도메인 중심의 이벤트 처리를 구현하는 것을 목표로 합니다.

## 2. 현재 상태 분석 (As-Is)
- **위치**: `io.jgitkins.server.infrastructure.config.git.hook.push.PushHook`
- **문제점**:
    1. **경로 기반 메타데이터 추출**: 파일 시스템 구조에 직접 의존하여 저장소 정보를 파싱함.
    2. **도메인 로직 누수**: 브랜치 생성/삭제 여부 판단 및 저장소 로딩 로직이 어댑터에 포함됨.
    3. **강한 결합**: `HttpServletRequest`, `SecurityContextHolder` 등을 직접 참조하여 요청자를 식별함.

## 3. 리팩토링 원칙 (To-Be)
- **Inbound Adapter (Driving)**: `PushHook`은 JGit 데이터를 파싱하여 `PushEventCommand`로 변환하고 유즈케이스를 호출하는 역할만 수행.
- **Mapper 활용**: 애플리케이션 계층에서 정의된 `PushEventCommand` 빌더나 팩토리를 활용하여 어댑터 내 매핑 로직을 슬림화함.
- **Outbound Port (Driven)**: 저장소 도메인 로딩, 브랜치 영속화 등은 `RepositoryPort`, `BranchPort`를 통해 애플리케이션 계층에서 처리.
- **도메인 중심 처리**: 브랜치 생성/삭제에 따른 영속성 동기화 및 Job 생성은 `PushEventHandleUseCase` 내부 비즈니스 로직으로 구현.

## 4. 상세 설계안

### 4.1 Inbound Adapter 리팩토링 (`PushHook`)
- JGit의 `ReceiveCommand` 목록을 분석하여 순수한 데이터 뭉치인 `PushEventCommand` 생성.
- 요청자 식별 로직을 별도 유틸리티나 포트로 분리하여 `HttpServletRequest` 직접 의존성 제거.

### 4.2 Application Service 및 Port 확장
- **`PushEventHandleUseCase` 구현체**:
    1. `RepositoryPort`를 통해 저장소 도메인 로딩.
    2. `PushEventCommand`의 내용에 따라 브랜치 생성/삭제 로직 수행 (`BranchPort` 활용).
    3. 변경된 상태에 따라 `JobDispatchUseCase` 등을 호출하여 후속 작업 트리거.
- **`RepositoryResolutionPort` (신규)**:
    - JGit `Repository` 객체 또는 경로 정보를 바탕으로 도메인상의 `RepositoryId`를 찾아주는 기능 제공.

## 5. 코드 스니펫 예시 (Expected)

### 5.1 PushHook (Inbound Adapter)
```java
public class PushHook implements PostReceiveHook {
    private final PushEventHandleUseCase pushEventHandleUseCase;
    private final PushEventRequestResolver pushEventRequestResolver;

    @Override
    public void onPostReceive(ReceivePack rp, Collection<ReceiveCommand> commands) {
        // 1. 기술 데이터에서 기본 정보 추출
        String gitDirPath = rp.getRepository().getDirectory().getAbsolutePath();
        Long requesterId = pushEventRequestResolver.resolveRequesterId();

        // 2. Command 생성 (Application 계층 빌더 활용)
        PushEventCommand command = PushEventCommand.from(gitDirPath, requesterId, commands);

        // 3. 유즈케이스 호출
        pushEventHandleUseCase.handle(command);
    }
}
```

### 5.2 PushEventHandleService (Application Service)
```java
@Service
@RequiredArgsConstructor
public class PushEventHandleService implements PushEventHandleUseCase {
    private final RepositoryPort repositoryPort;
    private final BranchPort branchPort;
    private final JobDispatchUseCase jobDispatchUseCase;

    @Override
    @Transactional
    public void handle(PushEventCommand command) {
        // 1. 저장소 로딩 (Port 활용)
        Repository repository = repositoryPort.findByPath(command.getGitDirPath())
                .orElseThrow(() -> new JgitkinsException(ApplicationErrorCode.REPOSITORY_NOT_FOUND));

        // 2. 브랜치 상태 영속화
        if (command.isBranchCreated()) {
            branchPort.save(Branch.create(repository.getId(), command.getBranchName(), command.getCommitHash()));
        } else if (command.isBranchDeleted()) {
            branchPort.delete(repository.getId(), command.getBranchName());
        }

        // 3. 후속 작업 트리거 (Job 생성 등)
        jobDispatchUseCase.dispatch(PushEvent.from(repository, command));
    }
}
```

## 6. 단계별 실행 계획 (Subtasks)

### Step 1. 도메인 및 유즈케이스 정의
- [ ] `PushEventCommand` 필드 확정 및 매핑 팩토리 메서드 구현.
- [ ] `PushEventHandleUseCase` 인터페이스 정의.

### Step 2. Outbound Port 및 어댑터 구현
- [ ] 경로 기반 저장소 조회를 위한 `RepositoryPort.findByPath` 또는 전용 Port 구현.
- [ ] 요청자 식별을 위한 `PushEventRequestResolver` (Adapter 지원군) 구현.

### Step 3. PushHook 어댑터 슬림화
- [ ] `PushHook` 클래스의 복잡한 파싱 로직을 `PushEventCommand` 내부 또는 전용 Mapper로 이전.
- [ ] `HttpServletRequest` 직접 의존성 제거.

### Step 4. 비즈니스 로직 구현 (Application Layer)
- [ ] `PushEventHandleService`에서 브랜치 영속성 동기화 및 Job 생성 흐름 구현.

## 7. 완료 기준 (DoD)
- `PushHook` 클래스는 오직 데이터 변환 및 유즈케이스 호출만 담당함.
- 파일 시스템 구조 변경이 비즈니스 로직(유즈케이스)에 영향을 주지 않음.
- 브랜치 생성/삭제 이벤트가 도메인 모델에 정상적으로 반영됨.

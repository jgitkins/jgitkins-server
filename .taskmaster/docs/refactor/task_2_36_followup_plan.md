# 리팩토링 계획서

### 제목
- **리팩토링 계획**: `JobPersistenceAdapter.findNextDispatchableJob` 선택 쿼리 및 경합 처리 개선 계획 수립하였음

### 배경 (왜?)
- 현재 [JobPersistenceAdapter.java](/Users/hwiryungkim/task/sources/jgitkins/jgitkins-server/src/main/java/io/jgitkins/server/infrastructure/adapter/persistence/JobPersistenceAdapter.java) 의 `findNextDispatchableJob(...)`는 메서드명과 달리 실제로는 "다음 dispatch 가능 job 선택"을 효율적으로 수행하지 못하고 있음을 확인하였음
- 현재 구현은 모든 Job을 조회한 뒤 각 Job의 latest history를 다시 읽고 `PENDING` 상태 여부를 애플리케이션 메모리에서 판별하는 구조이므로, pending job 수가 증가할수록 조회 비용이 커지는 문제가 있음을 확인하였음
- 현재 `matchesScope(...)`는 `GLOBAL`, `ORGANIZE`, `REPOSITORY` 수준의 최소 조건만 검토하고 있어, 실질적인 runner 접근 가능 조건을 충분히 반영하지 못하는 상태임을 확인하였음
- 현재 경합 처리는 `saveHistory(...)` 단계의 낙관적 확인에만 의존하고 있으므로, 중복 조회 이후 실패가 반복될 가능성이 있으며 dispatch 효율 저하가 발생할 수 있음을 확인하였음
- 따라서 본 계획은 dispatch selection query, 접근 가능 조건, race 처리 전략을 후속 리팩토링 범위로 명확히 정의하기 위해 작성함

### 목표 (Goals)
- `findNextDispatchableJob(...)`가 이름에 맞는 dispatch selection 로직이 되도록 정리함
- 모든 Job 전체 탐색 대신 dispatch 후보를 더 이른 단계에서 축소하도록 조회 전략을 개선함
- runner 접근 가능 조건을 최소 scope match 이상의 명시적 규칙으로 정리함
- race 처리 전략에서 DB 기반 낙관적 제어와 Valkey 기반 분산 제어의 우선순위를 명확히 함
- 향후 구현 시 기능 범위가 과도하게 확장되지 않도록 단계별 변경 기준을 수립함

### 범위 (Scope)
- **수정 대상**
  - `src/main/java/io/jgitkins/server/infrastructure/adapter/persistence/JobPersistenceAdapter.java`
  - `src/main/java/io/jgitkins/server/application/port/out/JobPersistencePort.java`
  - `src/main/java/io/jgitkins/server/application/dto/DispatchableJob.java`
  - `src/main/java/io/jgitkins/server/application/dto/RunnerDispatchContext.java`
  - 필요 시 dispatch 조회 전용 MyBatis mapper query 또는 condition 확장부
  - 관련 단위 테스트 및 persistence adapter 테스트
- **수정 제외 대상**
  - Job dispatch gRPC API 스펙 변경은 제외함
  - Job aggregate 상태 머신 자체 변경은 제외함
  - Runner 등록/활성화 정책 변경은 제외함
  - Valkey 도입은 즉시 구현 대상으로 확정하지 않고 검토 범위로 한정함

### 계획 (Plan)
- **단계 1**: dispatch 후보 선택 기준을 명세화함
  - `PENDING` 상태의 Job만 대상이 되는지 명확히 고정함
  - runner 접근 가능 조건을 최소 규칙으로 정의함
  - 현재 단계에서는 다음을 기본 규칙으로 삼는 방안을 검토함
    - runner scope와 repository owner/scopeTargetId가 일치하여야 함
    - repository 정보가 존재하여야 함
    - latest history가 `PENDING` 이어야 함
  - 예시를 아래와 같이 정리함

```java
// BEFORE
List<JobEntity> jobs = jobEntityMbgMapper.selectByCondition(condition);
for (JobEntity jobEntity : jobs) {
    // latest history 조회 후 메모리에서 판별
}

// AFTER
Optional<DispatchableJob> job = jobDispatchQueryPort.findNextPendingJobForRunner(context);
```

- **단계 2**: 조회 책임을 "전체 탐색"에서 "선택 쿼리"로 전환함
  - 현재 `JobEntity` 전체 조회 후 반복 탐색하는 구조를 지양함
  - latest history가 `PENDING` 인 Job만 대상으로 좁힐 수 있는 조회 경로를 우선 도입함
  - repository scope 조건도 가능하면 SQL 또는 mapper condition 수준에서 먼저 반영함
  - 예시를 아래와 같이 정의함

```java
// BEFORE
Optional<DispatchableJob> findNextDispatchableJob(RunnerDispatchContext context);

// AFTER
Optional<DispatchableJob> findNextDispatchableJob(RunnerDispatchContext context);
// 내부 구현은 "전체 Job 조회"가 아니라
// "PENDING latest history + scope match" 기준의 선택 쿼리로 변경함
```

- **단계 3**: 접근 가능 조건을 별도 규칙으로 정리함
  - 현재 `matchesScope(...)`는 최소 구현이므로, 추후 policy 또는 support 메서드로 정리할 수 있는지 검토함
  - scope 외 조건이 추가되더라도 adapter 메서드 본문이 과도하게 비대해지지 않도록 분리 기준을 마련함
  - 예시를 아래와 같이 정의함

```java
// BEFORE
if (repo == null || !matchesScope(context, repo)) {
    continue;
}

// AFTER
if (!dispatchEligibilityPolicy.isAccessible(context, repositoryView)) {
    continue;
}
```

- **단계 4**: race 처리 전략의 우선순위를 정리함
  - 1차 방안은 현재 `saveHistory(...)`의 낙관적 제어를 유지·보강하는 방향으로 설정함
  - 2차 방안은 조회 시점 경쟁을 줄이기 위한 쿼리 개선을 적용함
  - 3차 방안으로 Valkey 기반 분산 락을 검토하되, 필수 전제로 보지 않음
  - Valkey는 다음 조건에서만 검토 가치가 있다고 판단함
    - 다수 runner가 동시에 polling 하여 DB 경쟁이 높은 경우
    - DB 낙관적 제어만으로 중복 조회 비용이 과도한 경우
  - 예시를 아래와 같이 정의함

```java
// BEFORE
Optional<Long> historyId = jobPort.saveHistory(job, previousHistory);
if (historyId.isEmpty()) {
    return Optional.empty();
}

// TO-BE 1차
// saveHistory 기반 최종 경합 제어는 유지함
// findNextDispatchableJob 단계에서 후보 수를 먼저 줄임

// TO-BE 2차
// 필요 시 "job:{id}:dispatch-lock" 형태의 Valkey 락을 검토함
```

- **단계 5**: 테스트 및 검증 기준을 수립함
  - scope별 dispatch 후보 선택이 올바르게 작동하는지 검증함
  - `PENDING` 이 아닌 latest history는 제외되는지 검증함
  - 동시 dispatcher 상황에서 하나만 성공하고 나머지는 실패하는지 검증함
  - Valkey를 도입하지 않는 단계에서도 현재 낙관적 제어가 유지되는지 회귀 테스트를 보강함
  - 예시를 아래와 같이 정리함

```java
findNextDispatchableJob_returnsOnlyPendingJobForRepositoryScope()
findNextDispatchableJob_skipsJobWhenLatestHistoryIsNotPending()
dispatch_returnsEmpty_whenConcurrentSaveHistoryFails()
```

- **단계 6**: 문서화 및 후속 의사결정 기준을 남김
  - DB 조회 개선만으로 충분한지, Valkey 도입이 필요한지 판단 기준을 문서화함
  - dispatch accessibility가 authorization 정책으로 확장될 경우 어느 계층에서 다룰지 후속 논의를 연결함

### 기대효과 (Expected Benefits)
- dispatch selection 로직이 메서드명과 실제 구현 의미가 일치하게 되어 가독성과 유지보수성이 향상될 것으로 예상함
- 전체 Job 탐색을 줄이면 runner polling 빈도가 높을 때 DB 조회 부담을 완화할 수 있을 것으로 예상함
- runner 접근 가능 조건이 명시되면 정책 변경 시 수정 지점을 더 쉽게 파악할 수 있을 것으로 예상함
- race 처리 전략이 DB 우선인지, Valkey 보조인지 기준이 명확해져 과설계 위험을 줄일 수 있을 것으로 예상함

### 예시 (선택 방안 기준 코드 스니펫)

#### AS-IS (현재 구조)
```java
public Optional<DispatchableJob> findNextDispatchableJob(RunnerDispatchContext context) {
    JobEntityCondition condition = new JobEntityCondition();

    List<JobEntity> jobs = jobEntityMbgMapper.selectByCondition(condition);
    for (JobEntity jobEntity : jobs) {
        JobHistoryEntityCondition historyCondition = new JobHistoryEntityCondition();
        historyCondition.createCriteria().andJobIdEqualTo(jobEntity.getId());
        historyCondition.setOrderByClause("CREATED_AT DESC");
        List<JobHistoryEntity> historyEntities = jobHistoryEntityMbgMapper.selectByCondition(historyCondition);

        if (!historyEntities.isEmpty() && JobStatus.PENDING.name().equals(historyEntities.get(0).getStatus())) {
            RepositoryEntity repo = repositoryEntityMbgMapper.selectByPrimaryKey(jobEntity.getRepositoryId());
            if (repo == null || !matchesScope(context, repo)) {
                continue;
            }
            return Optional.of(...);
        }
    }
    return Optional.empty();
}
```

#### TO-BE (개선 제안 구조)
```java
public Optional<DispatchableJob> findNextDispatchableJob(RunnerDispatchContext context) {
    Optional<DispatchCandidateView> candidate = jobDispatchQueryMapper.findNextPendingCandidate(context);
    if (candidate.isEmpty()) {
        return Optional.empty();
    }

    if (!dispatchEligibilityPolicy.isAccessible(context, candidate.get())) {
        return Optional.empty();
    }

    return Optional.of(toDispatchableJob(candidate.get()));
}
```

### 주의사항
- **포맷팅 금지**: 기능과 구조 개선 범위만 다루며 포맷팅 변경은 수행하지 않음
- **기존 기능 보장**: 기존 dispatch 성공/실패 동작은 유지되어야 하며, 경합 실패 시 empty 반환 계약을 깨지 않도록 주의함
- **계획우선**: 본 문서는 구현 전 검토 문서이며, 작성 단계에서는 코드 구현을 진행하지 않음
- **예시전체나열**: 전체 탐색 구조와 선택 쿼리 구조의 BEFORE / AFTER 예시를 모두 명시하였음
- **Valkey 신중 검토**: Valkey는 문제 정의가 충분히 정리된 이후 보조 수단으로만 검토하며, 1차 해법으로 확정하지 않음

### 결론 (추후작성)
- `findNextDispatchableJob(...)`의 핵심 문제는 네이밍이 아니라 "선택 쿼리와 접근 가능 조건이 아직 최소 구현 수준"이라는 점으로 정리하였음
- 후속 구현은 `조회 후보 축소 -> 접근 가능 규칙 정리 -> 경합 제어 보강` 순으로 진행하는 것이 가장 안전하다고 판단하였음
- Valkey는 현재로서는 검토 옵션으로 유지하고, 먼저 DB 조회 구조와 낙관적 제어를 정리하는 방향으로 계획 수립하였음

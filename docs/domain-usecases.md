# 도메인별 Use Case 플로우 (Senior Review)

## Organize
### Organize 생성
> 조직을 생성하고, 조직을 생성한 사용자를 조직에 할당.

```mermaid
flowchart TD
    A[Organize Creation API] --> B[Organization Exist?]
    B --> C{Exist?}
    C -->|Yes| D[Respond Conflict Message]
    C -->|No| E[Creation Organization]
    E --> F[Assign Creator as Member]
    F --> G[Respond Creation Result]


```


<br>
<br>
<br>
<br>

``` mermaid 

flowchart TD
    A[Client POST organizations] --> B[Controller parse request and validate]
    B -->|valid| C[UseCase OrganizeCreate]
    B -->|invalid| E1[400 Bad Request]

    C --> D[Check idempotency]
    D -->|already processed| E2[Return previous result]
    D -->|new request| F[Check authorization]

    F -->|forbidden| E3[403 Forbidden]
    F -->|authorized| G[Normalize request data]

    G --> H[Check duplicate organization]
    H -->|duplicate| E4[409 Conflict]
    H -->|not duplicate| I[Load required references]

    I -->|missing reference| E5[404 Not Found]
    I -->|ok| J[Create Organization aggregate]

    J --> K{Domain rules valid}
    K -->|invalid| E6[422 Domain rule violation]
    K -->|valid| L[Save organization]

    L --> M[Call external services]
    M -->|failed| N[Handle failure or retry]
    M -->|success| O[Publish OrganizationCreated event]

    O --> P[Map response DTO]
    P --> Q[201 Created]

```

## Repository 프로비저닝 & 초기 콘텐츠 플래그
```mermaid
flowchart TD
    A[Repository 생성 명령] --> B[Repository.create]
    B --> C[인자 검증/정규화]
    C --> D[Repository.register]
    D --> E[Provisioned 이벤트 기록]
    E --> F[RepositoryPersistencePort.save]
    F --> G{初期 콘텐츠 필요?}
    G -->|Yes| H[프로비저닝 워커 호출]
    G -->|No| I[동기화 완료]
    H --> J[응답]
    I --> J
```

## Runner 등록 & 활성화
```mermaid
flowchart TD
    A[Runner 등록 요청] --> B[Runner.create]
    B --> C[RunnerPersistencePort.save]
    C --> D[Runner.activate(token,ip)]
    D --> E{토큰 유효?}
    E -->|No| F[예외 발생]
    E -->|Yes| G[ONLINE + heartbeat]
    G --> H[RunnerActivatedEvent 기록]
    H --> I[RunnerCommandPort.update]
    I --> J[디스패처 알림]
```

## Job 큐잉 & 히스토리 관리
```mermaid
flowchart TD
    A[JobService.createJob] --> B[Job.create]
    B --> C[초기 JobHistory(PENDING)]
    C --> D[JobPersistencePort.save]
    D --> E[RunnerAllocation 호출]
    E --> F{Runner 할당?}
    F -->|No| G[대기 상태 유지]
    F -->|Yes| H[Job.publish(runnerId)]
    H --> I[JobHistory IN_PROGRESS]
    I --> J[JobQueuedEvent 기록]
    J --> K[JobPersistencePort.update]
    K --> L[Dispatcher 알림]
```



## Option2 (NOW)
``` mermaid 
graph LR
    A[SERVER] <---|gRPC| D[RUNNER]
    A[SERVER] <---|gRPC| E[RUNNER2]
    A[SERVER] <---|gRPC| F[RUNNER3]
```


개선안: gRPC + 독립적 상태관리
gRPC를 사용해 Server와 Runner간의 직접적인 통신을 하도록 구성해 미들웨어 의존성을 최소화할 수 있고,
상태를 러너가 독립적으로 관리할 수 있음. 1안의 문제점인 의존성을 줄이면서 독립적인 러너 설계 가능할것으로 판단됨

### Flow
1. occur commit event
2. create job (pending)
3. a runner checks have to execute job exist to server
4. server gives jobs and runner executes them 
5. runner responds execute result to server
6. server left the result



### 
gRPC가 아닌, REST를 사용해서도 충분히 구현이 가능하나, 성능향상을 목적으로 작업이 진행될 예정
HTTP/2 기반, 헤더압축 성능 최적화를 통해 HTTP/1.1 기반으로 동작할 때보다 더 빠르고 효율적인 통신 가능성 기대
추가로 Protocol Buffers를 통해 이진 형식으로 데이터를 직렬화하고 전송화할 수 있음. 
이 방식은 테스트 기반의 JSON을 사용하는 REST보다 더 빠르고, 더 작은 크기의 데이터로 직렬화 할 수 있음


## Option1

``` mermaid
graph LR
    A[SERVER] ---> B[QUEUE]
    A[SERVER] ---> C[DATABASE]
    B <---> D[RUNNER]
    C <---> D[RUNNER]
    B <---> E[RUNNER2]
    C <---> E[RUNNER2]
    B <---> F[RUNNFR3 ...]
    C <---> F[RUNNFR3 ...]
```

### Flow
1. 커밋이벤트 발생 
2. Job 이벤트 생성 (PENDING) 
3. 스케쥴러 INQUEUE 하기위한 스케쥴링 (탐지시 큐에넣고 상태변경 INQUEUE) 
4. RUNNER Consume (및 상태변경 IN_PROGRESS) 
5. 작업 수행 후 결과 적재 (SUCCESS OR FAIL)

1안의 문제점: 
- 미들웨어, DB 의존성 증대: 
    Server는 Queue, Database에 의존하고, 각 Runner는 미들웨어들을 통해 작업을 소비하고 상태를 관리
    따라서 Runner가 늘어날수록 Queue, Database의 부담이 증대, 즉, 확장성 및 유지보수에서 문제의 소지가 됨
    Runner 가 DB에 직접 접근해서 처리해야하는점에서 독립성 저하



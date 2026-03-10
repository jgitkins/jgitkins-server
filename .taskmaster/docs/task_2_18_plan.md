# [Task 2.18] Application→Infrastructure 직접 의존 제거 (초안)

## 1. 개요
현재 `application` 계층 내부에 `infrastructure` 패키지를 직접 import 하거나, `infrastructure` 전용 타입을 예외 코드/설정/유틸 형태로 참조하는 지점이 남아 있습니다. 이는 헥사고날 아키텍처의 핵심 원칙인 "Application은 Port와 도메인 규칙에만 의존한다"는 기준을 약화시키며, 인프라 교체 비용과 테스트 격리 비용을 높입니다.

이 작업의 목표는 다음과 같습니다.

1. `application -> infrastructure` 정적 의존을 제거한다.
2. 외부 기술 세부사항은 `port.out` 또는 Infrastructure Adapter 내부에 가둔다.
3. 순수 유틸/설정/에러코드의 위치를 재정렬해 계층 경계를 명확하게 만든다.

---

## 2. 현재 확인된 직접 의존 지점

### 2.1 설정 객체 직접 참조
- `application/common/CloneUrlBuilder.java`
  - `io.jgitkins.server.infrastructure.config.RunnerRuntimeProperties` 직접 참조
- `application/support/RunnerRuntimeConfigProvider.java`
  - `io.jgitkins.server.infrastructure.config.RunnerRuntimeProperties` 직접 참조

### 2.2 Infrastructure 유틸 직접 참조
- `application/service/RepositoryLifecycleService.java`
  - `io.jgitkins.server.infrastructure.support.RepositoryPathHelper` 직접 참조
- `application/support/RepositoryLookupService.java`
  - `io.jgitkins.server.infrastructure.support.RepositoryPathHelper` 직접 참조
- `application/support/RepositoryNamespaceResolver.java`
  - `io.jgitkins.server.infrastructure.support.RepositoryPathHelper` 직접 참조

### 2.3 Infrastructure 에러코드 직접 참조
- `application/factory/CommitFileFactory.java`
  - `InfrastructureErrorCode.FILESYSTEM_ACCESS_FAILED`
- `application/service/RunnerManagementService.java`
  - `InfrastructureErrorCode.RUNNER_DELETE_FAILED`
  - `InfrastructureErrorCode.RUNNER_ACTIVATION_FAILED`

---

## 3. 방법 조사

### 방법 1. 모든 직접 의존을 Port로 치환
- 정의: 설정값, 유틸, 에러 전파까지 모두 `application.port.out` 인터페이스를 만들고 Infrastructure가 구현한다.
- 장점: 규칙이 단순하고 기계적으로 적용 가능하다.
- 단점: 순수 문자열 조합 유틸까지 Port로 만들면 과설계가 된다. 테스트 목이 불필요하게 늘어난다.

### 방법 2. 의존 유형별로 해법을 분리하는 혼합 전략
- 정의:
  - 설정/외부 런타임 값은 `port.out` 또는 Application 전용 Provider 인터페이스로 추상화
  - 순수 유틸은 `application.common` 또는 `domain` 인접 패키지로 승격/이동
  - Infrastructure 전용 에러코드는 Adapter 내부에 한정하고, Application은 `ApplicationErrorCode` 또는 계층 중립 기술 에러코드만 사용
- 장점: 헥사고날 원칙을 지키면서도 구조를 과도하게 복잡하게 만들지 않는다.
- 단점: 유형 분류 판단이 필요해 초기 설계 문서화가 필요하다.

### 방법 3. 현재 구조 유지 + 허용 목록 문서화
- 정의: 일부 직접 의존은 "예외적 허용"으로 남기고 나머지만 정리한다.
- 장점: 작업량이 가장 적다.
- 단점: 원칙이 다시 흔들리고, 이후 리팩토링 기준이 모호해진다.

### 최종 선택
- **방법 2 선택**
- 이유: `RunnerRuntimeProperties`는 외부 설정이므로 추상화 대상이 맞지만, `RepositoryPathHelper`는 순수 문자열 조합 유틸이라 Port보다 계층 재배치가 더 적합합니다. `InfrastructureErrorCode`는 Application에서 직접 들고 있으면 계층 누수가 발생하므로, 예외 번역 지점을 Application/Adapter 쪽으로 분리하는 것이 가장 일관적입니다.

---

## 4. 목표 구조

### 4.1 설정값 접근
```text
application
  port/out
    RuntimeConfigPort.java         (또는 CloneUrlConfigPort)
  support
    CloneUrlBuilder.java
  support
    RunnerRuntimeConfigProvider.java

infrastructure
  adapter/config
    RuntimeConfigAdapter.java
  config
    RunnerRuntimeProperties.java
```

예상 코드 형태:

```java
// application/port/out/RuntimeConfigPort.java
package io.jgitkins.server.application.port.out;

public interface RuntimeConfigPort {
    String serviceHost();
    String restScheme();
    Integer restPort();
    String restBasePath();
    Integer grpcPort();
    Long pollIntervalMs();
    Long busyWaitIntervalMs();
}
```

```java
// infrastructure/adapter/config/RunnerRuntimeConfigAdapter.java
@Component
@RequiredArgsConstructor
public class RunnerRuntimeConfigAdapter implements RuntimeConfigPort {

    private final RunnerRuntimeProperties properties;

    @Override
    public String serviceHost() {
        return properties.getServiceHost();
    }

    @Override
    public String restScheme() {
        return properties.getRestScheme();
    }

    // 나머지 메서드도 RunnerRuntimeProperties 위임
}
```

```java
// application/support/CloneUrlBuilder.java
@Component
@RequiredArgsConstructor
public class CloneUrlBuilder {

    private final RuntimeConfigPort runtimeConfigPort;

    public String build(String clonePath) {
        String normalizedPath = clonePath.startsWith("/") ? clonePath : "/" + clonePath;
        return "%s://%s%s".formatted(
                runtimeConfigPort.restScheme(),
                runtimeConfigPort.serviceHost(),
                normalizedPath);
    }
}
```

정리 기준:
- `CloneUrlBuilder`는 순수 공용 유틸이 아니라 설정 포트를 주입받아 URL을 조합하는 내부 협력 컴포넌트이므로 `application.common`보다 `application.support`가 더 적합하다.
- 반대로 정적 메서드 기반 순수 문자열 유틸은 `application.common`에 두는 것이 맞다.

### 4.2 저장소 경로/네임스페이스 유틸
```text
application
  common/support
    RepositoryPathPolicy.java      (또는 RepositoryPathHelper 이동)
```

예상 코드 형태:

```java
// application/common/RepositoryPathHelper.java
package io.jgitkins.server.application.common;

public final class RepositoryPathHelper {

    private RepositoryPathHelper() {
    }

    public static String buildClonePath(String namespace, String repoName) {
        return normalizeNamespace(namespace) + "/" + normalizeRepo(repoName) + ".git";
    }

    public static String buildUserNamespace(String username) {
        return normalizeNamespace(username);
    }

    public static String buildOrganizeNamespace(String organizeName) {
        return normalizeNamespace(organizeName);
    }

    private static String normalizeNamespace(String value) {
        return value == null ? "" : value.trim().replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private static String normalizeRepo(String value) {
        return value == null ? "" : value.trim().replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
```

적용 후 참조 방향:

```java
import io.jgitkins.server.application.common.RepositoryPathHelper;

String clonePath = RepositoryPathHelper.buildClonePath(namespace, repositoryPath.getValue());
```

`RepositoryPathHelper`가 외부 I/O 없이 순수 문자열 조합만 수행한다면 Infrastructure가 아니라 Application 공용 유틸로 이동하는 것이 맞습니다.

### 4.3 에러코드 사용 원칙
- Infrastructure Adapter 내부
  - `InfrastructureErrorCode` 사용 가능
- Application
  - `ApplicationErrorCode`
  - 필요 시 `TechnicalErrorCode` 같은 계층 중립 코드 신설 검토
- 변환 규칙
  - **유형 A. 기술 실패를 그대로 500 계열로 처리해도 되는 경우**
    - Adapter가 `JgitkinsException(InfrastructureErrorCode.*)`로 래핑하고 종료한다.
    - Application은 이를 별도 번역하지 않고 전파한다.
  - **유형 B. 기술 실패를 유즈케이스 의미의 예외로 치환해야 하는 경우**
    - Adapter는 `JgitkinsException`으로 감싸지 않고, 실제 기술예외 또는 계층 중립 기술예외를 그대로 던진다.
    - Application이 그 예외를 직접 잡아 `ApplicationErrorCode`로 매핑한다.
  - 핵심 규칙은 "Adapter가 항상 `JgitkinsException`으로 끝내는 것이 아니라, Application이 번역 책임을 가져야 하는 흐름이면 기술예외를 먼저 노출한다"이다.

권장 처리 흐름은 두 갈래다.

#### 유형 A. Adapter에서 종료
```java
// infrastructure adapter
try {
    // file/git/db access
} catch (IOException ex) {
    throw new JgitkinsException(InfrastructureErrorCode.FILESYSTEM_ACCESS_FAILED, "I/O failed", ex);
}
```

```java
// application service
adapterPort.doSomething(); // 별도 번역 없음
```

#### 유형 B. Application에서 번역
```java
// port
void upload(...) throws IOException;
```

```java
// infrastructure adapter
@Override
public void upload(...) throws IOException {
    fileSystemClient.write(...); // IOException 그대로 전파
}
```

```java
// application service
try {
    filePort.upload(...);
} catch (IOException ex) {
    throw new JgitkinsException(ApplicationErrorCode.REPOSITORY_FILE_READ_FAILED,
            "Failed to load repository file",
            ex);
}
```

정리하면 다음 원칙으로 문서를 확정한다.

1. Adapter에서 끝내도 되는 기술 실패는 `InfrastructureErrorCode`로 래핑한다.
2. 유즈케이스 예외로 치환해야 하는 기술 실패는 Adapter가 실제 기술예외를 유지한다.
3. Application은 `JgitkinsException(INF)`가 아니라 실제 기술예외를 잡아 `ApplicationErrorCode`로 매핑한다.
4. 단, 위 원칙이 기본값일 뿐이며, **어댑터 호출 결과를 처리하는 Application catch 지점에서 기술 실패를 그대로 상위로 올려야 하는 경우에는 `InfrastructureErrorCode` 직접 참조를 예외적으로 허용**할 수 있다.

이 방식이 사용자 의도와 더 가깝고, "직접 의존 제거"라는 Task 2.18의 목적과도 충돌하지 않습니다.

#### 논의 필요 이슈: `InfrastructureErrorCode`의 Application 직접 참조를 어디까지 허용할 것인가

이 항목의 핵심은 "무조건 금지"가 아니라 **허용 범위를 어디까지 열 것인지**입니다.

현재 정리하려는 기준은 다음과 같습니다.

1. **기본 원칙**
   - Application은 가능한 한 `ApplicationErrorCode`를 사용한다.
   - Infrastructure 세부 구현의 오류코드 enum을 일반적인 비즈니스 흐름에서 직접 사용하지 않는다.

2. **예외적 허용 지점**
   - Application이 어댑터를 호출하고,
   - 그 결과를 `catch` 하면서,
   - 이 실패를 유즈케이스 예외로 번역하지 않고 기술 실패 그대로 상위에 전파해야 한다면,
   - 그 `catch` 블록 안에서는 `InfrastructureErrorCode` 직접 참조를 허용할 수 있다.

예시:

```java
try {
    runnerPort.deleteById(runnerId);
} catch (RuntimeException ex) {
    throw new JgitkinsException(InfrastructureErrorCode.INTERNAL_ERROR, "Runner deletion failed", ex);
}
```

이 경우의 의미:
- Application이 Infrastructure를 새로 의존해서 비즈니스 로직을 구현하는 것이 아니라,
- 어댑터 호출 결과의 기술 실패를 **경계에서 그대로 재표현(re-wrap)** 하는 것이다.

즉, 다음 두 경우를 구분해야 한다.

1. **유즈케이스 의미로 번역할 때**
   - `ApplicationErrorCode` 사용
   - 예: `RUNNER_NOT_FOUND`, `REPOSITORY_ACCESS_DENIED`

2. **기술 실패로 유지할 때**
   - Application catch 에서 `InfrastructureErrorCode` 직접 참조 허용
   - 예: 어댑터 delete/save 호출 실패, 외부 시스템 장애, 예외를 굳이 비즈니스 의미로 바꾸지 않을 때

잠정 결론:
- "Application에서는 `InfrastructureErrorCode`를 절대 쓰면 안 된다"는 규칙은 너무 강하다.
- 대신 **어댑터 호출 결과를 처리하는 예외 경계(catch 블록)에서만 제한적으로 허용**하는 쪽이 현실적이다.
- 평시 로직, 검증, 분기, 상태 결정 로직에서의 직접 참조는 계속 금지한다.

---

## 5. 세부 리팩토링 계획

### Step 1. 직접 의존 인벤토리 확정
- [ ] `application` 패키지 전체에서 `infrastructure` import 전수 조사
- [ ] 의존 유형을 `설정 / 유틸 / 에러코드 / 기타`로 분류
- [ ] 예외적 허용 여부 없이 모두 제거 대상으로 명시

### Step 2. 설정 의존 분리
- [ ] `CloneUrlBuilder`를 `application.support`로 이관하고, `RunnerRuntimeProperties` 대신 Application 포트 또는 Provider 인터페이스에 의존하도록 변경
- [ ] `RunnerRuntimeConfigProvider`도 동일한 추상화 경유로 설정값을 읽도록 변경
- [ ] Infrastructure에서만 `RunnerRuntimeProperties`를 주입하는 Adapter 구현 추가

예상 후보:
- `application.port.out.RuntimeConfigPort`
- 메서드 예시:
  - `String serviceHost()`
  - `String restScheme()`
  - `Integer restPort()`
  - `String restBasePath()`
  - `Integer grpcPort()`

### Step 3. RepositoryPathHelper 계층 재배치
- [ ] `RepositoryPathHelper`가 순수 함수 유틸인지 재확인
- [ ] 순수 유틸이면 `application.common` 또는 `common.support`로 이동
- [ ] `RepositoryLifecycleService`, `RepositoryLookupService`, `RepositoryNamespaceResolver` import 갱신
- [ ] Infrastructure 쪽에서도 동일 유틸을 사용 중이면 새 위치로 함께 정리

#### 논의 필요 이슈: 순수 문자열 유틸의 소유 계층

이 항목은 구현 전에 한 번 더 팀 기준을 맞출 필요가 있습니다.

현재 쟁점:
- 순수 문자열 유틸도 `infrastructure`에 몰아둘 것인가
- 아니면 `application/common` 같은 상위 공용 계층으로 올릴 것인가

정리 원칙:
1. **외부 시스템 지식이 없는 순수 연산 유틸**
   - 예: 문자열 정규화, 경로 조합, 포맷 변환
   - 권장 위치: `application.common` 또는 더 상위 공용 패키지
2. **특정 기술 구현/외부 시스템 규약에 종속된 유틸**
   - 예: JGit 전용 경로 해석, 파일시스템 구현 세부 규칙, HTTP 서블릿/헤더 해석
   - 권장 위치: `infrastructure`

`RepositoryPathHelper`에 대한 현재 판단:
- 현재 기능만 보면 외부 I/O가 없고, 사용자명/조직명/저장소명 기반의 clone path 문자열 조합이 핵심이다.
- 즉, **Infrastructure 세부기술이 아니라 애플리케이션의 경로 정책(policy)** 에 가깝다.
- 따라서 1차 권장안은 `application.common` 이동이다.

다만 아래 조건이 확인되면 재논의가 필요하다.
- Adapter만 아는 파일시스템 실제 저장 경로 규칙(`/bare/...`, OS 경로 구분자, 로컬 디렉터리 구조 등)이 포함되는 경우
- 특정 인프라 구현체(JGit adapter, local fs adapter)와 강하게 결합된 문자열 규약이 포함되는 경우

이 경우의 대안:
- **대안 A. 정책과 구현 유틸 분리**
  - `application.common.RepositoryPathPolicy`: clone path, namespace 같은 도메인/애플리케이션 정책
  - `infrastructure.support.RepositoryStoragePathHelper`: 로컬 저장 경로, 실제 파일시스템 경로 계산
- **대안 B. 공용 모듈화**
  - application/infrastructure가 함께 참조 가능한 `common` 최상위 패키지로 승격

현재 문서의 잠정 결론:
- `RepositoryPathHelper`가 clone path 정책만 담당하면 `application.common`으로 이동한다.
- 향후 adapter 전용 경로 계산까지 포함되면 하나의 Helper에 섞지 말고 정책 유틸과 인프라 유틸로 분리한다.

### Step 4. InfrastructureErrorCode 누수 제거
- [ ] `CommitFileFactory`에서 파일 읽기 실패 시 사용할 상위 계층 코드 재정의
- [ ] `RunnerManagementService`의 delete/activate 실패 코드도 Application 또는 계층 중립 코드로 치환
- [ ] Adapter 내부에서 던진 `InfrastructureErrorCode`는 유지하되, Application import는 제거

후보 방안:
1. `ApplicationErrorCode.FILE_READ_FAILED`, `RUNNER_DELETE_FAILED`, `RUNNER_ACTIVATION_FAILED` 추가

현재 기준 권장안:
- **1안 우선**
- 이유: 이미 `ApplicationErrorCode`가 유즈케이스 흐름 실패/자원 부재를 담당하고 있어, Application 서비스가 직접 던지는 기술 실패도 해당 계층 코드로 관리하는 편이 현 구조와 충돌이 적음

논의 메모:
- `TechnicalErrorCode`는 계층 소유권을 다시 흐릴 가능성이 있어 이번 작업 범위에서는 도입하지 않는다.
- 우선 `ApplicationErrorCode`로 정리하고, 실제 중복/과밀이 발생할 때 별도 태스크로 재평가한다.
- 단, "Application import는 제거"를 절대 규칙으로 두지는 않는다.
- 다음 상황은 예외 허용 범위로 본다.
  1. Application이 어댑터 호출을 `catch` 하며
  2. 해당 실패를 비즈니스 예외로 번역하지 않고
  3. 기술 실패 그대로 `JgitkinsException(InfrastructureErrorCode.*)`로 재포장하는 경우
- 따라서 Step 4의 실제 논점은 "`InfrastructureErrorCode`를 0건으로 만들자"가 아니라,
  - 일반 로직에서의 직접 참조는 제거하고
  - 예외 경계에서의 제한적 직접 참조는 허용할지 팀 기준을 확정하는 것이다.
- 현재 문서의 잠정 권장안은 다음과 같다.
  - 유즈케이스 의미가 확정되면 `ApplicationErrorCode`
  - 기술 실패를 그대로 유지하면 catch 경계에서 `InfrastructureErrorCode` 허용

### Step 5. 회귀 테스트 보강
- [ ] `application` 계층에서 `infrastructure` import가 없음을 검증하는 아키텍처 테스트 추가
- [ ] `CloneUrlBuilder`, `RunnerRuntimeConfigProvider` 단위 테스트 갱신
- [ ] `RepositoryLifecycleService`, `RunnerManagementService`, `CommitFileFactory` 관련 테스트 보강

---

## 6. Before / After 예시

### 6.1 CloneUrlBuilder

#### Before
```java
import io.jgitkins.server.infrastructure.config.RunnerRuntimeProperties;

private final RunnerRuntimeProperties properties;
```

#### After
```java
import io.jgitkins.server.application.port.out.RuntimeConfigPort;

private final RuntimeConfigPort runtimeConfigPort;
```

추가 변경:
```java
package io.jgitkins.server.application.support;
```

### 6.2 RepositoryLifecycleService

#### Before
```java
import io.jgitkins.server.infrastructure.support.RepositoryPathHelper;
```

#### After
```java
import io.jgitkins.server.application.common.RepositoryPathHelper;
```

### 6.3 RunnerManagementService

#### Before
```java
throw new JgitkinsException(InfrastructureErrorCode.RUNNER_DELETE_FAILED, ...);
```

#### After
```java
throw new JgitkinsException(ApplicationErrorCode.RUNNER_DELETE_FAILED, ...);
```

---

## 7. 개선 사항 점검

### 개선안 1. 아키텍처 테스트 자동화
- `application..` 패키지에서 `infrastructure..` 참조 금지 규칙을 테스트로 고정

### 개선안 2. Helper 패키지 분류 규칙 문서화
- 순수 함수 유틸은 `application.common`
- 외부 시스템 I/O 연동은 `infrastructure`

### 개선안 3. ErrorCode 소유권 재정의
- 어느 계층이 어떤 코드 enum을 참조할 수 있는지 규칙을 명문화

### 최종 반영 선택
- **개선안 1 우선 반영**
- 이유: 이번 작업은 구조 정리보다 회귀 방지가 더 중요합니다. 테스트 한 번으로 이후 직접 import 재발을 빠르게 탐지할 수 있습니다.

---

## 8. 완료 기준 (DoD)

- `src/main/java/io/jgitkins/server/application/**` 에서 `io.jgitkins.server.infrastructure..` import가 0건이다.
- 설정값 접근은 Application 포트/Provider를 경유한다.
- `RepositoryPathHelper` 같은 순수 유틸은 Infrastructure 밖으로 이동한다.
- Application 계층에서 `InfrastructureErrorCode`를 직접 참조하지 않는다.
- 관련 단위 테스트 및 아키텍처 테스트가 통과한다.

---

## 9. 이번 작업의 1차 구현 우선순위

1. 설정 객체 직접 참조 제거
2. `RepositoryPathHelper` 재배치
3. `InfrastructureErrorCode` 직접 참조 제거
4. 아키텍처 회귀 테스트 추가

이 순서가 가장 안전합니다. 설정/유틸 정리는 영향 범위가 비교적 명확하고, 마지막에 아키텍처 테스트를 추가하면 이후 리팩토링도 안정적으로 이어갈 수 있습니다.

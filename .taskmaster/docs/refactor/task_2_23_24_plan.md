# Task 2.23 & 2.24: Dead Code 인벤토리 작성 및 제거 검증

## 1. 개요
* **작업 목표:**
  1. **(Task 2.23) Dead code 인벤토리 작성:** 참조 그래프, 빈 주입 여부 등을 기준으로 미사용 중인 클래스/메서드/설정을 찾아 삭제 리스크를 분류합니다.
  2. **(Task 2.24) 미사용 코드 제거 및 빌드 검증:** 위에서 작성된 인벤토리 내역을 토대로 실제 코드를 제거(Delete)하고, 컴파일 및 테스트 과정에서 회귀(Regression) 오류가 발생하는지 확인합니다.

## 2. 자료 조사 및 방법 정리

### 미사용 코드 탐색 및 식별 방법론 (3가지 방안)
1. **IDE(IntelliJ 등)의 Inspection 기능 활용 (`Unused declaration`)**
   * 장점: IDE 레벨에서 문맥(Context)을 이해하여 사용되지 않는 메서드나 클래스를 매우 높은 정확도로 탐지함. 리플렉션 누락 방지 가능.
   * 단점: 대규모 프로젝트에서는 분석 시간이 길어질 수 있음.
2. **SonarQube나 SpotBugs 등 외부 정적 분석 툴 사용**
   * 장점: CI/CD 과정에서 지속적인 확인이 가능하며, 체계적인 대시보드를 제공.
   * 단점: 즉각적인 로컬 환경에서의 확인 및 일회성 작업 용도로는 설정 공수가 많이 듦.
3. **`grep` 등 터미널 기반의 원시 텍스트 검색 및 스크립팅 매칭**
   * 장점: 속도가 매우 빠르고, 특정한 네이밍 컨벤션이 잡혀있다면 일괄 처리에 용이.
   * 단점: 텍스트 단순 매칭이므로 다형성, 스프링 빈 컨테이너에 의해 리플렉션으로 주입되는 코드를 삭제해 버리는 치명적 오류 발생 위험이 큼.

### 선택된 방법: 방안 1 (IDE Inspection 및 컴파일러 지원 혼합)
클린 아키텍처 상, 스프링의 의존성 주입(Dependency Injection) 때문에 단순 텍스트 탐색(3번)은 런타임 에러의 주원인이 될 수 있습니다. 따라서 빌드 도구의 컴파일 검증 능력과 IDE의 객체 참조 그래프(`Unused declaration`)를 사용하여 안전한 "인벤토리 리스트"를 마련하는 1번 방법을 주축으로 진행합니다. 

## 3. 작업 프로세스 및 Action Plan

### Step 1 (Task 2.23): Dead Code 후보군 식별 (인벤토리 작성)
* 프로젝트 전역에서 사용되지 않는 모델(Entity, DTO), 서비스, 그리고 레포지토리 메서드를 식별합니다.
* 사용되지 않는 설정(Configuration) 및 쓸모없어진 테스트 코드를 추출합니다.

### Step 2 (Task 2.23): 삭제 리스크(Risk) 분류
* **High Risk:** 스프링 빈(`@Component`, `@Service`) 및 리플렉션/JSON 파싱 대상 DTO. (지울 경우 구동 직후 런타임 예외 가능성)
* **Low Risk:** 그 외 직접 호출되지 않는 `private` 메서드, 쓰이지 않는 Util 클래스 등.

### Step 3 (Task 2.24): 코드 삭제 적용
* 작성된 인벤토리 리스트를 토대로, 불필요한 파일과 패키지, 메서드 본문을 과감하게 삭제(Delete)합니다.
* 코드베이스의 덩치를 줄임으로써 유지보수 범위 지수를 최소화합니다.

### Step 4 (Task 2.24): 변경 내역 빌드 및 회귀 도메인 검증
* **검증:** `./gradlew clean build` 및 `./gradlew test` 수행. 빌드 에러 및 테스트 실패가 없는지 교차 검증합니다.

## 4. 개선 사항 점검 (추가 제안 및 반영)

### 3가지 개선 제안
1. **코드 커버리지 리포팅 활성화 (JaCoCo):** 실행되지 않는 테스트 커버리지를 시각화하여 미사용 코드에 대한 지속적인 로깅 수행.
2. **보일러플레이트 코드 제거 (Lombok/MapStruct 적극 활용):** 안 쓰는 Getter/Setter, 명시적 생성자 등을 아예 선언하지 않게 유도하여 잠재적 데드 코드를 축소.
3. **ArchUnit 도입:** 각 계층(Port-Adapter) 간 무의미한 순환 의존성이나 쓰이지 않는 의존 코드가 발생하지 않도록 테스트(패키지 방향성) 차원의 방어 코드 삽입.

### 채택된 개선 방안
- **보일러플레이트 코드 적극 제거 및 도메인 레벨 컴파일 점검 도구 고도화 적용**
- 현재 진행 중인 MapStruct 등의 도입 맥락과 어우러져, 아예 코드가 필요 없는 구조로 발전시키는 것을 목표로 합니다.

## 5. 예시 (인벤토리 작성 및 제거 시나리오)

### 5.1. 실제 스캔된 인벤토리 목록표 (Task 2.23 산출물)
현행 프로젝트의 `grep` 파생 참조 스캔 및 Gradle 빌드 경고(Warnings) 로그를 종합하여 식별된 대상들입니다.

| 식별된 컴포넌트 경로/이름 | 타입 | 미사용/제거 사유 | 리스크(Risk) | 조치 계획 |
| :--- | :--- | :--- | :--- | :--- |
| `io.jgitkins.server.domain.model.vo.RepositoryStatus` | Enum (VO) | 소스 코드 내 어떤 곳에서도 참조되지 않고 `@Deprecated` 처리된 순수 데드 코드 | **Low** | 전체 파일(`RepositoryStatus.java`) 삭제 |
| `RepositoryJGitCommitAdapter` 내 Deprecated API 사용 | Method | Gradle 빌드에서 `uses or overrides a deprecated API` 경고가 지속 발생 | **Medium** | 해당 클래스 내 JGit Deprecated API 사용부를 신규 API로 교체 또는 미사용 시 메서드 삭제 |
| `BranchDomainMapper`, `RepositoryMemberDomainMapper` 등 | Interface | MapStruct 생성 중 `Unmapped target properties` 경고 대량 발생 (`withId` 등) | **Low** | `@Mapping(target="...", ignore=true)` 등을 통해 미사용 프로퍼티 맵핑 제외 및 보일러플레이트 제거 적용 |

### 5.2. 제거 및 변경 내역 리스크 대응 (Task 2.24)
위 인벤토리를 바탕으로 실제 파일 제거와 로직 수정을 강행합니다.

**[제거 대상 삭제]**
1. 파일 삭제: `rm src/main/java/io/jgitkins/server/domain/model/vo/RepositoryStatus.java` 
2. 어댑터 클래스와 매퍼 인터페이스 내부의 불필요한 레거시 코드 및 애노테이션 정리 (`@Deprecated`된 로직 최적화)

**[검증 프로세스]**
1. 로컬 빌드 정합성 확인: `./gradlew clean build -x test`를 실행했을 때 **기존 7개의 Warnings(경고)가 현저히 줄어드는지, 그리고 빌드 에러가 없는지 검증**.
2. 테스트 실행: 삭제된 VO(`RepositoryStatus`)가 리플렉션이나 직렬화 과정에서 문제를 일으키지 않는지 `./gradlew test` 로 회귀 테스트. 

**[제거 전 (레거시 의존성이 남아있을 수 있는 부분 탐색)]**
```java
@Configuration
public class SecurityConfig {
    // 💡 아래 필터 주입부가 잔존해 있는지 반드시 확인해야 함
    // private final TokenValidaionFilter tokenFilter; 
}
```

**[제거 및 검증]**
1. 파일 삭제: `rm TokenValidaionFilter.java`
2. 참조하던 `SecurityConfig` 에러 발생 시 동반 삭제
3. 로컬 구동 검증: `./gradlew bootRun` (Application Context가 정상적으로 띄워지는지 확인)
4. 테스트: `./gradlew test` (관련된 Security 테스트가 초록색(성공) 불빛을 내는지 확인)

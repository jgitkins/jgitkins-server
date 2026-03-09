# Task 2.26: Application 서비스 로직 일관화 (Input Validation 분리 및 Application Validator 캡슐화)

## 1. 개요 및 설계 철학 (Architecture Overview)

* **배경:** 현재 `Application Service` 레이어(유스케이스) 내에 클라이언트 입력값에 대한 원시적인 유효성 검증(Null 체크, Blank 체크 등) 로직과 비즈니스 정합성 검증이 혼재되어 있습니다. 이는 각 계층(Layer)의 단일 책임 원칙(SRP)을 위반하며, 코드가 방대해짐에 따라 일관성이 저하되는 원인이 됩니다.
* **해결 목표:**
  1. **Input Validating 제거 및 계층 이동:** Application Service에 있는 단순 입력 검증 로직을 모두 제거하고, Presentation 계층(웹 어댑터)에 그 책임을 이관합니다.
  2. **Application Validator 캡슐화:** 도메인(VO) 단위의 검증이 아닌, 영속성/상태 등 시스템 전반의 컨텍스트(DB 조회 등)가 필요한 **데이터 정합성 검증** 로직을 `Validator` 모듈로 캡슐화하여 분리합니다.

## 2. 작업 상세 계획 및 Action Plan

### Step 1: Input Validation 제거 명세 작성 (동기성 제어)
모든 Application Service를 탐색하여 입력 파라미터(DTO, ID, 문자열 등)의 순수 유효성을 점검하는 로직을 일괄 삭제합니다.

* **삭제 대상 (예시):** 
  * `if (param == null || param.isEmpty()) throw new IllegalArgumentException();`
  * `if (email == null || !email.contains("@")) ...`
* **조치 사항:** 해당 로직이 있던 곳 상단에 `// TODO: Presentation 계층에서 Controller @Valid / @NotBlank 등으로 검증 필요` 주석을 삽입합니다.

### Step 2: Application Validator 도입 및 캡슐화 설계
VO 단위에서 검증할 수 있는 '도메인 자체의 규칙'(예: 패스워드 조합 규칙, 아이디 길이 정책 등)을 제외하고, **데이터베이스나 타 모듈과 연관된 애플리케이션 정책**을 전담할 `Validator` 컴포넌트를 설계합니다.

* **예시 도출:** 
  * 중복 유저명 확인 (DB I/O 필요)
  * 특정 Repository에 해당 멤버가 추가될 수 있는 권한 및 상태인지 검사 
* **구현 방향:**
  * 서비스의 덩치를 줄이기 위해 각 유스케이스나 도메인(Entity)에 대응하는 `[Domain]ApplicationValidator`를 생성하고 서비스 레이어에서 연동(DI)합니다.

## 3. 코드 적용 예시 (Before & After)

### 3.1. [Before] 혼재된 책임을 가진 Application Service
```java
@Service
@RequiredArgsConstructor
public class LegacyOrganizeService {
    
    private final OrganizeRepository organizeRepository;
    
    public void createOrganize(String name, String description) {
        // [문제 1] Input Validation이 서비스에 존재함 (표현 계층의 역할)
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("조직 이름은 필수입니다.");
        }
        
        // [문제 2] DB 정합성 관련 검증이 서비스 비즈니스 흐름을 방해함
        if (organizeRepository.existsByName(name)) {
            throw new OrganizeAlreadyExistsException("이미 존재하는 조직 이름입니다.");
        }
        
        // 실제 비즈니스 로직 시동...
        Organize organize = Organize.create(name, description);
        organizeRepository.save(organize);
    }
}
```

### 3.2. [After] 역할이 분리된 일관화 모델

**1. Application Validator 분리**
```java
@Component
@RequiredArgsConstructor
public class OrganizeApplicationValidator {
    private final OrganizeRepository organizeRepository;
    
    /**
     * 영속성(DB) 정보나 타 애그리거트와의 연관 관계를 바탕으로 정합성을 검증합니다.
     */
    public void validateCreationTarget(String name) {
        if (organizeRepository.existsByName(name)) {
            throw new OrganizeAlreadyExistsException("이미 존재하는 조직 이름입니다.");
        }
    }
}
```

**2. 리팩토링된 Application Service**
```java
@Service
@RequiredArgsConstructor
public class RefactoredOrganizeService {
    
    private final OrganizeApplicationValidator organizeValidator;
    private final OrganizeRepository organizeRepository;
    
    public void createOrganize(String name, String description) {
        // TODO: name null/empty 여부는 Presentation 계층(@Valid)에서 검증 필요
        
        // 1. 애플리케이션 단위 정합성 검증 위임 (캡슐화)
        organizeValidator.validateCreationTarget(name);
        
        // 2. 도메인 단위 처리에만 집중
        Organize organize = Organize.create(name, description);
        organizeRepository.save(organize);
    }
}
```

## 4. 기대 효과 및 향후 과제 (시니어 관점의 회고)

* **계층형 아키텍처(클린 아키텍처)의 이점 극대화:** Input 유효성은 프론트 엔드와 가장 맞닿은 `Spring Web(Presentation)`에게 위임하며, 순수 자바 런타임 비즈니스 로직(Usecase)과 도메인이 외부 요청 포맷에 더럽혀지지 않게 은닉됩니다.
* **단일 책임 원칙(SRP) 준수:** 서비스는 "저장소에서 무엇을 꺼내와서, 도메인에게 행위를 지시하고, 다시 집어넣는다"라는 파사드(Facade)의 역할만 담당하게 되므로 흐름 파악이 매우 용이해집니다.
* **TODO 과제:** 이후 Presentation 계층(Controller 및 DTO) 작업 태스크가 진행될 때 반드시 `jakarta.validation.constraints` (예: `@NotBlank`, `@Size`, `@Email` 등) 애노테이션을 부착하는 후속 조치가 이어져야 합니다.

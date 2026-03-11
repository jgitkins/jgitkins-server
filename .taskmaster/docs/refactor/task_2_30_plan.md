# 리팩토링 계획서

### 제목
- **리팩토링 계획**: MyBatis 어댑터 예외 처리 일관성 확보 (InfrastructureException 적용)

### 배경 (왜?)
- 현재 `infrastructure/adapter/persistence` 패키지의 MyBatis 어댑터들이 DB 연동 과정에서 발생하는 예외를 별도로 처리하지 않거나, 프레임워크 원천 예외(DataAccessException 등)를 그대로 노출하고 있음.
- 헥사고날 아키텍처 원칙에 따라 인프라 계층의 기술적 세부 사항이 상위 계층으로 누수되는 것을 방지하고자 함.
- 모든 인프라 에러를 `InfrastructureException`으로 래핑하여 `GlobalExceptionHandler`에서 일관된 포맷으로 처리할 수 있도록 개선이 필요함.

### 목표 (Goals)
- 모든 persistence 어댑터에서 발생하는 DB 예외를 `InfrastructureException`으로 일관되게 래핑함.
- `InfrastructureErrorCode.INTERNAL_ERROR` 등을 활용하여 인프라 장애임을 명확히 정의함.
- 상위 계층(Application/Domain)이 특정 인프라 기술에 종속되지 않도록 예외 전파 구조를 정돈함.

### 범위 (Scope)
- **수정 대상**: `io.jgitkins.server.infrastructure.adapter.persistence` 패키지 내 모든 MyBatis 어댑터 클래스.
    - `RunnerMybatisAdapter`, `JobMybatisAdapter`, `RepositoryMybatisAdapter` 등 총 10개 클래스.
- **수정 제외 대상**: 도메인 로직 및 이미 `InfrastructureException`을 정상적으로 사용 중인 외부 연동 어댑터.

### 계획 (Plan)
- **단계 1**: 각 어댑터의 주요 CRUD 메서드에서 예외 발생 가능 지점을 분석함.
- **단계 2**: MyBatis 예외를 `InfrastructureException`으로 전환하는 공통 대응 전략을 수립함.
- **단계 3**: 어댑터 내부에 `try-catch` 블록을 도입하여 예외 발생 시 `InfrastructureException`으로 재발행(re-throw)하는 작업을 수행함.
- **단계 4**: DB 연동 실패 시나리오를 통해 `GlobalExceptionHandler`의 응답 포맷 및 `source` 필드 정합성을 검증함.
- **단계 5**: 리팩토링 결과 및 변경 사항을 문서화하여 기록함.

### 기대효과 (Expected Benefits)
- 인프라 계층의 캡슐화 및 기술 은닉을 강화함.
- 전역 예외 처리기를 통한 에러 응답의 일관성을 확보함.
- 로그 분석 시 인프라 레벨의 장애 유무를 신속하게 식별하여 유지보수 효율을 높임.

### 예시

#### AS-IS
```java
@Override
public Runner save(Runner runner) {
    RunnerEntity entity = runnerDomainMapper.toEntity(runner);
    runnerEntityMbgMapper.insertSelective(entity); // 예외 발생 시 프레임워크 예외가 그대로 상위로 전파됨
    return ...;
}
```

#### TO-BE
```java
@Override
public Runner save(Runner runner) {
    try {
        RunnerEntity entity = runnerDomainMapper.toEntity(runner);
        runnerEntityMbgMapper.insertSelective(entity);
        return ...;
    } catch (Exception e) {
        // InfrastructureException으로 래핑하여 기술 세부 사항을 은닉함
        throw new InfrastructureException(InfrastructureErrorCode.INTERNAL_ERROR, "Database operation failed during save runner", e);
    }
}
```

### 주의사항
- **포맷팅 금지**: 리팩토링 과정에서 코드 포맷팅만을 수정하지 않도록 주의하며, 코드의 기능과 구조 개선에 집중함.
- **기존 기능 보장**: 리팩토링 후에도 기존의 비즈니스 로직이 정상적으로 동작하는지 확인하기 위한 테스트를 병행함.
- **계획우선**: 계획문서 작성 중에는 실제 구현을 진행하지 않으며, 승인 후 작업을 시작함.
- **문서체규약**:
    - 모든 문장은 "~~하였음" 또는 "~~함" 형태로 마무리하여 공식적인 형식을 유지함.
    - 간결하면서도 핵심적인 정보를 포함하도록 작성함.

### 결론
MyBatis 어댑터의 예외 처리 구조를 표준화하여 인프라 계층의 견고함을 확보하고 유지보수성을 향상시키고자 함.

### 실행 결과 (Implementation Results)
- **대상 클래스**: `RunnerMybatisAdapter`, `JobMybatisAdapter`, `RepositoryMybatisAdapter`, `BranchMybatisAdapter`, `OrganizeMybatisAdapter`, `OrganizeMemberMybatisAdapter`, `UserMybatisAdapter`, `UserIdentityMybatisAdapter`, `UserCredentialMybatisAdapter`, `RepositoryMemberAdapter` (총 10개 클래스) 리팩토링 완료함.
- **주요 변경 사항**:
    - 모든 public 메서드에 `try-catch` 블록을 도입하여 MyBatis 관련 예외를 포착함.
    - 포착된 예외를 `InfrastructureException`으로 래핑하고, `InfrastructureErrorCode.PERSISTENCE_OPERATION_FAILED` 에러 코드를 부여하여 재발행함.
    - 각 메서드별로 에러 메시지를 구체화하여 디버깅 편의성을 높임.
- **검증**: 컴파일 오류 없음을 확인함.

모든 작업 완료함.

# Task 2.25: 로깅 설정 변경 (Java 기반 Configuration)

## 1. 개요
* **작업 목표:** 기존 XML 파일(`.xml`) 기반의 로깅 설정을 Java 기반 Configuration으로 마이그레이션.
* **요구 사항:**
  1. Java 코드를 이용한 Logback 설정 구성 (XML 설정 제거).
  2. 콘솔 출력 시 로깅 레벨별로 적절한 색상(Color) 부여.

## 2. 자료 조사 및 구현 방법

### 방법론 탐색 (3가지 방법)
1. **방법 1: 순수 `application.yml` 속성과 Spring Boot 기본 로깅 조합**
   * 장점: 기존 Spring Boot 기능을 활용하므로 구현 공수가 극히 낮음. `logging.pattern.console`과 Spring Boot의 색상 포맷 `%clr()`를 활용.
   * 단점: XML을 온전히 Java 코드로 변경하라는 'Java 기반 Configuration' 요구 의도보다 단순 속성 변경에 가까움.
2. **방법 2: `@Configuration`과 Custom Logback Configurator/ApplicationListener 활용**
   * 장점: Logback의 `LoggerContext`를 프로그래밍 방식으로 완벽하게 제어할 수 있어 기존 XML 내용을 1:1로 코드로 맵핑할 수 있음. 구조적 유연성이 뛰어남.
   * 단점: 초기 보일러플레이트 코드량이 증가함.
3. **방법 3: `Log4j2` 등 다른 로깅 프레임워크로 교체 및 Java Config 도입**
   * 장점: 성능 상 이점 확보 가능. Builder API 기반으로 설정이 편리함.
   * 단점: 프레임워크 자체를 전환해야 하므로 기존 시스템 리팩토링 범위가 커질 위험.

### 선택된 방법: 방법 2 (Custom Logback Configurator 및 Listener 방식)
최종적으로 기존 `logback.xml` / `logback-spring.xml` 파일을 제거하고, `ApplicationEnvironmentPreparedEvent`를 통해 Spring 구동 초기에 Logback을 코드로 초기화하는 방식을 채택합니다.
레벨별 색상은 Logback의 `%highlight` 또는 Spring Boot가 제공하는 `ColorConverter`를 Java 코드로 등록하여 레벨별로 구별된 색상을 명시 적용합니다.

## 3. 작업 프로세스 및 Action Plan

### Step 1: 기존 XML 설정 분석
* 기존 프로젝트에 존재하는 `logback.xml` 또는 `logback-spring.xml`을 파악하여 사용 중인 Console, File Appender 정보 등 필수 속성을 추출합니다.

### Step 2: Java 기반 Logging Configurator 작성
* Spring Boot 시작 시점에 개입하기 위해 `ApplicationListener`를 구현하거나, 프레임워크 초기화 시점에 `LoggerContext`를 구성하는 코드를 작성합니다.
* `PatternLayoutEncoder`를 생성하고, `%clr(%5p)` 같은 형식 지정자를 통해 색상을 부여합니다.
  * 예: *ERROR=Red, WARN=Yellow, INFO=Green, DEBUG=Blue*

### Step 3: 기존 XML 설정 제거
* 레거시 `.xml` 파일들을 리소스 파일 경로에서 완전하게 삭제합니다.

### Step 4: 테스트 및 검증
* 애플리케이션을 구동하여 색상별로 로그(Info, Debug, Error 등)가 정확히 출력되는지 확인합니다.
* 환경(Profile)별 로깅 (예: 로컬은 콘솔, 서버는 파일/콘솔 병행 등)이 기존과 동일하게 유지되는지 검증합니다.

## 4. 개선 사항 점검 (추가 제안 및 반영)

### 3가지 개선 제안
1. **에러 로그 알림 연동:** ERROR 레벨 발생 시 외부 슬랙(Slack) 등과 연동하는 `Appender`를 추가하여 모니터링 편의성 강화.
2. **MDC (Mapped Diagnostic Context) 패턴 적용:** User Id나 Transaction ID (trace ID) 패턴을 모든 로그에 반영하여 멀티스레딩 추적 강화.
3. **AsyncAppender 도입 대규모 트래픽 대비:** 디스크/콘솔 I/O 병목을 해결하기 위해, 동기 방식으로 동작하는 기존 Appender를 `AsyncAppender` 래퍼로 감싸 처리.

### 채택된 개선 방안
- **MDC 패턴 추적 및 AsyncAppender 도입**
  - XML을 벗어나 코드로 설정하는 만큼, 프로그래밍 방식으로 `AsyncAppender`를 보다 유연하게 선언.
  - Trace ID 관리를 기본 포맷에 선구축하여 클린 아키텍처 관점에서의 운영 안정성 및 디버깅 효율을 고도화합니다.

## 5. 예시 코드 스니펫

### 5.1. Spring ApplicationListener를 이용한 Logback 설정 예시
Spring Application 구동 초기에 로깅을 명시적으로 컨트롤하기 위해 구현하는 `ApplicationListener`의 뼈대입니다.

```java
package io.jgitkins.server.infrastructure.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.classic.AsyncAppender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

public class LoggingConfigurator implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // 1. Console Appender 설정
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName("CONSOLE");
        
        // 2. %highlight 를 통한 로깅 레벨별 컬러 매핑 및 MDC 추적 ID(%X{traceId}) 적용 패턴
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %highlight(%-5level) [%X{traceId:-}] [%thread] %cyan(%logger{36}) - %msg%n");
        encoder.start();
        
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        
        // 3. AsyncAppender 도입 (대규모 트래픽 대비)
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(loggerContext);
        asyncAppender.setName("ASYNC_CONSOLE");
        asyncAppender.addAppender(consoleAppender);
        asyncAppender.setQueueSize(512); // 동시 처리를 위한 큐 사이즈 할당
        asyncAppender.start();
        
        // 4. Root Logger 설정 (기존 설정 덮어쓰기)
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        // XML이나 기본설정에 의해 추가된 기존 Appender 초기화 후 새 Async Appender 추가
        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(asyncAppender);
    }
}
```

### 5.2. `spring.factories`를 활용한 리스너 자동 등록
Spring Boot가 로깅 프레임워크를 초기화하는 시점은 컨텍스트가 띄워지기 전이므로 빈(Bean) 등록 방식이 아닌, `spring.factories`나 서비스 로더 등을 활용해야 합니다.

**파일 경로:** `src/main/resources/META-INF/spring.factories`
```properties
org.springframework.context.ApplicationListener=\
io.jgitkins.server.infrastructure.config.LoggingConfigurator
```

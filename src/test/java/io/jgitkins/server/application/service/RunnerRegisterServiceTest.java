//package io.jgitkins.server.application.service;
//
//import io.jgitkins.server.application.dto.RunnerRegisterCommand;
//import io.jgitkins.server.application.dto.RunnerRegistrationResult;
//import io.jgitkins.server.application.mapper.RunnerApplicationMapper;
//import io.jgitkins.server.application.port.in.RunnerRegisterUseCase;
//import io.jgitkins.server.application.port.out.RunnerCommandPort;
//import io.jgitkins.server.infrastructure.adapter.persistence.FakeRunnerMybatisAdapter;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mapstruct.factory.Mappers;
//import org.mockito.MockitoAnnotations;
//import org.springframework.context.annotation.Profile;
//
//@Profile("test")
//class RunnerRegisterServiceTest {
//
//    private RunnerCommandPort commandPort;
//
//    private RunnerRegisterService runnerRegistrationService;
//    private RunnerRegisterUseCase runnerRegisterUseCase;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        RunnerApplicationMapper mapper = Mappers.getMapper(RunnerApplicationMapper.class);
//        commandPort = new FakeRunnerMybatisAdapter();
//        runnerRegistrationService = new RunnerRegisterService(commandPort, mapper);
//        new RunnerWriteService(commandPort, mapper);
////        new Fake
//    }
//
//
//    @Test
//    @DisplayName("등록 테스트 성공")
//    void register_success() {
//
//        RunnerRegisterCommand command = RunnerRegisterCommand.builder()
//                .description("hi")
//                .build();
//
//        RunnerRegistrationResult runner = runnerRegistrationService.register(command);
//
//    }
//}
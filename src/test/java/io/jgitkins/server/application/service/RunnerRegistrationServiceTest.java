//package io.jgitkins.server.application.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import io.jgitkins.server.application.dto.RunnerRegisterCommand;
//import io.jgitkins.server.application.dto.RunnerRegistrationResult;
//import io.jgitkins.server.application.mapper.RunnerApplicationMapper;
//import io.jgitkins.server.application.port.out.RunnerCommandPort;
//import io.jgitkins.server.domain.model.Runner;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mapstruct.factory.Mappers;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//class RunnerRegistrationServiceTest {
//
//    @Mock
//    private RunnerCommandPort commandPort;
//
//    private RunnerRegisterService runnerRegistrationService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        RunnerApplicationMapper mapper = Mappers.getMapper(RunnerApplicationMapper.class);
//        runnerRegistrationService = new RunnerRegisterService(commandPort, mapper);
//    }
//
//    @Test
//    @DisplayName("Runner registration persists entity and returns token")
//    void createRunner() {
//        when(commandPort.save(any())).thenAnswer(invocation -> {
//            Runner runner = invocation.getArgument(0);
//            return runner.withId(42L);
//        });
//
//        RunnerRegisterCommand command = RunnerRegisterCommand.builder()
//                .description("docker executor")
//                .build();
//
//        RunnerRegistrationResult result = runnerRegistrationService.register(command);
//
//        verify(commandPort).save(any());
//        assertThat(result.getRunnerId()).isEqualTo(42L);
//        assertThat(result.getToken()).startsWith("RNR-");
//        assertThat(result.getStatus()).isEqualTo("OFFLINE");
//        assertThat(result.getRegisteredAt()).isNotNull();
//    }
//}

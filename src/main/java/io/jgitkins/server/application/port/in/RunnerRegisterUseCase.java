package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.RunnerRegisterCommand;
import io.jgitkins.server.application.dto.RunnerRegistrationResult;

public interface RunnerRegisterUseCase {
    RunnerRegistrationResult register(RunnerRegisterCommand command);
}

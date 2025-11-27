package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.CreateRepositoryCommand;

public interface CreateRepositoryUseCase {
    void createBareRepository(CreateRepositoryCommand command);
}

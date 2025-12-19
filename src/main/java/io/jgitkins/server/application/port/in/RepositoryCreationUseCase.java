package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.command.CreateRepositoryCommand;
import io.jgitkins.server.application.dto.result.RepositoryResult;

public interface RepositoryCreationUseCase {
    RepositoryResult create(CreateRepositoryCommand command);
}

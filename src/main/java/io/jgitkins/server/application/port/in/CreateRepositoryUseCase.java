package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.CreateRepositoryCommand;
import io.jgitkins.server.application.dto.RepositoryResult;

public interface CreateRepositoryUseCase {
    RepositoryResult create(CreateRepositoryCommand command);
}

package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.RepositoryResult;
import io.jgitkins.server.application.dto.UpdateRepositoryCommand;

public interface UpdateRepositoryUseCase {
    RepositoryResult updateRepository(Long repositoryId, UpdateRepositoryCommand command);
}

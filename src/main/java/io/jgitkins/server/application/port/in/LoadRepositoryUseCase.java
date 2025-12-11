package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.RepositoryResult;

public interface LoadRepositoryUseCase {
    RepositoryResult getRepository(Long repositoryId);
}

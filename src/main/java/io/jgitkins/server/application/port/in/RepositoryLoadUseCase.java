package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.result.RepositoryResult;

public interface RepositoryLoadUseCase {
    RepositoryResult getRepository(Long repositoryId);
}

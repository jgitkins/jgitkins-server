package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.UserId;

public interface RepositoryMemberAccessPort {
    boolean existsByRepositoryAndUser(RepositoryId repositoryId, UserId userId);
}

package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.model.RepositoryMember;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.UserId;

public interface RepositoryMemberPort {
    RepositoryMember save(RepositoryMember member);

    boolean existsByRepositoryAndUser(RepositoryId repositoryId, UserId userId);

    void deleteByRepositoryAndUser(RepositoryId repositoryId, UserId userId);

    java.util.List<RepositoryMember> findAllByRepository(RepositoryId repositoryId);

}

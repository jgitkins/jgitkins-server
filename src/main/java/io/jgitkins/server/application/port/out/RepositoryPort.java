package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.domain.model.vo.UserId;

import java.util.List;
import java.util.Optional;

public interface RepositoryPort {

    Repository save(Repository repository);

    Repository update(Repository repository);

    void delete(RepositoryId id);

    Optional<Repository> findById(RepositoryId id);

    List<Repository> findAll();

    Optional<Repository> findByOwnerAndPath(OwnerType ownerType, OwnerId ownerId, RepositoryPath path);

    Optional<Repository> findByOwnerAndName(OwnerType ownerType, OwnerId ownerId, RepositoryName name);

    Optional<Long> findRepositoryId(String ownerNamespace, String repoName);

}

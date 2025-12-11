package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;

import java.util.Optional;

public interface RepositoryPersistencePort {

    Repository save(Repository repository);

    Repository update(Repository repository);

    void delete(RepositoryId id);

    Optional<Repository> findById(RepositoryId id);

    Optional<Repository> findByOrganizeAndPath(OrganizeId organizeId, RepositoryPath path);

    Optional<Repository> findByOrganizeAndName(OrganizeId organizeId, RepositoryName name);
}

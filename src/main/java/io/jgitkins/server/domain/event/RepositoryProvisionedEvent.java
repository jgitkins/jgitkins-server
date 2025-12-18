package io.jgitkins.server.domain.event;

import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.domain.model.vo.RepositoryVisibility;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RepositoryProvisionedEvent implements DomainEvent {

    private final RepositoryId repositoryId;
    private final OrganizeId organizeId;
    private final RepositoryName name;
    private final RepositoryPath path;
    private final RepositoryVisibility visibility;
    private final String repositoryType;
    private final Instant occurredAt;

    public static RepositoryProvisionedEvent from(Repository repository) {
        return new RepositoryProvisionedEvent(
                repository.getId(),
                repository.getOrganizeId(),
                repository.getName(),
                repository.getPath(),
                repository.getVisibility(),
                repository.getRepositoryType(),
                Instant.now()
        );
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}

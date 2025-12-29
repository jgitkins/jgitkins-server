package io.jgitkins.server.application.service;

import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.RepositoryMemberPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.domain.model.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GitRepositoryAccessService {

    private final OrganizePort organizePort;
    private final RepositoryPort repositoryPort;
    private final OrganizeMemberPort organizeMemberPort;
    private final RepositoryMemberPort repositoryMemberPort;
    private final UserPort userPort;

    public boolean canRead(String namespace, String ownerSlug, String repositoryName, Long userId) {
        return canAccess(namespace, ownerSlug, repositoryName, userId);
    }

    public boolean canWrite(String namespace, String ownerSlug, String repositoryName, Long userId) {
        return canAccess(namespace, ownerSlug, repositoryName, userId);
    }

    private boolean canAccess(String namespace, String ownerSlug, String repositoryName, Long userId) {
        if (userId == null) {
            return false;
        }
        Optional<Repository> repository = resolveRepository(namespace, ownerSlug, repositoryName);
        if (repository.isEmpty()) {
            return false;
        }
        Repository repo = repository.get();
        UserId uid = UserId.of(userId);
        if (repo.getOwnerType() == OwnerType.USER
                && repo.getOwnerId() != null
                && repo.getOwnerId().getValue().equals(uid.getValue())) {
            return true;
        }
        if (repositoryMemberPort.existsByRepositoryAndUser(repo.getId(), uid)) {
            return true;
        }
        if (repo.getOwnerType() != OwnerType.ORGANIZATION || repo.getOwnerId() == null) {
            return false;
        }
        return organizeMemberPort.existsByOrganizeAndUser(OrganizeId.of(repo.getOwnerId().getValue()), uid);
    }

    private Optional<Repository> resolveRepository(String namespace, String ownerSlug, String repositoryName) {
        if (namespace == null || namespace.isBlank()
                || repositoryName == null || repositoryName.isBlank()) {
            return Optional.empty();
        }
        if ("users".equals(namespace)) {
            if (ownerSlug == null || ownerSlug.isBlank()) {
                return Optional.empty();
            }
            return userPort.findByUsername(ownerSlug)
                    .map(user -> repositoryPort.findByOwnerAndName(OwnerType.USER, OwnerId.of(user.getId()),
                            RepositoryName.from(repositoryName)))
                    .orElse(Optional.empty());
        }
        Optional<Organize> organize = organizePort.findByName(OrganizeName.from(namespace));
        if (organize.isEmpty()) {
            return Optional.empty();
        }
        OrganizeId organizeId = organize.get().getId();
        return repositoryPort.findByOwnerAndPath(OwnerType.ORGANIZATION, OwnerId.of(organizeId.getValue()),
                RepositoryPath.from(repositoryName));
    }
}

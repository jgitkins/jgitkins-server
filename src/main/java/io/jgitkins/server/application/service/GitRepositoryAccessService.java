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
import io.jgitkins.server.domain.model.vo.RepositoryVisibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitRepositoryAccessService {

    private final OrganizePort organizePort;
    private final RepositoryPort repositoryPort;
    private final OrganizeMemberPort organizeMemberPort;
    private final RepositoryMemberPort repositoryMemberPort;
    private final UserPort userPort;

    public boolean canRead(OwnerType ownerType, String ownerName, String repositoryName, Long userId) {
        Optional<Repository> repository = resolveRepository(ownerType, ownerName, repositoryName);
        if (repository.isEmpty()) {
            return false;
        }
        Repository repo = repository.get();
        if (repo.getVisibility() == io.jgitkins.server.domain.model.vo.RepositoryVisibility.PUBLIC) {
            return true;
        }
        return canAccess(repo, userId);
    }

    public boolean canWrite(OwnerType ownerType, String ownerName, String repositoryName, Long userId) {
        Optional<Repository> repository = resolveRepository(ownerType, ownerName, repositoryName);
        if (repository.isEmpty()) {
            return false;
        }
        return canAccess(repository.get(), userId);
    }

    public boolean isPublicRepo(OwnerType ownerType, String ownerName, String repositoryName) {
        return resolveRepository(ownerType, ownerName, repositoryName)
                .map(repo -> repo.getVisibility() == io.jgitkins.server.domain.model.vo.RepositoryVisibility.PUBLIC)
                .orElse(false);
    }

    public Optional<Boolean> resolveVisibility(OwnerType ownerType, String ownerName, String repositoryName) {
        log.debug("find repository ownerType: [{}], ownerName: [{}], repositoryName: [{}]", ownerType, ownerName, repositoryName);
        return resolveRepository(ownerType, ownerName, repositoryName)
                .map(repo -> repo.getVisibility() == RepositoryVisibility.PUBLIC);
    }

    private boolean canAccess(Repository repo, Long userId) {
        if (userId == null) {
            return false;
        }
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

    private Optional<Repository> resolveRepository(OwnerType ownerType, String ownerName, String repositoryName) {
        if (ownerName == null || ownerName.isBlank()
            || repositoryName == null || repositoryName.isBlank()) {
            return Optional.empty();
        }
        if (ownerType == null) {
            return resolveRepositoryByNamespace(ownerName, repositoryName);
        }
        if (ownerType == OwnerType.USER) {
            return userPort.findByUsername(ownerName)
                    .map(user -> repositoryPort.findByOwnerAndName(OwnerType.USER, OwnerId.of(user.getId()), RepositoryName.from(repositoryName)))
                    .orElse(Optional.empty());
        }
        Optional<Organize> organize = organizePort.findByName(OrganizeName.from(ownerName));
        if (organize.isEmpty()) {
            return Optional.empty();
        }
        OrganizeId organizeId = organize.get().getId();
        return repositoryPort.findByOwnerAndPath(OwnerType.ORGANIZATION, OwnerId.of(organizeId.getValue()),
                RepositoryPath.from(repositoryName));
    }

    private Optional<Repository> resolveRepositoryByNamespace(String namespace, String repositoryName) {
        Optional<io.jgitkins.server.domain.model.User> user = userPort.findByUsername(namespace);
        Optional<Organize> organize = findOrganizeByNamespace(namespace);

        if (user.isPresent() && organize.isPresent()) {
            log.warn("Ambiguous namespace for git access. namespace=[{}]", namespace);
            return Optional.empty();
        }
        if (user.isPresent()) {
            return repositoryPort.findByOwnerAndName(OwnerType.USER,
                    OwnerId.of(user.get().getId()),
                    RepositoryName.from(repositoryName));
        }
        if (organize.isPresent()) {
            OrganizeId organizeId = organize.get().getId();
            return repositoryPort.findByOwnerAndPath(OwnerType.ORGANIZATION,
                    OwnerId.of(organizeId.getValue()),
                    RepositoryPath.from(repositoryName));
        }
        return Optional.empty();
    }

    private Optional<Organize> findOrganizeByNamespace(String namespace) {
        try {
            return organizePort.findByName(OrganizeName.from(namespace));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

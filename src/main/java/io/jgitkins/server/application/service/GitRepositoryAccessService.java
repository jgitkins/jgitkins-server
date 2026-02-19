package io.jgitkins.server.application.service;

import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.RepositoryMemberPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.OrganizeMember;
import io.jgitkins.server.domain.model.RepositoryMember;
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
        return resolvePermission(repo, userId).member();
    }

    public boolean canWrite(OwnerType ownerType, String ownerName, String repositoryName, Long userId) {
        Optional<Repository> repository = resolveRepository(ownerType, ownerName, repositoryName);
        if (repository.isEmpty()) {
            return false;
        }
        return resolvePermission(repository.get(), userId).writable();
    }

    public RepositoryPermission resolvePermission(OwnerType ownerType, String ownerName, String repositoryName, Long userId) {
        Optional<Repository> repository = resolveRepository(ownerType, ownerName, repositoryName);
        if (repository.isEmpty()) {
            return RepositoryPermission.none();
        }
        return resolvePermission(repository.get(), userId);
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

    public RepositoryPermission resolvePermission(Repository repo, Long userId) {
        if (repo == null) {
            return RepositoryPermission.none();
        }
        if (userId == null) {
            return RepositoryPermission.anonymous();
        }
        UserId uid = UserId.of(userId);
        if (repo.getOwnerType() == OwnerType.USER
                && repo.getOwnerId() != null
                && repo.getOwnerId().getValue().equals(uid.getValue())) {
            return new RepositoryPermission("OWNER", true, true);
        }

        Optional<RepositoryMember> repositoryMember = repositoryMemberPort.findByRepositoryAndUser(repo.getId(), uid);
        if (repositoryMember.isPresent()) {
            var role = repositoryMember.get().getRole();
            boolean writable = role == io.jgitkins.server.domain.model.vo.RepositoryMemberRole.WRITER
                    || role == io.jgitkins.server.domain.model.vo.RepositoryMemberRole.MAINTAINER;
            return new RepositoryPermission("REPOSITORY_" + role.name(), writable, true);
        }

        if (repo.getOwnerType() == OwnerType.ORGANIZATION && repo.getOwnerId() != null) {
            Optional<OrganizeMember> organizeMember = organizeMemberPort.findByOrganizeAndUser(
                    OrganizeId.of(repo.getOwnerId().getValue()),
                    uid
            );
            if (organizeMember.isPresent()) {
                var role = organizeMember.get().getRole();
                boolean writable = role == io.jgitkins.server.domain.model.vo.OrganizeMemberRole.OWNER
                        || role == io.jgitkins.server.domain.model.vo.OrganizeMemberRole.MAINTAINER;
                return new RepositoryPermission("ORGANIZATION_" + role.name(), writable, true);
            }
        }
        return new RepositoryPermission("NONE", false, false);
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

    public record RepositoryPermission(String role, boolean writable, boolean member) {
        public static RepositoryPermission anonymous() {
            return new RepositoryPermission("ANONYMOUS", false, false);
        }

        public static RepositoryPermission none() {
            return new RepositoryPermission("NONE", false, false);
        }
    }
}

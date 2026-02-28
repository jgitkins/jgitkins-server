package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.common.event.DomainEventPublisher;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.dto.RepositoryCreationContext;
import io.jgitkins.server.application.dto.command.RepositoryCreateCommand;
import io.jgitkins.server.application.dto.result.RepositoryResult;
import io.jgitkins.server.application.mapper.RepositoryApplicationMapper;
import io.jgitkins.server.application.port.in.RepositoryCreateUseCase;
import io.jgitkins.server.application.port.in.RepositoryDeleteUseCase;
import io.jgitkins.server.application.port.in.RepositoryLoadUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.RepositoryGitPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.application.service.RepositoryNamespaceResolver;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.*;
import io.jgitkins.server.infrastructure.support.RepositoryPathHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryLifecycleService implements RepositoryCreateUseCase,
                                                   RepositoryLoadUseCase,
                                                   RepositoryDeleteUseCase {

    private final RepositoryNamespaceResolver repositoryNamespaceResolver;
    private final RepositoryApplicationMapper repositoryApplicationMapper;
    private final DomainEventPublisher domainEventPublisher;

    private final RepositoryGitPort repositoryGitPort;
    private final RepositoryPort repositoryPort;
    private final OrganizeMemberPort organizeMemberPort;
    private final OrganizePort organizePort;
    private final CurrentUserPort currentUserPort;
    private final UserPort userPort;

    @Override
    @Transactional
    public RepositoryResult create(RepositoryCreateCommand command) {
        long startedAt = System.nanoTime();
        log.info("Repository create started. ownerType={}, organizeId={}, repoName={}",
                command.getOwnerType(),
                command.getOrganizeId(),
                command.getRepoName());

        InitialCommitOptions initialCommitOptions = InitialCommitOptions.of(command.isReadme(),
                                                                            command.getMessage(),
                                                                            command.getAuthorName(),
                                                                            command.getAuthorEmail());

        RepositoryCreationContext context = prepareCreateContext(command);
        log.debug("repositoryCreationContext: [{}]", context);

        Repository repository = Repository.create(context.ownerType(),
                                                  context.ownerId(),
                                                  context.repositoryName(),
                                                  context.repositoryPath(),
                                                  context.defaultBranch(),
                                                  context.visibility(),
                                                  command.getDescription(),
                                                  context.clonePath(),
                                                  command.getCredentialId(),
                                                  initialCommitOptions);

        Repository savedRepository = repositoryPort.save(repository);

        repositoryGitPort.create(context.namespace(), context.repositoryName().getValue());
        log.info("repository has created successful");

        publishDomainEvents(savedRepository);

        Repository refreshed = repositoryPort.findById(savedRepository.getId())
                                             .orElse(savedRepository);
        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("Repository create completed. repositoryId={}, ownerType={}, ownerId={}, repoName={}, durationMs={}",
                refreshed.getId() != null ? refreshed.getId().getValue() : null,
                refreshed.getOwnerType(),
                refreshed.getOwnerId() != null ? refreshed.getOwnerId().getValue() : null,
                refreshed.getName() != null ? refreshed.getName().getValue() : null,
                durationMs);

        return repositoryApplicationMapper.toDto(refreshed);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryResult getRepository(Long repositoryId) {
        Repository repository = repositoryPort.findById(RepositoryId.of(repositoryId))
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found: " + repositoryId));
        return repositoryApplicationMapper.toDto(repository);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryResult getRepositoryByPath(String namespace, String repoName) {
        Repository repository = resolveRepositoryByPath(namespace, repoName)
                .orElseThrow(() -> new JgitkinsException(
                        io.jgitkins.server.application.common.error.ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                        String.format("Repository not found: %s/%s", namespace, repoName)
                ));
        return repositoryApplicationMapper.toDto(repository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepositoryResult> getRepositories() {
        Optional<Long> requesterId = currentUserPort.currentUserId();
        Map<OrganizeId, Boolean> membershipCache = new HashMap<>();

        List<RepositoryResult> repositories = repositoryPort.findAll()
                .stream()
                .filter(repository -> isVisibleToRequester(repository, requesterId, membershipCache))
                .map(repositoryApplicationMapper::toDto)
                .toList();
        log.debug("repositories: [{}]", repositories);
        return repositories;
    }

    @Transactional(readOnly = true)
    public List<RepositoryResult> getRepositoriesByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.BAD_REQUEST, "username is required.");
        }
        Long ownerId = userPort.findUserIdByUsername(username)
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                        "User not found: " + username));

        Optional<Long> requesterId = currentUserPort.currentUserId();
        List<Repository> repositories = repositoryPort.findAllByOwner(OwnerType.USER, OwnerId.of(ownerId));
        return repositories.stream()
                .filter(repository -> isVisibleToUserOwner(repository, requesterId, ownerId))
                .map(repositoryApplicationMapper::toDto)
                .toList();
    }

    private boolean isVisibleToRequester(Repository repository,
                                         java.util.Optional<Long> requesterId,
                                         java.util.Map<OrganizeId, Boolean> membershipCache) {
        if (repository == null) {
            return false;
        }
        RepositoryVisibility visibility = repository.getVisibility();
        if (visibility == RepositoryVisibility.PUBLIC) {
            return true;
        }
        if (requesterId.isEmpty()) {
            return false;
        }
        Long userId = requesterId.get();
        if (repository.getOwnerType() == OwnerType.USER) {
            return repository.getOwnerId() != null && userId.equals(repository.getOwnerId().getValue());
        }
        if (repository.getOwnerType() == OwnerType.ORGANIZATION && repository.getOwnerId() != null) {
            OrganizeId organizeId = OrganizeId.of(repository.getOwnerId().getValue());
            return membershipCache.computeIfAbsent(organizeId,
                    id -> organizeMemberPort.existsByOrganizeAndUser(id, UserId.of(userId)));
        }
        return false;
    }

    private boolean isVisibleToUserOwner(Repository repository,
                                         Optional<Long> requesterId,
                                         Long ownerId) {
        if (repository == null) {
            return false;
        }
        if (repository.getVisibility() == RepositoryVisibility.PUBLIC) {
            return true;
        }
        if (requesterId.isEmpty()) {
            return false;
        }
        return ownerId != null && ownerId.equals(requesterId.get());
    }

    @Override
    @Transactional
    public void deleteRepository(Long repositoryId) {
        RepositoryId id = RepositoryId.of(repositoryId);

        Repository repository = repositoryPort.findById(id)
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found: " + repositoryId));

        enforceDeletionPermission(repository);

        deleteRepositoryDirectory(repository);

        repositoryPort.delete(id);
    }








    private void validateRepositoryNameUnique(OwnerType ownerType, OwnerId ownerId, RepositoryName name) {
        repositoryPort.findByOwnerAndName(ownerType, ownerId, name)
                .ifPresent(existing -> {
                    throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.REPOSITORY_ALREADY_EXISTS,
                            "Repository name already exists for owner: " + name.getValue());
                });
    }

    private void deleteRepositoryDirectory(Repository repository) {
        String namespace = repositoryNamespaceResolver.resolve(repository);
        repositoryGitPort.delete(namespace, repository.getName().getValue());
    }

    private RepositoryCreationContext prepareCreateContext(RepositoryCreateCommand command) {

        OwnerType ownerType = requireOwnerType(command);
        validateOwnership(command, ownerType);

        RepositoryPath repositoryPath = RepositoryPath.from(command.getRepoName());
        RepositoryName repositoryName = RepositoryName.from(command.getRepoName());
        BranchName defaultBranch = BranchName.of(command.getMainBranch());
        RepositoryVisibility visibility = resolveVisibility(command.getVisibility());

        OwnerContext ownerContext = resolveOwnerContext(command, ownerType, repositoryName);
        String clonePath = RepositoryPathHelper.buildClonePath(ownerContext.namespace(), repositoryPath.getValue());

        return new RepositoryCreationContext(ownerContext.ownerType(),
                                             ownerContext.ownerId(),
                                             repositoryName,
                                             repositoryPath,
                                             defaultBranch,
                                             visibility,
                                             clonePath,
                                             ownerContext.namespace());
    }

    private void publishDomainEvents(Repository repository) {
        domainEventPublisher.publish(repository.getDomainEvents());
        repository.clearDomainEvents();
    }

    private OwnerType requireOwnerType(RepositoryCreateCommand command) {
        OwnerType ownerType = OwnerType.from(command.getOwnerType());
        if (ownerType == null) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.BAD_REQUEST, "ownerType is required.");
        }
        return ownerType;
    }

    private void validateOwnership(RepositoryCreateCommand command, OwnerType ownerType) {
        // USER
        if (ownerType == OwnerType.USER) {
            if (command.getOrganizeId() != null) {
                throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.BAD_REQUEST,
                        "organizeId must be null when ownerType is USER.");
            }
            requireCurrentUserId();
            return;
        }

        // ORGANIZE
        if (command.getOrganizeId() == null) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.BAD_REQUEST,
                    "organizeId is required when ownerType is ORGANIZATION.");
        }
        assertOrganizeMembership(command.getOrganizeId());
    }

    private void enforceDeletionPermission(Repository repository) {
        if (repository.getOwnerType() != OwnerType.USER
                || repository.getOwnerId() == null
                || repository.getOwnerId().getValue() == null) {
            return;
        }
        Long requesterId = currentUserPort.currentUserId()
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.UNAUTHORIZED, "Unauthenticated"));
        if (!repository.getOwnerId().getValue().equals(requesterId)) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.FORBIDDEN, "Cannot delete another user's repository");
        }
    }

    private RepositoryVisibility resolveVisibility(String value) {
        return value != null ? RepositoryVisibility.from(value) : RepositoryVisibility.PRIVATE;
    }

    private Long requireCurrentUserId() {
        return currentUserPort.currentUserId()
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.UNAUTHORIZED, "Unauthenticated"));
    }

    private void assertOrganizeMembership(Long organizeId) {
        Long requesterId = requireCurrentUserId();
        boolean isMember = organizeMemberPort.existsByOrganizeAndUser(OrganizeId.of(organizeId),
                                                                      UserId.of(requesterId));
        if (!isMember) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.FORBIDDEN, "User is not a member of the organization.");
        }
    }

    private OwnerContext resolveOwnerContext(RepositoryCreateCommand command,
                                             OwnerType ownerType,
                                             RepositoryName repositoryName) {
        if (ownerType == OwnerType.ORGANIZATION) {
            OwnerId ownerId = OwnerId.of(command.getOrganizeId());
            validateRepositoryNameUnique(ownerType, ownerId, repositoryName);
            String namespace = repositoryNamespaceResolver.resolve(ownerType, ownerId);
            return new OwnerContext(ownerType, ownerId, namespace);
        }
        Long currentUserId = requireCurrentUserId();
        OwnerId ownerId = OwnerId.of(currentUserId);
        validateRepositoryNameUnique(OwnerType.USER, ownerId, repositoryName);
        String namespace = repositoryNamespaceResolver.resolve(ownerType, ownerId);
        return new OwnerContext(OwnerType.USER, ownerId, namespace);
    }

    private Optional<Repository> resolveRepositoryByPath(String namespace, String repoName) {
        if (namespace == null || namespace.isBlank() || repoName == null || repoName.isBlank()) {
            return Optional.empty();
        }

        String normalizedNamespace = namespace.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        String normalizedRepo = repoName.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        String clonePath = RepositoryPathHelper.buildClonePath(normalizedNamespace, normalizedRepo);
        Optional<Repository> byClonePath = repositoryPort.findByClonePath(clonePath);
        if (byClonePath.isPresent()) {
            return byClonePath;
        }

        Optional<Repository> userRepository = userPort.findByUsername(normalizedNamespace)
                .flatMap(user -> repositoryPort.findByOwnerAndName(
                        OwnerType.USER,
                        OwnerId.of(user.getId()),
                        RepositoryName.from(normalizedRepo)
                ));

        Optional<Repository> organizeRepository = findOrganizeByNamespace(normalizedNamespace)
                .flatMap(organize -> repositoryPort.findByOwnerAndPath(
                        OwnerType.ORGANIZATION,
                        OwnerId.of(organize.getId().getValue()),
                        RepositoryPath.from(normalizedRepo)
                ));

        if (userRepository.isPresent() && organizeRepository.isPresent()) {
            log.warn("Ambiguous repository path. namespace={}, repoName={}. Prefer USER-owned repository.", namespace, repoName);
            return userRepository;
        }
        return userRepository.isPresent() ? userRepository : organizeRepository;
    }

    private Optional<io.jgitkins.server.domain.aggregate.Organize> findOrganizeByNamespace(String namespace) {
        try {
            return organizePort.findByName(OrganizeName.from(namespace));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private record OwnerContext(OwnerType ownerType, OwnerId ownerId, String namespace) {}
}

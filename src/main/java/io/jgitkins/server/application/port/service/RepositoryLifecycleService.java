package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.event.DomainEventPublisher;
import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.common.exception.UnprocessableException;
import io.jgitkins.server.application.dto.RepositoryCreationContext;
import io.jgitkins.server.application.dto.command.RepositoryCreateCommand;
import io.jgitkins.server.application.dto.result.RepositoryResult;
import io.jgitkins.server.application.mapper.RepositoryApplicationMapper;
import io.jgitkins.server.application.port.in.RepositoryCreateUseCase;
import io.jgitkins.server.application.port.in.RepositoryDeleteUseCase;
import io.jgitkins.server.application.port.in.RepositoryLoadUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.RepositoryGitPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.service.RepositoryNamespaceResolver;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.*;
import io.jgitkins.server.infrastructure.support.RepositoryPathHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final CurrentUserPort currentUserPort;

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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found: " + repositoryId));
        return repositoryApplicationMapper.toDto(repository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepositoryResult> getRepositories() {
        List<RepositoryResult> repositories= repositoryPort.findAll()
                .stream()
                .map(repository -> repositoryApplicationMapper.toDto(repository))
                .toList();
        log.debug("repositories: [{}]", repositories);
        return repositories;
    }

    @Override
    @Transactional
    public void deleteRepository(Long repositoryId) {
        RepositoryId id = RepositoryId.of(repositoryId);

        Repository repository = repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND, "Repository not found: " + repositoryId));

        enforceDeletionPermission(repository);

        deleteRepositoryDirectory(repository);

        repositoryPort.delete(id);
    }








    private void ensureRepositoryNameUnique(OwnerType ownerType, OwnerId ownerId, RepositoryName name) {
        repositoryPort.findByOwnerAndName(ownerType, ownerId, name)
                .ifPresent(existing -> {
                    throw new ConflictException(ErrorCode.REPOSITORY_ALREADY_EXISTS,
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
            throw new UnprocessableException(ErrorCode.BAD_REQUEST, "ownerType is required.");
        }
        return ownerType;
    }

    private void validateOwnership(RepositoryCreateCommand command, OwnerType ownerType) {
        // USER
        if (ownerType == OwnerType.USER) {
            if (command.getOrganizeId() != null) {
                throw new UnprocessableException(ErrorCode.BAD_REQUEST,
                        "organizeId must be null when ownerType is USER.");
            }
            requireCurrentUserId();
            return;
        }

        // ORGANIZE
        if (command.getOrganizeId() == null) {
            throw new UnprocessableException(ErrorCode.BAD_REQUEST,
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
                .orElseThrow(() -> new UnprocessableException(ErrorCode.UNAUTHORIZED, "Unauthenticated"));
        if (!repository.getOwnerId().getValue().equals(requesterId)) {
            throw new UnprocessableException(ErrorCode.FORBIDDEN, "Cannot delete another user's repository");
        }
    }

    private RepositoryVisibility resolveVisibility(String value) {
        return value != null ? RepositoryVisibility.from(value) : RepositoryVisibility.PRIVATE;
    }

    private Long requireCurrentUserId() {
        return currentUserPort.currentUserId()
                .orElseThrow(() -> new UnprocessableException(ErrorCode.UNAUTHORIZED, "Unauthenticated"));
    }

    private void assertOrganizeMembership(Long organizeId) {
        Long requesterId = requireCurrentUserId();
        boolean isMember = organizeMemberPort.existsByOrganizeAndUser(OrganizeId.of(organizeId),
                                                                      UserId.of(requesterId));
        if (!isMember) {
            throw new UnprocessableException(ErrorCode.FORBIDDEN, "User is not a member of the organization.");
        }
    }

    private OwnerContext resolveOwnerContext(RepositoryCreateCommand command,
                                             OwnerType ownerType,
                                             RepositoryName repositoryName) {
        if (ownerType == OwnerType.ORGANIZATION) {
            OwnerId ownerId = OwnerId.of(command.getOrganizeId());
            ensureRepositoryNameUnique(ownerType, ownerId, repositoryName);
            String namespace = repositoryNamespaceResolver.resolve(ownerType, ownerId);
            return new OwnerContext(ownerType, ownerId, namespace);
        }
        Long currentUserId = requireCurrentUserId();
        OwnerId ownerId = OwnerId.of(currentUserId);
        ensureRepositoryNameUnique(OwnerType.USER, ownerId, repositoryName);
        String namespace = repositoryNamespaceResolver.resolve(ownerType, ownerId);
        return new OwnerContext(OwnerType.USER, ownerId, namespace);
    }

    private record OwnerContext(OwnerType ownerType, OwnerId ownerId, String namespace) {}
}

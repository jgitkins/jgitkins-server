package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.event.DomainEventPublisher;
import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.CreateRepositoryCommand;
import io.jgitkins.server.application.dto.RepositoryCreationContext;
import io.jgitkins.server.application.dto.RepositoryResult;
import io.jgitkins.server.application.mapper.RepositoryApplicationMapper;
import io.jgitkins.server.application.port.in.RepositoryCreationUseCase;
import io.jgitkins.server.application.port.in.RepositoryDeletionUseCase;
import io.jgitkins.server.application.port.in.RepositoryLoadUseCase;
import io.jgitkins.server.application.port.out.CreateRepositoryPort;
import io.jgitkins.server.application.port.out.DeleteRepositoryPort;
import io.jgitkins.server.application.port.out.OrganizePersistencePort;
import io.jgitkins.server.application.port.out.RepositoryPersistencePort;
import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.BranchName;
import io.jgitkins.server.domain.model.vo.InitialCommitOptions;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.domain.model.vo.RepositoryVisibility;
import io.jgitkins.server.domain.model.vo.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryLifecycleService implements RepositoryCreationUseCase,
        RepositoryLoadUseCase,
        RepositoryDeletionUseCase {

    private final CreateRepositoryPort createRepositoryPort;
    private final DeleteRepositoryPort deleteRepositoryPort;
    private final RepositoryPersistencePort repositoryPersistencePort;
    private final OrganizePersistencePort organizePersistencePort;
    private final RepositoryApplicationMapper repositoryApplicationMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    public RepositoryResult create(CreateRepositoryCommand command) {

        InitialCommitOptions initialCommitOptions = InitialCommitOptions.of(command.isReadme(),
                                                                            command.getMessage(),
                                                                            command.getAuthorName(),
                                                                            command.getAuthorEmail());

        RepositoryCreationContext context = prepareCreationContext(command);

        Repository repository = Repository.create(context.organizeId(),
                                                  context.repositoryName(),
                                                  context.repositoryPath(),
                                                  context.defaultBranch(),
                                                  context.visibility(),
                                                  context.owner(),
                                                  command.getDescription(),
                                                  context.clonePath(),
                                                  command.getCredentialId(),
                                                  initialCommitOptions);

        Repository savedRepository = repositoryPersistencePort.save(repository);

        createRepositoryPort.create(context.organizeSlug(), context.repositoryName().getValue());
        log.info("repository has created successful");

        publishDomainEvents(savedRepository);

        Repository refreshed = repositoryPersistencePort.findById(savedRepository.getId())
                .orElse(savedRepository);
        return repositoryApplicationMapper.toDto(refreshed);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryResult getRepository(Long repositoryId) {
        Repository repository = repositoryPersistencePort.findById(RepositoryId.of(repositoryId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found: " + repositoryId));
        return repositoryApplicationMapper.toDto(repository);
    }

//    @Override
//    @Transactional
//    public RepositoryResult updateRepository(Long repositoryId, UpdateRepositoryCommand command) {
//        Repository repository = repositoryPersistencePort.findById(RepositoryId.of(repositoryId))
//                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND, "Repository not found: " + repositoryId));
//
//        String normalizedType = command.getRepositoryType() != null
//                ? command.getRepositoryType().trim().toUpperCase()
//                : null;
//
//        if (command.getName() != null) {
//            RepositoryName newName = RepositoryName.from(command.getName());
//            if (!repository.getName().getValue().equalsIgnoreCase(newName.getValue())) {
//                ensureRepositoryNameUnique(repository.getOrganizeId(), newName, repository.getId());
//            }
//        }
//
//        RepositoryPath newPath = command.getPath() != null ? RepositoryPath.from(command.getPath()) : null;
//        String organizeSlug = loadOrganize(repository.getOrganizeId()).getPath().getValue();
//        String effectiveRepoPath = newPath != null ? newPath.getValue() : repository.getPath().getValue();
//        String clonePath = buildClonePath(organizeSlug, effectiveRepoPath);
//
//        Repository updated = repository.updateMetadata(
//                command.getName() != null ? RepositoryName.from(command.getName()) : null,
//                newPath,
//                command.getDefaultBranch() != null ? BranchName.of(command.getDefaultBranch()) : null,
//                command.getVisibility() != null ? RepositoryVisibility.from(command.getVisibility()) : null,
//                normalizedType,
//                command.getOwnerId() != null ? UserId.of(command.getOwnerId()) : null,
//                command.getDescription(),
//                clonePath,
//                command.getCredentialId()
//        );
//
//        if (command.getLastSyncedAt() != null) {
//            updated = updated.markSynced(command.getLastSyncedAt());
//        }
//
//        Repository persisted = repositoryPersistencePort.update(updated);
//        return repositoryApplicationMapper.toDto(persisted);
//    }

    @Override
    @Transactional
    public void deleteRepository(Long repositoryId) {
        RepositoryId id = RepositoryId.of(repositoryId);
        Repository repository = repositoryPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND, "Repository not found: " + repositoryId));
        deleteRepositoryDirectory(repository);
        repositoryPersistencePort.delete(id);
    }

    private void ensureRepositoryNameUnique(OrganizeId organizeId,
                                            RepositoryName name,
                                            RepositoryId currentRepositoryId) {
        repositoryPersistencePort.findByOrganizeAndName(organizeId, name)
                .ifPresent(existing -> {
                    if (currentRepositoryId == null || !existing.getId().equals(currentRepositoryId)) {
                        throw new ConflictException(ErrorCode.REPOSITORY_ALREADY_EXISTS,
                                "Repository name already exists in organize: " + name.getValue());
                    }
                });
    }

    private void deleteRepositoryDirectory(Repository repository) {
        Organize organize = loadOrganize(repository.getOrganizeId());
        deleteRepositoryPort.delete(organize.getName().getValue(), repository.getName().getValue());
    }

    private Organize loadOrganize(OrganizeId organizeId) {
        return organizePersistencePort.findById(organizeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + organizeId.getValue()));
    }

    private String buildClonePath(String organizeSlug, String repoPath) {
        String orgSegment = trimSlashes(organizeSlug);
        String repoSegment = trimSlashes(repoPath);
        if (!repoSegment.endsWith(".git")) {
            repoSegment = repoSegment + ".git";
        }
        return "/" + orgSegment + "/" + repoSegment;
    }

    private String trimSlashes(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private RepositoryPath resolveRepositoryPath(String requestedPath, String repoName) {
        String candidate = (requestedPath == null || requestedPath.isBlank()) ? repoName : requestedPath;
        return RepositoryPath.from(candidate);
    }

    private RepositoryCreationContext prepareCreationContext(CreateRepositoryCommand command) {
        OrganizeId organizeId = OrganizeId.of(command.getOrganizeId());
        RepositoryName repositoryName = RepositoryName.from(command.getRepoName());
        ensureRepositoryNameUnique(organizeId, repositoryName, null);
        Organize organize = loadOrganize(organizeId);

        RepositoryPath repositoryPath = resolveRepositoryPath(command.getPath(), command.getRepoName());
        BranchName defaultBranch = BranchName.of(command.getMainBranch());
        RepositoryVisibility visibility = command.getVisibility() != null
                ? RepositoryVisibility.from(command.getVisibility())
                : RepositoryVisibility.PRIVATE;
        UserId owner = command.getOwnerId() != null ? UserId.of(command.getOwnerId()) : null;
        String clonePath = buildClonePath(organize.getName().getValue(), repositoryPath.getValue());

        return new RepositoryCreationContext(organizeId,
                                             repositoryName,
                                             repositoryPath,
                                             defaultBranch,
                                             visibility,
                                             owner,
                                             clonePath,
                                             organize.getName().getValue());
    }

    private void publishDomainEvents(Repository repository) {
        domainEventPublisher.publish(repository.getDomainEvents());
        repository.clearDomainEvents();
    }
}

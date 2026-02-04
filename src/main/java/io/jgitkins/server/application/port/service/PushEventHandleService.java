package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.command.JobCreateCommand;
import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.port.in.PushEventHandleUseCase;
import io.jgitkins.server.application.port.in.JobCreateUseCase;
import io.jgitkins.server.application.port.out.BranchPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import io.jgitkins.server.domain.model.vo.OwnerId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushEventHandleService implements PushEventHandleUseCase {

    private final JobCreateUseCase jobCreateUseCase;
    private final RepositoryPort repositoryPort;
    private final BranchPort branchPort;

    private final OrganizePort organizePort;
    private final UserPort userPort;

    @Override
    @Transactional
    public void handle(PushEventCommand command) {

        OwnerContext ownerContext = resolveOwnerContext(command.getNamespace());
        if (ownerContext == null) {
            log.warn("push event skipped: owner not resolved. namespace: [{}]", command.getNamespace());
            return;
        }

        Optional<Long> repositoryIdOptional = repositoryPort.findRepositoryId(ownerContext.ownerType(),
                                                                              ownerContext.ownerId(),
                                                                              command.getRepositoryName());
        log.debug("repository: [{}]", repositoryIdOptional);

        if (repositoryIdOptional.isEmpty()) {
            log.warn("push event skipped: repository not registered. ownerType: [{}] namespace: [{}] repoName: [{}]",
                    ownerContext.ownerType(), command.getNamespace(), command.getRepositoryName());
            return;
        }

        Long repositoryId = repositoryIdOptional.get();
        createBranchIfNeeded(repositoryId, command);

        if (shouldSkipJob(command)) {
            return;
        }

        jobCreateUseCase.create(buildJobCommand(command, repositoryId));
    }

    private void createBranchIfNeeded(Long repositoryId, PushEventCommand command) {
        if (!command.isBranchCreated()) {
            return;
        }
        branchPort.create(Branch.create(repositoryId, command.getBranchName()));
    }

    private boolean shouldSkipJob(PushEventCommand command) {
        if (command.getCommitHash() == null || command.getCommitHash().isBlank()) {
            log.warn("push event skipped: missing commit hash for repo={} branch={}",
                    command.getRepositoryName(), command.getBranchName());
            return true;
        }

        if (command.getTriggeredBy() == null) {
            log.warn("push event skipped: unable to resolve triggering user for repo={} branch={}",
                    command.getRepositoryName(), command.getBranchName());
            return true;
        }
        return false;
    }

    private JobCreateCommand buildJobCommand(PushEventCommand command, Long repositoryId) {
        return JobCreateCommand.builder()
                .taskCd(command.getNamespace())
                .repoName(command.getRepositoryName())
                .repositoryId(repositoryId)
                .branchName(command.getBranchName())
                .commitHash(command.getCommitHash())
                .triggeredBy(command.getTriggeredBy())
                .build();
    }

    private OwnerContext resolveOwnerContext(String namespace) {
        if (namespace == null || namespace.isBlank()) {
            return null;
        }
        OwnerContext userOwner = userPort.findUserIdByUsername(namespace)
                .map(userId -> new OwnerContext(OwnerType.USER, OwnerId.of(userId)))
                .orElse(null);
        OwnerContext organizeOwner = findOrganizeOwner(namespace);
        if (userOwner != null && organizeOwner != null) {
            log.warn("push event skipped: ambiguous namespace. namespace=[{}]", namespace);
            return null;
        }
        return userOwner != null ? userOwner : organizeOwner;
    }

    private OwnerContext findOrganizeOwner(String namespace) {
        try {
            return organizePort.findByName(OrganizeName.from(namespace))
                    .map(organize -> new OwnerContext(OwnerType.ORGANIZATION, OwnerId.of(organize.getId().getValue())))
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private record OwnerContext(OwnerType ownerType, OwnerId ownerId) {}
}

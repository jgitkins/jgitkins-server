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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

        OwnerId ownerId = resolveOwnerId(command.getOwnerType(), command.getNamespace());
        if (ownerId == null) {
            log.warn("push event skipped: owner not resolved. ownerType: [{}] namespace: [{}]", command.getOwnerType(), command.getNamespace());
            return;
        }

        Optional<Long> repositoryIdOptional = repositoryPort.findRepositoryId(command.getOwnerType(), ownerId, command.getRepositoryName());
        log.debug("repository: [{}]", repositoryIdOptional);

        if (repositoryIdOptional.isEmpty()) {
            log.warn("push event skipped: repository not registered. ownerType: [{}] namespace: [{}] repoName: [{}]", command.getOwnerType(), command.getNamespace(), command.getRepositoryName());
            return;
        }

        Long repositoryId = repositoryIdOptional.get();
        if (command.isBranchCreated()) {
            branchPort.create(Branch.create(repositoryId, command.getBranchName()));
        }

        if (command.getCommitHash() == null || command.getCommitHash().isBlank()) {
            log.warn("push event skipped: missing commit hash for repo={} branch={}",
                    command.getRepositoryName(), command.getBranchName());
            return;
        }

        if (command.getTriggeredBy() == null) {
            log.warn("push event skipped: unable to resolve triggering user for repo={} branch={}",
                    command.getRepositoryName(), command.getBranchName());
            return;
        }

        JobCreateCommand jobCommand = JobCreateCommand.builder()
                .taskCd(command.getNamespace())
                .repoName(command.getRepositoryName())
                .repositoryId(repositoryId)
                .branchName(command.getBranchName())
                .commitHash(command.getCommitHash())
                .triggeredBy(command.getTriggeredBy())
                .build();

        jobCreateUseCase.create(jobCommand);
    }


    private OwnerId resolveOwnerId(OwnerType ownerType, String namespace) {
        if (ownerType == null || namespace == null || namespace.isBlank()) {
            return null;
        }
        if (OwnerType.USER == ownerType) {
            return userPort.findUserIdByUsername(namespace)
                    .map(OwnerId::of)
                    .orElse(null);
        }
        if (OwnerType.ORGANIZATION == ownerType) {
            return organizePort.findByName(OrganizeName.from(namespace))
                    .map(organize -> OwnerId.of(organize.getId().getValue()))
                    .orElse(null);
        }
        return null;
    }
}

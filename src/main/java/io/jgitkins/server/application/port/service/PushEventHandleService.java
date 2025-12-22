package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.command.JobCreateCommand;
import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.port.in.PushEventHandleUseCase;
import io.jgitkins.server.application.port.in.JobCreationUseCase;
import io.jgitkins.server.application.port.out.BranchPersistenceCommandPort;
import io.jgitkins.server.application.port.out.RepositoryLoadPort;
import io.jgitkins.server.domain.Branch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushEventHandleService implements PushEventHandleUseCase {

    private final RepositoryLoadPort repositoryLoadPort;
    private final BranchPersistenceCommandPort branchPersistenceCommandPort;
    private final JobCreationUseCase jobCreationUseCase;

    @Override
    @Transactional
    public void handle(PushEventCommand command) {
        Optional<Long> repositoryIdOptional = repositoryLoadPort.findRepositoryId(command.getOrganizeCode(), command.getRepositoryName());

        if (repositoryIdOptional.isEmpty()) {
            log.warn("push event skipped: repository not registered. taskCd={} repo={}", command.getOrganizeCode(), command.getRepositoryName());
            return;
        }

        Long repositoryId = repositoryIdOptional.get();

        if (command.isBranchCreated()) {
            branchPersistenceCommandPort.create(Branch.create(repositoryId, command.getBranchName()));
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
                .taskCd(command.getOrganizeCode())
                .repoName(command.getRepositoryName())
                .repositoryId(repositoryId)
                .branchName(command.getBranchName())
                .commitHash(command.getCommitHash())
                .triggeredBy(command.getTriggeredBy())
                .build();

        jobCreationUseCase.create(jobCommand);
    }
}

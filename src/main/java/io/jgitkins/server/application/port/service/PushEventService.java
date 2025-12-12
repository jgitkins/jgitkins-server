package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.JobCreateCommand;
import io.jgitkins.server.application.dto.PushEventCommand;
import io.jgitkins.server.application.port.in.HandlePushEventUseCase;
import io.jgitkins.server.application.port.in.JobCreateUseCase;
import io.jgitkins.server.application.port.out.BranchPersistencePort;
import io.jgitkins.server.application.port.out.RepositoryLoadPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushEventService implements HandlePushEventUseCase {

    private final RepositoryLoadPort repositoryLoadPort;
    private final BranchPersistencePort branchPersistencePort;
    private final JobCreateUseCase jobCreateUseCase;

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
            branchPersistencePort.create(repositoryId, command.getBranchName());
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

        jobCreateUseCase.create(jobCommand);
    }
}

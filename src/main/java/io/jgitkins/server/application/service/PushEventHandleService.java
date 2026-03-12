package io.jgitkins.server.application.service;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.dto.command.JobCreateCommand;
import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.port.in.JobCreateUseCase;
import io.jgitkins.server.application.port.in.PushEventHandleUseCase;
import io.jgitkins.server.application.port.out.BranchPersistencePort;
import io.jgitkins.server.domain.Branch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushEventHandleService implements PushEventHandleUseCase {

    private final JobCreateUseCase jobCreateUseCase;
    private final BranchPersistencePort branchPort;

    @Override
    @Transactional
    public void handle(PushEventCommand command) {
        if (command.getRepositoryId() == null) {
            throw new io.jgitkins.server.application.exception.ApplicationException(
                    ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                    "Repository identifier is required for push event handling.");
        }

        log.debug("Handling push event for repositoryId=[{}], repoName=[{}]", command.getRepositoryId(), command.getRepoName());

        // 2. 브랜치 상태 영속화
        updateBranchState(command.getRepositoryId(), command);

        // 3. 후속 작업 트리거 (Job 생성 등)
        if (validateCanCreateJob(command)) {
            jobCreateUseCase.create(buildJobCommand(command));
        }
    }

    private void updateBranchState(Long repositoryId, PushEventCommand command) {
        if (command.isBranchCreated()) {
            log.info("Creating new branch [{}] for repository [{}]", command.getBranchName(), repositoryId);
            branchPort.save(Branch.create(repositoryId, command.getBranchName()));
        } else if (command.isBranchDeleted()) {
            log.info("Deleting branch [{}] from repository [{}]", command.getBranchName(), repositoryId);
            branchPort.deleteByRepositoryIdAndName(repositoryId, command.getBranchName());
        }
        // UPDATE(Push)의 경우 현재 로직에서는 별도의 Branch 엔티티 갱신이 필요 없음 (커밋 해시는 Job에 기록됨)
    }

    private boolean validateCanCreateJob(PushEventCommand command) {
        if (command.isBranchDeleted()) {
            return false;
        }

        if (command.getCommitHash() == null || command.getCommitHash().isBlank()) {
            log.warn("push event job skipped: missing commit hash for branch={}", command.getBranchName());
            return false;
        }

        if (command.getTriggeredBy() == null) {
            log.warn("push event job skipped: unable to resolve triggering user for branch={}", command.getBranchName());
            return false;
        }
        return true;
    }

    private JobCreateCommand buildJobCommand(PushEventCommand command) {
        return JobCreateCommand.builder()
                .taskCd(command.getTaskCd())
                .repoName(command.getRepoName())
                .repositoryId(command.getRepositoryId())
                .branchName(command.getBranchName())
                .commitHash(command.getCommitHash())
                .triggeredBy(command.getTriggeredBy())
                .build();
    }
}

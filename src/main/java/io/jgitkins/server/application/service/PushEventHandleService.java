package io.jgitkins.server.application.service;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.dto.command.JobCreateCommand;
import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.port.in.JobCreateUseCase;
import io.jgitkins.server.application.port.in.PushEventHandleUseCase;
import io.jgitkins.server.application.port.out.BranchPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.exception.ApplicationException;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.domain.aggregate.Repository;
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

    @Override
    @Transactional
    public void handle(PushEventCommand command) {
        // 1. 저장소 로딩 (Port 활용하여 경로 기반 조회)
        Repository repository = repositoryPort.findByPath(command.getGitDirPath())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found for path: " + command.getGitDirPath()));

        log.debug("Handling push event for repository: [{}]", repository.getName().getValue());

        // 2. 브랜치 상태 영속화
        updateBranchState(repository.getId().getValue(), command);

        // 3. 후속 작업 트리거 (Job 생성 등)
        if (shouldTriggerJob(command)) {
            jobCreateUseCase.create(buildJobCommand(command, repository));
        }
    }

    private void updateBranchState(Long repositoryId, PushEventCommand command) {
        if (command.isBranchCreated()) {
            log.info("Creating new branch [{}] for repository [{}]", command.getBranchName(), repositoryId);
            branchPort.create(Branch.create(repositoryId, command.getBranchName()));
        } else if (command.isBranchDeleted()) {
            log.info("Deleting branch [{}] from repository [{}]", command.getBranchName(), repositoryId);
            branchPort.delete(repositoryId, command.getBranchName());
        }
        // UPDATE(Push)의 경우 현재 로직에서는 별도의 Branch 엔티티 갱신이 필요 없음 (커밋 해시는 Job에 기록됨)
    }

    private boolean shouldTriggerJob(PushEventCommand command) {
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

    private JobCreateCommand buildJobCommand(PushEventCommand command, Repository repository) {
        return JobCreateCommand.builder()
                .taskCd(repository.getOwnerId().toString()) // Namespace 역할
                .repoName(repository.getName().getValue())
                .repositoryId(repository.getId().getValue())
                .branchName(command.getBranchName())
                .commitHash(command.getCommitHash())
                .triggeredBy(command.getTriggeredBy())
                .build();
    }
}

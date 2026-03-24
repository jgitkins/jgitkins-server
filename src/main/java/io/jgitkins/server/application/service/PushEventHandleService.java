package io.jgitkins.server.application.service;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.dto.command.JobCreateCommand;
import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.dto.result.JobPlan;
import io.jgitkins.server.application.dto.support.PushJobPlanRequest;
import io.jgitkins.server.application.port.in.JobCreateUseCase;
import io.jgitkins.server.application.port.in.PushEventHandleUseCase;
import io.jgitkins.server.application.port.out.BranchPersistencePort;
import io.jgitkins.server.application.support.PushJobCreationPlanner;
import io.jgitkins.server.domain.Branch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j // TODO: rename 필요? 어쨌든 Push 이벤트가 호출됐을 때 PostHook 이 호출하는 서비스이다보니, 명칭에 Post 수식이 필요한지 검토 필요
public class PushEventHandleService implements PushEventHandleUseCase {

    private final JobCreateUseCase jobCreateUseCase;
    private final BranchPersistencePort branchPort;
    private final PushJobCreationPlanner pushJobCreationPlanner;

    @Override
    @Transactional
    public void handle(PushEventCommand command) {
        // TODO: 예외를 던지는게 의미가 없을 것같음 > `PushHook` 쪽에서 이미 던지고있는데 이 부분도 검토 진행바람
        if (command.getRepositoryId() == null) {
            throw new io.jgitkins.server.application.exception.ApplicationException(
                    ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                    "Repository identifier is required for push event handling.");
        }

        log.debug("Handling push event for repositoryId=[{}], repoName=[{}]", command.getRepositoryId(), command.getRepoName());

        // 2. 브랜치 상태 영속화
        updateBranchState(command.getRepositoryId(), command);

        // TODO: 별도의 validator 를 두어진행해도 좋을 듯 함  `JobCreationValidator`
        if (!canCreateJob(command)) {
            return;
        }

        // TODO: 해당 내용도 Job을 생성할지 말지 결정되는 규칙이다. `JobCreationValidator` 의 별도 메세지로두는게 어떤지?
        //  그리고 두 Validator 를 현재 클래스의 별도의 private method (validateCanCreateJob) 로 분기해서 내부클래스 하나만호출하는 방향성 검토
        JobPlan jobPlan;
        try {
            jobPlan = pushJobCreationPlanner.plan(PushJobPlanRequest.from(command));
        } catch (RuntimeException ex) {
            log.warn("push event job planning skipped due to planner error. repo=[{}] branch=[{}] commit=[{}]",
                    command.getRepoName(), command.getBranchName(), command.getCommitHash(), ex);
            return;
        }

        if (jobPlan.isSkipped()) {
            log.info("push event job skipped: reason={}", jobPlan.getSkipReason());
            return;
        }

        jobCreateUseCase.create(buildJobCommand(command, jobPlan.getPipelineFilePath()));
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

    private boolean canCreateJob(PushEventCommand command) {
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

    private JobCreateCommand buildJobCommand(PushEventCommand command, String pipelineFilePath) {
        return JobCreateCommand.builder()
                .repoName(command.getRepoName())
                .repositoryId(command.getRepositoryId())
                .branchName(command.getBranchName())
                .commitHash(command.getCommitHash())
                .pipelineFilePath(pipelineFilePath)
                .triggeredBy(command.getTriggeredBy())
                .build();
    }
}

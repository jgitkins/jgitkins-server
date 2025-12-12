package io.jgitkins.server.application.service;

import io.jgitkins.server.application.common.CloneUrlBuilder;
import io.jgitkins.server.application.dto.JobDispatchMessage;
import io.jgitkins.server.application.dto.PendingJob;
import io.jgitkins.server.application.dto.RunnerAssignmentCandidate;
import io.jgitkins.server.application.dto.RunnerJobFetchRequest;
import io.jgitkins.server.application.port.in.JobDispatchUseCase;
import io.jgitkins.server.application.port.out.JobDispatchEventPort;
import io.jgitkins.server.application.port.out.JobQueuePort;
import io.jgitkins.server.application.port.out.RunnerQueryPort;
import io.jgitkins.server.domain.aggregate.Job;
import io.jgitkins.server.domain.aggregate.Runner;
import io.jgitkins.server.domain.model.JobHistory;
import io.jgitkins.server.domain.model.vo.RunnerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobDispatchService implements JobDispatchUseCase {

    private final JobQueuePort jobQueuePort;
    private final JobDispatchEventPort jobDispatchEventPort;
    private final RunnerQueryPort runnerQueryPort;
    private final CloneUrlBuilder cloneUrlBuilder;

    @Override
    @Transactional
    public Optional<JobDispatchMessage> dispatchJobForRunner(RunnerJobFetchRequest request) {
        Optional<RunnerAssignmentCandidate> candidateOptional = resolveRunnerCandidate(request.getRunnerToken());
        if (candidateOptional.isEmpty()) {
            return Optional.empty();
        }

        RunnerAssignmentCandidate candidate = candidateOptional.get();
        Optional<PendingJob> pendingJob = jobQueuePort.fetchPendingJobFor(candidate);
        if (pendingJob.isEmpty()) {
            return Optional.empty();
        }
        return assignRunner(candidate, pendingJob.get());
    }

    private Optional<RunnerAssignmentCandidate> resolveRunnerCandidate(String runnerToken) {
        if (runnerToken == null || runnerToken.isBlank()) {
            log.warn("Runner token is missing");
            return Optional.empty();
        }
        Optional<Runner> runnerOptional = runnerQueryPort.findByToken(runnerToken);
        if (runnerOptional.isEmpty()) {
            log.warn("Runner not found for token={}", runnerToken);
            return Optional.empty();
        }
        Runner runner = runnerOptional.get();
        return Optional.of(RunnerAssignmentCandidate.builder()
                                                    .runnerId(runner.getId())
                                                    .targetType(runner.getScopeType().name())
                                                    .targetId(runner.getScopeTargetId())
                                                    .build());
    }

    private Optional<JobDispatchMessage> assignRunner(RunnerAssignmentCandidate candidate, PendingJob pendingJob) {
        Job job = pendingJob.getJob();
        JobHistory previousHistory = job.getLatestHistory();
        RunnerId runnerId = RunnerId.of(String.valueOf(candidate.getRunnerId()));
        job.publish(runnerId);

        Optional<Long> historyId = jobQueuePort.persistHistory(job, previousHistory);
        if (historyId.isEmpty()) {
            log.debug("Job {} was already processed by another dispatcher", job.getId().getValue());
            return Optional.empty();
        }

        JobDispatchMessage message = publishDispatchMessage(candidate, pendingJob, job, historyId.get());
        return Optional.of(message);
    }

    private JobDispatchMessage publishDispatchMessage(RunnerAssignmentCandidate candidate,
                                                      PendingJob pendingJob,
                                                      Job job,
                                                      Long jobHistoryId) {

        JobDispatchMessage message = JobDispatchMessage.builder()
                                                        .jobId(parseJobId(job))
                                                        .jobHistoryId(jobHistoryId)
                                                        .runnerId(candidate.getRunnerId())
                                                        .repositoryId(job.getRepositoryId().getValue())
                                                        .organizeId(pendingJob.getOrganizeId())
                                                        .commitHash(job.getCommitHash().getValue())
                                                        .branchName(job.getBranchName().getValue())
                                                        .triggeredBy(job.getTriggeredBy().getValue())
                                                        .dispatchedAt(LocalDateTime.now())
                                                        .cloneUrl(cloneUrlBuilder.build(pendingJob.getRepositoryClonePath()))
                                                        .build();
//        jobDispatchEventPort.publish(message);
        return message;
    }

    private Long parseJobId(Job job) {
        try {
            return Long.parseLong(job.getId().getValue());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

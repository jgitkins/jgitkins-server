package io.jgitkins.server.application.service;

import io.jgitkins.server.application.dto.PendingJob;
import io.jgitkins.server.application.dto.RunnerAssignmentCandidate;
import io.jgitkins.server.application.port.in.JobDispatchUseCase;
import io.jgitkins.server.application.port.out.JobQueuePort;
import io.jgitkins.server.application.port.out.RunnerAllocationPort;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobDispatchService implements JobDispatchUseCase {

    private final JobQueuePort jobQueuePort;
    private final RunnerAllocationPort runnerAllocationPort;

    @Override
    public void dispatchPendingJob() {
        List<RunnerAssignmentCandidate> runnableAssignments = runnerAllocationPort.findRunnableAssignments();
        log.info("Runnable Runner Count: [{}]", runnableAssignments.size());

        for (RunnerAssignmentCandidate candidate : runnableAssignments) {
            Optional<PendingJob> pendingJob = jobQueuePort.fetchPendingJobFor(candidate);
            if (pendingJob.isPresent()) {
                if (assignRunner(candidate, pendingJob.get())) {
                    break;
                }
            }
        }
    }

    private boolean assignRunner(RunnerAssignmentCandidate candidate, PendingJob pendingJob) {
        boolean updated = jobQueuePort.markJobInQueue(pendingJob.getJobHistoryId(), candidate.getRunnerId());
        if (updated) {
            log.info("Job {} assigned to runner {} (jobHistoryId={})", pendingJob.getJobId(), candidate.getRunnerId(), pendingJob.getJobHistoryId());
            return true;
        } else {
            log.debug("Job {} was already processed by another dispatcher", pendingJob.getJobId());
            return false;
        }
    }
}

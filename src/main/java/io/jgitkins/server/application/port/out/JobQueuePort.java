package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.PendingJob;
import io.jgitkins.server.application.dto.RunnerAssignmentCandidate;
import io.jgitkins.server.domain.aggregate.Job;
import io.jgitkins.server.domain.model.JobHistory;
import java.util.Optional;

public interface JobQueuePort {
    Optional<PendingJob> fetchPendingJobFor(RunnerAssignmentCandidate candidate);
    Optional<Long> persistHistory(Job job, JobHistory previousHistory);
    Optional<Job> loadJob(Long jobId);
}

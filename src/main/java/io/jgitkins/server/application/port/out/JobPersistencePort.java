package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.PendingJob;
import io.jgitkins.server.application.dto.RunnerAssignmentCandidate;
import io.jgitkins.server.domain.aggregate.Job;
import io.jgitkins.server.domain.model.JobHistory;

import java.util.Optional;

public interface JobPersistencePort {
    void save(Job job);

    Optional<PendingJob> findPendingByCandidate(RunnerAssignmentCandidate candidate);
    Optional<Long> saveHistory(Job job, JobHistory previousHistory);
    Optional<Job> findById(Long jobId);
}

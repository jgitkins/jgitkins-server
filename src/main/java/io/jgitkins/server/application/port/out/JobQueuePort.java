package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.PendingJob;
import io.jgitkins.server.application.dto.RunnerAssignmentCandidate;
import java.util.Optional;

public interface JobQueuePort {
    Optional<PendingJob> fetchPendingJobFor(RunnerAssignmentCandidate candidate);
    boolean markJobInQueue(Long jobHistoryId, Long runnerId);
}

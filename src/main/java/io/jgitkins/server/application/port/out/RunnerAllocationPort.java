package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.RunnerAssignmentCandidate;
import java.util.List;

public interface RunnerAllocationPort {
    List<RunnerAssignmentCandidate> findRunnableAssignments();
}

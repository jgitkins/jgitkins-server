package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.JobDispatchMessage;
import io.jgitkins.server.application.dto.RunnerJobFetchRequest;
import java.util.Optional;

public interface JobDispatchUseCase {
    Optional<JobDispatchMessage> dispatchJobForRunner(RunnerJobFetchRequest request);
}

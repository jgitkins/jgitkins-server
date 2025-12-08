package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.RunnerDetailResult;
import java.util.List;

public interface RunnerQueryUseCase {
    RunnerDetailResult getRunner(Long runnerId);
    List<RunnerDetailResult> getRunners();
}

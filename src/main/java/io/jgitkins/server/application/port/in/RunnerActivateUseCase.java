package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.RunnerDetailResult;

public interface RunnerActivateUseCase {
    RunnerDetailResult activate(Long runnerId, String token, String remoteIp);
}

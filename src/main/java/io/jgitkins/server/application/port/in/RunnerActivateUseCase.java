package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.RunnerActivateResult;

public interface RunnerActivateUseCase {
    //    RunnerDetailResult activate(Long runnerId, String token, String remoteIp);
    RunnerActivateResult activate(String token, String remoteIp);
}

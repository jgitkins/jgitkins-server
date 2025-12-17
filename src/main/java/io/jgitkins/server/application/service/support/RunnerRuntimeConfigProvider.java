package io.jgitkins.server.application.service.support;

import io.jgitkins.server.application.dto.RunnerRuntimeConfig;
import io.jgitkins.server.infrastructure.config.RunnerRuntimeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RunnerRuntimeConfigProvider {

    private final RunnerRuntimeProperties properties;

    public RunnerRuntimeConfig createConfig() {
        return RunnerRuntimeConfig.builder()
                .restHost(properties.getRestHost())
                .restPort(properties.getRestPort())
                .restBasePath(properties.getRestBasePath())
                .grpcHost(properties.getGrpcHost())
                .grpcPort(properties.getGrpcPort())
                .pollIntervalMs(properties.getPollIntervalMs())
                .busyWaitIntervalMs(properties.getBusyWaitIntervalMs())
                .build();
    }
}

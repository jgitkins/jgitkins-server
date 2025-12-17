package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RunnerActivateResult {
    private final RunnerRuntimeConfig runtimeConfig;
    private final RunnerExecutionConfig executionConfig;
}

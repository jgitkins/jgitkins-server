package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RunnerJobFetchRequest {
    private final String runnerToken;
}

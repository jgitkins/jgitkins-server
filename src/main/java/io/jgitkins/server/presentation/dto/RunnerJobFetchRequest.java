package io.jgitkins.server.presentation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RunnerJobFetchRequest {
    private final String runnerToken;
}

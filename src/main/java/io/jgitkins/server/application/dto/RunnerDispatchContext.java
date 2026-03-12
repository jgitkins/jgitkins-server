package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class RunnerDispatchContext {
    private final Long runnerId;
    private final JobDispatchScope dispatchScope;
    private final Long scopeTargetId;
}

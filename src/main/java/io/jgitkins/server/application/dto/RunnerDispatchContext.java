package io.jgitkins.server.application.dto;

public record RunnerDispatchContext(Long runnerId,
                                    JobDispatchScope dispatchScope,
                                    Long scopeTargetId) {
}

package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class RunnerAssignmentCandidate {
    private final Long runnerId;
    private final String targetType;
    private final Long targetId;
}

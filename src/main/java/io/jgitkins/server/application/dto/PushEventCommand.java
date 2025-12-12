package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PushEventCommand {
    private final String organizeCode;
    private final String repositoryName;
    private final String branchName;
    private final boolean branchCreated;
    private final String commitHash;
    private final Long triggeredBy;
}

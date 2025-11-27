package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BranchCreateCommand {

    private final String taskCd;
    private final String repoName;
    private final String branchName;
    private final String sourceBranch;
    private final boolean physicalCreationRequired;
}

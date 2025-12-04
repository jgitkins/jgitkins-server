package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobCreateCommand {
    private final String taskCd;
    private final String repoName;
    private final String commitHash;
    private final String branchName;
    private final String triggeredBy; // username or userId
}

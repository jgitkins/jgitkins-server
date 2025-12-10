package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PendingJob {
    private final Long jobId;
    private final Long jobHistoryId;
    private final Long repositoryId;
    private final Long organizeId;
    private final String commitHash;
    private final String branchName;
    private final Long triggeredBy;
}

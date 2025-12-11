package io.jgitkins.server.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobDispatchMessage {
    private final Long jobId;
    private final Long jobHistoryId;
    private final Long runnerId;
    private final Long repositoryId;
    private final Long organizeId;
    private final String commitHash;
    private final String branchName;
    private final Long triggeredBy;
    private final LocalDateTime dispatchedAt;
}

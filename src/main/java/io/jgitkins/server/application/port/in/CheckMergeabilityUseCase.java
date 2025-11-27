package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.MergeResult;

import java.io.IOException;

public interface CheckMergeabilityUseCase {
    MergeResult checkMergeability(String taskCd, String repoName, String sourceBranch, String targetBranch) throws IOException;
}


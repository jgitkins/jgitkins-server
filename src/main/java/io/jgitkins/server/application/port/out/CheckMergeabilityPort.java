package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.result.MergeResult;

import java.io.IOException;

public interface CheckMergeabilityPort {
    MergeResult checkMergeability(String taskCd, String repoName, String sourceBranch, String targetBranch) throws IOException;
}


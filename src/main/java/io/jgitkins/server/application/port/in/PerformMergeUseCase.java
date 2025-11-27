package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.MergeRequest;
import io.jgitkins.server.application.dto.MergeResult;

import java.io.IOException;

public interface PerformMergeUseCase {
    MergeResult performMerge(String taskCd, String repoName, MergeRequest request) throws IOException;
}


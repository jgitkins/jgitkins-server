package io.jgitkins.server.application.port.service;

import java.io.IOException;

import io.jgitkins.server.application.dto.MergeRequest;
import io.jgitkins.server.application.dto.MergeResult;
import io.jgitkins.server.application.port.in.CheckMergeabilityUseCase;
import io.jgitkins.server.application.port.in.PerformMergeUseCase;
import io.jgitkins.server.application.port.out.CheckMergeabilityPort;
import io.jgitkins.server.application.port.out.PerformMergePort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MergeService implements CheckMergeabilityUseCase, PerformMergeUseCase {

    private final CheckMergeabilityPort checkMergeabilityPort;
    private final PerformMergePort performMergePort;

    @Override
    public MergeResult checkMergeability(String taskCd, String repoName, String sourceBranch, String targetBranch) throws IOException {
        return checkMergeabilityPort.checkMergeability(taskCd, repoName, sourceBranch, targetBranch);
    }

    @Override
    public MergeResult performMerge(String taskCd, String repoName, MergeRequest request) throws IOException {
        return performMergePort.performMerge(taskCd, repoName, request);
    }
}


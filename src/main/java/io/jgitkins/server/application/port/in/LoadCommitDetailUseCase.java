package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.CommitHistory;

import java.io.IOException;

public interface LoadCommitDetailUseCase {
    CommitHistory getCommitDetail(String taskCd, String repoName, String commitHash) throws IOException;
}

package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.CommitHistory;

import java.io.IOException;

public interface LoadCommitDetailPort {
    CommitHistory getCommitDetail(String taskCd, String repoName, String commitHash) throws IOException;
}

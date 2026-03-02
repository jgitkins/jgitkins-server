package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.CommitHistory;

import java.io.IOException;
import java.util.List;

public interface CommitLoadUseCase {
    CommitHistory getCommit(String taskCd, String repoName, String commitHash);
    List<CommitHistory> getCommits(String taskCd, String repoName, String branch);
}

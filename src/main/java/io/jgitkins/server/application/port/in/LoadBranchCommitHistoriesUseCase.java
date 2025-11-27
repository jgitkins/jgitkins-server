package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.CommitHistory;

import java.io.IOException;
import java.util.List;

public interface LoadBranchCommitHistoriesUseCase {
    List<CommitHistory> getBranchCommitHistories(String taskCd, String repoName, String branch) throws IOException;
}

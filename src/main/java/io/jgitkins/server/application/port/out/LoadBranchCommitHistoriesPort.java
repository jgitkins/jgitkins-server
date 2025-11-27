package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.CommitHistory;

import java.io.IOException;
import java.util.List;

public interface LoadBranchCommitHistoriesPort {
    List<CommitHistory> getCommitHistories(String taskCd, String repoName, String branch) throws IOException;
}

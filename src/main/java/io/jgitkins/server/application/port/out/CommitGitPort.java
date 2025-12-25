package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.dto.CommitHistory;

import java.io.IOException;
import java.util.List;

public interface CommitGitPort {
    CommitHistory getCommitHistory(String taskCd, String repoName, String commitHash) throws IOException;
    List<CommitHistory> getCommitHistories(String taskCd, String repoName, String branch) throws IOException;
    void commit(String taskCd,
                String repoName,
                String branch,
                String message,
                String authorName,
                String authorEmail,
                List<CommitFile> files);

}
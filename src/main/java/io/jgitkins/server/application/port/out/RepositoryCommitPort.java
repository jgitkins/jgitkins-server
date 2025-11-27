package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.CommitFile;

import java.util.List;

public interface RepositoryCommitPort {

    void commit(String taskCd,
                String repoName,
                String branch,
                String message,
                String authorName,
                String authorEmail,
                List<CommitFile> files);
}


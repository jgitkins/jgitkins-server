package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.CommitHistory;
import io.jgitkins.server.application.port.in.CommitLoadUseCase;
import io.jgitkins.server.application.port.out.CommitGitPort;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommitService implements CommitLoadUseCase {

    private final CommitGitPort commitGitPort;

    @Override
    @Transactional(readOnly = true)
    public CommitHistory getCommit(String taskCd,
                                   String repoName,
                                   String commitHash) {
        return commitGitPort.getCommitHistory(taskCd, repoName, commitHash);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommitHistory> getCommits(String taskCd,
                                          String repoName,
                                          String branch) {
        return commitGitPort.getCommitHistories(taskCd, repoName, branch);
    }
}

package io.jgitkins.server.application.service;

import io.jgitkins.server.application.dto.CommitHistory;
import io.jgitkins.server.application.port.in.CommitLoadUseCase;
import io.jgitkins.server.application.port.out.CommitGitPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommitService implements CommitLoadUseCase {

    private final CommitGitPort commitGitPort;

    @Override
    @Transactional(readOnly = true)
    public CommitHistory getCommit(String taskCd,
                                   String repoName,
                                   String commitHash) {
        return commitGitPort.loadCommit(taskCd, repoName, commitHash);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommitHistory> getCommits(String taskCd,
                                          String repoName,
                                          String branch) {
        return commitGitPort.listCommitHistory(taskCd, repoName, branch);
    }
}

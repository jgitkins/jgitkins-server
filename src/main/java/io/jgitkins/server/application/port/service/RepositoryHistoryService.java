package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.CommitHistory;
import io.jgitkins.server.application.port.in.CommitLoadUseCase;
import io.jgitkins.server.application.port.out.CommitGitLoadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepositoryHistoryService implements CommitLoadUseCase {


    private final CommitGitLoadPort commitGitLoadPort;
//    private final LoadBranchCommitHistoriesPort loadBranchCommitHistoriesPort;

    @Override
    @Transactional(readOnly = true)
    public CommitHistory getCommit(String taskCd,
                                   String repoName,
                                   String commitHash) throws IOException {
        return commitGitLoadPort.getCommitHistory(taskCd, repoName, commitHash);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommitHistory> getCommits(String taskCd,
                                          String repoName,
                                          String branch) throws IOException {
        return commitGitLoadPort.getCommitHistories(taskCd, repoName, branch);
    }
}

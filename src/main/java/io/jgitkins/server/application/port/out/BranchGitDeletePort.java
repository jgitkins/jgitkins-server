package io.jgitkins.server.application.port.out;

import java.io.IOException;

public interface BranchGitDeletePort {
    void deleteBranch(String taskCd, String repoName, String branchName) throws IOException;
}

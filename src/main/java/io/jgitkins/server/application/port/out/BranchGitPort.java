package io.jgitkins.server.application.port.out;

import java.io.IOException;

import io.jgitkins.server.application.dto.command.BranchCreationContext;

public interface BranchGitPort {
    void createBranch(BranchCreationContext context) throws IOException;
    void deleteBranch(String taskCd, String repoName, String branchName) throws IOException;

}

package io.jgitkins.server.application.port.in;

import java.io.IOException;

public interface BranchDeletetionUseCase {
    void deleteBranch(String taskCd, String repoName, String branchName) throws IOException;
}

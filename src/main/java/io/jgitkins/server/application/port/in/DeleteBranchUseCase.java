package io.jgitkins.server.application.port.in;

import java.io.IOException;

public interface DeleteBranchUseCase {
    void deleteBranch(String taskCd, String repoName, String branchName) throws IOException;
}

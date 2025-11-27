package io.jgitkins.server.application.port.out;

import java.io.IOException;

public interface DeleteBranchPort {
    void deleteBranch(String taskCd, String repoName, String branchName) throws IOException;
}

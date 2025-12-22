package io.jgitkins.server.application.port.in;

import java.io.IOException;

public interface BranchDeletetionUseCase {
    void deleteBranch(Long repositoryId, String branchName) throws IOException;
}

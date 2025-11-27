package io.jgitkins.server.application.port.in;

import java.io.IOException;

import io.jgitkins.server.application.dto.BranchCreateCommand;

public interface CreateBranchUseCase {
    void createBranch(BranchCreateCommand command) throws IOException;
}

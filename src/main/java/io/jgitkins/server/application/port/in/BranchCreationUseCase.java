package io.jgitkins.server.application.port.in;

import java.io.IOException;

import io.jgitkins.server.application.dto.command.BranchCreateCommand;

public interface BranchCreationUseCase {
    void createBranch(BranchCreateCommand command) throws IOException;
}

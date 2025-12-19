package io.jgitkins.server.application.port.out;

import java.io.IOException;

import io.jgitkins.server.application.dto.command.BranchCreateCommand;

public interface CreateBranchPort {
    void createBranch(BranchCreateCommand command) throws IOException;
}

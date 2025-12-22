package io.jgitkins.server.application.port.out;

import java.io.IOException;

import io.jgitkins.server.application.dto.command.BranchCreationContext;

public interface BranchGitCreatePort {
    void createBranch(BranchCreationContext context) throws IOException;
}

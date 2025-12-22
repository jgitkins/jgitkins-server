package io.jgitkins.server.application.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.common.exception.UnprocessableException;
import io.jgitkins.server.application.dto.command.BranchCreateCommand;
import io.jgitkins.server.application.port.out.BranchPersistenceLoadPort;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.domain.aggregate.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class BranchCreationValidator {

    private final BranchPersistenceLoadPort branchPersistenceLoadPort;

    public void ensureBranchDoesNotExist(Long repositoryId, String branchName) throws IOException {
        branchPersistenceLoadPort.getBranch(repositoryId, branchName)
                .ifPresent(existing -> {
                    throw new ConflictException(ErrorCode.BRANCH_ALREADY_EXISTS);
                });
    }

    public void ensureRepositoryInitialized(Repository repository) {
        if (!repository.isInitialized()) {
            throw new UnprocessableException(ErrorCode.REPOSITORY_DOES_NOT_INITIALIZED, "Repository is not yet initialized. Initialize default branch before creating new branches.");
        }
    }

    public String resolveAndValidateSourceBranch(BranchCreateCommand command, Repository repository) throws IOException {
        String sourceBranch = (command.getSourceBranch() == null || command.getSourceBranch().isBlank())
                ? repository.getDefaultBranch().getValue()
                : command.getSourceBranch();

        branchPersistenceLoadPort.getBranch(repository.getId().getValue(), sourceBranch)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SOURCE_BRANCH_NOT_FOUND,
                        "Source branch not found or not initialized: " + sourceBranch));
        return sourceBranch;
    }

    public void ensureNotDefaultBranch(Repository repository, Branch branch) {
        if (repository.getDefaultBranch().getValue().equals(branch.getName()) || branch.isDefaultBranch()) {
            throw new ConflictException(ErrorCode.BRANCH_DELETE_FAILED,
                    "Default branch cannot be deleted: " + branch.getName());
        }
    }
}

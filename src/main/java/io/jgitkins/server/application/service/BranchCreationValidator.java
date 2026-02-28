package io.jgitkins.server.application.service;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.dto.command.BranchCreateCommand;
import io.jgitkins.server.application.port.out.BranchPort;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.error.DomainErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BranchCreationValidator {

    private final BranchPort branchPort;

    public void validateBranchDoesNotExist(Long repositoryId, String branchName) {
        branchPort.getBranch(repositoryId, branchName)
                .ifPresent(existing -> {
                    throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.BRANCH_ALREADY_EXISTS);
                });
    }

    public void validateRepositoryInitialized(Repository repository) {
        if (!repository.isInitialized()) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.REPOSITORY_DOES_NOT_INITIALIZED, "Repository is not yet initialized. Initialize default branch before creating new branches.");
        }
    }

    public String resolveAndValidateSourceBranch(BranchCreateCommand command, Repository repository) {
        String sourceBranch = (command.getSourceBranch() == null || command.getSourceBranch().isBlank())
                ? repository.getDefaultBranch().getValue()
                : command.getSourceBranch();

        branchPort.getBranch(repository.getId().getValue(), sourceBranch)
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.SOURCE_BRANCH_NOT_FOUND,
                        "Source branch not found or not initialized: " + sourceBranch));
        return sourceBranch;
    }

    public void validateNotDefaultBranch(Repository repository, Branch branch) {
        if (repository.getDefaultBranch().getValue().equals(branch.getName()) || branch.isDefaultBranch()) {
            throw new JgitkinsException(DomainErrorCode.RULE_VIOLATION,
                    "Default branch cannot be deleted: " + branch.getName());
        }
    }
}

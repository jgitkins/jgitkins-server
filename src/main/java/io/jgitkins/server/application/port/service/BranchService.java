package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.command.BranchCreateCommand;
import io.jgitkins.server.application.dto.command.BranchCreationContext;
import io.jgitkins.server.application.dto.result.BranchSearchResult;
import io.jgitkins.server.application.port.in.BranchCreationUseCase;
import io.jgitkins.server.application.port.in.BranchDeletetionUseCase;
import io.jgitkins.server.application.port.in.BranchLoadUseCase;
import io.jgitkins.server.application.mapper.BranchApplicationMapper;
import io.jgitkins.server.application.service.BranchCreationValidator;
import io.jgitkins.server.application.port.out.*;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService implements BranchLoadUseCase, BranchCreationUseCase, BranchDeletetionUseCase {

    private final BranchGitCreatePort branchGitCreatePort;
    private final BranchGitDeletePort branchGitDeletePort;

    private final BranchPersistenceCommandPort branchPersistenceCommandPort;
    private final BranchPersistenceLoadPort branchPersistenceLoadPort;
    private final RepositoryPersistencePort repositoryPersistencePort;
    private final OrganizePersistencePort organizePersistencePort;
    private final BranchApplicationMapper branchApplicationMapper;
    private final BranchCreationValidator branchCreationValidator;


    @Override
    public List<BranchSearchResult> getBranches(Long repositoryId) {
        return branchPersistenceLoadPort.getBranches(repositoryId)
                .stream()
                .map(branchApplicationMapper::toSearchResult)
                .toList();
    }

    @Override
    public BranchSearchResult getBranch(Long repositoryId, String branchName) throws IOException {
        return branchPersistenceLoadPort.getBranch(repositoryId, branchName)
                .map(branchApplicationMapper::toSearchResult)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND,
                        "Branch not found: " + branchName));
    }

    @Override
    public void createBranch(BranchCreateCommand command) throws IOException {
        // 1) load repository & organize context
        Repository repository = repositoryPersistencePort.findById(RepositoryId.of(command.getRepositoryId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found: " + command.getRepositoryId()));
        Organize organize = organizePersistencePort.findById(repository.getOrganizeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + repository.getOrganizeId().getValue()));

        // 2) validate semantic rules
        branchCreationValidator.ensureRepositoryInitialized(repository);
        branchCreationValidator.ensureBranchDoesNotExist(command.getRepositoryId(), command.getBranchName());
        String resolvedSourceBranch = branchCreationValidator.resolveAndValidateSourceBranch(command, repository);

        // 3) create new branch aggregate
        Branch newBranch = Branch.create(command.getRepositoryId(), command.getBranchName());
        BranchCreationContext context = BranchCreationContext.of(command, organize, repository, resolvedSourceBranch);

        // 4) create branch from file system
        branchGitCreatePort.createBranch(context);

        // 5) create branch from persistence
        branchPersistenceCommandPort.create(newBranch);
    }

    @Override
    public void deleteBranch(Long repositoryId, String branchName) throws IOException {
        Repository repository = repositoryPersistencePort.findById(RepositoryId.of(repositoryId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found: " + repositoryId));
        Organize organize = organizePersistencePort.findById(repository.getOrganizeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + repository.getOrganizeId().getValue()));

        Branch branch = branchPersistenceLoadPort.getBranch(repositoryId, branchName)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND,
                        "Branch not found: " + branchName));

        branchCreationValidator.ensureNotDefaultBranch(repository, branch);

        branchGitDeletePort.deleteBranch(organize.getName().getValue(),
                                         repository.getName().getValue(),
                                         branchName);
        branchPersistenceCommandPort.delete(repositoryId, branchName);
    }

}

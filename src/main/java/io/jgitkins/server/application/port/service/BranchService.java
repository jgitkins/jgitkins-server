package io.jgitkins.server.application.port.service;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.dto.command.BranchCreateCommand;
import io.jgitkins.server.application.dto.command.BranchCreationContext;
import io.jgitkins.server.application.dto.result.BranchSearchResult;
import io.jgitkins.server.application.port.in.BranchCreateUseCase;
import io.jgitkins.server.application.port.in.BranchDeleteUseCase;
import io.jgitkins.server.application.port.in.BranchLoadUseCase;
import io.jgitkins.server.application.port.out.BranchGitPort;
import io.jgitkins.server.application.port.out.BranchPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.mapper.BranchApplicationMapper;
import io.jgitkins.server.application.service.BranchCreationValidator;
import io.jgitkins.server.application.service.RepositoryNamespaceResolver;
import io.jgitkins.server.application.service.RepositoryUploadPermissionGuard;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService implements BranchLoadUseCase, BranchCreateUseCase, BranchDeleteUseCase {

    private final RepositoryNamespaceResolver repositoryNamespaceResolver;
    private final BranchApplicationMapper branchApplicationMapper;
    private final BranchCreationValidator branchCreationValidator;
    private final RepositoryUploadPermissionGuard repositoryWritePermissionGuard;

    private final BranchGitPort branchGitPort;
    private final BranchPort branchPort;
    private final RepositoryPort repositoryPort;

    @Override
    public List<BranchSearchResult> getBranches(Long repositoryId) {
        return branchPort.getBranches(repositoryId)
                .stream()
                .map(branchApplicationMapper::toSearchResult)
                .toList();
    }

    @Override
    public BranchSearchResult getBranch(Long repositoryId, String branchName) {
        return branchPort.getBranch(repositoryId, branchName)
                .map(branchApplicationMapper::toSearchResult)
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.BRANCH_NOT_FOUND,
                        "Branch not found: " + branchName));
    }

    @Override
    public void createBranch(BranchCreateCommand command) {
        Repository repository = requireWritableRepository(command.getRepositoryId());
        String namespace = resolveNamespace(repository);

        branchCreationValidator.validateRepositoryInitialized(repository);
        branchCreationValidator.validateBranchDoesNotExist(command.getRepositoryId(), command.getBranchName());
        String resolvedSourceBranch = branchCreationValidator.resolveAndValidateSourceBranch(command, repository);

        Branch newBranch = Branch.create(command.getRepositoryId(), command.getBranchName());
        BranchCreationContext context = BranchCreationContext.of(command, namespace, repository, resolvedSourceBranch);

        branchGitPort.createBranch(context);

        branchPort.create(newBranch);
    }

    @Override
    public void deleteBranch(Long repositoryId, String branchName) {
        Repository repository = requireWritableRepository(repositoryId);
        String namespace = resolveNamespace(repository);
        Branch branch = loadBranch(repositoryId, branchName);
        branchCreationValidator.validateNotDefaultBranch(repository, branch);

        branchGitPort.deleteBranch(namespace, repository.getName().getValue(), branchName);
        branchPort.delete(repositoryId, branchName);
    }

    private Repository loadRepository(Long repositoryId) {
        return repositoryPort.findById(RepositoryId.of(repositoryId))
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found: " + repositoryId));
    }

    private Branch loadBranch(Long repositoryId, String branchName) {
        return branchPort.getBranch(repositoryId, branchName)
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.BRANCH_NOT_FOUND,
                        "Branch not found: " + branchName));
    }

    private Repository requireWritableRepository(Long repositoryId) {
        Repository repository = loadRepository(repositoryId);
        repositoryWritePermissionGuard.assertCanWrite(repository);
        return repository;
    }

    private String resolveNamespace(Repository repository) {
        return repositoryNamespaceResolver.resolve(repository);
    }
}

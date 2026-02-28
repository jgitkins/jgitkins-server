package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.common.ErrorCode;
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
import io.jgitkins.server.application.service.RepositoryWritePermissionGuard;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService implements BranchLoadUseCase, BranchCreateUseCase, BranchDeleteUseCase {

    private final RepositoryNamespaceResolver repositoryNamespaceResolver;
    private final BranchApplicationMapper branchApplicationMapper;
    private final BranchCreationValidator branchCreationValidator;
    private final RepositoryWritePermissionGuard repositoryWritePermissionGuard;

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
    public BranchSearchResult getBranch(Long repositoryId, String branchName) throws IOException {
        return branchPort.getBranch(repositoryId, branchName)
                .map(branchApplicationMapper::toSearchResult)
                .orElseThrow(() -> new JgitkinsException(ErrorCode.BRANCH_NOT_FOUND,
                        "Branch not found: " + branchName));
    }

    @Override
    public void createBranch(BranchCreateCommand command) throws IOException {
        Repository repository = loadRepository(command.getRepositoryId());
        repositoryWritePermissionGuard.assertCanWrite(repository);
        String namespace = repositoryNamespaceResolver.resolve(repository);

        branchCreationValidator.validateRepositoryInitialized(repository);
        branchCreationValidator.validateBranchDoesNotExist(command.getRepositoryId(), command.getBranchName());
        String resolvedSourceBranch = branchCreationValidator.resolveAndValidateSourceBranch(command, repository);

        Branch newBranch = Branch.create(command.getRepositoryId(), command.getBranchName());
        BranchCreationContext context = BranchCreationContext.of(command, namespace, repository, resolvedSourceBranch);

        branchGitPort.createBranch(context);

        branchPort.create(newBranch);
    }

    @Override
    public void deleteBranch(Long repositoryId, String branchName) throws IOException {
        Repository repository = loadRepository(repositoryId);
        repositoryWritePermissionGuard.assertCanWrite(repository);
        String namespace = repositoryNamespaceResolver.resolve(repository);
        Branch branch = loadBranch(repositoryId, branchName);
        branchCreationValidator.validateNotDefaultBranch(repository, branch);

        branchGitPort.deleteBranch(namespace,
                repository.getName().getValue(),
                branchName);
        branchPort.delete(repositoryId, branchName);
    }

    private Repository loadRepository(Long repositoryId) {
        return repositoryPort.findById(RepositoryId.of(repositoryId))
                .orElseThrow(() -> new JgitkinsException(ErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found: " + repositoryId));
    }

    private Branch loadBranch(Long repositoryId, String branchName) throws IOException {
        return branchPort.getBranch(repositoryId, branchName)
                .orElseThrow(() -> new JgitkinsException(ErrorCode.BRANCH_NOT_FOUND,
                        "Branch not found: " + branchName));
    }
}

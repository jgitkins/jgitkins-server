package io.jgitkins.server.infrastructure.adapter.git;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.GitConstants;
import io.jgitkins.server.application.common.exception.InternalServerErrorException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.command.BranchCreationContext;
import io.jgitkins.server.application.port.out.BranchGitPort;
import io.jgitkins.server.infrastructure.support.RepositoryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BranchJGitAdapter implements BranchGitPort {

    private final RepositoryResolver repositoryResolver;


    @Override
    public void createBranch(BranchCreationContext context) {
        try (Repository repo = repositoryResolver.openBareRepository(context.getTaskCd(), context.getRepositoryName())) {
            ObjectId sourceId = resolveRef(repo, context.getSourceBranch());
            if (sourceId == null) {
                throw new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Source branch not found: " + context.getSourceBranch());
            }

            String newRef = GitConstants.REFS_HEADS_PREFIX + context.getBranchName();
            if (repo.exactRef(newRef) != null) {
                throw new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Source branch not found: " + context.getSourceBranch());
            }

            RefUpdate update = repo.updateRef(newRef);
            update.setNewObjectId(sourceId);
            update.setExpectedOldObjectId(ObjectId.zeroId());
            update.setRefLogMessage(String.format("branch: Created %s from %s", context.getBranchName(), context.getSourceBranch()), false);

            RefUpdate.Result result = update.update();
            if (result != RefUpdate.Result.NEW) {
                throw new InternalServerErrorException(ErrorCode.BRANCH_CREATE_FAILED, String.format("Failed to create branch [%s]", context.getSourceBranch()));
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.BRANCH_CREATE_FAILED, String.format("Failed to create branch [%s]", context.getSourceBranch()), e);
        }
    }

    @Override
    public void deleteBranch(String taskCd, String repoName, String branchName) {
        String refName = GitConstants.REFS_HEADS_PREFIX + branchName;
        try (Repository repo = repositoryResolver.openBareRepository(taskCd, repoName)) {
            if (repo.exactRef(refName) == null) {
                throw new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Branch not found: " + branchName);
            }

            RefUpdate update = repo.updateRef(refName);
            update.setForceUpdate(true);
            RefUpdate.Result result = update.delete();
            if (result != RefUpdate.Result.FORCED && result != RefUpdate.Result.NEW) {
                throw new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND, String.format("Failed to delete branch %s for repo %s/%s", branchName, taskCd, repoName));
            }

        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.BRANCH_ALREADY_EXISTS, String.format("Failed to delete branch %s for repo %s/%s", branchName, taskCd, repoName), e);
        }
    }


    private ObjectId resolveRef(Repository repo, String branch) throws IOException {
        ObjectId oid = repo.resolve(branch);
        if (oid == null) {
            oid = repo.resolve(GitConstants.REFS_HEADS_PREFIX + branch);
        }
        return oid;
    }


}

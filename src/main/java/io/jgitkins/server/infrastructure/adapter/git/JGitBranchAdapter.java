package io.jgitkins.server.infrastructure.adapter.git;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.GitConstants;
import io.jgitkins.server.application.common.exception.InternalServerErrorException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.BranchCreateCommand;
import io.jgitkins.server.application.dto.BranchInfo;
import io.jgitkins.server.application.port.out.CreateBranchPort;
import io.jgitkins.server.application.port.out.DeleteBranchPort;
import io.jgitkins.server.application.port.out.LinkHeadPort;
import io.jgitkins.server.application.port.out.LoadBranchPort;
import io.jgitkins.server.infrastructure.support.RepositoryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JGitBranchAdapter implements LinkHeadPort, LoadBranchPort, CreateBranchPort, DeleteBranchPort {

    private final RepositoryResolver repositoryResolver;

    private ObjectId resolveRef(Repository repo, String branch) throws IOException {
        ObjectId oid = repo.resolve(branch);
        if (oid == null) {
            oid = repo.resolve(GitConstants.REFS_HEADS_PREFIX + branch);
        }
        return oid;
    }

    private String stripBranchName(String refName) {
        if (refName.startsWith(GitConstants.REFS_HEADS_PREFIX)) {
            return refName.substring(GitConstants.REFS_HEADS_PREFIX.length());
        }
        return refName;
    }

    private String resolveCommitId(Ref ref) {
        ObjectId objectId = ref.getObjectId();
        return objectId != null ? objectId.name() : null;
    }










    @Override
    public void linkHead(String taskCd, String repoName, String branch) {

        try (Repository repo = repositoryResolver.openBareRepository(taskCd, repoName)) {
            String mainRef = GitConstants.REFS_HEADS_PREFIX + branch;
            repo.updateRef(Constants.HEAD, true)
                .link(mainRef);
        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.HEAD_POINT_FAILED, String.format("Failed to link HEAD for repo %s/%s", taskCd, repoName), e);
//            throw new HeadLinkException(String.format("Failed to link HEAD for repo %s/%s", taskCd, repoName), e);
        }
    }

    @Override
    public List<BranchInfo> getBranches(String taskCd, String repoName) {
        try (Repository repo = repositoryResolver.openBareRepository(taskCd, repoName)) {
            return repo.getRefDatabase()
                    .getRefsByPrefix(GitConstants.REFS_HEADS_PREFIX)
                    .stream()
                    .map(ref -> BranchInfo.builder()
                            .name(stripBranchName(ref.getName()))
                            .fullRef(ref.getName())
                            .commitId(resolveCommitId(ref))
                            .build())
                    .collect(Collectors.toList());
        } catch (IOException e) {
//            throw new BranchLoadException(String.format("Failed to load branches for repo %s/%s", taskCd, repoName), e);
            throw new InternalServerErrorException(ErrorCode.BRANCH_LOAD_FAILED, String.format("Failed to load branches for repo %s/%s", taskCd, repoName), e);
        }
    }

    @Override
    public Optional<BranchInfo> getBranch(String taskCd, String repoName, String branchName) throws IOException {
        String orgRef = branchName.startsWith(GitConstants.REFS_HEADS_PREFIX)
                ? branchName
                : GitConstants.REFS_HEADS_PREFIX + branchName;

        try (Repository repo = repositoryResolver.openBareRepository(taskCd, repoName)) {

            Ref ref = repo.findRef(orgRef);
            if (ref == null){
                return Optional.empty();
            }

            // 3. BranchInfo 로 매핑
            BranchInfo branchInfo = BranchInfo.builder()
                    .name(stripBranchName(ref.getName())) // 예: refs/heads/main -> main
                    .fullRef(ref.getName())               // 예: refs/heads/main
                    .commitId(resolveCommitId(ref))
                    .build();

            return Optional.of(branchInfo);

        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.BRANCH_LOAD_FAILED, String.format("Failed to load branch [%s] for repo %s/%s", branchName, taskCd, repoName), e);

        }

    }

    @Override
    public void createBranch(BranchCreateCommand command) {
        try (Repository repo = repositoryResolver.openBareRepository(command.getTaskCd(), command.getRepoName())) {
            ObjectId sourceId = resolveRef(repo, command.getSourceBranch());
            if (sourceId == null) {
                throw new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Source branch not found: " + command.getSourceBranch());
//                throw new BranchCreateException("Source branch not found: " + command.getSourceBranch());
            }

            String newRef = GitConstants.REFS_HEADS_PREFIX + command.getBranchName();
            if (repo.exactRef(newRef) != null) {
//                throw new BranchCreateException("Branch already exists: " + command.getBranchName());
                throw new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Source branch not found: " + command.getSourceBranch());
            }

            RefUpdate update = repo.updateRef(newRef);
            update.setNewObjectId(sourceId);
            update.setExpectedOldObjectId(ObjectId.zeroId());
            update.setRefLogMessage(String.format("branch: Created %s from %s", command.getBranchName(), command.getSourceBranch()), false);

            RefUpdate.Result result = update.update();
            if (result != RefUpdate.Result.NEW) {
                throw new InternalServerErrorException(ErrorCode.BRANCH_CREATE_FAILED, String.format("Failed to create branch [%s]", command.getSourceBranch()));
//                throw new BranchCreateException("Failed to create branch: " + result);
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.BRANCH_CREATE_FAILED, String.format("Failed to create branch [%s]", command.getSourceBranch()), e);
//            throw new BranchCreateException(String.format("Failed to create branch %s for repo %s/%s",
//                    command.getBranchName(), command.getTaskCd(), command.getRepoName()), e);
        }
    }

    @Override
    public void deleteBranch(String taskCd, String repoName, String branchName) {
        String refName = GitConstants.REFS_HEADS_PREFIX + branchName;
        try (Repository repo = repositoryResolver.openBareRepository(taskCd, repoName)) {
            if (repo.exactRef(refName) == null) {
                throw new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Branch not found: " + branchName);
//                throw new BranchDeleteException("Branch not found: " + branchName);
            }

            RefUpdate update = repo.updateRef(refName);
            update.setForceUpdate(true);
            RefUpdate.Result result = update.delete();
            if (result != RefUpdate.Result.FORCED && result != RefUpdate.Result.NEW) {
//                throw new BranchDeleteException("Failed to delete branch: " + result);
                throw new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND, String.format("Failed to delete branch %s for repo %s/%s", branchName, taskCd, repoName));
//                throw new InternalServerErrorException(ErrorCode.BRANCH_ALREADY_EXISTS, String.format("Failed to delete branch %s for repo %s/%s", branchName, taskCd, repoName));
            }

        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.BRANCH_ALREADY_EXISTS, String.format("Failed to delete branch %s for repo %s/%s", branchName, taskCd, repoName), e);
//            throw new BranchDeleteException(String.format("Failed to delete branch %s for repo %s/%s",
//                    branchName, taskCd, repoName), e);
        }
    }

}

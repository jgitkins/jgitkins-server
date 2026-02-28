package io.jgitkins.server.infrastructure.adapter.git;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.GitConstants;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.dto.CommitHistory;
import io.jgitkins.server.application.port.out.CommitGitPort;
import io.jgitkins.server.infrastructure.support.RepositoryResolver;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RepositoryJGitCommitAdapter implements CommitGitPort {

    private final RepositoryResolver repositoryResolver;

    @Override
    public void commit(String taskCd,
                       String repoName,
                       String branch,
                       String message,
                       String authorName,
                       String authorEmail,
                       List<CommitFile> files) {

        File gitDir = repositoryResolver.resolveGitDir(taskCd, repoName);
        try (Repository repo = repositoryResolver.openBareRepository(gitDir);
             ObjectInserter inserter = repo.newObjectInserter();
             RevWalk revWalk = new RevWalk(repo)) {

            RevCommit parentCommit = resolveParentCommit(repo, revWalk, branch);
            DirCache inCoreIndex = buildUpdatedIndex(repo, parentCommit, files, inserter);
            ObjectId treeId = inCoreIndex.writeTree(inserter);

            PersonIdent pi = new PersonIdent(authorName, authorEmail);
            ObjectId commitId = createCommit(inserter, treeId, parentCommit, pi, message);

            updateBranchRef(repo, branch, commitId, parentCommit);
        } catch (IOException e) {
            throw new JgitkinsException(ErrorCode.COMMIT_CREATE_FAILED, String.format("Failed to commit to repo %s/%s", taskCd, repoName), e);
        }
    }

    @Override
    public CommitHistory getCommitHistory(String taskCd, String repoName, String commitHash) throws IOException {
        File gitDir = repositoryResolver.resolveGitDir(taskCd, repoName);
        try (Repository repo = repositoryResolver.openBareRepository(gitDir)) {
            ObjectId commitId = repo.resolve(commitHash);
            if (commitId == null) {
                throw new JgitkinsException(ErrorCode.COMMIT_LOAD_FAILED,
                        String.format("Failed to load commit detail for repo %s/%s", taskCd, repoName));
            }
            try (RevWalk revWalk = new RevWalk(repo)) {
                RevCommit commit = revWalk.parseCommit(commitId);
                return CommitHistory.builder()
                        .id(commit.getId().name())
                        .authorName(commit.getAuthorIdent().getName())
                        .authorEmail(commit.getAuthorIdent().getEmailAddress())
                        .committerName(commit.getCommitterIdent().getName())
                        .committerEmail(commit.getCommitterIdent().getEmailAddress())
                        .shortMessage(commit.getShortMessage())
                        .fullMessage(commit.getFullMessage())
                        .commitTime(commit.getAuthorIdent().getWhen().toInstant()
                                .atZone(commit.getAuthorIdent().getTimeZone().toZoneId()).toLocalDateTime())
                        .parentId(commit.getParentCount() > 0 ? commit.getParent(0).name() : null)
                        .build();
            }
        } catch (IOException e) {
            throw new JgitkinsException(ErrorCode.COMMIT_LOAD_FAILED,
                    String.format("Failed to load commit detail for repo %s/%s", taskCd, repoName), e);
        }
    }

    @Override
    public List<CommitHistory> getCommitHistories(String taskCd, String repoName, String branch) throws IOException {
        List<CommitHistory> histories = new ArrayList<>();
        File gitDir = repositoryResolver.resolveGitDir(taskCd, repoName);
        try (Repository repo = repositoryResolver.openBareRepository(gitDir)) {
            ObjectId branchId = resolveRef(repo, branch);
            if (branchId == null) {
                throw new JgitkinsException(ErrorCode.BRANCH_NOT_FOUND, "Branch Not Found");
            }
            try (RevWalk revWalk = new RevWalk(repo)) {
                revWalk.markStart(revWalk.parseCommit(branchId));
                for (RevCommit commit : revWalk) {
                    histories.add(CommitHistory.builder()
                            .id(commit.getId().name())
                            .authorName(commit.getAuthorIdent().getName())
                            .authorEmail(commit.getAuthorIdent().getEmailAddress())
                            .committerName(commit.getCommitterIdent().getName())
                            .committerEmail(commit.getCommitterIdent().getEmailAddress())
                            .shortMessage(commit.getShortMessage())
                            .fullMessage(commit.getFullMessage())
                            .commitTime(commit.getAuthorIdent().getWhen().toInstant()
                                    .atZone(commit.getAuthorIdent().getTimeZone().toZoneId()).toLocalDateTime())
                            .parentId(commit.getParentCount() > 0 ? commit.getParent(0).name() : null)
                            .build());
                }
            }
        } catch (IOException e) {
            throw new JgitkinsException(ErrorCode.COMMIT_LOAD_FAILED,
                    String.format("Failed to retrieve branch commit histories for repo %s/%s", taskCd, repoName), e);
        }
        return histories;
    }

    private RevCommit resolveParentCommit(Repository repo, RevWalk revWalk, String branch) throws IOException {
        ObjectId headId = resolveRef(repo, branch);
        if (headId == null) {
            return null;
        }
        return revWalk.parseCommit(headId);
    }

    private DirCache buildUpdatedIndex(Repository repo,
                                       RevCommit parentCommit,
                                       List<CommitFile> files,
                                       ObjectInserter inserter) throws IOException {
        DirCache inCoreIndex = DirCache.newInCore();
        DirCacheBuilder builder = inCoreIndex.builder();
        Set<String> updatedPaths = collectUpdatedPaths(files);

        if (parentCommit != null) {
            addExistingEntries(repo, parentCommit, builder, updatedPaths);
        }
        addNewFileEntries(files, builder, inserter);
        builder.finish();
        return inCoreIndex;
    }

    private Set<String> collectUpdatedPaths(List<CommitFile> files) {
        Set<String> updatedPaths = new HashSet<>();
        for (CommitFile file : files) {
            updatedPaths.add(file.getPath());
        }
        return updatedPaths;
    }

    private void addExistingEntries(Repository repo,
                                    RevCommit parentCommit,
                                    DirCacheBuilder builder,
                                    Set<String> updatedPaths) throws IOException {
        try (TreeWalk tw = new TreeWalk(repo)) {
            tw.addTree(parentCommit.getTree());
            tw.setRecursive(true);
            while (tw.next()) {
                String path = tw.getPathString();
                if (updatedPaths.contains(path)) {
                    continue;
                }
                DirCacheEntry existing = new DirCacheEntry(path);
                existing.setFileMode(tw.getFileMode(0));
                existing.setObjectId(tw.getObjectId(0));
                builder.add(existing);
            }
        }
    }

    private void addNewFileEntries(List<CommitFile> files,
                                   DirCacheBuilder builder,
                                   ObjectInserter inserter) throws IOException {
        for (CommitFile file : files) {
            ObjectId blobId = inserter.insert(Constants.OBJ_BLOB, file.getContent());
            DirCacheEntry entry = new DirCacheEntry(file.getPath());
            entry.setFileMode(FileMode.REGULAR_FILE);
            entry.setObjectId(blobId);
            builder.add(entry);
        }
    }

    private ObjectId createCommit(ObjectInserter inserter,
                                  ObjectId treeId,
                                  RevCommit parentCommit,
                                  PersonIdent author,
                                  String message) throws IOException {
        org.eclipse.jgit.lib.CommitBuilder cb = new org.eclipse.jgit.lib.CommitBuilder();
        cb.setTreeId(treeId);
        cb.setAuthor(author);
        cb.setCommitter(author);
        cb.setMessage(message);
        if (parentCommit != null) {
            cb.setParentIds(parentCommit);
        }
        ObjectId commitId = inserter.insert(cb);
        inserter.flush();
        return commitId;
    }

    private void updateBranchRef(Repository repo,
                                 String branch,
                                 ObjectId commitId,
                                 RevCommit parentCommit) throws IOException {
        String originRef = GitConstants.REFS_HEADS_PREFIX + branch;
        RefUpdate ru = repo.updateRef(originRef);
        ru.setNewObjectId(commitId);
        ru.setExpectedOldObjectId(parentCommit != null ? parentCommit.getId() : ObjectId.zeroId());
        RefUpdate.Result updateResult = ru.update();
        if (updateResult == RefUpdate.Result.REJECTED || updateResult == RefUpdate.Result.LOCK_FAILURE) {
            throw new JgitkinsException(ErrorCode.HEAD_POINT_FAILED, String.format("Failed to update originRef %s, %s", originRef, updateResult));
        }
    }

    private static ObjectId resolveRef(Repository repo, String ref) throws IOException {
        ObjectId oid = repo.resolve(ref);
        if (oid == null) {
            oid = repo.resolve(GitConstants.REFS_HEADS_PREFIX + ref);
        }
        return oid;
    }
}

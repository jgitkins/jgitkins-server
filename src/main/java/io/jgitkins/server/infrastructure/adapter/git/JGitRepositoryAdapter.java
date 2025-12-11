package io.jgitkins.server.infrastructure.adapter.git;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.GitConstants;
import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.InternalServerErrorException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.dto.CommitHistory;
import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.port.out.*;
import io.jgitkins.server.infrastructure.support.RepositoryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JGitRepositoryAdapter implements
        CreateRepositoryPort,
        DeleteRepositoryPort,
        LoadTreePort,
        LoadAllFilesPort,
        LoadCommitDetailPort,
        LoadBranchCommitHistoriesPort,
        RepositoryCommitPort,
        CheckFileExistencePort {

    private final RepositoryResolver repositoryResolver;

    private List<FileEntry> listTopLevelEntries(Repository repository, RevTree commitTree) throws IOException {
        List<FileEntry> files = new ArrayList<>();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(commitTree);
            treeWalk.setRecursive(false);

            while (treeWalk.next()) {
                files.add(buildEntry(repository, treeWalk.getObjectId(0), treeWalk.getPathString(),
                        treeWalk.getFileMode(0)));
            }
        }
        return files;
    }

    private void createRepositoryDir(File gitDir) {
        if (gitDir.exists()) {
            throw new ConflictException(ErrorCode.REPOSITORY_ALREADY_EXISTS,
                    "Repository already exists: " + gitDir.getAbsolutePath());
            // throw new CreationFailedException("Repository already exists: " +
            // gitDir.getAbsolutePath());
        }
        if (!gitDir.mkdirs() && !gitDir.exists()) {
            throw new InternalServerErrorException(ErrorCode.REPOSITORY_CREATE_FAILED,
                    "Failed to create directories: " + gitDir.getAbsolutePath());
            // throw new CreationFailedException("Failed to create directories: " +
            // gitDir.getAbsolutePath());
        }
    }

    private void initializeBareRepository(Repository repo) throws IOException {
        repo.create(true);
        repo.getConfig().setBoolean("http", null, "receivepack", true);
        repo.getConfig().save();
    }

    private void deleteRecursively(File target) throws IOException {
        if (target == null || !target.exists()) {
            return;
        }
        File[] contents = target.listFiles();
        if (contents != null) {
            for (File child : contents) {
                deleteRecursively(child);
            }
        }
        if (!target.delete()) {
            throw new IOException("Failed to delete " + target.getAbsolutePath());
        }
    }

    private RevTree resolveCommitTree(Repository repository, String branch) throws IOException {
        ObjectId branchId = Optional.ofNullable(resolveRef(repository, branch))
                .orElseThrow(
                        () -> new ResourceNotFoundException(ErrorCode.COMMIT_TREE_NOT_FOUND, "Unknown ref: " + branch));

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(branchId);
            return commit.getTree();
        }
    }

    private List<FileEntry> collectTreeEntries(Repository repository, RevTree commitTree, String directory)
            throws IOException {
        if (StringUtils.isBlank(directory)) {
            return listTopLevelEntries(repository, commitTree);
        }
        return listDirectoryEntries(repository, commitTree, directory);
    }

    private List<FileEntry> listDirectoryEntries(Repository repository, RevTree commitTree, String directory)
            throws IOException {

        try (TreeWalk at = TreeWalk.forPath(repository, directory, commitTree)) {
            if (at == null) {
                throw new IOException(
                        String.format("Repository [%s] Does Not Exist Path: [%s]", repository, directory));
            }
            return listTree(repository, at.getObjectId(0), ensureTrailingSlash(directory), false);
        }
    }

    private String ensureTrailingSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
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

        CommitBuilder cb = new CommitBuilder();
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
            throw new InternalServerErrorException(ErrorCode.HEAD_POINT_FAILED,
                    String.format("Failed to update originRef %s, %s", originRef, updateResult));
        }
    }

    // helpers copied from original service
    private List<FileEntry> listTree(Repository repo, ObjectId treeId, String prefix, boolean recursive)
            throws IOException {
        List<FileEntry> out = new ArrayList<>();
        try (TreeWalk treeWalk = new TreeWalk(repo)) {
            treeWalk.addTree(treeId);
            treeWalk.setRecursive(recursive);

            while (treeWalk.next()) {
                if (!recursive && treeWalk.getPathString().contains("/")) {
                    continue;
                }
                out.add(buildEntry(repo, treeWalk.getObjectId(0), prefix + treeWalk.getPathString(),
                        treeWalk.getFileMode(0)));
            }
        }

        out.sort(Comparator
                .comparing((FileEntry e) -> e.getType().equals("blob"))
                .thenComparing(FileEntry::getName, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    private FileEntry buildEntry(Repository repo, ObjectId id, String fullPath, FileMode mode) throws IOException {
        String type = mode == FileMode.TREE ? "tree" : "blob";
        String name = fullPath.substring(fullPath.lastIndexOf('/') + 1);
        Long size = null;
        if ("blob".equals(type)) {
            try (ObjectReader reader = repo.newObjectReader()) {
                size = reader.open(id, Constants.OBJ_BLOB).getSize();
            }
        }

        return new FileEntry(
                id.name(),
                name,
                fullPath,
                type,
                String.format("%06o", mode.getBits()),
                size);
    }

    private static ObjectId resolveRef(Repository repo, String ref) throws IOException {
        ObjectId oid = repo.resolve(ref);
        if (oid == null) {
            oid = repo.resolve(GitConstants.REFS_HEADS_PREFIX + ref);
        }
        return oid;
    }

    private RevTree resolveAllFilesTree(Repository repository, String ref) throws IOException {

        ObjectId branchId = Optional.ofNullable(resolveRef(repository, ref))
                .orElseThrow(
                        () -> new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Branch Not Found: " + ref));

        try (RevWalk revWalk = new RevWalk(repository)) {
            return revWalk.parseCommit(branchId).getTree();
        }
    }

    private List<FileEntry> collectAllFileEntries(Repository repository, RevTree tree) throws IOException {
        List<FileEntry> files = new ArrayList<>();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);

            while (treeWalk.next()) {
                FileMode mode = treeWalk.getFileMode(0);
                if (!isSupportedFileMode(mode)) {
                    continue;
                }
                files.add(buildEntry(repository, treeWalk.getObjectId(0), treeWalk.getPathString(), mode));
            }
        }

        files.sort(Comparator.comparing(FileEntry::getPath, String.CASE_INSENSITIVE_ORDER));
        return files;
    }

    private boolean isSupportedFileMode(FileMode mode) {
        return FileMode.REGULAR_FILE.equals(mode)
                || FileMode.EXECUTABLE_FILE.equals(mode)
                || FileMode.SYMLINK.equals(mode);
    }

    @Override
    public void create(String taskCd, String repoName) {
        File gitDir = repositoryResolver.resolveGitDir(taskCd, repoName);

        try {
            createRepositoryDir(gitDir);
            try (Repository repo = repositoryResolver.openBareRepository(gitDir)) {
                initializeBareRepository(repo);
                log.info("Bare repository initialized. repo=[{}], task=[{}]", gitDir.getName(), taskCd);
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.REPOSITORY_CREATE_FAILED,
                    "Repository creation failed: " + gitDir.getAbsolutePath(), e);
            // throw new CreationFailedException("Repository creation failed: " +
            // gitDir.getAbsolutePath(), e);
        }
    }

    @Override
    public void delete(String taskCd, String repoName) {
        File gitDir = repositoryResolver.resolveGitDir(taskCd, repoName);
        if (!gitDir.exists()) {
            log.info("Skip repository delete. repo not found path={}, task={}", gitDir.getAbsolutePath(), taskCd);
            return;
        }
        try {
            deleteRecursively(gitDir);
            File parent = gitDir.getParentFile();
            if (parent != null && parent.isDirectory()) {
                File[] siblings = parent.listFiles();
                if (siblings != null && siblings.length == 0) {
                    parent.delete();
                }
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.REPOSITORY_DELETE_FAILED, "Failed to delete repository directory: " + gitDir.getAbsolutePath(), e);
        }
    }

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
            throw new InternalServerErrorException(ErrorCode.COMMIT_CREATE_FAILED,
                    String.format("Failed to commit to repo %s/%s", taskCd, repoName), e);
            // throw new RepositoryCommitException(String.format("Failed to commit to repo
            // %s/%s", taskCd, repoName), e);
        }
    }

    @Override
    public List<FileEntry> getTree(String taskCd, String repoName, String branch, String directory) {
        File bareRepositoryPath = repositoryResolver.resolveGitDir(taskCd, repoName);

        try (Repository repository = repositoryResolver.openBareRepository(bareRepositoryPath)) {
            RevTree commitTree = resolveCommitTree(repository, branch);
            return collectTreeEntries(repository, commitTree, directory);
        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.COMMIT_TREE_LOAD_FAILED,
                    String.format("Failed to load tree for repo %s/%s", taskCd, repoName), e);
            // throw new TreeLoadException(String.format("Failed to load tree for repo
            // %s/%s", taskCd, repoName), e);
        }
    }

    @Override
    public List<FileEntry> getAllFiles(String taskCd, String repoName, String branch) {
        File bareRepositoryPath = repositoryResolver.resolveGitDir(taskCd, repoName);
        String ref = StringUtils.isBlank(branch) ? Constants.HEAD : branch;

        try (Repository repository = repositoryResolver.openBareRepository(bareRepositoryPath)) {
            RevTree tree = resolveAllFilesTree(repository, ref);
            return collectAllFileEntries(repository, tree);
        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.FILE_LOAD_FAILED,
                    String.format("Failed to load files for repo %s/%s", taskCd, repoName), e);
        }
    }

    @Override
    public CommitHistory getCommitDetail(String taskCd, String repoName, String commitHash) {
        File bareRepositoryPath = repositoryResolver.resolveGitDir(taskCd, repoName);

        try (Repository repository = repositoryResolver.openBareRepository(bareRepositoryPath)) {

            ObjectId commitId = repository.resolve(commitHash);
            if (commitId == null) {
                // throw new CommitDetailLoadException("Unknown commit hash: " + commitHash);
                throw new ResourceNotFoundException(ErrorCode.COMMIT_LOAD_FAILED,
                        String.format("Failed to load commit detail for repo %s/%s", taskCd, repoName));
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(commitId);
                revWalk.dispose();

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
            throw new InternalServerErrorException(ErrorCode.COMMIT_LOAD_FAILED,
                    String.format("Failed to load commit detail for repo %s/%s", taskCd, repoName), e);
        }
    }

    @Override
    public List<CommitHistory> getCommitHistories(String taskCd, String repoName, String branch) {
        List<CommitHistory> commitHistories = new ArrayList<>();
        File bareRepositoryPath = repositoryResolver.resolveGitDir(taskCd, repoName);

        try (Repository repository = repositoryResolver.openBareRepository(bareRepositoryPath)) {

            ObjectId branchId = resolveRef(repository, branch);
            if (branchId == null) {
                throw new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Branch Not Found");
                // throw new CommitHistoriesLoadException("Unknown branch: " + branch);
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit headCommit = revWalk.parseCommit(branchId);
                revWalk.markStart(headCommit);

                for (RevCommit commit : revWalk) {
                    commitHistories.add(CommitHistory.builder()
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
            throw new InternalServerErrorException(ErrorCode.COMMIT_LOAD_FAILED,
                    String.format("Failed to retrieve branch commit histories for repo %s/%s", taskCd, repoName), e);
            // throw new CommitHistoriesLoadException(String.format("Failed to retrieve
            // branch commit histories for repo %s/%s", taskCd, repoName), e);
        }
        return commitHistories;
    }

    @Override
    public boolean exists(String taskCd, String repoName, String commitHash, String filePath) {
        File bareRepositoryPath = repositoryResolver.resolveGitDir(taskCd, repoName);

        try (Repository repository = repositoryResolver.openBareRepository(bareRepositoryPath)) {
            ObjectId commitId = repository.resolve(commitHash);
            if (commitId == null) {
                return false;
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(commitId);
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = TreeWalk.forPath(repository, filePath, tree)) {
                    return treeWalk != null;
                }
            }
        } catch (IOException e) {
            log.error("Failed to check file existence: {} in repo: {}/{}", filePath, taskCd, repoName, e);
            return false;
        }
    }

}

package io.jgitkins.server.infrastructure.adapter.git;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.GitConstants;
import io.jgitkins.server.application.common.exception.InternalServerErrorException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.port.out.FileGitPort;
import io.jgitkins.server.infrastructure.support.RepositoryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepositoryJGitFileAdapter implements FileGitPort {

    private final RepositoryResolver repositoryResolver;

    @Override
    public List<FileEntry> getTree(String namespace, String repoName, String branch, String directory) {

        File gitDir = repositoryResolver.resolveGitDir(namespace, repoName);

        try (Repository repo = repositoryResolver.openBareRepository(gitDir)) {
            RevTree commitTree = resolveCommitTree(repo, branch);
            return collectTreeEntries(repo, commitTree, directory);
        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.COMMIT_TREE_LOAD_FAILED,
                    String.format("Failed to load tree for repo %s/%s", namespace, repoName), e);
        }
    }

    @Override
    public List<FileEntry> getAllFiles(String taskCd, String repoName, String branch) {
        File gitDir = repositoryResolver.resolveGitDir(taskCd, repoName);
        String ref = StringUtils.isBlank(branch) ? Constants.HEAD : branch;
        try (Repository repo = repositoryResolver.openBareRepository(gitDir)) {
            RevTree tree = resolveAllFilesTree(repo, ref);
            return collectAllFileEntries(repo, tree);
        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.FILE_LOAD_FAILED,
                    String.format("Failed to load files for repo %s/%s", taskCd, repoName), e);
        }
    }

    @Override
    public boolean exists(String taskCd, String repoName, String commitHash, String filePath) {
        File gitDir = repositoryResolver.resolveGitDir(taskCd, repoName);
        try (Repository repo = repositoryResolver.openBareRepository(gitDir)) {
            ObjectId commitId = repo.resolve(commitHash);
            if (commitId == null) {
                return false;
            }
            try (RevWalk revWalk = new RevWalk(repo)) {
                RevCommit commit = revWalk.parseCommit(commitId);
                RevTree tree = commit.getTree();
                try (TreeWalk treeWalk = TreeWalk.forPath(repo, filePath, tree)) {
                    return treeWalk != null;
                }
            }
        } catch (IOException e) {
            log.error("Failed to check file existence: {} in repo: {}/{}", filePath, taskCd, repoName, e);
            return false;
        }
    }

    private RevTree resolveCommitTree(Repository repository, String branch) throws IOException {
        ObjectId branchId = Optional.ofNullable(resolveRef(repository, branch))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COMMIT_TREE_NOT_FOUND, "Unknown ref: " + branch));
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

    private List<FileEntry> listTopLevelEntries(Repository repository, RevTree commitTree) throws IOException {
        List<FileEntry> files = new ArrayList<>();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(commitTree);
            treeWalk.setRecursive(false);
            while (treeWalk.next()) {
                files.add(buildEntry(repository,
                        treeWalk.getObjectId(0),
                        treeWalk.getPathString(),
                        treeWalk.getFileMode(0)));
            }
        }
        return files;
    }

    private List<FileEntry> listDirectoryEntries(Repository repository, RevTree commitTree, String directory)
            throws IOException {
        try (TreeWalk at = TreeWalk.forPath(repository, directory, commitTree)) {
            if (at == null) {
                throw new IOException(String.format("Repository [%s] does not contain path: [%s]", repository, directory));
            }
            return listTree(repository, at.getObjectId(0), ensureTrailingSlash(directory), false);
        }
    }

    private String ensureTrailingSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

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
                out.add(buildEntry(repo,
                        treeWalk.getObjectId(0),
                        prefix + treeWalk.getPathString(),
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
        return new FileEntry(id.name(),
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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Branch Not Found: " + ref));
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
}

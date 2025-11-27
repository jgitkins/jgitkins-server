package io.jgitkins.server.infrastructure.support;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.springframework.stereotype.Component;

@Component
public class RepositoryResolver {

    private final String rootPath;

    public RepositoryResolver() {
        this.rootPath = String.format("%s/%s", System.getProperty("user.home"), "tmptmp/bare");
    }

    public Repository openBareRepository(String taskCd, String repoName) throws IOException {
        return openBareRepository(resolveGitDir(taskCd, repoName));
    }

    public Repository openBareRepository(File gitDir) throws IOException {
        return new RepositoryBuilder().setBare().setGitDir(gitDir).build();
    }

    public File resolveGitDir(String taskCd, String repoName) {
        File repoRoot = new File(rootPath + "/" + taskCd);
        String finalRepoName = repoName.endsWith(".git") ? repoName : repoName + ".git";
        return new File(repoRoot, finalRepoName);
    }
}

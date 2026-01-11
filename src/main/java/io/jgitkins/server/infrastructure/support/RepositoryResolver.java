package io.jgitkins.server.infrastructure.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RepositoryResolver {

    private String rootPath;

    public RepositoryResolver(@Value("${jgitkins.server.runtime.volume:${user.home}}") String runtimeVolume) {
        this.rootPath = runtimeVolume;
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

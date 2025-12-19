package io.jgitkins.server.infrastructure.adapter.git;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.InternalServerErrorException;
import io.jgitkins.server.application.port.out.RepositoryFileAdminPort;
import io.jgitkins.server.infrastructure.support.RepositoryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepositoryJGitAdminAdapter implements RepositoryFileAdminPort {

    private final RepositoryResolver repositoryResolver;

    @Override
    public void create(String taskCd, String repoName) {
        File gitDir = repositoryResolver.resolveGitDir(taskCd, repoName);
        try {
            RepositoryLocalHelper.createRepositoryDir(gitDir);
            try (Repository repo = repositoryResolver.openBareRepository(gitDir)) {
                RepositoryLocalHelper.initializeBareRepository(repo);
                log.info("Bare repository initialized. repo=[{}], task=[{}]", gitDir.getName(), taskCd);
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.REPOSITORY_CREATE_FAILED,
                                                    "Repository creation failed: " + gitDir.getAbsolutePath(), e);
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
            RepositoryLocalHelper.deleteRecursively(gitDir);
            File parent = gitDir.getParentFile();
            if (parent != null && parent.isDirectory()) {
                File[] siblings = parent.listFiles();
                if (siblings != null && siblings.length == 0) {
                    parent.delete();
                }
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(ErrorCode.REPOSITORY_DELETE_FAILED,
                                                    "Failed to delete repository directory: " + gitDir.getAbsolutePath(), e);
        }
    }
}

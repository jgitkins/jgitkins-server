package io.jgitkins.server.application.port.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.dto.FileUploadInfo;
import io.jgitkins.server.application.factory.CommitFileFactory;
import io.jgitkins.server.application.port.in.FileLoadUseCase;
import io.jgitkins.server.application.port.in.FileTreeLoadUseCase;
import io.jgitkins.server.application.port.in.FileUploadUseCase;
import io.jgitkins.server.application.port.out.CommitGitPort;
import io.jgitkins.server.application.port.out.FileGitPort;
import io.jgitkins.server.application.service.RepositoryUploadPermissionValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepositoryFileService implements FileUploadUseCase,
                                              FileTreeLoadUseCase,
                                              FileLoadUseCase {

    private static final String DEFAULT_AUTHOR_NAME = "jgitkins";
    private static final String DEFAULT_AUTHOR_EMAIL = "no-reply@jgitkins.local";

    private final CommitFileFactory commitFileFactory;

    private final CommitGitPort commitGitPort;
    private final FileGitPort fileGitPort;
    private final RepositoryUploadPermissionValidator repositoryUploadPermissionValidator;

    @Override
    @Transactional
    public void uploadFileToRepository(String taskCd,
                                       String repoName,
                                       String branch,
                                       MultipartFile file,
                                       FileUploadInfo request) {
        repositoryUploadPermissionValidator.validateCanUpload(taskCd, repoName);

        List<CommitFile> files = commitFileFactory.prepareUploadFile(file, request);

        commitGitPort.commit(taskCd,
                             repoName,
                             branch,
                             request.getCommitMessage(),
                             resolveAuthorName(request),
                             resolveAuthorEmail(request),
                             files);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileEntry> getTree(String namespace,
                                   String repoName,
                                   String branch,
                                   String directory) {
        return fileGitPort.getTree(namespace, repoName, branch, directory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileEntry> getAllFiles(String taskCd,
                                       String repoName,
                                       String reference) {
        return fileGitPort.getAllFiles(taskCd, repoName, reference);
    }

    private String resolveAuthorName(FileUploadInfo request) {
        if (request == null || !StringUtils.hasText(request.getAuthorName())) {
            return DEFAULT_AUTHOR_NAME;
        }
        return request.getAuthorName();
    }

    private String resolveAuthorEmail(FileUploadInfo request) {
        if (request == null || !StringUtils.hasText(request.getAuthorEmail())) {
            return DEFAULT_AUTHOR_EMAIL;
        }
        return request.getAuthorEmail();
    }
}

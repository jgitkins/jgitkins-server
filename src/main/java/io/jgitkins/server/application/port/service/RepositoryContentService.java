package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.dto.FileUploadInfo;
import io.jgitkins.server.application.port.in.FileLoadUseCase;
import io.jgitkins.server.application.port.in.FileUploadUseCase;
import io.jgitkins.server.application.port.in.FileTreeLoadUseCase;
import io.jgitkins.server.application.port.out.FileTreeGitLoadPort;
import io.jgitkins.server.application.port.out.RepositoryCommitPort;
import io.jgitkins.server.application.port.out.RepositoryContentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepositoryContentService implements FileUploadUseCase,
        FileTreeLoadUseCase,
        FileLoadUseCase {

    private final RepositoryCommitPort repositoryCommitPort;
    private final RepositoryContentPort repositoryContentPort;
    private final FileTreeGitLoadPort fileTreeGitLoadPort;

    @Override
    @Transactional
    public void uploadFileToRepository(String taskCd,
                                       String repoName,
                                       String branch,
                                       MultipartFile file,
                                       FileUploadInfo request) throws IOException {

        List<CommitFile> files = repositoryContentPort.prepareUploadFiles(file, request);

        repositoryCommitPort.commit(taskCd,
                                    repoName,
                                    branch,
                                    request.getCommitMessage(),
                                    request.getAuthorName(),
                                    request.getAuthorEmail(),
                                    files);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileEntry> getTree(String taskCd,
                                   String repoName,
                                   String branch,
                                   String directory) throws IOException {
        return fileTreeGitLoadPort.getTree(taskCd, repoName, branch, directory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileEntry> getAllFiles(String taskCd,
                                       String repoName,
                                       String reference) {
        return fileTreeGitLoadPort.getAllFiles(taskCd, repoName, reference);
    }
}

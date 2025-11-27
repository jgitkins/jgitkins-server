package io.jgitkins.server.application.port.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.jgitkins.server.application.dto.CommitHistory;
import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.dto.CreateRepositoryCommand;
import io.jgitkins.server.application.dto.FileUploadInfo;
import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.port.in.CreateRepositoryUseCase;
import io.jgitkins.server.application.port.in.LoadAllFilesUseCase;
import io.jgitkins.server.application.port.in.LoadBranchCommitHistoriesUseCase;
import io.jgitkins.server.application.port.in.LoadCommitDetailUseCase;
import io.jgitkins.server.application.port.in.LoadTreeUseCase;
import io.jgitkins.server.application.port.in.UploadFileUseCase;
import io.jgitkins.server.application.port.out.CreateRepositoryPort;
import io.jgitkins.server.application.port.out.RepositoryContentPort;
import io.jgitkins.server.application.port.out.RepositoryCommitPort;
import io.jgitkins.server.application.port.out.LinkHeadPort;
import io.jgitkins.server.application.port.out.LoadAllFilesPort;
import io.jgitkins.server.application.port.out.LoadBranchCommitHistoriesPort;
import io.jgitkins.server.application.port.out.LoadCommitDetailPort;
import io.jgitkins.server.application.port.out.LoadTreePort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryService implements
        CreateRepositoryUseCase,
        UploadFileUseCase,
        LoadTreeUseCase,
        LoadAllFilesUseCase,
        LoadCommitDetailUseCase,
        LoadBranchCommitHistoriesUseCase {

    private final CreateRepositoryPort createRepositoryPort;
    private final LoadTreePort getTreePort;
    private final LoadAllFilesPort getAllFilesPort;
    private final LoadCommitDetailPort getCommitDetailPort;
    private final LoadBranchCommitHistoriesPort getBranchCommitHistoriesPort;

    private final LinkHeadPort linkHeadPort;
    private final RepositoryCommitPort repositoryCommitPort;
    private final RepositoryContentPort repositoryContentPort;

    @Override
    public void createBareRepository(CreateRepositoryCommand command) {
        // 1. create empty repo
        createRepositoryPort.createBareRepository(command.getTaskCd(), command.getRepoName());

        if (!command.isReadme()) {
            linkHeadPort.linkHead(command.getTaskCd(), command.getRepoName(), command.getMainBranch());
            return;
        }

        // 2. create a file
        List<CommitFile> files = repositoryContentPort.prepareInitialFiles(command.getRepoName());

        // 3. commit 
        repositoryCommitPort.commit(
                command.getTaskCd(),
                command.getRepoName(),
                command.getMainBranch(),
                command.getMessage(),
                command.getAuthorName(),
                command.getAuthorEmail(),
                files
        );

//        // 4. change head
//        linkHeadPort.linkHead(command.getTaskCd(), command.getRepoName(), command.getMainBranch());

    }

    @Override
    public void uploadFileToRepository(String taskCd, String repoName, String branch, MultipartFile file, FileUploadInfo request) throws IOException {
        List<CommitFile> files = repositoryContentPort.prepareUploadFiles(file, request);
        repositoryCommitPort.commit(
                taskCd,
                repoName,
                branch,
                request.getCommitMessage(),
                request.getAuthorName(),
                request.getAuthorEmail(),
                files
        );
//        linkHeadPort.linkHead(taskCd, repoName, branch);
    }

    @Override
    public List<FileEntry> getTree(String taskCd, String repoName, String branch, String directory) throws IOException {
        return getTreePort.getTree(taskCd, repoName, branch, directory);
    }

    @Override
    public List<FileEntry> getAllFiles(String taskCd, String repoName, String reference) {
        return getAllFilesPort.getAllFiles(taskCd, repoName, reference);
    }

    @Override
    public CommitHistory getCommitDetail(String taskCd, String repoName, String commitHash) throws IOException {
        return getCommitDetailPort.getCommitDetail(taskCd, repoName, commitHash);
    }

    @Override
    public List<CommitHistory> getBranchCommitHistories(String taskCd, String repoName, String branch) throws IOException {
        return getBranchCommitHistoriesPort.getCommitHistories(taskCd, repoName, branch);
    }
}

package io.jgitkins.server.application.port.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.dto.FileUploadInfo;
import io.jgitkins.server.application.factory.CommitFileFactory;
import io.jgitkins.server.application.port.out.CommitGitPort;
import io.jgitkins.server.application.port.out.FileGitPort;
import io.jgitkins.server.application.service.RepositoryUploadPermissionValidator;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class RepositoryFileServiceTest {

    @Mock
    private CommitFileFactory commitFileFactory;

    @Mock
    private CommitGitPort commitGitPort;

    @Mock
    private FileGitPort fileGitPort;

    @Mock
    private RepositoryUploadPermissionValidator repositoryUploadPermissionValidator;

    @InjectMocks
    private RepositoryFileService service;

    @Test
    void uploadFileToRepository_commitsPreparedFiles() throws IOException {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        FileUploadInfo request = new FileUploadInfo();
        request.setCommitMessage("msg");
        request.setAuthorName("author");
        request.setAuthorEmail("a@b.com");
        List<CommitFile> files = List.of(CommitFile.builder().path("README.md").build());
        when(commitFileFactory.prepareUploadFile(file, request)).thenReturn(files);

        service.uploadFileToRepository("task", "repo", "main", file, request);

        verify(repositoryUploadPermissionValidator).validateCanUpload("task", "repo");
        verify(commitGitPort).commit(eq("task"), eq("repo"), eq("main"),
                eq("msg"), eq("author"), eq("a@b.com"), eq(files));
    }

    @Test
    void uploadFileToRepository_usesDefaultAuthorWhenMissing() throws IOException {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        FileUploadInfo request = new FileUploadInfo();
        request.setCommitMessage("msg");
        List<CommitFile> files = List.of(CommitFile.builder().path("README.md").build());
        when(commitFileFactory.prepareUploadFile(file, request)).thenReturn(files);

        service.uploadFileToRepository("task", "repo", "main", file, request);

        verify(repositoryUploadPermissionValidator).validateCanUpload("task", "repo");
        verify(commitGitPort).commit(eq("task"), eq("repo"), eq("main"),
                eq("msg"), eq("jgitkins"), eq("no-reply@jgitkins.local"), eq(files));
    }

    @Test
    void getTree_delegatesToFileGitPort() throws IOException {
        when(fileGitPort.getTree("ns", "repo", "main", "src")).thenReturn(Collections.emptyList());

        service.getTree("ns", "repo", "main", "src");

        verify(fileGitPort).getTree("ns", "repo", "main", "src");
    }

    @Test
    void getAllFiles_delegatesToFileGitPort() {
        when(fileGitPort.getAllFiles("ns", "repo", "main")).thenReturn(Collections.emptyList());

        service.getAllFiles("ns", "repo", "main");

        verify(fileGitPort).getAllFiles("ns", "repo", "main");
    }
}

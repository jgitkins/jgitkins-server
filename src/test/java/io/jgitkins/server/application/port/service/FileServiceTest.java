package io.jgitkins.server.application.port.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.dto.FileUploadInfo;
import io.jgitkins.server.application.factory.CommitFileFactory;
import io.jgitkins.server.application.port.out.CommitGitPort;
import io.jgitkins.server.application.port.out.FileGitPort;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private CommitFileFactory commitFileFactory;

    @Mock
    private CommitGitPort commitGitPort;

    @Mock
    private FileGitPort fileGitPort;

    @InjectMocks
    private FileService service;

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

        verify(commitGitPort).commit(eq("task"), eq("repo"), eq("main"),
                eq("msg"), eq("author"), eq("a@b.com"), eq(files));
    }
}

package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.FileUploadInfo;
import io.jgitkins.server.application.dto.CommitFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface RepositoryContentPort {

    List<CommitFile> prepareInitialFiles(String repoName);

    List<CommitFile> prepareUploadFiles(MultipartFile file, FileUploadInfo request) throws IOException;
}


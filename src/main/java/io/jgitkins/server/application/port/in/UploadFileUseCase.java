package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.FileUploadInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadFileUseCase {
    void uploadFileToRepository(String taskCd, String repoName, String branch, MultipartFile file, FileUploadInfo request) throws IOException;
}


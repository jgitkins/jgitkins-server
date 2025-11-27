package io.jgitkins.server.infrastructure.adapter.git;

import io.jgitkins.server.application.dto.FileUploadInfo;
import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.port.out.RepositoryContentPort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class RepositoryContentAdapter implements RepositoryContentPort {

    @Override
    public List<CommitFile> prepareInitialFiles(String repoName) {
        String displayName = stripGitSuffix(repoName);
        String readmeContent = "# " + displayName + "\n";

        return List.of(CommitFile.builder()
                .path("README.md")
                .content(readmeContent.getBytes(StandardCharsets.UTF_8))
                .build());
    }

    @Override
    public List<CommitFile> prepareUploadFiles(MultipartFile file, FileUploadInfo request) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String targetPath = StringUtils.hasText(request.getFilePath()) ? request.getFilePath() : file.getOriginalFilename();
        if (!StringUtils.hasText(targetPath)) {
            throw new IllegalArgumentException("File path is missing");
        }

        return List.of(CommitFile.builder()
                .path(targetPath)
                .content(file.getBytes())
                .build());
    }

    private String stripGitSuffix(String name) {
        return name != null && name.endsWith(".git") ? name.substring(0, name.length() - 4) : name;
    }
}


package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.FileEntry;

import java.util.List;

public interface FileLoadUseCase {
    List<FileEntry> getAllFiles(String taskCd, String repoName, String reference);
}

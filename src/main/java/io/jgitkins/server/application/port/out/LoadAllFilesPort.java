package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.FileEntry;

import java.util.List;

public interface LoadAllFilesPort {
    List<FileEntry> getAllFiles(String taskCd, String repoName, String reference);
}

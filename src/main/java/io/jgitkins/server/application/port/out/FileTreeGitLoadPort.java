package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.FileEntry;

import java.io.IOException;
import java.util.List;

public interface FileTreeGitLoadPort {
    List<FileEntry> getTree(String taskCd, String repoName, String branch, String directory) throws IOException;
    List<FileEntry> getAllFiles(String taskCd, String repoName, String reference);
}
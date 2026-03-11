package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.FileEntry;

import java.io.IOException;
import java.util.List;

public interface FileGitPort {
    List<FileEntry> listTree(String taskCd, String repoName, String branch, String directory);
    List<FileEntry> listAllFiles(String taskCd, String repoName, String reference);

    boolean exists(String taskCd, String repoName, String commitHash, String filePath);
}
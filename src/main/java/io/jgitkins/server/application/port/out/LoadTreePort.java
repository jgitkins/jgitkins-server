package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.FileEntry;

import java.io.IOException;
import java.util.List;

public interface LoadTreePort {
    List<FileEntry> getTree(String taskCd, String repoName, String branch, String directory) throws IOException;
}

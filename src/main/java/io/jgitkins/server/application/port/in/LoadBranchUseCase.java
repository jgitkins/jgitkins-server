package io.jgitkins.server.application.port.in;

import java.io.IOException;
import java.util.List;

import io.jgitkins.server.application.dto.BranchInfo;

public interface LoadBranchUseCase {
    List<BranchInfo> getBranches(String taskCd, String repoName) throws IOException;
}

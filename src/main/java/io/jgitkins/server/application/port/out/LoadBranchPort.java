package io.jgitkins.server.application.port.out;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import io.jgitkins.server.application.dto.BranchInfo;

public interface LoadBranchPort {
    List<BranchInfo> getBranches(String taskCd, String repoName) throws IOException;
    Optional<BranchInfo> getBranch(String taskCd, String repoName, String branchName) throws IOException;
}

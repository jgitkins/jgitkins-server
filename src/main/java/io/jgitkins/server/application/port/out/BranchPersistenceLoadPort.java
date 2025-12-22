package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.Branch;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface BranchPersistenceLoadPort {
//    List<BranchInfo> getBranches(String taskCd, String repoName) throws IOException;
    Optional<Branch> getBranch(Long repositoryId, String branch) throws IOException;
    List<Branch> getBranches(Long repositoryId);
}


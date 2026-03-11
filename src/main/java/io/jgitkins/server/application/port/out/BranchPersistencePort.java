package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.Branch;

import java.util.List;
import java.util.Optional;

public interface BranchPersistencePort {
    void create(Branch branch);
    void delete(Long repositoryId, String branchName);

    Optional<Branch> getBranch(Long repositoryId, String branch);
    List<Branch> getBranches(Long repositoryId);
}

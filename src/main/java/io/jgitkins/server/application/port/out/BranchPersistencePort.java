package io.jgitkins.server.application.port.out;

public interface BranchPersistencePort {
    void saveBranch(String taskCd, String repoName, String branchName);

    void deleteBranch(String taskCd, String repoName, String branchName);
}

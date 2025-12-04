package io.jgitkins.server.application.port.out;

public interface BranchPersistencePort {
    void create(Long repositoryId, String name);
    void delete(Long id);
//    void create(String taskCd, String repoName, String branchName);
//    void deleteBranch(String taskCd, String repoName, String branchName);
}

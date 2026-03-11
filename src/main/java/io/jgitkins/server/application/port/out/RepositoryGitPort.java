package io.jgitkins.server.application.port.out;

public interface RepositoryGitPort {
    void initialize(String taskCd, String repoName);
    void deleteRepository(String taskCd, String repoName);

    void updateHeadReference(String taskCd, String repoName, String branch);
}

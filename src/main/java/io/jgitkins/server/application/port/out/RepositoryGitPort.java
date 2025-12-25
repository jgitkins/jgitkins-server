package io.jgitkins.server.application.port.out;

public interface RepositoryGitPort {
    void create(String taskCd, String repoName);
    void delete(String taskCd, String repoName);

    void updateHeadReference(String taskCd, String repoName, String branch);
}

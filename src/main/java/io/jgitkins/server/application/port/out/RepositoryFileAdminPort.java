package io.jgitkins.server.application.port.out;

public interface RepositoryFileAdminPort {
    void create(String taskCd, String repoName);
    void delete(String taskCd, String repoName);
}

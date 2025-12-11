package io.jgitkins.server.application.port.out;

public interface DeleteRepositoryPort {
    void delete(String taskCd, String repoName);
}

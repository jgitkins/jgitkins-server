package io.jgitkins.server.application.port.out;

public interface CreateRepositoryPort {
//    void createBareRepository(CreateRepositoryCommand command);
    void createBareRepository(String taskCd, String repoName);
}

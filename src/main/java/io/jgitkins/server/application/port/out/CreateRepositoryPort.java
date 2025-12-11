package io.jgitkins.server.application.port.out;

public interface CreateRepositoryPort {
//    void createBareRepository(CreateRepositoryCommand command);
    void create(String taskCd, String repoName);
}

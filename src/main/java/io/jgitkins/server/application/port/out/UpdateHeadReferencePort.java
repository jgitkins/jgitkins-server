package io.jgitkins.server.application.port.out;

public interface UpdateHeadReferencePort {

    void updateHeadReference(String taskCd, String repoName, String branch);
}

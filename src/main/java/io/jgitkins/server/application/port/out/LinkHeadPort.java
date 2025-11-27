package io.jgitkins.server.application.port.out;

public interface LinkHeadPort {

    void linkHead(String taskCd, String repoName, String branch);
    
}


package io.jgitkins.server.presentation.dto;

import lombok.Getter;

@Getter
public class CreateRepositoryRequest {
    private String taskCd;
    private String repoName;
    private String mainBranch;
    private boolean readme;
    private String username;
    private String email;
    private String message;
}

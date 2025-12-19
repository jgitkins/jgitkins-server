package io.jgitkins.server.presentation.dto;

import lombok.Getter;

@Getter
public class RepositoryCreateRequest {
    private String repoName;
    private String mainBranch;
    private boolean readme;
    private String username;
    private String email;
    private String message;

    private Long organizeId;
    private String path;
    private String visibility;
    private Long ownerId;
    private String description;
    private String credentialId;
}

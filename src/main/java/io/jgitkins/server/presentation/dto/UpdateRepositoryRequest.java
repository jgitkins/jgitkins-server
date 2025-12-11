package io.jgitkins.server.presentation.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UpdateRepositoryRequest {
    private String name;
    private String path;
    private String defaultBranch;
    private String visibility;
    private String repositoryType;
    private Long ownerId;
    private String description;
    private String credentialId;
    private LocalDateTime lastSyncedAt;
}

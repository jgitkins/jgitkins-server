package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RepositoryResult {
    private final Long id;
    private final Long organizeId;
    private final String name;
    private final String path;
    private final String defaultBranch;
    private final String visibility;
    private final String description;
    private final String repositoryType;
    private final Long ownerId;
    private final String credentialId;
    private final String clonePath;
    private final String cloneUrl;
    private final boolean requiresInitialContent;
    private final LocalDateTime lastSyncedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}

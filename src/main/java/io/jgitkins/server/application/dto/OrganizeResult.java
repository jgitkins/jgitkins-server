package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrganizeResult {
    private final Long id;
    private final String name;
    private final String path;
    private final String description;
    private final Long ownerId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}

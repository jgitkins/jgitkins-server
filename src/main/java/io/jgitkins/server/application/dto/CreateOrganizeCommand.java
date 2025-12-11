package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateOrganizeCommand {
    private final String name;
    private final String path;
    private final Long ownerId;
    private final String description;
}

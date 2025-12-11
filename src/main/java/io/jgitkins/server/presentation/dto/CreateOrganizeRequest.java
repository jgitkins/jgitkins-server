package io.jgitkins.server.presentation.dto;

import lombok.Getter;

@Getter
public class CreateOrganizeRequest {
    private String name;
    private String path;
    private Long ownerId;
    private String description;
}

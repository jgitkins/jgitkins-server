package io.jgitkins.server.presentation.dto;

import lombok.Getter;

@Getter
public class OrganizeCreationRequest {
    private String name;
    private Long ownerId;
    private String description;
}

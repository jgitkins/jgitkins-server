package io.jgitkins.server.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCredentialIssueRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private String expiration;
}

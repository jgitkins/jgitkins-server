package io.jgitkins.server.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UserUsernameUpdateRequest {
    @NotBlank(message = "username is required")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Username allows only letters, numbers, dot, hyphen, or underscore.")
    private String username;
}

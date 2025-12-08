package io.jgitkins.server.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunnerRegistrationRequest {

    @NotBlank
    private String description;

//    private String ipAddress;
}

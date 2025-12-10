package io.jgitkins.server.presentation.dto;

import io.jgitkins.server.domain.model.vo.RunnerScopeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunnerRegistrationRequest {

    @NotBlank
    private String description;

    @NotNull
    private RunnerScopeType scopeType;

    private Long targetId;

//    private String ipAddress;
}

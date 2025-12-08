package io.jgitkins.server.presentation.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RunnerRegistrationResponse {
    private final Long runnerId;
    private final String token;
    private final String status;
    private final LocalDateTime registeredAt;
}

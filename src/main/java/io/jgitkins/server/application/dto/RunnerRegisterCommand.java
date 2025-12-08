package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RunnerRegisterCommand {
    private final String description;
//    private final String ipAddress;
}

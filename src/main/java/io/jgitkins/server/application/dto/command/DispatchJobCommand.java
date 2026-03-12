package io.jgitkins.server.application.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DispatchJobCommand {
    private final String runnerToken;
}

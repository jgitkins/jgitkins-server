package io.jgitkins.server.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobResultReportCommand {
    private final String runnerToken;
    private final Long jobId;
    private final JobResultStatus status;
}

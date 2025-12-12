package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.JobResultReportCommand;

public interface JobResultReportUseCase {
    void reportJobResult(JobResultReportCommand command);
}

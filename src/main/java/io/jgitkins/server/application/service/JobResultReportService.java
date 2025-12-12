package io.jgitkins.server.application.service;

import io.jgitkins.server.application.dto.JobResultReportCommand;
import io.jgitkins.server.application.dto.JobResultStatus;
import io.jgitkins.server.application.port.in.JobResultReportUseCase;
import io.jgitkins.server.application.port.out.JobQueuePort;
import io.jgitkins.server.application.port.out.RunnerQueryPort;
import io.jgitkins.server.domain.aggregate.Job;
import io.jgitkins.server.domain.aggregate.Runner;
import io.jgitkins.server.domain.model.JobHistory;
import io.jgitkins.server.domain.model.vo.RunnerId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobResultReportService implements JobResultReportUseCase {

    private final JobQueuePort jobQueuePort;
    private final RunnerQueryPort runnerQueryPort;

    @Override
    @Transactional
    public void reportJobResult(JobResultReportCommand command) {
        Runner runner = runnerQueryPort.findByToken(command.getRunnerToken())
                                       .orElseThrow(() -> new IllegalArgumentException("Runner not found for token"));

        Job job = jobQueuePort.loadJob(command.getJobId())
                              .orElseThrow(() -> new IllegalArgumentException("Job not found for id " + command.getJobId()));

        JobHistory previousHistory = job.getLatestHistory();
        RunnerId runnerId = RunnerId.of(String.valueOf(runner.getId()));

        if (command.getStatus() == JobResultStatus.SUCCESS) {
            job.completeSuccess(runnerId);
        } else {
            job.completeFailure(runnerId);
        }

        Optional<Long> persistedId = jobQueuePort.persistHistory(job, previousHistory);
        if (persistedId.isEmpty()) {
            throw new IllegalStateException("Failed to persist job result history for job " + command.getJobId());
        }
    }
}

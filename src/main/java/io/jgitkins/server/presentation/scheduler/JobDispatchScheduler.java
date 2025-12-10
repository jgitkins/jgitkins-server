package io.jgitkins.server.presentation.scheduler;

import io.jgitkins.server.application.port.in.JobDispatchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobDispatchScheduler {

    private final JobDispatchUseCase jobDispatchUseCase;

    @Scheduled(fixedDelayString = "${job.scheduler.dispatch-interval-ms:5000}")
    public void dispatchPendingJob() {
        jobDispatchUseCase.dispatchPendingJob();
    }
}

package io.jgitkins.server.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.common.CloneUrlBuilder;
import io.jgitkins.server.application.dto.RunnerAssignmentCandidate;
import io.jgitkins.server.application.port.out.JobPort;
import io.jgitkins.server.application.port.out.RunnerPort;
import io.jgitkins.server.domain.aggregate.Runner;
import io.jgitkins.server.domain.model.vo.RunnerScopeType;
import io.jgitkins.server.domain.model.vo.RunnerStatus;
import io.jgitkins.server.presentation.dto.RunnerJobFetchRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobDispatchServiceTest {

    @Mock
    private JobPort jobPort;

    @Mock
    private RunnerPort runnerPort;

    @Mock
    private CloneUrlBuilder cloneUrlBuilder;

    @InjectMocks
    private JobDispatchService service;

    @Test
    void fetchJob_returnsEmpty_whenRunnerTokenMissing() {
        RunnerJobFetchRequest request = RunnerJobFetchRequest.builder().runnerToken(" ").build();

        Optional<?> result = service.fetchJob(request);

        assertThat(result).isEmpty();
        verifyNoInteractions(runnerPort, jobPort, cloneUrlBuilder);
    }

    @Test
    void fetchJob_returnsEmpty_whenRunnerNotFound() {
        RunnerJobFetchRequest request = RunnerJobFetchRequest.builder().runnerToken("token").build();
        when(runnerPort.findByToken("token")).thenReturn(Optional.empty());

        Optional<?> result = service.fetchJob(request);

        assertThat(result).isEmpty();
        verify(runnerPort).findByToken("token");
        verifyNoInteractions(jobPort, cloneUrlBuilder);
    }

    @Test
    void fetchJob_returnsEmpty_whenNoPendingJobForCandidate() {
        RunnerJobFetchRequest request = RunnerJobFetchRequest.builder().runnerToken("token").build();
        Runner runner = Runner.restore(
                7L,
                "token",
                "runner",
                RunnerStatus.OFFLINE,
                RunnerScopeType.GLOBAL,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(runnerPort.findByToken("token")).thenReturn(Optional.of(runner));
        when(jobPort.fetchPendingJobFor(any(RunnerAssignmentCandidate.class))).thenReturn(Optional.empty());

        Optional<?> result = service.fetchJob(request);

        assertThat(result).isEmpty();
        verify(runnerPort).findByToken("token");
        verify(jobPort).fetchPendingJobFor(any(RunnerAssignmentCandidate.class));
    }
}

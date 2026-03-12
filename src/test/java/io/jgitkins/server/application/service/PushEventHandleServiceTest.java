package io.jgitkins.server.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.jgitkins.server.application.dto.command.JobCreateCommand;
import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.port.in.JobCreateUseCase;
import io.jgitkins.server.application.port.out.BranchPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushEventHandleServiceTest {

    @Mock
    private JobCreateUseCase jobCreateUseCase;

    @Mock
    private BranchPersistencePort branchPort;

    @InjectMocks
    private PushEventHandleService service;

    @Test
    void handle_createsJobWhenPushIsValid() {
        PushEventCommand command = PushEventCommand.builder()
                .repositoryId(9L)
                .taskCd("1")
                .repoName("repo")
                .branchName("main")
                .branchCreated(true)
                .commitHash("abc")
                .triggeredBy(1L)
                .build();

        service.handle(command);

        verify(branchPort).save(any());

        ArgumentCaptor<JobCreateCommand> captor = ArgumentCaptor.forClass(JobCreateCommand.class);
        verify(jobCreateUseCase).create(captor.capture());
        JobCreateCommand job = captor.getValue();
        assertEquals("1", job.getTaskCd());
        assertEquals("repo", job.getRepoName());
        assertEquals(9L, job.getRepositoryId());
    }
}

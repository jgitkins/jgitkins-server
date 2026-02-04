package io.jgitkins.server.application.port.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.command.JobCreateCommand;
import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.port.in.JobCreateUseCase;
import io.jgitkins.server.application.port.out.BranchPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import java.util.Optional;
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
    private RepositoryPort repositoryPort;

    @Mock
    private BranchPort branchPort;

    @Mock
    private OrganizePort organizePort;

    @Mock
    private UserPort userPort;

    @InjectMocks
    private PushEventHandleService service;

    @Test
    void handle_createsJobWhenPushIsValid() {
        when(userPort.findUserIdByUsername("user")).thenReturn(Optional.of(1L));
        when(repositoryPort.findRepositoryId(any(), any(), any())).thenReturn(Optional.of(9L));

        PushEventCommand command = PushEventCommand.builder()
                .namespace("user")
                .repositoryName("repo")
                .branchName("main")
                .branchCreated(true)
                .commitHash("abc")
                .triggeredBy(1L)
                .build();

        service.handle(command);

        verify(branchPort).create(any());

        ArgumentCaptor<JobCreateCommand> captor = ArgumentCaptor.forClass(JobCreateCommand.class);
        verify(jobCreateUseCase).create(captor.capture());
        JobCreateCommand job = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("user", job.getTaskCd());
        org.junit.jupiter.api.Assertions.assertEquals("repo", job.getRepoName());
        org.junit.jupiter.api.Assertions.assertEquals(9L, job.getRepositoryId());
    }
}

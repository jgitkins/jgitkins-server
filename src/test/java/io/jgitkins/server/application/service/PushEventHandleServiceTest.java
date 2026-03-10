package io.jgitkins.server.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.jgitkins.server.application.dto.command.JobCreateCommand;
import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.port.in.JobCreateUseCase;
import io.jgitkins.server.application.port.out.BranchPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import java.time.LocalDateTime;
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

    @InjectMocks
    private PushEventHandleService service;

    @Test
    void handle_createsJobWhenPushIsValid() {
        String gitPath = "/path/to/repo.git";
        Repository repository = Repository.rehydrate(
                RepositoryId.of(9L),
                OwnerType.USER,
                OwnerId.of(1L),
                RepositoryName.from("repo"),
                RepositoryPath.from("user-repo"),
                null,
                null,
                "desc",
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        when(repositoryPort.findByPath(gitPath)).thenReturn(Optional.of(repository));

        PushEventCommand command = PushEventCommand.builder()
                .gitDirPath(gitPath)
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
        assertEquals("1", job.getTaskCd());
        assertEquals("repo", job.getRepoName());
        assertEquals(9L, job.getRepositoryId());
    }
}

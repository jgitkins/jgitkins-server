package io.jgitkins.server.infrastructure.config.git.hook.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.exception.ApplicationException;
import io.jgitkins.server.application.port.out.RepositoryPersistencePort;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushEventCommandMapperTest {

    @Mock
    private RepositoryPersistencePort repositoryPort;

    @Test
    void map_buildsPurePushEventCommand() {
        PushEventCommandMapper mapper = new PushEventCommandMapper(repositoryPort);
        when(repositoryPort.findByPath("/bare/users/alice/repo.git"))
                .thenReturn(Optional.of(repository()));

        ReceiveCommand command = new ReceiveCommand(
                ObjectId.zeroId(),
                ObjectId.fromString("0123456789012345678901234567890123456789"),
                "refs/heads/main"
        );

        Optional<PushEventCommand> result = mapper.map(
                "/bare/users/alice/repo.git",
                7L,
                List.of(command)
        );

        assertThat(result).isPresent();
        assertThat(result.get().getRepositoryId()).isEqualTo(9L);
        assertThat(result.get().getTaskCd()).isEqualTo("1");
        assertThat(result.get().getRepoName()).isEqualTo("repo");
        assertThat(result.get().getBranchName()).isEqualTo("main");
        assertThat(result.get().getCommitHash()).isEqualTo("0123456789012345678901234567890123456789");
        assertThat(result.get().isBranchCreated()).isTrue();
    }

    @Test
    void map_throwsWhenRepositoryCannotBeResolved() {
        PushEventCommandMapper mapper = new PushEventCommandMapper(repositoryPort);
        when(repositoryPort.findByPath("/bare/users/alice/repo.git")).thenReturn(Optional.empty());

        ReceiveCommand command = new ReceiveCommand(
                ObjectId.zeroId(),
                ObjectId.fromString("0123456789012345678901234567890123456789"),
                "refs/heads/main"
        );

        assertThrows(ApplicationException.class,
                () -> mapper.map("/bare/users/alice/repo.git", 7L, List.of(command)));
    }

    private Repository repository() {
        return Repository.rehydrate(
                RepositoryId.of(9L),
                OwnerType.USER,
                OwnerId.of(1L),
                RepositoryName.from("repo"),
                RepositoryPath.from("repo"),
                null,
                null,
                "desc",
                "/users/alice/repo.git",
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }
}

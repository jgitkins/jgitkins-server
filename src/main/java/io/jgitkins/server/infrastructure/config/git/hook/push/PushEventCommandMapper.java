package io.jgitkins.server.infrastructure.config.git.hook.push;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.exception.ApplicationException;
import io.jgitkins.server.application.port.out.RepositoryPersistencePort;
import io.jgitkins.server.domain.aggregate.Repository;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushEventCommandMapper {

    private final RepositoryPersistencePort repositoryPort;

    public Optional<PushEventCommand> map(String gitDirPath, Long triggeredBy, Collection<ReceiveCommand> commands) {
        Optional<ReceiveCommand> lastCommand = commands.stream()
                .filter(cmd -> cmd.getRefName().startsWith(Constants.R_HEADS))
                .reduce((first, second) -> second);

        if (lastCommand.isEmpty()) {
            return Optional.empty();
        }

        ReceiveCommand command = lastCommand.get();
        String branchName = extractBranchName(command.getRefName()).orElse(null);
        if (branchName == null) {
            return Optional.empty();
        }

        Repository repository = repositoryPort.findByPath(gitDirPath)
                .orElseThrow(() -> new ApplicationException(
                        ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found for path: " + gitDirPath));

        return Optional.of(PushEventCommand.builder()
                .repositoryId(repository.getId().getValue())
                .taskCd(repository.getOwnerId().toString())
                .repoName(repository.getName().getValue())
                .branchName(branchName)
                .branchCreated(command.getType() == ReceiveCommand.Type.CREATE)
                .branchDeleted(command.getType() == ReceiveCommand.Type.DELETE)
                .commitHash(command.getNewId() != null ? command.getNewId().getName() : null)
                .triggeredBy(triggeredBy)
                .build());
    }

    private Optional<String> extractBranchName(String refName) {
        if (refName == null || !refName.startsWith(Constants.R_HEADS)) {
            return Optional.empty();
        }
        return Optional.of(refName.substring(Constants.R_HEADS.length()));
    }
}

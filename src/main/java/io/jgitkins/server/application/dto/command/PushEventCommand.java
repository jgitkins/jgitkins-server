package io.jgitkins.server.application.dto.command;

import lombok.Builder;
import lombok.Getter;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.ReceiveCommand;

import java.util.Collection;
import java.util.Optional;

@Getter
@Builder
public class PushEventCommand {
    private final String gitDirPath;
    private final String namespace;
    private final String repositoryName;
    private final String branchName;
    private final boolean branchCreated;
    private final boolean branchDeleted;
    private final String commitHash;
    private final Long triggeredBy;

    public static PushEventCommand from(String gitDirPath, Long triggeredBy, Collection<ReceiveCommand> commands) {
        Optional<ReceiveCommand> lastCommand = commands.stream()
                .filter(cmd -> cmd.getRefName().startsWith(Constants.R_HEADS))
                .reduce((first, second) -> second);

        if (lastCommand.isEmpty()) {
            return null;
        }

        ReceiveCommand command = lastCommand.get();
        String branchName = extractBranchName(command.getRefName()).orElse(null);
        if (branchName == null) {
            return null;
        }

        return PushEventCommand.builder()
                .gitDirPath(gitDirPath)
                .branchName(branchName)
                .branchCreated(command.getType() == ReceiveCommand.Type.CREATE)
                .branchDeleted(command.getType() == ReceiveCommand.Type.DELETE)
                .commitHash(command.getNewId() != null ? command.getNewId().getName() : null)
                .triggeredBy(triggeredBy)
                .build();
    }

    private static Optional<String> extractBranchName(String refName) {
        if (!refName.startsWith(Constants.R_HEADS)) {
            return Optional.empty();
        }
        return Optional.of(refName.substring(Constants.R_HEADS.length()));
    }
}

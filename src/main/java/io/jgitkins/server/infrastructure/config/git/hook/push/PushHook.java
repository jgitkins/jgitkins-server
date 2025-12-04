package io.jgitkins.server.infrastructure.config.git.hook.push;

import io.jgitkins.server.application.dto.JobCreateCommand;
import io.jgitkins.server.application.port.in.JobCreateUseCase;
import io.jgitkins.server.application.port.out.BranchPersistencePort;
import io.jgitkins.server.application.port.out.RepositoryLoadPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

/**
 * Push-side hook that logs generic push info and branch creation events.
 */
@Slf4j
@RequiredArgsConstructor
public class PushHook implements PostReceiveHook {

    private final HttpServletRequest request;
    private final BranchPersistencePort branchPersistencePort;
    private final RepositoryLoadPort repositoryLookupPort;
    private final JobCreateUseCase createJobUseCase;

    @Override
    public void onPostReceive(ReceivePack receivePack, Collection<ReceiveCommand> commands) {
        Repository repository = receivePack.getRepository();
        log.debug("push event: user={} ip={} repo={}", request.getRemoteUser(), request.getRemoteAddr(), repository.getDirectory());

        // loading repository's context
        RepositoryContext repositoryContext = resolveRepositoryContext(repository)
                .orElse(null);
        if (repositoryContext == null) {
            return;
        }

        // loading last hash of commits
        Optional<ReceiveCommand> lastCommand = resolveLatestBranchCommand(commands);
        if (lastCommand.isEmpty()) {
            log.debug("push hook skipped: no applicable branch command found");
            return;
        }

        ReceiveCommand command = lastCommand.get();
        String branchName = extractBranchName(command).orElse(null);
        if (branchName == null) {
            log.warn("push hook skipped: unable to resolve branch name for ref={}", command.getRefName());
            return;
        }

        // new branch create event
        if (command.getType() == ReceiveCommand.Type.CREATE) {
            branchPersistencePort.create(repositoryContext.repositoryId(), branchName);
        }

        // TODO: get userId from custom header (filter)
        Optional<Long> triggeredBy = resolveUserId(request.getRemoteUser());
        if (triggeredBy.isEmpty()) {
            log.warn("job creation skipped: unable to resolve remote user={} for repository={}",
                    request.getRemoteUser(), repositoryContext.repositoryName());
            return;
        }

        Optional<String> commitHash = resolveCommitHash(command);
        if (commitHash.isEmpty()) {
            log.warn("job creation skipped: missing commit hash for ref={} type={}",
                    command.getRefName(), command.getType());
            return;
        }

        JobCreateCommand jobCommand = JobCreateCommand.builder()
                .taskCd(repositoryContext.organizeCd())
                .repoName(repositoryContext.repositoryName())
                .repositoryId(repositoryContext.repositoryId())
                .branchName(branchName)
                .commitHash(commitHash.get())
                .triggeredBy(triggeredBy.get())
                .build();

        createJobUseCase.createJob(jobCommand);
    }

    private Optional<RepositoryContext> resolveRepositoryContext(Repository repository) {
        File gitDir = repository.getDirectory();
        if (gitDir == null) {
            log.warn("push hook skipped: git directory not found in repository: {}", repository);
            return Optional.empty();
        }

        File organizeDir = gitDir.getParentFile();
        if (organizeDir == null) {
            log.warn("push hook skipped: organize directory missing for repo dir: {}", gitDir);
            return Optional.empty();
        }

        String repoName = stripGitSuffix(gitDir.getName());
        String organizeCd = organizeDir.getName();

        Optional<Long> repositoryId = repositoryLookupPort.findRepositoryId(organizeCd, repoName);
        if (repositoryId.isEmpty()) {
            log.warn("push hook skipped: repository not registered. taskCd={} repo={}", organizeCd, repoName);
            return Optional.empty();
        }

        return Optional.of(new RepositoryContext(organizeCd, repoName, repositoryId.get()));
    }

    private Optional<String> extractBranchName(ReceiveCommand command) {
        String refName = command.getRefName();
        if (!refName.startsWith(Constants.R_HEADS)) {
            return Optional.empty();
        }
        return Optional.of(refName.substring(Constants.R_HEADS.length()));
    }

    private Optional<ReceiveCommand> resolveLatestBranchCommand(Collection<ReceiveCommand> commands) {
        return commands.stream()
                .filter(cmd -> cmd.getRefName().startsWith(Constants.R_HEADS))
                .filter(cmd -> cmd.getType() != ReceiveCommand.Type.DELETE)
                .reduce((first, second) -> second);
    }

    private Optional<String> resolveCommitHash(ReceiveCommand command) {
        return Optional.ofNullable(command.getNewId()).map(objectId -> objectId.getName());
    }

    private Optional<Long> resolveUserId(String remoteUser) {
        if (remoteUser == null || remoteUser.isBlank()) {
            log.warn("job creation skipped: remote user not provided");
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(remoteUser));
        } catch (NumberFormatException ex) {
            log.warn("job creation skipped: unable to parse remote user to Long: {}", remoteUser);
            return Optional.empty();
        }
    }

    private String stripGitSuffix(String name) {
        return name.endsWith(".git") ? name.substring(0, name.length() - 4) : name;
    }

    private record RepositoryContext(String organizeCd, String repositoryName, Long repositoryId) {
    }
}

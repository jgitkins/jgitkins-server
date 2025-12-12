package io.jgitkins.server.infrastructure.config.git.hook.push;

import io.jgitkins.server.application.dto.PushEventCommand;
import io.jgitkins.server.application.port.in.HandlePushEventUseCase;
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
    private final HandlePushEventUseCase handlePushEventUseCase;

    // Adapter 일부
    @Override
    public void onPostReceive(ReceivePack receivePack, Collection<ReceiveCommand> commands) {
        Repository repository = receivePack.getRepository();
        Long requesterId = 1L; // TODO: custom
        log.debug("push event: user={} ip={} repo={}", request.getRemoteUser(), request.getRemoteAddr(), repository.getDirectory());

        // TODO: 저장소 도메인 로딩
        //  1. 저장소 도메인 로딩 with OutgoingPort
        //  2. 훅이벤트가 신규 브랜치 생성 포함시 브랜치도 생성이 필요함 (반대로 삭제 이벤트때는 브랜치 영속화 해제 해야함)
        //  3. 마지막으로 이벤트 변경에 따라 Job 생성이 필요

        // loading repository's context
        RepositoryContext repositoryContext = resolveRepositoryContext(repository).orElse(null);
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

        PushEventCommand pushEventCommand = PushEventCommand.builder()
                .organizeCode(repositoryContext.organizeCd())
                .repositoryName(repositoryContext.repositoryName())
                .branchName(branchName)
                .branchCreated(command.getType() == ReceiveCommand.Type.CREATE)
                .commitHash(resolveCommitHash(command).orElse(null))
//                .triggeredBy(resolveUserId(request.getRemoteUser()).orElse(null))
                .triggeredBy(requesterId)
                .build();

        handlePushEventUseCase.handle(pushEventCommand);
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

        return Optional.of(new RepositoryContext(organizeCd, repoName));
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

    private record RepositoryContext(String organizeCd, String repositoryName) {
    }
}

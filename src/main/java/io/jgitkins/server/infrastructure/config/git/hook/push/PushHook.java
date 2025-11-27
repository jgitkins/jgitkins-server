package io.jgitkins.server.infrastructure.config.git.hook.push;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import io.jgitkins.server.application.dto.BranchCreateCommand;
import io.jgitkins.server.application.port.in.CreateBranchUseCase;

/**
 * Push-side hook that logs generic push info and branch creation events.
 */
@Slf4j
@RequiredArgsConstructor
public class PushHook implements PostReceiveHook {

    private final HttpServletRequest request;
    private final CreateBranchUseCase createBranchUseCase;

    @Override
    public void onPostReceive(ReceivePack receivePack, Collection<ReceiveCommand> commands) {
        Repository repository = receivePack.getRepository();
        String user = request.getRemoteUser();
        String ip = request.getRemoteAddr();
        log.debug("push event: user={} ip={} repo={}", user, ip, repository.getDirectory());

        commands.stream()
                .filter(cmd -> cmd.getRefName().startsWith(Constants.R_HEADS))
                .filter(cmd -> cmd.getType() == ReceiveCommand.Type.CREATE)
                .forEach(cmd -> {
                    String branchName = cmd.getRefName().substring(Constants.R_HEADS.length());
                    log.info("new branches pushed: [{}]", branchName);
                    persistBranch(repository, branchName);
                });
    }

    private void persistBranch(Repository repository, String branchName) {
        File gitDir = repository.getDirectory();
        if (gitDir == null) {
            return;
        }

        File taskDir = gitDir.getParentFile();
        if (taskDir == null) {
            return;
        }

        String repoName = stripGitSuffix(gitDir.getName());
        String taskCd = taskDir.getName();
        BranchCreateCommand command = BranchCreateCommand.builder()
                .taskCd(taskCd)
                .repoName(repoName)
                .branchName(branchName)
                .physicalCreationRequired(false)
                .build();

        try {
            createBranchUseCase.createBranch(command);
        } catch (IOException e) {
            log.warn("Failed to persist branch [{}] for repo {}/{}", branchName, taskCd, repoName, e);
        }
    }

    private String stripGitSuffix(String name) {
        return name.endsWith(".git") ? name.substring(0, name.length() - 4) : name;
    }
}

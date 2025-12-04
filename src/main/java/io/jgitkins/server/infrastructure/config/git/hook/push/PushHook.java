package io.jgitkins.server.infrastructure.config.git.hook.push;

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


    private void onBranchCreationHandler(Collection<ReceiveCommand> commands, Repository repository) {
        ReceiveCommand command = commands.stream()
//                .filter(cmd -> cmd.getRefName().startsWith(Constants.R_HEADS))
                .filter(cmd -> cmd.getType() == ReceiveCommand.Type.CREATE) // 신규 브랜치 의미
                .findFirst()
                .get();

        if (command == null)
            return;

        String branchName = command.getRefName().substring(Constants.R_HEADS.length());
        persistBranch(repository, branchName);

    }

    private void persistBranch(Repository repository, String branchName) {

        File gitDir = repository.getDirectory();
        if (gitDir == null) {
            return;
        }

        File organizeDir = gitDir.getParentFile();
        if (organizeDir == null) {
            return;
        }

        String repoName = stripGitSuffix(gitDir.getName());
        String organizeCd = organizeDir.getName();

        Optional<Long> repositoryId = repositoryLookupPort.findRepositoryId(organizeCd, repoName);
        if (repositoryId.isEmpty()) {
            log.warn("branch persistence skipped because repository not found: taskCd={} repo={}", organizeCd, repoName);
            return;
        }

        branchPersistencePort.create(repositoryId.get(), branchName);
    }

    private String stripGitSuffix(String name) {
        return name.endsWith(".git") ? name.substring(0, name.length() - 4) : name;
    }










    @Override
    public void onPostReceive(ReceivePack receivePack, Collection<ReceiveCommand> commands) {
        Repository repository = receivePack.getRepository();
        String user = request.getRemoteUser();
        String ip = request.getRemoteAddr();
        log.debug("push event: user={} ip={} repo={}", user, ip, repository.getDirectory());


        // 브랜치 생성 리스너
        onBranchCreationHandler(commands, repository);

        // TODO: Jenkins File Check And Create A Job

    }
}

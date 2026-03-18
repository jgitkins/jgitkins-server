package io.jgitkins.server.infrastructure.config.git.hook.push;

import io.jgitkins.server.application.common.RepositoryPathHelper;
import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.dto.command.PushEventCommand;
import io.jgitkins.server.application.exception.ApplicationException;
import io.jgitkins.server.application.port.out.RepositoryPersistencePort;
import io.jgitkins.server.domain.aggregate.Repository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

@Component
public class PushEventCommandMapper {

    private final RepositoryPersistencePort repositoryPort;
    private final Path repoRootPath;

    public PushEventCommandMapper(RepositoryPersistencePort repositoryPort,
            @org.springframework.beans.factory.annotation.Value("${jgitkins.server.runtime.volume:${user.home}}") String runtimeVolume) {
        this.repositoryPort = repositoryPort;
        this.repoRootPath = Paths.get(runtimeVolume).toAbsolutePath().normalize();
    }

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

        Repository repository = resolveRepository(gitDirPath)
                .orElseThrow(() -> new ApplicationException(
                        ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found for path: " + gitDirPath));
        String namespace = extractNamespace(repository)
                .orElseThrow(() -> new ApplicationException(
                        ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository namespace not found for path: " + gitDirPath));

        return Optional.of(PushEventCommand.builder()
                .repositoryId(repository.getId().getValue())
                .taskCd(namespace)
                .repoName(repository.getName().getValue())
                .branchName(branchName)
                .branchCreated(command.getType() == ReceiveCommand.Type.CREATE)
                .branchDeleted(command.getType() == ReceiveCommand.Type.DELETE)
                .commitHash(command.getNewId() != null ? command.getNewId().getName() : null)
                .triggeredBy(triggeredBy)
                .build());
    }

    private Optional<Repository> resolveRepository(String gitDirPath) {
        Optional<Repository> byStoredPath = repositoryPort.findByPath(gitDirPath);
        if (byStoredPath.isPresent()) {
            return byStoredPath;
        }

        return toClonePath(gitDirPath)
                .flatMap(repositoryPort::findByClonePath);
    }

    private Optional<String> extractNamespace(Repository repository) {
        if (repository == null || !StringUtils.hasText(repository.getClonePath())) {
            return Optional.empty();
        }
        String clonePath = repository.getClonePath().trim().replaceAll("^/+", "").replaceAll("/+$", "");
        int lastSlash = clonePath.lastIndexOf('/');
        if (lastSlash <= 0) {
            return Optional.empty();
        }
        return Optional.of(clonePath.substring(0, lastSlash));
    }

    private Optional<String> toClonePath(String gitDirPath) {
        if (!StringUtils.hasText(gitDirPath)) {
            return Optional.empty();
        }
        Path absoluteGitDir = Paths.get(gitDirPath).toAbsolutePath().normalize();
        if (!absoluteGitDir.startsWith(repoRootPath)) {
            return Optional.empty();
        }
        Path relativePath = repoRootPath.relativize(absoluteGitDir);
        if (relativePath.getNameCount() < 2) {
            return Optional.empty();
        }
        String clonePath = RepositoryPathHelper.buildClonePath(
                relativePath.subpath(0, relativePath.getNameCount() - 1).toString().replace('\\', '/'),
                relativePath.getFileName().toString());
        return Optional.of(clonePath);
    }

    private Optional<String> extractBranchName(String refName) {
        if (refName == null || !refName.startsWith(Constants.R_HEADS)) {
            return Optional.empty();
        }
        return Optional.of(refName.substring(Constants.R_HEADS.length()));
    }
}

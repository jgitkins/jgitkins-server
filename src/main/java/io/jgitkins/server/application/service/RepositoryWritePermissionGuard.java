package io.jgitkins.server.application.service;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.dto.RepositoryKey;
import io.jgitkins.server.application.port.in.GitRepositoryAccessUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.domain.aggregate.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class RepositoryWritePermissionGuard {

    private final CurrentUserPort currentUserPort;
    private final GitRepositoryAccessUseCase gitRepositoryAccessUseCase;
    private final RepositoryNamespaceResolver repositoryNamespaceResolver;

    public void assertCanWrite(Repository repository) {
        RepositoryKey key = RepositoryKey.fromPath(repository.getClonePath());

        String namespace;
        String repoName;
        if (key != null) {
            namespace = key.namespace();
            repoName = key.repoName();
        } else {
            namespace = repositoryNamespaceResolver.resolve(repository);
            repoName = repository.getPath() != null
                    ? repository.getPath().getValue()
                    : repository.getName().getValue();
        }

        assertCanWrite(namespace, repoName);
    }

    public void assertCanWrite(String namespace, String repoName) {
        if (!StringUtils.hasText(namespace) || !StringUtils.hasText(repoName)) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.BAD_REQUEST, "Repository namespace/repoName is required.");
        }

        Long userId = currentUserPort.currentUserId()
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.UNAUTHORIZED, "Unauthenticated"));

        boolean allowed = gitRepositoryAccessUseCase.canWrite(null, namespace.trim(), repoName.trim(), userId);
        if (!allowed) {
            throw new JgitkinsException(
                    io.jgitkins.server.application.common.error.ApplicationErrorCode.FORBIDDEN,
                    String.format("Write access denied: %s/%s", namespace, repoName)
            );
        }
    }
}

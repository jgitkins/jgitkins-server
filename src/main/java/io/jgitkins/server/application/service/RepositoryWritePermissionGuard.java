package io.jgitkins.server.application.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.UnprocessableException;
import io.jgitkins.server.application.dto.RepositoryKey;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.domain.aggregate.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class RepositoryWritePermissionGuard {

    private final CurrentUserPort currentUserPort;
    private final GitRepositoryAccessService gitRepositoryAccessService;
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
            throw new UnprocessableException(ErrorCode.BAD_REQUEST, "Repository namespace/repoName is required.");
        }

        Long userId = currentUserPort.currentUserId()
                .orElseThrow(() -> new UnprocessableException(ErrorCode.UNAUTHORIZED, "Unauthenticated"));

        boolean allowed = gitRepositoryAccessService.canWrite(null, namespace.trim(), repoName.trim(), userId);
        if (!allowed) {
            throw new UnprocessableException(
                    ErrorCode.FORBIDDEN,
                    String.format("Write access denied: %s/%s", namespace, repoName)
            );
        }
    }
}

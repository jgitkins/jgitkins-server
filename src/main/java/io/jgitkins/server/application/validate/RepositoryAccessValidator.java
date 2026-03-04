package io.jgitkins.server.application.validate;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.dto.RepositoryKey;
import io.jgitkins.server.application.port.in.GitRepositoryAccessUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.support.RepositoryNamespaceResolver;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.aggregate.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepositoryAccessValidator {

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

        validateCanCommit(namespace, repoName);
    }

    public void validateCanCommit(String namespace, String repoName) {
        Long userId = currentUserPort.currentUserId()
                .orElseThrow(() -> new JgitkinsException(ApplicationErrorCode.UNAUTHORIZED, "Unauthenticated"));

        boolean allowed = gitRepositoryAccessUseCase.canWrite(null, namespace.trim(), repoName.trim(), userId);
        if (!allowed) {
            throw new JgitkinsException(
                    ApplicationErrorCode.FORBIDDEN,
                    String.format("Write access denied: %s/%s", namespace, repoName)
            );
        }
    }
}

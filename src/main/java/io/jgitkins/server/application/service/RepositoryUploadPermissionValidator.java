package io.jgitkins.server.application.service;

import org.springframework.stereotype.Component;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.dto.RepositoryKey;
import io.jgitkins.server.application.port.in.GitRepositoryAccessUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.aggregate.Repository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RepositoryUploadPermissionValidator {

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

        validateCanUpload(namespace, repoName);
    }

    public void validateCanUpload(String namespace, String repoName) {
        // TODO: 해당 검증은 Presentation 에서 진행하므로 (중복) 제거
        // if (!StringUtils.hasText(namespace) || !StringUtils.hasText(repoName)) {
        //     throw new JgitkinsException(DomainErrorCode.RULE_VIOLATION, "Repository namespace/repoName is required.");
        // }

        Long userId = currentUserPort.currentUserId()
                .orElseThrow(() -> new JgitkinsException(ApplicationErrorCode.UNAUTHORIZED, "Unauthenticated"));

        boolean allowed = gitRepositoryAccessUseCase.canWrite(null, namespace.trim(), repoName.trim(), userId);
        if (!allowed) {
            throw new JgitkinsException(
                    io.jgitkins.server.application.common.error.ApplicationErrorCode.FORBIDDEN,
                    String.format("Write access denied: %s/%s", namespace, repoName)
            );
        }
    }
}

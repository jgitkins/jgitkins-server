package io.jgitkins.server.application.validate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.port.in.GitRepositoryAccessUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.support.RepositoryNamespaceResolver;
import io.jgitkins.server.common.exception.JgitkinsException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepositoryAccessValidatorTest {

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private GitRepositoryAccessUseCase gitRepositoryAccessUseCase;

    @Mock
    private RepositoryNamespaceResolver repositoryNamespaceResolver;

    private RepositoryAccessValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RepositoryAccessValidator(currentUserPort, gitRepositoryAccessUseCase, repositoryNamespaceResolver);
    }

    @Test
    void validateCanCommit_throwsUnauthorized_whenCurrentUserMissing() {
        when(currentUserPort.currentUserId()).thenReturn(Optional.empty());

        JgitkinsException ex = assertThrows(JgitkinsException.class,
                () -> validator.validateCanCommit("team", "repo"));

        org.junit.jupiter.api.Assertions.assertEquals("UNAUTHORIZED", ex.getErrorCode().getCode());
    }

    @Test
    void validateCanCommit_throwsForbidden_whenWritePermissionDenied() {
        when(currentUserPort.currentUserId()).thenReturn(Optional.of(7L));
        when(gitRepositoryAccessUseCase.canWrite(null, "team", "repo", 7L)).thenReturn(false);

        JgitkinsException ex = assertThrows(JgitkinsException.class,
                () -> validator.validateCanCommit("team", "repo"));

        org.junit.jupiter.api.Assertions.assertEquals("FORBIDDEN", ex.getErrorCode().getCode());
    }

    @Test
    void validateCanCommit_allows_whenWritePermissionGranted() {
        when(currentUserPort.currentUserId()).thenReturn(Optional.of(7L));
        when(gitRepositoryAccessUseCase.canWrite(null, "team", "repo", 7L)).thenReturn(true);

        assertDoesNotThrow(() -> validator.validateCanCommit("team", "repo"));
    }
}

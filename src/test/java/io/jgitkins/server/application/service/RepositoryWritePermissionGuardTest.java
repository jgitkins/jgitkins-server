package io.jgitkins.server.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepositoryWritePermissionGuardTest {

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private GitRepositoryAccessService gitRepositoryAccessService;

    @Mock
    private RepositoryNamespaceResolver repositoryNamespaceResolver;

    private RepositoryWritePermissionGuard guard;

    @BeforeEach
    void setUp() {
        guard = new RepositoryWritePermissionGuard(currentUserPort, gitRepositoryAccessService, repositoryNamespaceResolver);
    }

    @Test
    void assertCanWrite_throwsUnauthorized_whenCurrentUserMissing() {
        when(currentUserPort.currentUserId()).thenReturn(Optional.empty());

        ((JgitkinsException) ex) = assertThrows(JgitkinsException.class,
                () -> guard.assertCanWrite("team", "repo"));

        org.junit.jupiter.api.Assertions.assertEquals("UNAUTHORIZED", ex.getErrorCode().getCode());
    }

    @Test
    void assertCanWrite_throwsForbidden_whenWritePermissionDenied() {
        when(currentUserPort.currentUserId()).thenReturn(Optional.of(7L));
        when(gitRepositoryAccessService.canWrite(null, "team", "repo", 7L)).thenReturn(false);

        ((JgitkinsException) ex) = assertThrows(JgitkinsException.class,
                () -> guard.assertCanWrite("team", "repo"));

        org.junit.jupiter.api.Assertions.assertEquals("FORBIDDEN", ex.getErrorCode().getCode());
    }

    @Test
    void assertCanWrite_allows_whenWritePermissionGranted() {
        when(currentUserPort.currentUserId()).thenReturn(Optional.of(7L));
        when(gitRepositoryAccessService.canWrite(null, "team", "repo", 7L)).thenReturn(true);

        assertDoesNotThrow(() -> guard.assertCanWrite("team", "repo"));
    }
}

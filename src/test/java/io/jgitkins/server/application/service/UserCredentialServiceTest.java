package io.jgitkins.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.command.UserCredentialIssueCommand;
import io.jgitkins.server.application.dto.result.UserCredentialIssueResult;
import io.jgitkins.server.application.dto.result.UserCredentialSummary;
import io.jgitkins.server.application.port.out.UserCredentialPort;
import io.jgitkins.server.domain.model.UserCredential;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class UserCredentialServiceTest {

    @Mock
    private UserCredentialPort port;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserCredentialService service;

    @Test
    void issueToken_issuesPlainTokenAndPersistsHashedCredential() {
        when(encoder.encode(any())).thenReturn("hashed");
        when(port.save(any(UserCredential.class))).thenAnswer(invocation -> {
            UserCredential credential = invocation.getArgument(0);
            return credential.withId(10L);
        });

        UserCredentialIssueResult result = service.issueToken(new UserCredentialIssueCommand(1L, "token", "desc", null));

        assertNotNull(result.getToken());
        assertTrue(result.getToken().startsWith("jkpat_"));
        assertEquals(10L, result.getCredentialId());

        verify(encoder).encode(result.getToken());

        ArgumentCaptor<UserCredential> captor = ArgumentCaptor.forClass(UserCredential.class);
        verify(port).save(captor.capture());
        UserCredential saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals("PAT", saved.getProvider());
        assertEquals("token", saved.getName());
        assertEquals("desc", saved.getDescription());
        assertEquals("hashed", saved.getPasswordHash());
    }

    @Test
    void getPatList_mapsToSummary() {
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0);
        UserCredential credential = UserCredential.rehydrate(7L, 2L, "PAT", "n", "d", "hash", createdAt, updatedAt);

        when(port.findAllByUserIdAndProvider(2L, "PAT")).thenReturn(List.of(credential));
        List<UserCredentialSummary> result = service.getPatList(2L);

        assertEquals(1, result.size());
        UserCredentialSummary summary = result.get(0);
        assertEquals(7L, summary.getId());
        assertEquals("PAT", summary.getProvider());
        assertEquals("n", summary.getName());
        assertEquals("d", summary.getDescription());
        assertEquals(createdAt, summary.getCreatedAt());
        assertEquals(updatedAt, summary.getUpdatedAt());
    }

    @Test
    void revokePat_deletesByCredentialIdAndUserId() {
        service.revokePat(3L, 9L);

        verify(port).deleteByIdAndUserId(9L, 3L);
    }
}

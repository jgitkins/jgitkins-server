package io.jgitkins.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.command.OAuthLoginCommand;
import io.jgitkins.server.application.dto.result.OAuthLoginResult;
import io.jgitkins.server.application.port.out.TokenIssuerPort;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OAuthLoginServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TokenIssuerPort tokenIssuerPort;

    @InjectMocks
    private OAuthLoginService oauthLoginService;

    @Test
    void login_issuesTokenForLoggedInUser() {
        OAuthLoginCommand command = new OAuthLoginCommand(
                "google",
                "sub-1",
                "user@example.com",
                "tester",
                true,
                "https://img.example.com/me.png"
        );
        User user = User.rehydrate(
                11L,
                "tester",
                "user@example.com",
                "tester",
                "https://img.example.com/me.png",
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(userService.loginOrSignUp(
                command.getProvider(),
                command.getSubject(),
                command.getEmail(),
                command.isEmailVerified(),
                command.getName(),
                command.getAvatarUrl()
        )).thenReturn(user);
        when(tokenIssuerPort.issueToken(11L, List.of("ROLE_USER"))).thenReturn("jwt-token");

        OAuthLoginResult result = oauthLoginService.login(command);

        assertEquals("jwt-token", result.getAppToken());
        assertEquals("google", result.getProvider());
        assertEquals(11L, result.getUser().getId());
        verify(tokenIssuerPort).issueToken(11L, List.of("ROLE_USER"));
    }

    @Test
    void login_propagatesUserLookupFailureForInvalidProviderOrSubject() {
        OAuthLoginCommand invalid = new OAuthLoginCommand(
                "unknown-provider",
                "",
                "user@example.com",
                "tester",
                true,
                null
        );
        when(userService.loginOrSignUp(
                invalid.getProvider(),
                invalid.getSubject(),
                invalid.getEmail(),
                invalid.isEmailVerified(),
                invalid.getName(),
                invalid.getAvatarUrl()
        )).thenThrow(new IllegalArgumentException("unsupported provider"));

        assertThrows(IllegalArgumentException.class, () -> oauthLoginService.login(invalid));
    }

    @Test
    void login_propagatesTokenIssueFailure() {
        OAuthLoginCommand command = new OAuthLoginCommand(
                "google",
                "sub-1",
                "user@example.com",
                "tester",
                true,
                null
        );
        User user = User.rehydrate(
                22L,
                "tester",
                "user@example.com",
                "tester",
                null,
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(userService.loginOrSignUp(
                command.getProvider(),
                command.getSubject(),
                command.getEmail(),
                command.isEmailVerified(),
                command.getName(),
                command.getAvatarUrl()
        )).thenReturn(user);
        when(tokenIssuerPort.issueToken(22L, List.of("ROLE_USER")))
                .thenThrow(new IllegalStateException("token issue failed"));

        assertThrows(IllegalStateException.class, () -> oauthLoginService.login(command));
    }
}

package io.jgitkins.server.application.service;

import io.jgitkins.server.application.dto.command.OAuthLoginCommand;
import io.jgitkins.server.application.dto.result.OAuthLoginResult;
import io.jgitkins.server.application.port.in.OAuthLoginUseCase;
import io.jgitkins.server.application.port.out.TokenIssuerPort;
import io.jgitkins.server.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OAuthLoginService implements OAuthLoginUseCase {

    private final UserService userService;
    private final TokenIssuerPort tokenIssuerPort;

    @Override
    public OAuthLoginResult login(OAuthLoginCommand command) {
        User user = userService.findOrCreateUser(
                command.getProvider(),
                command.getSubject(),
                command.getEmail(),
                command.isEmailVerified(),
                command.getName(),
                command.getAvatarUrl()
        );
        String appToken = tokenIssuerPort.issueToken(user.getId(), List.of("ROLE_USER"));
        return new OAuthLoginResult(appToken, user, command.getProvider());
    }
}

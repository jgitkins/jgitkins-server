package io.jgitkins.server.application.service;

import io.jgitkins.server.application.dto.result.OAuthLoginResult;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.infrastructure.config.security.JwtService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final UserService userService;
    private final JwtService jwtService;

    public OAuthLoginResult loginWithOidc(String providerName, OidcUser oidcUser) {
        return loginWithOidcAttributes(providerName,
                                       oidcUser.getSubject(),
                                       oidcUser.getEmail(),
                                       oidcUser.getFullName(),
                                       oidcUser.getEmailVerified() != null && oidcUser.getEmailVerified(),
                                       oidcUser.getPicture() != null ? oidcUser.getPicture().toString() : null);
    }

    public OAuthLoginResult loginWithOidcAttributes(String providerName,
                                                    String providerSub,
                                                    String email,
                                                    String name,
                                                    boolean emailVerified,
                                                    String avatarUrl) {
        User user = userService.findOrCreateUser(providerName,
                                                 providerSub,
                                                 email,
                                                 emailVerified,
                                                 name,
                                                 avatarUrl);
        String appToken = jwtService.issueToken(user.getId(), List.of("ROLE_USER"));
        return new OAuthLoginResult(appToken, user, providerName);
    }
}

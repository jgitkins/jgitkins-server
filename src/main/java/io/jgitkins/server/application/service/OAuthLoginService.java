package io.jgitkins.server.application.service;

import io.jgitkins.server.application.dto.result.OAuthLoginResult;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.security.JwtService;
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
        String providerSub = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        boolean emailVerified = oidcUser.getEmailVerified() != null && oidcUser.getEmailVerified();
        String avatarUrl = oidcUser.getPicture() != null ? oidcUser.getPicture().toString() : null;

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

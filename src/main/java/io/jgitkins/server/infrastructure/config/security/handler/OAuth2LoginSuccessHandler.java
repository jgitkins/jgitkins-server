package io.jgitkins.server.infrastructure.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jgitkins.server.application.dto.result.OAuthLoginResult;
import io.jgitkins.server.application.service.OAuthLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final OAuthLoginService oauthLoginService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            log.warn("Unsupported authentication type: {}", authentication.getClass().getName());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication");
            return;
        }

        // get oidc(OPEN ID CONNECT) from token
        Object principal = oauthToken.getPrincipal();
        if (!(principal instanceof OidcUser oidcUser)) {
            log.warn("OIDC principal missing for provider {}", oauthToken.getAuthorizedClientRegistrationId());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OIDC principal required");
            return;
        }

        OAuthLoginResult result = oauthLoginService.loginWithOidc(oauthToken.getAuthorizedClientRegistrationId(),
                                                                  oidcUser);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

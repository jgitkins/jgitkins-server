package io.jgitkins.server.infrastructure.config.git;

import io.jgitkins.server.infrastructure.config.security.filter.PatAuthenticationProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class GitRequestAuthSupport {

    private final PatAuthenticationProvider patAuthenticationProvider;

    public Long resolveUserId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return authenticateFromBasic(request);
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long authenticateFromBasic(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Basic ")) {
            return null;
        }
        String base64 = header.substring("Basic ".length());
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        int idx = decoded.indexOf(':');
        if (idx <= 0) {
            return null;
        }
        String username = decoded.substring(0, idx);
        String password = decoded.substring(idx + 1);
        try {
            Authentication auth = patAuthenticationProvider.authenticate(
                    org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated(
                            username, password
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            return Long.valueOf(auth.getName());
        } catch (AuthenticationException | NumberFormatException ex) {
            return null;
        }
    }
}

package io.jgitkins.server.infrastructure.config.security;

import io.jgitkins.server.application.port.out.UserCredentialPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.model.UserCredential;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatAuthenticationProvider implements AuthenticationProvider {

    private static final String PAT_PREFIX = "jkpat_";

    private final UserPort userPort;
    private final UserCredentialPort userCredentialPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawToken = authentication.getCredentials() == null ? null : authentication.getCredentials().toString();
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadCredentialsException("Missing token");
        }
        if (!rawToken.startsWith(PAT_PREFIX)) {
            throw new BadCredentialsException("Invalid token format");
        }

        Long userId = userPort.findUserIdByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<UserCredential> credential = userCredentialPort.findByUserIdAndProvider(userId, "PAT");
        if (credential.isEmpty()) {
            throw new BadCredentialsException("Token not registered");
        }

        if (!passwordEncoder.matches(rawToken, credential.get().getPasswordHash())) {
            throw new BadCredentialsException("Invalid token");
        }

        log.info("Authenticated user: [{}]", username);
        return new UsernamePasswordAuthenticationToken(
                String.valueOf(userId),
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_GIT"))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

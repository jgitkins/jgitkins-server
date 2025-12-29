package io.jgitkins.server.infrastructure.config.security;

import io.jgitkins.server.application.port.out.CurrentUserPort;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserAdapter implements CurrentUserPort {

    // loading an authentication info about specific request
    @Override
    public Optional<Long> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.valueOf(authentication.getName()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}

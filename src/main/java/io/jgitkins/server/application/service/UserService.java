package io.jgitkins.server.application.service;

import io.jgitkins.server.application.port.out.UserIdentityPersistencePort;
import io.jgitkins.server.application.port.out.UserPersistencePort;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.UserIdentity;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserPersistencePort userPersistencePort;
    private final UserIdentityPersistencePort userIdentityPersistencePort;

    public User findOrCreateUser(String providerName,
                                 String providerSub,
                                 String email,
                                 boolean emailVerified,
                                 String name,
                                 String avatarUrl) {
        LocalDateTime loginAt = LocalDateTime.now();
        Optional<UserIdentity> existingIdentity =
                userIdentityPersistencePort.findByProvider(providerName, providerSub);
        if (existingIdentity.isPresent()) {
            return handleExistingIdentity(existingIdentity.get(), email, emailVerified, name, avatarUrl, loginAt);
        }

        return handleNewIdentity(providerName, providerSub, email, emailVerified, name, avatarUrl, loginAt);
    }

    private User handleExistingIdentity(UserIdentity identity,
                                        String email,
                                        boolean emailVerified,
                                        String name,
                                        String avatarUrl,
                                        LocalDateTime loginAt) {

        User user = userPersistencePort.findById(identity.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for identity"));

        User updatedUser = applyUserUpdates(user, email, name, avatarUrl, loginAt);
        User persistedUser = userPersistencePort.save(updatedUser);

        UserIdentity updatedIdentity = maybeUpdateIdentity(identity, email, emailVerified, name, avatarUrl);
        if (updatedIdentity != identity) {
            userIdentityPersistencePort.save(updatedIdentity);
        }

        return persistedUser;
    }

    private User handleNewIdentity(String providerName,
                                   String providerSub,
                                   String email,
                                   boolean emailVerified,
                                   String name,
                                   String avatarUrl,
                                   LocalDateTime loginAt) {
        User user = resolveUserForNewIdentity(email, name, avatarUrl, providerName, providerSub);
        User updatedUser = applyUserUpdates(user, email, name, avatarUrl, loginAt);
        User persisted = userPersistencePort.save(updatedUser);

        UserIdentity identity = UserIdentity.create(
                persisted.getId(),
                providerName,
                providerSub,
                email,
                emailVerified,
                name,
                avatarUrl
        );
        userIdentityPersistencePort.save(identity);

        return persisted;
    }

    private User resolveUserForNewIdentity(String email,
                                           String name,
                                           String avatarUrl,
                                           String providerName,
                                           String providerSub) {
        if (email != null && !email.isBlank()) {
            Optional<User> existingByEmail = userPersistencePort.findByEmail(email.trim());
            if (existingByEmail.isPresent()) {
                return existingByEmail.get();
            }
        }

        String baseUsername = deriveUsername(email, providerName, providerSub);
        String username = ensureUniqueUsername(baseUsername, providerSub);
        return User.create(username, email, name, avatarUrl);
    }

    private User applyUserUpdates(User user,
                                  String email,
                                  String name,
                                  String avatarUrl,
                                  LocalDateTime loginAt) {
        User updated = maybeUpdateUser(user, email, name, avatarUrl);
        return updated.touchLogin(loginAt);
    }

    private String ensureUniqueUsername(String baseUsername, String providerSub) {
        if (userPersistencePort.findByUsername(baseUsername).isEmpty()) {
            return baseUsername;
        }
        String suffix = providerSub == null ? "user" : providerSub.substring(Math.max(0, providerSub.length() - 6));
        String fallback = baseUsername + "-" + suffix.toLowerCase();
        if (userPersistencePort.findByUsername(fallback).isEmpty()) {
            return fallback;
        }
        return baseUsername + "-" + System.currentTimeMillis();
    }

    private String deriveUsername(String email, String providerName, String providerSub) {
        if (email != null && !email.isBlank()) {
            String local = email.split("@")[0];
            return sanitize(local.toLowerCase());
        }
        String seed = providerName + "-" + providerSub;
        return sanitize(seed.toLowerCase());
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-z0-9._-]", "-");
    }

    private User maybeUpdateUser(User user, String email, String name, String avatarUrl) {
        boolean changed = !equals(user.getEmail(), normalize(email))
                || !equals(user.getDisplayName(), normalize(name))
                || !equals(user.getAvatarUrl(), normalize(avatarUrl));
        return changed ? user.updateProfile(email, name, avatarUrl) : user;
    }

    private UserIdentity maybeUpdateIdentity(UserIdentity identity,
                                             String email,
                                             boolean emailVerified,
                                             String name,
                                             String avatarUrl) {
        boolean changed = !equals(identity.getEmail(), normalize(email))
                || identity.isEmailVerified() != emailVerified
                || !equals(identity.getName(), normalize(name))
                || !equals(identity.getAvatarUrl(), normalize(avatarUrl));
        return changed ? identity.updateProfile(email, emailVerified, name, avatarUrl) : identity;
    }

    private boolean equals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

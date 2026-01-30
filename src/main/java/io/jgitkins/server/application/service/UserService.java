package io.jgitkins.server.application.service;

import io.jgitkins.server.application.port.out.UserIdentityPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.UserIdentity;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String STATUS_PENDING_USERNAME = "PENDING_USERNAME";

    private final UserPort userPort;
    private final UserIdentityPort userIdentityPort;
    private final OrganizePort organizePort;

    public User findOrCreateUser(String providerName,
                                 String providerSub,
                                 String email,
                                 boolean emailVerified,
                                 String name,
                                 String avatarUrl) {

        LocalDateTime loginAt = LocalDateTime.now();

        Optional<UserIdentity> existingIdentity = userIdentityPort.findByProvider(providerName, providerSub);

        if (existingIdentity.isPresent()) {
            return signin(existingIdentity.get(), email, emailVerified, name, avatarUrl, loginAt);
        }

        return signInAfterSignUp(providerName, providerSub, email, emailVerified, name, avatarUrl, loginAt);
    }

    private User signin(UserIdentity identity,
                        String email,
                        boolean emailVerified,
                        String name,
                        String avatarUrl,
                        LocalDateTime loginAt) {

        User user = userPort.findById(identity.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for identity"));

        User updatedUser = applyUserUpdates(user, email, name, avatarUrl, loginAt);

        User persistedUser = userPort.save(updatedUser);

        UserIdentity updatedIdentity = maybeUpdateIdentity(identity, email, emailVerified, name, avatarUrl);
        if (updatedIdentity != identity) {
            userIdentityPort.save(updatedIdentity);
        }

        return persistedUser;
    }

    private User signInAfterSignUp(String providerName,
                                   String providerSub,
                                   String email,
                                   boolean emailVerified,
                                   String name,
                                   String avatarUrl,
                                   LocalDateTime loginAt) {
        User user = resolveUserForNewIdentity(email, name, avatarUrl, providerName, providerSub);
        User updatedUser = applyUserUpdates(user, email, name, avatarUrl, loginAt);
        User persisted = userPort.save(updatedUser);

        UserIdentity identity = UserIdentity.create(
                persisted.getId(),
                providerName,
                providerSub,
                email,
                emailVerified,
                name,
                avatarUrl
        );
        userIdentityPort.save(identity);

        return persisted;
    }

    private User resolveUserForNewIdentity(String email,
                                           String name,
                                           String avatarUrl,
                                           String providerName,
                                           String providerSub) {
        Optional<User> existingByEmail = findExistingUserByEmail(email);
        if (existingByEmail.isPresent()) {
            return existingByEmail.get();
        }

        String baseUsername = deriveUsername(email, providerName, providerSub);
        String username = allocateUniqueUsername(baseUsername, providerSub);
        return User.createWithStatus(username, email, name, avatarUrl, STATUS_PENDING_USERNAME);
    }

    private Optional<User> findExistingUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userPort.findByEmail(email.trim());
    }

    private User applyUserUpdates(User user,
                                  String email,
                                  String name,
                                  String avatarUrl,
                                  LocalDateTime loginAt) {
        User updated = maybeUpdateUser(user, email, name, avatarUrl);
        return updated.touchLogin(loginAt);
    }

    private String allocateUniqueUsername(String baseUsername, String providerSub) {
        String providerSuffix = providerSub == null ? "user" : providerSub.substring(Math.max(0, providerSub.length() - 6));
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        String[] candidates = new String[] {
                baseUsername,
                baseUsername + "-" + providerSuffix.toLowerCase(),
                baseUsername + "-" + randomSuffix.toLowerCase(),
                baseUsername + "-" + System.currentTimeMillis()
        };

        for (String candidate : candidates) {
            if (isNamespaceAvailable(candidate)) {
                return candidate;
            }
        }

        return baseUsername + "-" + System.nanoTime();
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

    private boolean isNamespaceAvailable(String username) {
        if (userPort.findByUsername(username).isPresent()) {
            return false;
        }
        return findOrganizeByName(username).isEmpty();
    }

    private Optional<io.jgitkins.server.domain.aggregate.Organize> findOrganizeByName(String namespace) {
        try {
            return organizePort.findByName(OrganizeName.from(namespace));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
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

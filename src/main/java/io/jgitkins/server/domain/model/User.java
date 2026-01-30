package io.jgitkins.server.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    private final Long id;
    private final String username;
    private final String email;
    private final String displayName;
    private final String avatarUrl;
    private final UserAuthority authority;
    private final String status;
    private final LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static User create(String username,
                              String email,
                              String displayName,
                              String avatarUrl) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        LocalDateTime now = LocalDateTime.now();
        return new User(null,
                        username.trim(),
                        normalize(email),
                        normalize(displayName),
                        normalize(avatarUrl),
                        UserAuthority.USER,
                        "ACTIVE",
                        now,
                        now,
                        now);
    }

    public static User createWithStatus(String username,
                                        String email,
                                        String displayName,
                                        String avatarUrl,
                                        String status) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        LocalDateTime now = LocalDateTime.now();
        return new User(null,
                        username.trim(),
                        normalize(email),
                        normalize(displayName),
                        normalize(avatarUrl),
                        UserAuthority.USER,
                        status != null ? status : "ACTIVE",
                        now,
                        now,
                        now);
    }

    public User withId(Long id) {
        return new User(id,
                        username,
                        email,
                        displayName,
                        avatarUrl,
                        authority,
                        status,
                        lastLoginAt,
                        createdAt,
                        updatedAt);
    }

    public User updateProfile(String email, String displayName, String avatarUrl) {
        return new User(id,
                        username,
                        normalize(email),
                        normalize(displayName),
                        normalize(avatarUrl),
                        authority,
                        status,
                        lastLoginAt,
                        createdAt,
                        LocalDateTime.now());
    }

    public User updateUsername(String username, String status) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        return new User(id,
                        username.trim(),
                        email,
                        displayName,
                        avatarUrl,
                        authority,
                        status != null ? status : this.status,
                        lastLoginAt,
                        createdAt,
                        LocalDateTime.now());
    }

    public User touchLogin(LocalDateTime loginAt) {
        LocalDateTime when = loginAt != null ? loginAt : LocalDateTime.now();
        return new User(id,
                        username,
                        email,
                        displayName,
                        avatarUrl,
                        authority,
                        status,
                        when,
                        createdAt,
                        updatedAt);
    }

    public static User rehydrate(Long id,
                                 String username,
                                 String email,
                                 String displayName,
                                 String avatarUrl,
                                 UserAuthority authority,
                                 String status,
                                 LocalDateTime lastLoginAt,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt) {
        return new User(id,
                        username,
                        email,
                        displayName,
                        avatarUrl,
                        authority,
                        status,
                        lastLoginAt,
                        createdAt,
                        updatedAt);
    }

    public static User rehydrate(Long id,
                                 String username,
                                 String email,
                                 String displayName,
                                 String avatarUrl,
                                 String status,
                                 LocalDateTime lastLoginAt,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt) {
        return new User(id,
                        username,
                        email,
                        displayName,
                        avatarUrl,
                        UserAuthority.USER,
                        status,
                        lastLoginAt,
                        createdAt,
                        updatedAt);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

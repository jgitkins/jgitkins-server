package io.jgitkins.server.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCredential {

    private final Long id;
    private final Long userId;
    private final String provider;
    private final String passwordHash;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static UserCredential issuePat(Long userId, String passwordHash) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId is required");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash is required");
        }
        LocalDateTime now = LocalDateTime.now();
        return new UserCredential(null, userId, "PAT", passwordHash, now, now);
    }

    public UserCredential withId(Long id) {
        return new UserCredential(id, userId, provider, passwordHash, createdAt, updatedAt);
    }

    public UserCredential withUpdatedAt(LocalDateTime updatedAt) {
        return new UserCredential(id, userId, provider, passwordHash, createdAt, updatedAt);
    }

    public static UserCredential rehydrate(Long id,
                                           Long userId,
                                           String provider,
                                           String passwordHash,
                                           LocalDateTime createdAt,
                                           LocalDateTime updatedAt) {
        return new UserCredential(id, userId, provider, passwordHash, createdAt, updatedAt);
    }
}

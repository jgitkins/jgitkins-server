package io.jgitkins.server.application.service;

import io.jgitkins.server.application.dto.result.UserAdminDetail;
import io.jgitkins.server.application.dto.result.UserAdminSummary;
import io.jgitkins.server.application.dto.result.UserIdentitySummary;
import io.jgitkins.server.application.port.in.AdminUserQueryUseCase;
import io.jgitkins.server.application.port.in.AdminUserUpdateUseCase;
import io.jgitkins.server.application.port.out.UserIdentityPersistencePort;
import io.jgitkins.server.application.port.out.UserPersistencePort;
import io.jgitkins.server.domain.model.User;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService implements AdminUserQueryUseCase, AdminUserUpdateUseCase {

    private static final List<String> SUPPORTED_STATUSES = List.of("ACTIVE", "BLOCKED", "DELETED");

    private final UserPersistencePort userPersistencePort;
    private final UserIdentityPersistencePort userIdentityPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public List<UserAdminSummary> getUsers() {
        return userPersistencePort.findAll()
                .stream()
                .map(user -> new UserAdminSummary(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getStatus(),
                        user.getLastLoginAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserAdminDetail getUser(Long userId) {
        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<UserIdentitySummary> identities = userIdentityPersistencePort.findAllByUserId(userId)
                .stream()
                .map(identity -> new UserIdentitySummary(
                        identity.getProviderName(),
                        identity.getProviderSub(),
                        identity.getEmail(),
                        identity.isEmailVerified(),
                        identity.getName(),
                        identity.getAvatarUrl()
                ))
                .toList();
        return new UserAdminDetail(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                identities
        );
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, String status) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId is required");
        }
        String normalized = normalizeStatus(status);
        if (!SUPPORTED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported status: " + status);
        }
        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User updated = User.rehydrate(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                normalized,
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        userPersistencePort.save(updated);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }
}

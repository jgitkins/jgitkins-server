package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.result.UserAdminDetail;
import io.jgitkins.server.application.dto.result.UserAdminSummary;
import io.jgitkins.server.application.dto.result.UserIdentitySummary;
import io.jgitkins.server.application.mapper.UserApplicationMapper;
import io.jgitkins.server.application.port.in.AdminUserQueryUseCase;
import io.jgitkins.server.application.port.in.AdminUserUpdateUseCase;
import io.jgitkins.server.application.port.out.UserIdentityPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.UserStatus;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService implements AdminUserQueryUseCase, AdminUserUpdateUseCase {

    private static final List<UserStatus> SUPPORTED_STATUSES = List.of(
            UserStatus.ACTIVE,
            UserStatus.BLOCKED,
            UserStatus.DELETED,
            UserStatus.PENDING
    );

    private final UserPort userPort;
    private final UserIdentityPort userIdentityPort;
    private final UserApplicationMapper userApplicationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserAdminSummary> getUsers() {
        return userPort.findAll()
                .stream()
                .map(userApplicationMapper::toAdminSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserAdminDetail getUser(Long userId) {
        User user = userPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<UserIdentitySummary> identities = userIdentityPort.findAllByUserId(userId)
                .stream()
                .map(userApplicationMapper::toIdentitySummary)
                .toList();
        
        return userApplicationMapper.toAdminDetail(user, identities);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, String status) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId is required");
        }
        UserStatus normalized = normalizeStatus(status);
        if (!SUPPORTED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported status: " + status);
        }
        User user = userPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User updated = User.rehydrate(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getAuthority(),
                normalized,
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        userPort.save(updated);
    }

    private UserStatus normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if ("PENDING_USERNAME".equals(normalized)) {
            return UserStatus.PENDING;
        }
        return UserStatus.fromString(normalized);
    }
}

package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.common.exception.UnprocessableException;
import io.jgitkins.server.application.port.in.UserProfileUpdateUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.UserStatus;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService implements UserProfileUpdateUseCase {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");
    private static final Pattern ORGANIZE_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    private final CurrentUserPort currentUserPort;
    private final UserPort userPort;
    private final OrganizePort organizePort;
    private final RepositoryPort repositoryPort;

    @Override
    @Transactional
    public void updateUsername(String username) {
        String normalized = normalize(username);
        if (normalized == null || !USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new UnprocessableException(ErrorCode.BAD_REQUEST,
                    "Username allows only letters, numbers, dot, hyphen, or underscore.");
        }

        Long userId = currentUserPort.currentUserId()
                .orElseThrow(() -> new UnprocessableException(ErrorCode.UNAUTHORIZED, "Unauthenticated"));

        User user = userPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BAD_REQUEST, "User not found"));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new UnprocessableException(ErrorCode.BAD_REQUEST, "Username already set");
        }

        userPort.findByUsername(normalized)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ConflictException(ErrorCode.USERNAME_ALREADY_EXISTS, "Username already exists");
                });

        if (ORGANIZE_NAME_PATTERN.matcher(normalized).matches()) {
            organizePort.findByName(OrganizeName.from(normalized))
                    .ifPresent(existing -> {
                        throw new ConflictException(ErrorCode.ORGANIZE_ALREADY_EXISTS, "Namespace already exists");
                    });
        }

        List<Repository> repos = repositoryPort.findAllByOwner(OwnerType.USER, OwnerId.of(userId));
        if (!repos.isEmpty()) {
            throw new ConflictException(ErrorCode.BAD_REQUEST, "Cannot rename user with existing repositories");
        }

        User updated = user.updateUsername(normalized, UserStatus.ACTIVE);
        userPort.save(updated);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

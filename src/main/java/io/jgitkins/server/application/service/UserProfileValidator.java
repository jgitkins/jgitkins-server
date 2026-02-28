package io.jgitkins.server.application.service;

import org.springframework.stereotype.Component;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.domain.model.vo.Username;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserProfileValidator {

    private final UserPort userPort;
    private final OrganizePort organizePort;
    private final RepositoryPort repositoryPort;

    public Username validateUsername(String username) {
        try {
            return Username.from(username);
        } catch (IllegalArgumentException ex) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.BAD_REQUEST,
                    "Username allows only letters, numbers, dot, hyphen, or underscore.");
        }
    }

    public void validateUsernameNotTaken(Username requested, Long userId) {
        userPort.findByUsername(requested.getValue())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.USERNAME_ALREADY_EXISTS, "Username already exists");
                });
    }

    public void validateOrganizeNameNotTakenIfCompatible(Username requested) {
        if (!requested.isOrganizeNameCompatible()) {
            return;
        }
        organizePort.findByName(OrganizeName.from(requested.getValue()))
                .ifPresent(existing -> {
                    throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.ORGANIZE_ALREADY_EXISTS, "Namespace already exists");
                });
    }

    public void validateUserHasNoRepositories(Long userId) {
        long count = repositoryPort.countByOwner(OwnerType.USER, OwnerId.of(userId));
        if (count > 0) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.BAD_REQUEST, "Cannot rename user with existing repositories");
        }
    }
}

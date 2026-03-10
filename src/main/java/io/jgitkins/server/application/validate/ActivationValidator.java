package io.jgitkins.server.application.validate;

import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.error.DomainErrorCode;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.domain.model.vo.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivationValidator {

    private final UserPort userPort;
    private final OrganizePort organizePort;
    private final RepositoryPort repositoryPort;

    public Username validateUsername(String username) {
        return Username.from(username);
    }

    public void validateUsernameNotTaken(Username requested, Long userId) {
        userPort.findByUsername(requested.getValue())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new JgitkinsException(
                            io.jgitkins.server.application.common.error.ApplicationErrorCode.USERNAME_ALREADY_EXISTS,
                            "Username already exists");
                });
    }

    public void validateOrganizeNameNotTakenIfCompatible(Username requested) {
        if (!requested.isOrganizeNameCompatible()) {
            return;
        }
        organizePort.findByName(OrganizeName.from(requested.getValue()))
                .ifPresent(existing -> {
                    throw new JgitkinsException(
                            io.jgitkins.server.application.common.error.ApplicationErrorCode.ORGANIZE_ALREADY_EXISTS,
                            "Namespace already exists");
                });
    }

    public void validateUserHasNoRepositories(Long userId) {
        long count = repositoryPort.countByOwner(OwnerType.USER, OwnerId.of(userId));
        if (count > 0) {
            throw new JgitkinsException(DomainErrorCode.RULE_VIOLATION,
                    "Cannot rename user with existing repositories");
        }
    }
}

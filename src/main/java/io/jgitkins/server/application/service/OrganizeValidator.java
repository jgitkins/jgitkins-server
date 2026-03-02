package io.jgitkins.server.application.service;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import io.jgitkins.server.domain.model.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizeValidator {

    private final OrganizePort organizePort;
    private final UserPort userPort;
    private final OrganizeMemberPort organizeMemberPort;

    public void validateCreation(OrganizeName name) {
        if (organizePort.findByName(name).isPresent()) {
            throw new JgitkinsException(ApplicationErrorCode.ORGANIZE_ALREADY_EXISTS,
                    "Organize name already exists: " + name.getValue());
        }

        if (userPort.findByUsername(name.getValue()).isPresent()) {
            throw new JgitkinsException(ApplicationErrorCode.ORGANIZE_ALREADY_EXISTS,
                    "Namespace already exists: " + name.getValue());
        }
    }

    public Organize findByIdOrThrow(Long organizeId) {
        return organizePort.findById(OrganizeId.of(organizeId))
                .orElseThrow(() -> new JgitkinsException(ApplicationErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + organizeId));
    }

    public boolean isAccessible(Organize organize, UserId userId) {
        if (organize == null || userId == null) {
            return false;
        }
        return userId.equals(organize.getOwnerId()) ||
                organizeMemberPort.existsByOrganizeAndUser(organize.getId(), userId);
    }
}

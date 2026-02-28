package io.jgitkins.server.application.service;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.infrastructure.support.RepositoryPathHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RepositoryNamespaceResolver {

    private final OrganizePort organizePort;
    private final UserPort userPort;

    public String resolve(Repository repository) {
        if (repository == null) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.REPOSITORY_NOT_FOUND, "Repository not found");
        }
        OwnerType ownerType = repository.getOwnerType();
        OwnerId ownerId = repository.getOwnerId();
        return resolve(ownerType, ownerId);
    }

    public String resolve(OwnerType ownerType, OwnerId ownerId) {
        if (ownerType == null || ownerId == null) {
            throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.REPOSITORY_NOT_FOUND, "Repository owner context missing");
        }
        if (ownerType == OwnerType.ORGANIZATION) {
            String organizeName = organizePort.findById(OrganizeId.of(ownerId.getValue()))
                    .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.ORGANIZE_NOT_FOUND,
                            "Organize not found: " + ownerId.getValue()))
                    .getName()
                    .getValue();
            return RepositoryPathHelper.buildOrganizeNamespace(organizeName);
        }
        if (ownerType == OwnerType.USER) {
            String username = userPort.findById(ownerId.getValue())
                    .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.USER_NOT_FOUND,
                            "User not found: " + ownerId.getValue()))
                    .getUsername();
            return RepositoryPathHelper.buildUserNamespace(username);
        }
        throw new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.REPOSITORY_NOT_FOUND, "Repository owner context missing");
    }
}

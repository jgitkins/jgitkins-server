package io.jgitkins.server.application.service;

import io.jgitkins.server.application.common.ErrorCode;
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
            throw new JgitkinsException(ErrorCode.REPOSITORY_NOT_FOUND, "Repository not found");
        }
        OwnerType ownerType = repository.getOwnerType();
        OwnerId ownerId = repository.getOwnerId();
        return resolve(ownerType, ownerId);
    }

    public String resolve(OwnerType ownerType, OwnerId ownerId) {
        if (ownerType == null || ownerId == null) {
            throw new JgitkinsException(ErrorCode.REPOSITORY_NOT_FOUND, "Repository owner context missing");
        }
        if (ownerType == OwnerType.ORGANIZATION) {
            String organizeName = organizePort.findById(OrganizeId.of(ownerId.getValue()))
                    .orElseThrow(() -> new JgitkinsException(ErrorCode.ORGANIZE_NOT_FOUND,
                            "Organize not found: " + ownerId.getValue()))
                    .getName()
                    .getValue();
            return RepositoryPathHelper.buildOrganizeNamespace(organizeName);
        }
        if (ownerType == OwnerType.USER) {
            String username = userPort.findById(ownerId.getValue())
                    .orElseThrow(() -> new JgitkinsException(ErrorCode.BAD_REQUEST,
                            "User not found: " + ownerId.getValue()))
                    .getUsername();
            return RepositoryPathHelper.buildUserNamespace(username);
        }
        throw new JgitkinsException(ErrorCode.REPOSITORY_NOT_FOUND, "Repository owner context missing");
    }
}

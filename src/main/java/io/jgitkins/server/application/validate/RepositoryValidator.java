package io.jgitkins.server.application.validate;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.UserId;
import io.jgitkins.server.presentation.common.error.PresentationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepositoryValidator {

    private final RepositoryPort repositoryPort;
    private final OrganizeMemberPort organizeMemberPort;
    private final CurrentUserPort currentUserPort;

    public void validateCreation(OwnerType ownerType, Long organizeId, RepositoryName repositoryName) {
        validateOwnership(ownerType, organizeId);
        OwnerId ownerId = resolveOwnerId(ownerType, organizeId);
        validateRepositoryNameUnique(ownerType, ownerId, repositoryName);
    }

    public void validateRepositoryNameUnique(OwnerType ownerType, OwnerId ownerId, RepositoryName name) {
        repositoryPort.findByOwnerAndName(ownerType, ownerId, name)
                .ifPresent(existing -> {
                    throw new JgitkinsException(ApplicationErrorCode.REPOSITORY_ALREADY_EXISTS,
                            "Repository name already exists for owner: " + name.getValue());
                });
    }

    public void validateOwnership(OwnerType ownerType, Long organizeId) {
        if (ownerType == OwnerType.USER) {
            if (organizeId != null) {
                throw new JgitkinsException(ApplicationErrorCode.INVALID_OWNER_CONTEXT,
                        "organizeId must be null when ownerType is USER.");
            }
            requireCurrentUserId();
            return;
        }

        if (organizeId == null) {
            throw new JgitkinsException(ApplicationErrorCode.INVALID_OWNER_CONTEXT,
                    "organizeId is required when ownerType is ORGANIZATION.");
        }
        assertOrganizeMembership(organizeId);
    }

    public void enforceDeletionPermission(Repository repository) {
        if (repository.getOwnerType() != OwnerType.USER
                || repository.getOwnerId() == null
                || repository.getOwnerId().getValue() == null) {
            return;
        }
        Long requesterId = requireCurrentUserId();
        if (!repository.getOwnerId().getValue().equals(requesterId)) {
            throw new JgitkinsException(ApplicationErrorCode.REPOSITORY_ACCESS_DENIED, "Cannot delete another user's repository");
        }
    }

    public Long requireCurrentUserId() {
        return currentUserPort.currentUserId()
                .orElseThrow(() -> new JgitkinsException(PresentationErrorCode.UNAUTHORIZED, "Unauthenticated"));
    }

    private void assertOrganizeMembership(Long organizeId) {
        Long requesterId = requireCurrentUserId();
        boolean isMember = organizeMemberPort.existsByOrganizeAndUser(OrganizeId.of(organizeId),
                                                                      UserId.of(requesterId));
        if (!isMember) {
            throw new JgitkinsException(ApplicationErrorCode.ORGANIZE_ACCESS_DENIED, "User is not a member of the organization.");
        }
    }

    private OwnerId resolveOwnerId(OwnerType ownerType, Long organizeId) {
        if (ownerType == OwnerType.ORGANIZATION) {
            return OwnerId.of(organizeId);
        }
        return OwnerId.of(requireCurrentUserId());
    }
}

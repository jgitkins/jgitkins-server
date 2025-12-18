package io.jgitkins.server.domain.aggregate;

import io.jgitkins.server.domain.event.OrganizeCreatedEvent;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import io.jgitkins.server.domain.model.vo.OrganizePath;
import io.jgitkins.server.domain.model.vo.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Organize Aggregate Root
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Organize extends AbstractAggregateRoot<OrganizeId> {

    private final OrganizeId id;
    private final OrganizeName name;
    private final OrganizePath path;
    private final String description;
    private final UserId ownerId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static Organize create(String name,
                                  String path,
                                  Long ownerId,
                                  String description) {
        LocalDateTime now = LocalDateTime.now();
        Organize organize = new Organize(
                null,
                OrganizeName.from(name),
                OrganizePath.from(path),
                normalizeDescription(description),
                ownerId != null ? UserId.of(ownerId) : null,
                now,
                now
        );
        organize.registerEvent(OrganizeCreatedEvent.from(organize));
        return organize;
    }

    public Organize updateMetadata(String newName,
                                   String newPath,
                                   Long newOwnerId,
                                   String newDescription) {
        Organize updated = new Organize(
                id,
                newName != null ? OrganizeName.from(newName) : name,
                newPath != null ? OrganizePath.from(newPath) : path,
                newDescription != null ? newDescription.trim() : description,
                newOwnerId != null ? UserId.of(newOwnerId) : ownerId,
                createdAt,
                LocalDateTime.now()
        );
        updated.copyDomainEventsFrom(this);
        return updated;
    }

    public Organize withIdentity(OrganizeId organizeId,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt) {
        Organize identified = new Organize(organizeId,
                                           name,
                                           path,
                                           description,
                                           ownerId,
                                           createdAt != null ? createdAt : this.createdAt,
                                           updatedAt != null ? updatedAt : this.updatedAt);
        identified.copyDomainEventsFrom(this);
        return identified;
    }

    public static Organize reconstruct(OrganizeId id,
                                       OrganizeName name,
                                       OrganizePath path,
                                       String description,
                                       UserId ownerId,
                                       LocalDateTime createdAt,
                                       LocalDateTime updatedAt) {
        return new Organize(id, name, path, description, ownerId, createdAt, updatedAt);
    }

    private static String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }
}

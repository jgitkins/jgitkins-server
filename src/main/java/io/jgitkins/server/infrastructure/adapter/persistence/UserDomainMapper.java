package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.infrastructure.persistence.model.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDomainMapper {

    UserEntity toEntity(User user);

    default User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.rehydrate(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getAvatarUrl(),
                entity.getStatus(),
                entity.getLastLoginAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

package io.jgitkins.server.infrastructure.mapper;

import io.jgitkins.server.domain.model.UserCredential;
import io.jgitkins.server.infrastructure.persistence.model.UserCredentialsEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserCredentialDomainMapper {

    UserCredentialsEntity toEntity(UserCredential credential);

    default UserCredential toDomain(UserCredentialsEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserCredential.rehydrate(
                entity.getId(),
                entity.getUserId(),
                entity.getProvider(),
                entity.getPasswordHash(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

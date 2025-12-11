package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.BranchName;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.domain.model.vo.RepositoryVisibility;
import io.jgitkins.server.domain.model.vo.UserId;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepositoryDomainMapper {

    @Mapping(target = "id", expression = "java(repository.getId() != null ? repository.getId().getValue() : null)")
    @Mapping(target = "organizeId", expression = "java(repository.getOrganizeId().getValue())")
    @Mapping(target = "name", expression = "java(repository.getName().getValue())")
    @Mapping(target = "path", expression = "java(repository.getPath().getValue())")
    @Mapping(target = "defaultBranch", expression = "java(repository.getDefaultBranch().getValue())")
    @Mapping(target = "visibility", expression = "java(repository.getVisibility().name())")
    @Mapping(target = "ownerId", expression = "java(repository.getOwnerId() != null ? repository.getOwnerId().getValue() : null)")
    @Mapping(target = "status", expression = "java(repository.getLastSyncedAt() != null ? \"ACTIVE\" : \"REGISTERED\")")
    @Mapping(target = "clonePath", expression = "java(repository.getClonePath())")
    RepositoryEntity toEntity(Repository repository);

    default Repository toDomain(RepositoryEntity entity) {
        return Repository.rehydrate(
                RepositoryId.of(entity.getId()),
                OrganizeId.of(entity.getOrganizeId()),
                RepositoryName.from(entity.getName()),
                RepositoryPath.from(entity.getPath()),
                BranchName.of(entity.getDefaultBranch()),
                RepositoryVisibility.from(entity.getVisibility()),
                entity.getRepositoryType(),
                entity.getOwnerId() != null ? UserId.of(entity.getOwnerId()) : null,
                entity.getDescription(),
                entity.getClonePath(),
                entity.getCredentialId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastSyncedAt()
        );
    }
}

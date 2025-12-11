package io.jgitkins.server.application.mapper;

import io.jgitkins.server.application.common.CloneUrlBuilder;
import io.jgitkins.server.application.dto.RepositoryResult;
import io.jgitkins.server.domain.aggregate.Repository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class RepositoryApplicationMapper {

    @Autowired
    protected CloneUrlBuilder cloneUrlBuilder;

    @Mapping(target = "id", expression = "java(repository.getId() != null ? repository.getId().getValue() : null)")
    @Mapping(target = "organizeId", expression = "java(repository.getOrganizeId().getValue())")
    @Mapping(target = "name", expression = "java(repository.getName().getValue())")
    @Mapping(target = "path", expression = "java(repository.getPath().getValue())")
    @Mapping(target = "defaultBranch", expression = "java(repository.getDefaultBranch().getValue())")
    @Mapping(target = "visibility", expression = "java(repository.getVisibility().name())")
    @Mapping(target = "repositoryType", expression = "java(repository.getRepositoryType())")
    @Mapping(target = "ownerId", expression = "java(repository.getOwnerId() != null ? repository.getOwnerId().getValue() : null)")
    @Mapping(target = "cloneUrl", expression = "java(cloneUrlBuilder.build(repository.getClonePath()))")
    public abstract RepositoryResult toDto(Repository repository);
}

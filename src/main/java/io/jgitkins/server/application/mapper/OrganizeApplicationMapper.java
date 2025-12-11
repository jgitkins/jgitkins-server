package io.jgitkins.server.application.mapper;

import io.jgitkins.server.application.dto.OrganizeResult;
import io.jgitkins.server.domain.aggregate.Organize;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizeApplicationMapper {

    @Mapping(target = "id", expression = "java(organize.getId() != null ? organize.getId().getValue() : null)")
    @Mapping(target = "name", expression = "java(organize.getName().getValue())")
    @Mapping(target = "path", expression = "java(organize.getPath().getValue())")
    @Mapping(target = "ownerId", expression = "java(organize.getOwnerId() != null ? organize.getOwnerId().getValue() : null)")
    OrganizeResult toDto(Organize organize);
}

package io.jgitkins.server.presentation.mapper;

import io.jgitkins.server.application.dto.CreateRepositoryCommand;
import io.jgitkins.server.application.dto.UpdateRepositoryCommand;
import io.jgitkins.server.presentation.dto.CreateRepositoryRequest;
import io.jgitkins.server.presentation.dto.UpdateRepositoryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreateRepositoryMapper {

    @Mapping(source = "username", target = "authorName")
    @Mapping(source = "email", target = "authorEmail")
    CreateRepositoryCommand toCommand(CreateRepositoryRequest request);

    UpdateRepositoryCommand toUpdateCommand(UpdateRepositoryRequest request);
}

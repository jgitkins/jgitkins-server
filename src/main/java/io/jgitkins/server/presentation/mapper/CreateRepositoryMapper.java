package io.jgitkins.server.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import io.jgitkins.server.application.dto.CreateRepositoryCommand;
import io.jgitkins.server.presentation.dto.CreateRepositoryRequest;

@Mapper(componentModel = "spring")
public interface CreateRepositoryMapper {

    @Mapping(source = "username", target = "authorName")
    @Mapping(source = "email", target = "authorEmail")
    CreateRepositoryCommand toCommand(CreateRepositoryRequest request);
}


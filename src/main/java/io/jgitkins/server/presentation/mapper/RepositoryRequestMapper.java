package io.jgitkins.server.presentation.mapper;

import io.jgitkins.server.application.dto.command.RepositoryCreateCommand;
import io.jgitkins.server.application.dto.command.UpdateRepositoryCommand;
import io.jgitkins.server.presentation.dto.RepositoryCreateRequest;
import io.jgitkins.server.presentation.dto.RepositoryUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepositoryRequestMapper {

    @Mapping(source = "username", target = "authorName")
    @Mapping(source = "email", target = "authorEmail")
    RepositoryCreateCommand toCommand(RepositoryCreateRequest request);

    UpdateRepositoryCommand toUpdateCommand(RepositoryUpdateRequest request);
}

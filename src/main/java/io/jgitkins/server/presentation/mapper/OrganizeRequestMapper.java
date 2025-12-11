package io.jgitkins.server.presentation.mapper;

import io.jgitkins.server.application.dto.CreateOrganizeCommand;
import io.jgitkins.server.application.dto.UpdateOrganizeCommand;
import io.jgitkins.server.presentation.dto.CreateOrganizeRequest;
import io.jgitkins.server.presentation.dto.UpdateOrganizeRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizeRequestMapper {

    CreateOrganizeCommand toCommand(CreateOrganizeRequest request);

    UpdateOrganizeCommand toCommand(UpdateOrganizeRequest request);
}

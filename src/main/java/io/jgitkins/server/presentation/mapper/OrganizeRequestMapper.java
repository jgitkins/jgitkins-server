package io.jgitkins.server.presentation.mapper;

import io.jgitkins.server.application.dto.OrganizeCreationCommand;
import io.jgitkins.server.application.dto.UpdateOrganizeCommand;
import io.jgitkins.server.presentation.dto.OrganizeCreationRequest;
import io.jgitkins.server.presentation.dto.UpdateOrganizeRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizeRequestMapper {

    OrganizeCreationCommand toCommand(OrganizeCreationRequest request);

    UpdateOrganizeCommand toCommand(UpdateOrganizeRequest request);
}

package io.jgitkins.server.presentation.mapper;

import io.jgitkins.server.application.dto.RunnerRegisterCommand;
import io.jgitkins.server.application.dto.RunnerRegistrationResult;
import io.jgitkins.server.presentation.dto.RunnerRegistrationRequest;
import io.jgitkins.server.presentation.dto.RunnerRegistrationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RunnerRegistrationMapper {

    @Mapping(target = "description", source = "request.description")
//    @Mapping(target = "ipAddress", source = "resolvedIpAddress")
    RunnerRegisterCommand toCommand(RunnerRegistrationRequest request);

    RunnerRegistrationResponse toResponse(RunnerRegistrationResult result);
}

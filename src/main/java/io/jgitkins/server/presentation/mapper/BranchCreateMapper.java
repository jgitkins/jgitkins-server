package io.jgitkins.server.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import io.jgitkins.server.application.dto.BranchCreateCommand;
import io.jgitkins.server.presentation.dto.BranchCreateRequest;

@Mapper(componentModel = "spring")
public interface BranchCreateMapper {

    @Mapping(target = "taskCd", source = "taskCd")
    @Mapping(target = "repoName", source = "repoName")
    @Mapping(target = "branchName", source = "request.branchName")
    @Mapping(target = "sourceBranch", source = "request.sourceBranch")
    @Mapping(target = "physicalCreationRequired", constant = "true")
    BranchCreateCommand toCommand(String taskCd, String repoName, BranchCreateRequest request);
}


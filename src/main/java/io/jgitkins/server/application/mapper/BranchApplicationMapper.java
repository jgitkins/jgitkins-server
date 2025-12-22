package io.jgitkins.server.application.mapper;

import io.jgitkins.server.application.dto.result.BranchSearchResult;
import io.jgitkins.server.domain.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BranchApplicationMapper {

    @Mapping(target = "repositoryId", source = "repositoryId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "locked", source = "locked")
    @Mapping(target = "ciEnabled", source = "ciEnabled")
    BranchSearchResult toSearchResult(Branch branch);
}

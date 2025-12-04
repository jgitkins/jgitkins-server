package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.domain.model.Job;
import io.jgitkins.server.infrastructure.persistence.model.JobEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "repositoryId", expression = "java(job.getRepositoryId().getValue())")
    @Mapping(target = "commitHash", expression = "java(job.getCommitHash().getValue())")
    @Mapping(target = "branchName", expression = "java(job.getBranchName().getValue())")
    @Mapping(target = "triggeredBy", expression = "java(job.getTriggeredBy().getValue())")
    @Mapping(target = "createdAt", source = "createdAt")
    JobEntity toEntity(Job job);
}

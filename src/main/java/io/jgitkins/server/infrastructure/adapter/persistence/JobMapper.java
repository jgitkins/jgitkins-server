package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.domain.model.Job;
import io.jgitkins.server.domain.model.JobHistory;
import io.jgitkins.server.domain.model.vo.RunnerId;
import io.jgitkins.server.infrastructure.persistence.model.JobEntity;
import io.jgitkins.server.infrastructure.persistence.model.JobHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mapper(componentModel = "spring")
public interface JobMapper {

    Logger LOG = LoggerFactory.getLogger(JobMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "repositoryId", expression = "java(job.getRepositoryId().getValue())")
    @Mapping(target = "commitHash", expression = "java(job.getCommitHash().getValue())")
    @Mapping(target = "branchName", expression = "java(job.getBranchName().getValue())")
    @Mapping(target = "triggeredBy", expression = "java(job.getTriggeredBy().getValue())")
    @Mapping(target = "createdAt", source = "createdAt")
    JobEntity toEntity(Job job);

    default JobHistoryEntity toHistoryEntity(JobHistory history, Long jobId) {
        JobHistoryEntity entity = new JobHistoryEntity();
        entity.setJobId(jobId);
        entity.setStatus(history.getStatus().name());
        entity.setCreatedAt(history.getCreatedAt());
        entity.setRunnerId(convertRunnerId(history.getRunnerId()));
        return entity;
    }

    private Long convertRunnerId(RunnerId runnerId) {
        if (runnerId == null) {
            return null;
        }
        String runnerValue = runnerId.getValue();
        if (runnerValue == null || runnerValue.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(runnerValue);
        } catch (NumberFormatException ex) {
            LOG.warn("RunnerId [{}] is not numeric. runner_id will be stored as null.", runnerValue);
            return null;
        }
    }
}

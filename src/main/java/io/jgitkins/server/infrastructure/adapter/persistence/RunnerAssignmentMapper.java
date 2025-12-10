package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.domain.model.Runner;
import io.jgitkins.server.infrastructure.persistence.model.RunnerAssignmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RunnerAssignmentMapper {

    @Mapping(target = "runnerId", expression = "java(runner.getId())")
    @Mapping(target = "targetType", expression = "java(runner.getScopeType().name())")
    @Mapping(target = "targetId", expression = "java(runner.getScopeType().requiresTargetId() ? runner.getScopeTargetId() : null)")
    @Mapping(target = "assignedAt", expression = "java(java.time.LocalDateTime.now())")
    RunnerAssignmentEntity toEntity(Runner runner);
}

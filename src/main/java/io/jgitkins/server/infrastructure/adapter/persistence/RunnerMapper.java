package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.domain.model.Runner;
import io.jgitkins.server.domain.model.vo.RunnerStatus;
import io.jgitkins.server.infrastructure.persistence.model.RunnerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RunnerMapper {

    @Mapping(target = "status", expression = "java(runner.getStatus().name())")
    RunnerEntity toEntity(Runner runner);

    default Runner toDomain(RunnerEntity entity) {
        return Runner.restore(entity.getId(),
                              entity.getToken(),
                              entity.getDescription(),
                              RunnerStatus.valueOf(entity.getStatus()),
//                              entity.getIpAddress(),
                              entity.getLastHeartbeatAt(),
                              entity.getCreatedAt());
    }
}

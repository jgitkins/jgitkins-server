package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.RunnerCommandPort;
import io.jgitkins.server.application.port.out.RunnerQueryPort;
import io.jgitkins.server.domain.model.Runner;
import io.jgitkins.server.infrastructure.persistence.mapper.RunnerEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.RunnerEntity;
import io.jgitkins.server.infrastructure.persistence.model.RunnerEntityCondition;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RunnerMybatisAdapter implements RunnerCommandPort, RunnerQueryPort {

    private final RunnerEntityMbgMapper runnerEntityMbgMapper;
    private final RunnerMapper runnerMapper;

    @Override
    public Runner save(Runner runner) {
        RunnerEntity entity = runnerMapper.toEntity(runner);
        runnerEntityMbgMapper.insertSelective(entity);
        return runnerMapper.toDomain(entity);
    }

    @Override
    public Optional<Runner> findById(Long runnerId) {
        RunnerEntity entity = runnerEntityMbgMapper.selectByPrimaryKey(runnerId);
        return Optional.ofNullable(entity).map(runnerMapper::toDomain);
    }

    @Override
    public List<Runner> findAll() {
        List<RunnerEntity> entities = runnerEntityMbgMapper.selectByCondition(new RunnerEntityCondition());
        return entities.stream()
                       .map(runnerMapper::toDomain)
                       .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long runnerId) {
        runnerEntityMbgMapper.deleteByPrimaryKey(runnerId);
    }

    @Override
    public Runner update(Runner runner) {
        RunnerEntity entity = runnerMapper.toEntity(runner);
        runnerEntityMbgMapper.updateByPrimaryKeySelective(entity);
        RunnerEntity updated = runnerEntityMbgMapper.selectByPrimaryKey(runner.getId());
        return runnerMapper.toDomain(updated);
    }
}

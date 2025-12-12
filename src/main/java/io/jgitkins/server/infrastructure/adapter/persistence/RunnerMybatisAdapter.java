package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.RunnerCommandPort;
import io.jgitkins.server.application.port.out.RunnerQueryPort;
import io.jgitkins.server.domain.aggregate.Runner;
import io.jgitkins.server.domain.model.vo.RunnerScopeType;
import io.jgitkins.server.infrastructure.persistence.mapper.RunnerAssignmentEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.RunnerEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.RunnerAssignmentEntity;
import io.jgitkins.server.infrastructure.persistence.model.RunnerAssignmentEntityCondition;
import io.jgitkins.server.infrastructure.persistence.model.RunnerEntity;
import io.jgitkins.server.infrastructure.persistence.model.RunnerEntityCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RunnerMybatisAdapter implements RunnerCommandPort, RunnerQueryPort {

    private final RunnerEntityMbgMapper runnerEntityMbgMapper;
    private final RunnerAssignmentEntityMbgMapper runnerAssignmentEntityMbgMapper;
    private final RunnerMapper runnerMapper;
    private final RunnerAssignmentMapper runnerAssignmentMapper;

    @Override
    public Runner save(Runner runner) {
        RunnerEntity entity = runnerMapper.toEntity(runner);

        if (runner.getId() == null) {
            runnerEntityMbgMapper.insertSelective(entity);
            runnerAssignmentEntityMbgMapper.insertSelective(runnerAssignmentMapper.toEntity(runner));
            return runnerMapper.toDomain(entity, runner.getScopeType(), runner.getScopeTargetId());
        } else {
            runnerEntityMbgMapper.updateByPrimaryKeySelective(entity);
            runnerAssignmentEntityMbgMapper.updateByPrimaryKeySelective(runnerAssignmentMapper.toEntity(runner));
            RunnerEntity updated = runnerEntityMbgMapper.selectByPrimaryKey(runner.getId());
            return restoreRunner(updated);
        }
    }

    @Override
    public Optional<Runner> findById(Long runnerId) {
        RunnerEntity entity = runnerEntityMbgMapper.selectByPrimaryKey(runnerId);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(restoreRunner(entity));
    }

    @Override
    public Optional<Runner> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        RunnerEntityCondition condition = new RunnerEntityCondition();
        condition.createCriteria().andTokenEqualTo(token);
        condition.setOrderByClause("id DESC LIMIT 1");
        List<RunnerEntity> entities = runnerEntityMbgMapper.selectByCondition(condition);
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(restoreRunner(entities.get(0)));
    }

    @Override
    public List<Runner> findAll() {
        List<RunnerEntity> entities = runnerEntityMbgMapper.selectByCondition(new RunnerEntityCondition());
        return entities.stream()
                       .map(this::restoreRunner)
                       .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long runnerId) {
        deleteAssignment(runnerId);
        runnerEntityMbgMapper.deleteByPrimaryKey(runnerId);
    }

    @Override
    public Runner update(Runner runner) {
        RunnerEntity entity = runnerMapper.toEntity(runner);
        runnerEntityMbgMapper.updateByPrimaryKeySelective(entity);
        RunnerEntity updated = runnerEntityMbgMapper.selectByPrimaryKey(runner.getId());
        return restoreRunner(updated);
    }

    private void persistAssignment(Runner runner) {
        RunnerAssignmentEntity assignment = runnerAssignmentMapper.toEntity(runner);
        runnerAssignmentEntityMbgMapper.insertSelective(assignment);
    }

    private void deleteAssignment(Long runnerId) {
        RunnerAssignmentEntityCondition condition = new RunnerAssignmentEntityCondition();
        condition.createCriteria().andRunnerIdEqualTo(runnerId);
        runnerAssignmentEntityMbgMapper.deleteByCondition(condition);
    }

    private Runner restoreRunner(RunnerEntity entity) {
        RunnerAssignmentEntity assignment = fetchAssignment(entity.getId());
        RunnerScopeType scopeType = assignment != null ? RunnerScopeType.valueOf(assignment.getTargetType()) : RunnerScopeType.GLOBAL;
        Long targetId = assignment != null ? assignment.getTargetId() : null;
        return runnerMapper.toDomain(entity, scopeType, targetId);
    }

    private RunnerAssignmentEntity fetchAssignment(Long runnerId) {
        RunnerAssignmentEntityCondition condition = new RunnerAssignmentEntityCondition();
        condition.createCriteria().andRunnerIdEqualTo(runnerId);
        condition.setOrderByClause("assigned_at desc");
        List<RunnerAssignmentEntity> assignments = runnerAssignmentEntityMbgMapper.selectByCondition(condition);
        if (assignments.isEmpty()) {
            return null;
        }
        return assignments.get(0);
    }
}

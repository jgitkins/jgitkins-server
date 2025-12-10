package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.dto.RunnerAssignmentCandidate;
import io.jgitkins.server.application.port.out.RunnerAllocationPort;
import io.jgitkins.server.domain.model.vo.RunnerStatus;
import io.jgitkins.server.infrastructure.persistence.mapper.RunnerAssignmentEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.RunnerEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.RunnerAssignmentEntity;
import io.jgitkins.server.infrastructure.persistence.model.RunnerAssignmentEntityCondition;
import io.jgitkins.server.infrastructure.persistence.model.RunnerEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RunnerAllocationMybatisAdapter implements RunnerAllocationPort {

    private static final String TARGET_REPOSITORY = "REPOSITORY";
    private static final String TARGET_ORGANIZE = "ORGANIZE";
    private static final String TARGET_GLOBAL = "GLOBAL";

    private final RunnerAssignmentEntityMbgMapper runnerAssignmentEntityMbgMapper;
    private final RunnerEntityMbgMapper runnerEntityMbgMapper;

    @Override
    public List<RunnerAssignmentCandidate> findRunnableAssignments() {
        List<RunnerAssignmentCandidate> candidates = new ArrayList<>();
        candidates.addAll(findRunnersForTarget(TARGET_REPOSITORY));
        candidates.addAll(findRunnersForTarget(TARGET_ORGANIZE));
        candidates.addAll(findRunnersForTarget(TARGET_GLOBAL));
        return candidates;
    }

    private List<RunnerAssignmentCandidate> findRunnersForTarget(String targetType) {
        RunnerAssignmentEntityCondition condition = new RunnerAssignmentEntityCondition();
        condition.createCriteria()
                .andTargetTypeEqualTo(targetType);
        condition.setOrderByClause("assigned_at ASC");

        List<RunnerAssignmentEntity> assignments = runnerAssignmentEntityMbgMapper.selectByCondition(condition);
        List<RunnerAssignmentCandidate> runnable = new ArrayList<>();
        for (RunnerAssignmentEntity assignment : assignments) {
            RunnerEntity runnerEntity = runnerEntityMbgMapper.selectByPrimaryKey(assignment.getRunnerId());
            if (isRunnable(runnerEntity)) {
                runnable.add(RunnerAssignmentCandidate.builder()
                                                      .runnerId(runnerEntity.getId())
                                                      .targetType(targetType)
                                                      .targetId(assignment.getTargetId())
                                                      .build());
            }
        }
//        if (runnable.isEmpty()) {
//            log.debug("No runnable runner found for targetType={}", targetType);
//        }
        return runnable;
    }

    private boolean isRunnable(RunnerEntity entity) {
        return entity != null && RunnerStatus.ONLINE.name().equals(entity.getStatus());
    }
}

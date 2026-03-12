package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.dto.DispatchableJob;
import io.jgitkins.server.application.dto.JobDispatchScope;
import io.jgitkins.server.application.dto.RunnerDispatchContext;
import io.jgitkins.server.application.port.out.JobPersistencePort;
import io.jgitkins.server.domain.aggregate.Job;
import io.jgitkins.server.domain.model.JobHistory;
import io.jgitkins.server.domain.model.vo.JobStatus;
import io.jgitkins.server.infrastructure.common.error.InfrastructureErrorCode;
import io.jgitkins.server.infrastructure.exception.InfrastructureException;
import io.jgitkins.server.infrastructure.mapper.JobDomainMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.JobEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.JobHistoryEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.RepositoryEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobPersistenceAdapter implements JobPersistencePort {

    private final JobEntityMbgMapper jobEntityMbgMapper;
    private final JobHistoryEntityMbgMapper jobHistoryEntityMbgMapper;
    private final RepositoryEntityMbgMapper repositoryEntityMbgMapper;
    private final JobDomainMapper jobDomainMapper;

    @Override
    @Transactional
    public void save(Job job) {
        try {
            JobEntity entity = jobDomainMapper.toEntity(job);
            jobEntityMbgMapper.insertSelective(entity);

            Long generatedId = entity.getId();
            for (JobHistory history : job.getHistories()) {
                JobHistoryEntity historyEntity = jobDomainMapper.toHistoryEntity(history, generatedId);
                jobHistoryEntityMbgMapper.insertSelective(historyEntity);
            }
        } catch (Exception e) {
            throw new InfrastructureException(InfrastructureErrorCode.PERSISTENCE_OPERATION_FAILED,
                    "Database operation failed during job creation", e);
        }
    }

    @Override
    public Optional<Job> findById(Long jobId) {
        try {
            JobEntity entity = jobEntityMbgMapper.selectByPrimaryKey(jobId);
            if (entity == null) {
                return Optional.empty();
            }

            JobHistoryEntityCondition historyCondition = new JobHistoryEntityCondition();
            historyCondition.createCriteria().andJobIdEqualTo(jobId);
            historyCondition.setOrderByClause("CREATED_AT ASC");

            List<JobHistory> histories = jobHistoryEntityMbgMapper.selectByCondition(historyCondition).stream()
                    .map(jobDomainMapper::toHistoryDomain)
                    .toList();

            return Optional.ofNullable(jobDomainMapper.toDomain(entity, histories));
        } catch (Exception e) {
            throw new InfrastructureException(InfrastructureErrorCode.PERSISTENCE_OPERATION_FAILED,
                    "Database operation failed during job loading", e);
        }
    }

    // TODO: findNextDispatchableJob 변경필요
    //  호출지에서 러너가 선전되었을 때, 해당 러너가 수행할 수 있는 Job을 꺼내는것이 목표
    //  Job의 상태는 PENDING 이어야하며,
    //  해당 Job에 대해 Runner 가 Accessable 해야함
    //  추가로 Race 관리를 Valkey를 통해 진행할 수 있는지 확인 필요
    @Override
    @Transactional
    public Optional<DispatchableJob> findNextDispatchableJob(RunnerDispatchContext context) {
        try {
            JobEntityCondition condition = new JobEntityCondition();

            List<JobEntity> jobs = jobEntityMbgMapper.selectByCondition(condition); // 모든 Job?
            for (JobEntity jobEntity : jobs) {
                JobHistoryEntityCondition historyCondition = new JobHistoryEntityCondition();
                historyCondition.createCriteria().andJobIdEqualTo(jobEntity.getId());
                historyCondition.setOrderByClause("CREATED_AT DESC");
                List<JobHistoryEntity> historyEntities = jobHistoryEntityMbgMapper.selectByCondition(historyCondition);

                if (!historyEntities.isEmpty() && JobStatus.PENDING.name().equals(historyEntities.get(0).getStatus())) {
                    RepositoryEntity repo = repositoryEntityMbgMapper.selectByPrimaryKey(jobEntity.getRepositoryId());
                    if (repo == null || !matchesScope(context, repo)) {
                        continue;
                    }

                    List<JobHistory> histories = historyEntities.stream()
                            .map(jobDomainMapper::toHistoryDomain)
                            .toList();

                    return Optional.of(DispatchableJob.builder()
                            .job(jobDomainMapper.toDomain(jobEntity, histories))
                            .organizeId("ORGANIZATION".equals(repo.getOwnerType()) ? repo.getOwnerId() : null)
                            .repositoryClonePath(repo.getClonePath())
                            .build());
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            throw new InfrastructureException(InfrastructureErrorCode.PERSISTENCE_OPERATION_FAILED,
                    "Database operation failed during fetching pending jobs", e);
        }
    }

    @Override
    @Transactional
    public Optional<Long> saveHistory(Job job, JobHistory previousHistory) {
        try {
            // Optimistic locking: check if the previous history is still the latest
            Long jobIdLong = Long.parseLong(job.getId().getValue());

            JobHistoryEntityCondition condition = new JobHistoryEntityCondition();
            condition.createCriteria()
                    .andJobIdEqualTo(jobIdLong)
                    .andStatusEqualTo(previousHistory.getStatus().name())
                    .andCreatedAtEqualTo(previousHistory.getCreatedAt());

            // If we were using seqNo, it would be better.

            long count = jobHistoryEntityMbgMapper.countByCondition(condition);
            if (count == 0) {
                return Optional.empty(); // Stale data
            }

            JobHistory latest = job.getLatestHistory();
            JobHistoryEntity entity = jobDomainMapper.toHistoryEntity(latest, jobIdLong);
            jobHistoryEntityMbgMapper.insertSelective(entity);

            return Optional.of(entity.getId());
        } catch (Exception e) {
            throw new InfrastructureException(InfrastructureErrorCode.PERSISTENCE_OPERATION_FAILED,
                    "Database operation failed during history persistence", e);
        }
    }

    private boolean matchesScope(RunnerDispatchContext context, RepositoryEntity repository) {
        if (context.getDispatchScope() == JobDispatchScope.GLOBAL) {
            return true;
        }
        if (context.getDispatchScope() == JobDispatchScope.ORGANIZE) {
            return "ORGANIZATION".equals(repository.getOwnerType())
                    && context.getScopeTargetId() != null
                    && context.getScopeTargetId().equals(repository.getOwnerId());
        }
        if (context.getDispatchScope() == JobDispatchScope.REPOSITORY) {
            return context.getScopeTargetId() != null
                    && context.getScopeTargetId().equals(repository.getId());
        }
        return false;
    }
}

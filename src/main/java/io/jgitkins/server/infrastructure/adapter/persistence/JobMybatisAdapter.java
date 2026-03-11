package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.dto.PendingJob;
import io.jgitkins.server.application.dto.RunnerAssignmentCandidate;
import io.jgitkins.server.application.port.out.JobPort;
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
public class JobMybatisAdapter implements JobPort {

    private final JobEntityMbgMapper jobEntityMbgMapper;
    private final JobHistoryEntityMbgMapper jobHistoryEntityMbgMapper;
    private final RepositoryEntityMbgMapper repositoryEntityMbgMapper;
    private final JobDomainMapper jobDomainMapper;

    @Override
    @Transactional
    public void create(Job job) {
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
    public Optional<Job> loadJob(Long jobId) {
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

    @Override
    @Transactional
    public Optional<PendingJob> fetchPendingJobFor(RunnerAssignmentCandidate candidate) {
        try {
            // 1. Find jobs with PENDING status.
            // Simplified: find jobs where the latest history is PENDING.
            // In highly concurrent environment, this would need more complex SQL or
            // locking.

            // This is a simplified implementation.
            JobEntityCondition condition = new JobEntityCondition();
            // Here we would filter by repositoryId or organizeId based on candidate scope.
            // For now, let's just find any PENDING job.

            List<JobEntity> jobs = jobEntityMbgMapper.selectByCondition(condition);
            for (JobEntity jobEntity : jobs) {
                JobHistoryEntityCondition historyCondition = new JobHistoryEntityCondition();
                historyCondition.createCriteria().andJobIdEqualTo(jobEntity.getId());
                historyCondition.setOrderByClause("CREATED_AT DESC");
                List<JobHistoryEntity> historyEntities = jobHistoryEntityMbgMapper.selectByCondition(historyCondition);

                if (!historyEntities.isEmpty() && JobStatus.PENDING.name().equals(historyEntities.get(0).getStatus())) {
                    // Check scope
                    RepositoryEntity repo = repositoryEntityMbgMapper.selectByPrimaryKey(jobEntity.getRepositoryId());
                    if (repo != null) {
                        // Logic to check if runner is assigned to this repo or its organization
                        // ...

                        List<JobHistory> histories = historyEntities.stream()
                                .map(jobDomainMapper::toHistoryDomain)
                                .toList();

                        return Optional.of(PendingJob.builder()
                                .job(jobDomainMapper.toDomain(jobEntity, histories))
                                .organizeId("ORGANIZATION".equals(repo.getOwnerType()) ? repo.getOwnerId() : null)
                                .repositoryClonePath(repo.getClonePath())
                                .build());
                    }
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
    public Optional<Long> persistHistory(Job job, JobHistory previousHistory) {
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
}

package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.dto.PendingJob;
import io.jgitkins.server.application.dto.RunnerAssignmentCandidate;
import io.jgitkins.server.application.port.out.JobQueuePort;
import io.jgitkins.server.domain.aggregate.Job;
import io.jgitkins.server.domain.model.JobHistory;
import io.jgitkins.server.domain.model.vo.BranchName;
import io.jgitkins.server.domain.model.vo.CommitHash;
import io.jgitkins.server.domain.model.vo.JobHistoryId;
import io.jgitkins.server.domain.model.vo.JobId;
import io.jgitkins.server.domain.model.vo.JobStatus;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RunnerId;
import io.jgitkins.server.domain.model.vo.SequenceNumber;
import io.jgitkins.server.domain.model.vo.SystemUser;
import io.jgitkins.server.domain.model.vo.UserId;
import io.jgitkins.server.infrastructure.persistence.mapper.JobEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.JobHistoryEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.RepositoryEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.JobEntity;
import io.jgitkins.server.infrastructure.persistence.model.JobEntityCondition;
import io.jgitkins.server.infrastructure.persistence.model.JobHistoryEntity;
import io.jgitkins.server.infrastructure.persistence.model.JobHistoryEntityCondition;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryEntity;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryEntityCondition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobQueueMybatisAdapter implements JobQueuePort {

    private static final String TARGET_REPOSITORY = "REPOSITORY";
    private static final String TARGET_ORGANIZE = "ORGANIZE";
    private static final String TARGET_GLOBAL = "GLOBAL";
    private static final int JOB_FETCH_LIMIT = 50;
    private static final int PENDING_SCAN_LIMIT = 20;

    private final JobHistoryEntityMbgMapper jobHistoryEntityMbgMapper;
    private final JobEntityMbgMapper jobEntityMbgMapper;
    private final RepositoryEntityMbgMapper repositoryEntityMbgMapper;

    @Override
    public Optional<PendingJob> fetchPendingJobFor(RunnerAssignmentCandidate candidate) {
        return switch (candidate.getTargetType()) {
            case TARGET_REPOSITORY -> fetchByRepository(candidate.getTargetId());
            case TARGET_ORGANIZE -> fetchByOrganize(candidate.getTargetId());
            case TARGET_GLOBAL -> fetchGlobal();
            default -> {
                log.warn("Unsupported targetType={} for runnerId={}", candidate.getTargetType(), candidate.getRunnerId());
                yield Optional.empty();
            }
        };
    }

    @Override
    public Optional<Long> persistHistory(Job job, JobHistory previousHistory) {
        Long jobId = parseJobId(job);
        if (jobId == null) {
            return Optional.empty();
        }

        JobHistoryEntity latestPersisted = findLatestHistory(jobId);
        if (latestPersisted == null) {
            return Optional.empty();
        }

        if (!latestPersisted.getStatus().equals(previousHistory.getStatus().name())) {
            return Optional.empty();
        }

        JobHistory latestHistory = job.getLatestHistory();
        JobHistoryEntity entity = new JobHistoryEntity();
        entity.setJobId(jobId);
        entity.setStatus(latestHistory.getStatus().name());
        entity.setRunnerId(parseRunnerId(latestHistory.getRunnerId()));
        entity.setStartedAt(latestHistory.getCreatedAt());
        entity.setCreatedAt(latestHistory.getCreatedAt());

        jobHistoryEntityMbgMapper.insertSelective(entity);
        return Optional.ofNullable(entity.getId());
    }

    @Override
    public Optional<Job> loadJob(Long jobId) {
        if (jobId == null) {
            return Optional.empty();
        }
        JobEntity jobEntity = jobEntityMbgMapper.selectByPrimaryKey(jobId);
        if (jobEntity == null) {
            return Optional.empty();
        }
        return Optional.of(toAggregate(jobEntity));
    }

    private Optional<PendingJob> fetchByRepository(Long repositoryId) {
        if (repositoryId == null) {
            return Optional.empty();
        }
        List<Long> jobIds = findJobIdsByRepository(Collections.singletonList(repositoryId));
        return fetchPendingJobByJobIds(jobIds);
    }

    private Optional<PendingJob> fetchByOrganize(Long organizeId) {
        if (organizeId == null) {
            return Optional.empty();
        }
        List<Long> repositoryIds = findRepositoryIdsByOrganize(organizeId);
        if (repositoryIds.isEmpty()) {
            return Optional.empty();
        }
        List<Long> jobIds = findJobIdsByRepository(repositoryIds);
        return fetchPendingJobByJobIds(jobIds);
    }

    private Optional<PendingJob> fetchGlobal() {
        JobHistoryEntityCondition condition = new JobHistoryEntityCondition();
        condition.createCriteria()
                 .andStatusEqualTo(JobStatus.PENDING.name())
                 .andRunnerIdIsNull();
        condition.setOrderByClause("created_at ASC LIMIT " + PENDING_SCAN_LIMIT);
        return buildPendingJob(condition);
    }

    private Optional<PendingJob> fetchPendingJobByJobIds(List<Long> jobIds) {
        if (jobIds.isEmpty()) {
            return Optional.empty();
        }
        JobHistoryEntityCondition condition = new JobHistoryEntityCondition();
        condition.createCriteria()
                 .andJobIdIn(jobIds)
                 .andStatusEqualTo(JobStatus.PENDING.name())
//                 .andRunnerIdIsNull();
        ;
        condition.setOrderByClause("created_at ASC LIMIT " + PENDING_SCAN_LIMIT);
        return buildPendingJob(condition);
    }

    private Optional<PendingJob> buildPendingJob(JobHistoryEntityCondition condition) {
        List<JobHistoryEntity> histories = jobHistoryEntityMbgMapper.selectByCondition(condition);
        for (JobHistoryEntity historyEntity : histories) {
            Optional<PendingJob> pending = buildPendingJob(historyEntity);
            if (pending.isPresent()) {
                return pending;
            }
        }
        return Optional.empty();
    }

    private Optional<PendingJob> buildPendingJob(JobHistoryEntity historyEntity) {
        JobEntity jobEntity = jobEntityMbgMapper.selectByPrimaryKey(historyEntity.getJobId());
        if (jobEntity == null) {
            log.warn("Job entity not found for jobId={}", historyEntity.getJobId());
            return Optional.empty();
        }
        RepositoryEntity repositoryEntity = repositoryEntityMbgMapper.selectByPrimaryKey(jobEntity.getRepositoryId());
        Long organizeId = repositoryEntity != null ? repositoryEntity.getOrganizeId() : null;
        String clonePath = repositoryEntity != null ? repositoryEntity.getClonePath() : null;

        Job job = toAggregate(jobEntity);
        if (job.getLatestHistory().getStatus() != JobStatus.PENDING) {
            return Optional.empty();
        }

        return Optional.of(PendingJob.builder()
                .job(job)
                .organizeId(organizeId)
                .repositoryClonePath(clonePath)
                .build());
    }

    private List<Long> findJobIdsByRepository(List<Long> repositoryIds) {
        if (repositoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        JobEntityCondition condition = new JobEntityCondition();
        condition.createCriteria().andRepositoryIdIn(repositoryIds);
        condition.setOrderByClause("created_at ASC LIMIT " + JOB_FETCH_LIMIT);
        return jobEntityMbgMapper.selectByCondition(condition).stream()
                                 .map(JobEntity::getId)
                                 .collect(Collectors.toList());
    }

    private List<Long> findRepositoryIdsByOrganize(Long organizeId) {
        RepositoryEntityCondition condition = new RepositoryEntityCondition();
        condition.createCriteria().andOrganizeIdEqualTo(organizeId);
        condition.setOrderByClause("created_at ASC");
        return repositoryEntityMbgMapper.selectByCondition(condition).stream()
                                        .map(RepositoryEntity::getId)
                                        .collect(Collectors.toList());
    }

    private Job toAggregate(JobEntity jobEntity) {
        List<JobHistoryEntity> historyEntities = loadHistories(jobEntity.getId());
        List<JobHistory> domainHistories = toDomainHistories(jobEntity.getId(), historyEntities);
        return Job.reconstruct(
                JobId.of(String.valueOf(jobEntity.getId())),
                RepositoryId.of(jobEntity.getRepositoryId()),
                CommitHash.of(jobEntity.getCommitHash()),
                BranchName.of(jobEntity.getBranchName()),
                UserId.of(jobEntity.getTriggeredBy()),
                jobEntity.getCreatedAt(),
                domainHistories
        );
    }

    private List<JobHistoryEntity> loadHistories(Long jobId) {
        JobHistoryEntityCondition condition = new JobHistoryEntityCondition();
        condition.createCriteria().andJobIdEqualTo(jobId);
        condition.setOrderByClause("created_at ASC");
        return jobHistoryEntityMbgMapper.selectByCondition(condition);
    }

    private List<JobHistory> toDomainHistories(Long jobId, List<JobHistoryEntity> entities) {
        List<JobHistory> histories = new ArrayList<>();
        int seq = 1;
        for (JobHistoryEntity entity : entities.stream()
                                               .sorted(Comparator.comparing(JobHistoryEntity::getCreatedAt))
                                               .collect(Collectors.toList())) {
            histories.add(JobHistory.reconstruct(
                    JobHistoryId.of(String.valueOf(entity.getId())),
                    JobId.of(String.valueOf(jobId)),
                    SequenceNumber.of(seq++),
                    entity.getRunnerId() != null ? RunnerId.of(String.valueOf(entity.getRunnerId())) : null,
                    JobStatus.valueOf(entity.getStatus()),
                    SystemUser.SYSTEM,
                    entity.getCreatedAt()
            ));
        }
        return histories;
    }

    private JobHistoryEntity findLatestHistory(Long jobId) {
        JobHistoryEntityCondition condition = new JobHistoryEntityCondition();
        condition.createCriteria().andJobIdEqualTo(jobId);
        condition.setOrderByClause("created_at DESC LIMIT 1");
        List<JobHistoryEntity> entities = jobHistoryEntityMbgMapper.selectByCondition(condition);
        return entities.isEmpty() ? null : entities.get(0);
    }

    private Long parseJobId(Job job) {
        try {
            return Long.parseLong(job.getId().getValue());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long parseRunnerId(RunnerId runnerId) {
        if (runnerId == null || runnerId.getValue() == null || runnerId.getValue().isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(runnerId.getValue());
        } catch (NumberFormatException ex) {
            log.warn("RunnerId [{}] is not numeric. runner_id will be stored as null.", runnerId.getValue());
            return null;
        }
    }
}

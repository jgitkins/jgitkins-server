package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.dto.PendingJob;
import io.jgitkins.server.application.dto.RunnerAssignmentCandidate;
import io.jgitkins.server.application.port.out.JobQueuePort;
import io.jgitkins.server.domain.model.vo.JobStatus;
import io.jgitkins.server.infrastructure.persistence.mapper.JobEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.JobHistoryEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.RepositoryEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobQueueMybatisAdapter implements JobQueuePort {

    private static final String TARGET_REPOSITORY = "REPOSITORY";
    private static final String TARGET_ORGANIZE = "ORGANIZE";
    private static final String TARGET_GLOBAL = "GLOBAL";
    private static final int JOB_FETCH_LIMIT = 50;

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
    public boolean markJobInQueue(Long jobHistoryId, Long runnerId) {
        JobHistoryEntity updateEntity = new JobHistoryEntity();
        updateEntity.setRunnerId(runnerId);
        updateEntity.setStatus(JobStatus.IN_QUEUE.name());
        updateEntity.setStartedAt(LocalDateTime.now());

        JobHistoryEntityCondition condition = new JobHistoryEntityCondition();
        condition.createCriteria()
                 .andIdEqualTo(jobHistoryId)
                 .andStatusEqualTo(JobStatus.PENDING.name());

        int updated = jobHistoryEntityMbgMapper.updateByConditionSelective(updateEntity, condition);
        return updated > 0;
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
        condition.setOrderByClause("created_at ASC LIMIT 1");
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
                 .andRunnerIdIsNull();
        condition.setOrderByClause("created_at ASC LIMIT 1");
        return buildPendingJob(condition);
    }

    private Optional<PendingJob> buildPendingJob(JobHistoryEntityCondition condition) {
        List<JobHistoryEntity> histories = jobHistoryEntityMbgMapper.selectByCondition(condition);
        if (histories.isEmpty()) {
            return Optional.empty();
        }
        JobHistoryEntity historyEntity = histories.get(0);
        JobEntity jobEntity = jobEntityMbgMapper.selectByPrimaryKey(historyEntity.getJobId());
        if (jobEntity == null) {
            log.warn("Job entity not found for jobId={}", historyEntity.getJobId());
            return Optional.empty();
        }
        RepositoryEntity repositoryEntity = repositoryEntityMbgMapper.selectByPrimaryKey(jobEntity.getRepositoryId());
        Long organizeId = repositoryEntity != null ? repositoryEntity.getOrganizeId() : null;

        PendingJob pendingJob = PendingJob.builder()
                                          .jobId(jobEntity.getId())
                                          .jobHistoryId(historyEntity.getId())
                                          .repositoryId(jobEntity.getRepositoryId())
                                          .organizeId(organizeId)
                                          .commitHash(jobEntity.getCommitHash())
                                          .branchName(jobEntity.getBranchName())
                                          .triggeredBy(jobEntity.getTriggeredBy())
                                          .build();
        return Optional.of(pendingJob);
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
}

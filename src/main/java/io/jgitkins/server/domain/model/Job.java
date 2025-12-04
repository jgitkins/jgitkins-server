package io.jgitkins.server.domain.model;

import io.jgitkins.server.domain.model.vo.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Job Aggregate Root
 * 서버 관점: Job 생성 및 큐잉 관리
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Job {

    private final JobId id;
    private final RepositoryId repositoryId;
    private final CommitHash commitHash;
    private final BranchName branchName;
    private final UserId triggeredBy;
    private final LocalDateTime createdAt;

    // Aggregate 내부 Entity Collection
    private final List<JobHistory> histories;

    /**
     * Job 생성 (Factory Method)
     * 초기 상태는 PENDING으로 자동 생성
     */
    public static Job create(RepositoryId repositoryId,
                             CommitHash commitHash,
                             BranchName branchName,
                             UserId triggeredBy) {
        JobId jobId = JobId.generate();
        LocalDateTime now = LocalDateTime.now();

        // 초기 PENDING 상태의 History 생성
        JobHistory initialHistory = JobHistory.createPending(jobId, now);

        List<JobHistory> histories = new ArrayList<>();
        histories.add(initialHistory);

        return new Job(jobId,
                       repositoryId,
                       commitHash,
                       branchName,
                       triggeredBy,
                       now,
                       histories);
    }

    /**
     * Job을 큐에 등록 (PENDING -> IN_QUEUE)
     * 새로운 JobHistory 추가
     */
    public void enqueue() {
        validateCanEnqueue();

        int nextSeqNo = histories.size() + 1;
        JobHistory queuedHistory = JobHistory.createInQueue(
                id,
                nextSeqNo,
                LocalDateTime.now());

        histories.add(queuedHistory);
    }

    /**
     * 현재 Job의 최신 상태 조회
     */
    public JobStatus getCurrentStatus() {
        if (histories.isEmpty()) {
            throw new IllegalStateException("Job must have at least one history");
        }
        return histories.get(histories.size() - 1).getStatus();
    }

    /**
     * 읽기 전용 History 목록 반환
     */
    public List<JobHistory> getHistories() {
        return Collections.unmodifiableList(histories);
    }

    /**
     * 최신 JobHistory 조회
     */
    public JobHistory getLatestHistory() {
        if (histories.isEmpty()) {
            throw new IllegalStateException("Job must have at least one history");
        }
        return histories.get(histories.size() - 1);
    }

    /**
     * Enqueue 가능 여부 검증
     */
    private void validateCanEnqueue() {
        JobStatus currentStatus = getCurrentStatus();
        if (currentStatus != JobStatus.PENDING) {
            throw new IllegalStateException(
                    String.format("Cannot enqueue job in status: %s", currentStatus));
        }
    }

    /**
     * 재구성용 생성자 (Repository에서 사용)
     */
    public static Job reconstruct(
            JobId id,
            RepositoryId repositoryId,
            CommitHash commitHash,
            BranchName branchName,
            UserId triggeredBy,
            LocalDateTime createdAt,
            List<JobHistory> histories) {
        return new Job(
                id,
                repositoryId,
                commitHash,
                branchName,
                triggeredBy,
                createdAt,
                new ArrayList<>(histories));
    }
}

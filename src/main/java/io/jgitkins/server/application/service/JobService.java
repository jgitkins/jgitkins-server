package io.jgitkins.server.application.service;

import io.jgitkins.server.application.dto.JobCreateCommand;
import io.jgitkins.server.application.port.in.JobCreateUseCase;
import io.jgitkins.server.application.port.out.CheckFileExistencePort;
import io.jgitkins.server.application.port.out.JobCommandPort;
import io.jgitkins.server.domain.model.Job;
import io.jgitkins.server.domain.model.vo.BranchName;
import io.jgitkins.server.domain.model.vo.CommitHash;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService implements JobCreateUseCase {

    private final CheckFileExistencePort checkFileExistencePort;
    private final JobCommandPort jobCommandPort;

    private static final String JENKINS_FILE_PATH = "Jenkinsfile";

    @Override
    @Transactional
    public void create(JobCreateCommand command) {
        // 1. Jenkinsfile 존재 여부 확인
        boolean hasJenkinsFile = checkFileExistencePort.exists(command.getTaskCd(),
                                                               command.getRepoName(),
                                                               command.getCommitHash(),
                                                               JENKINS_FILE_PATH);

        if (!hasJenkinsFile) {
            log.info("Jenkinsfile not found in repo: {}, commit: {}. Skipping job creation.", command.getRepoName(), command.getCommitHash());
            return;
        }

        log.info("Jenkinsfile found. Creating job for repo: {}, commit: {}", command.getRepoName(), command.getCommitHash());

        // 2. Job 도메인 객체 생성
        Job job = Job.create(RepositoryId.of(command.getRepositoryId()),
                             CommitHash.of(command.getCommitHash()),
                             BranchName.of(command.getBranchName()),
                             UserId.of(command.getTriggeredBy()));

        jobCommandPort.create(job);

        log.info("Job created successfully. JobId: {}", job.getId());

    }
}

package io.jgitkins.server.application.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.InternalServerErrorException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.RunnerDetailResult;
import io.jgitkins.server.application.dto.RunnerRegisterCommand;
import io.jgitkins.server.application.dto.RunnerRegistrationResult;
import io.jgitkins.server.application.mapper.RunnerApplicationMapper;
import io.jgitkins.server.application.port.in.RunnerActivateUseCase;
import io.jgitkins.server.application.port.in.RunnerDeleteUseCase;
import io.jgitkins.server.application.port.in.RunnerRegisterUseCase;
import io.jgitkins.server.application.port.out.RunnerCommandPort;
import io.jgitkins.server.application.port.out.RunnerQueryPort;
import io.jgitkins.server.domain.model.Runner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RunnerWriteService implements RunnerRegisterUseCase, RunnerDeleteUseCase, RunnerActivateUseCase {

    private final RunnerCommandPort runnerCommandPort;
    private final RunnerQueryPort runnerQueryPort;
    private final RunnerApplicationMapper runnerApplicationMapper;

    @Override
    @Transactional
    public RunnerRegistrationResult register(RunnerRegisterCommand command) {
        Runner runner = Runner.create(command.getDescription(),
                                      command.getScopeType(),
                                      command.getTargetId());

        Runner savedRunner = runnerCommandPort.save(runner);

        log.info("Runner registered. runnerId={}", savedRunner.getId());
        return runnerApplicationMapper.toRegistrationResult(savedRunner);
    }

    @Override
    @Transactional
    public void deleteRunner(Long runnerId) {
        runnerQueryPort.findById(runnerId)
                       .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RUNNER_NOT_FOUND));

        try {
            runnerCommandPort.deleteById(runnerId);
        } catch (RuntimeException ex) {
            log.error("Runner deletion failed. runnerId={}", runnerId, ex);
            throw new InternalServerErrorException(ErrorCode.RUNNER_DELETE_FAILED,
                                                   "Runner deletion failed",
                                                   ex);
        }
    }

    @Override
    @Transactional
    public RunnerDetailResult activate(Long runnerId, String token, String remoteIp) {
        Runner runner = runnerQueryPort.findById(runnerId)
                                       .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RUNNER_NOT_FOUND));
        Runner activated = runner.activate(token, remoteIp);
        try {
            Runner persisted = runnerCommandPort.save(activated);
            log.info("Runner activated. runnerId={}", runnerId);
            return runnerApplicationMapper.toDetailResult(persisted);
        } catch (RuntimeException ex) {
            log.error("Runner activation failed. runnerId={}", runnerId, ex);
            throw new InternalServerErrorException(ErrorCode.RUNNER_ACTIVATION_FAILED, "Runner activation failed", ex);
        }
    }
}

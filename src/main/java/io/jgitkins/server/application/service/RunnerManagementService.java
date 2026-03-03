package io.jgitkins.server.application.service;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.dto.*;
import io.jgitkins.server.application.dto.command.RunnerRegisterCommand;
import io.jgitkins.server.application.dto.result.RunnerActivateResult;
import io.jgitkins.server.application.dto.result.RunnerRegistrationResult;
import io.jgitkins.server.application.mapper.RunnerApplicationMapper;
import io.jgitkins.server.application.service.support.RunnerRuntimeConfigProvider;
import io.jgitkins.server.application.port.in.RunnerActivateUseCase;
import io.jgitkins.server.application.port.in.RunnerDeleteUseCase;
import io.jgitkins.server.application.port.in.RunnerRegisterUseCase;
import io.jgitkins.server.application.port.out.RunnerPort;
import io.jgitkins.server.domain.aggregate.Runner;
import io.jgitkins.server.domain.error.DomainErrorCode;
import io.jgitkins.server.domain.exception.RunnerAlreadyActiveException;
import io.jgitkins.server.domain.exception.RunnerTokenMismatchException;
import io.jgitkins.server.domain.exception.RunnerTokenMissingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RunnerManagementService implements RunnerRegisterUseCase, RunnerDeleteUseCase, RunnerActivateUseCase {

    private final RunnerPort runnerPort;
    private final RunnerApplicationMapper runnerApplicationMapper;
    private final RunnerRuntimeConfigProvider runtimeConfigProvider;

    @Override
    @Transactional
    public RunnerRegistrationResult register(RunnerRegisterCommand command) {
        Runner runner = Runner.create(command.getDescription(),
                                      command.getScopeType(),
                                      command.getTargetId());

        Runner savedRunner = runnerPort.save(runner);

        log.info("Runner registered. runnerId={}", savedRunner.getId());
        return runnerApplicationMapper.toRegistrationResult(savedRunner);
    }

    @Override
    @Transactional
    public void deleteRunner(Long runnerId) {
        runnerPort.findById(runnerId)
                       .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.RUNNER_NOT_FOUND));

        try {
            runnerPort.deleteById(runnerId);
        } catch (RuntimeException ex) {
            log.error("Runner deletion failed. runnerId={}", runnerId, ex);
            throw new JgitkinsException(io.jgitkins.server.infrastructure.common.error.InfrastructureErrorCode.RUNNER_DELETE_FAILED,
                                                   "Runner deletion failed",
                                                   ex);
        }
    }

    @Override
    @Transactional
    public RunnerActivateResult activate(String token, String remoteIp) {
        Runner runner = runnerPort.findByToken(token)
                                       .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.RUNNER_NOT_FOUND));

        Runner activatedInfo;
        try {
            activatedInfo = runner.activate(token, remoteIp);
        } catch (RunnerAlreadyActiveException ex) {
            throw new JgitkinsException(DomainErrorCode.RUNNER_ALREADY_ACTIVE, ex.getMessage(), ex);
        } catch (RunnerTokenMismatchException | RunnerTokenMissingException ex) {
            throw new JgitkinsException(DomainErrorCode.RUNNER_TOKEN_INVALID, ex.getMessage(), ex);
        }

        try {

            Runner persisted = runnerPort.save(activatedInfo);
            log.info("Runner activated. runnerId={}", persisted.getId());

            return RunnerActivateResult.builder()
                    .executionConfig(RunnerExecutionConfig.defaultConfig())
                    .runtimeConfig(runtimeConfigProvider.createConfig())
                    .build();

        } catch (RuntimeException ex) {
            log.error("Runner activation failed. runnerId={}", runner.getId(), ex);
            throw new JgitkinsException(io.jgitkins.server.infrastructure.common.error.InfrastructureErrorCode.RUNNER_ACTIVATION_FAILED, "Runner activation failed", ex);
        }
    }
}

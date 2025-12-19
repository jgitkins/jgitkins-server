package io.jgitkins.server.application.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.result.RunnerDetailResult;
import io.jgitkins.server.application.mapper.RunnerApplicationMapper;
import io.jgitkins.server.application.port.in.RunnerLoadUseCase;
import io.jgitkins.server.application.port.out.RunnerQueryPort;
import io.jgitkins.server.domain.aggregate.Runner;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RunnerReadService implements RunnerLoadUseCase {

    private final RunnerQueryPort runnerQueryPort;
    private final RunnerApplicationMapper runnerApplicationMapper;

    @Override
    @Transactional(readOnly = true)
    public RunnerDetailResult getRunner(Long runnerId) {
        Runner runner = runnerQueryPort.findById(runnerId)
                                       .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RUNNER_NOT_FOUND));
        return runnerApplicationMapper.toActivationResult(runner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RunnerDetailResult> getRunners() {
        List<Runner> runners = runnerQueryPort.findAll();
        return runners.stream()
                      .map(runnerApplicationMapper::toActivationResult)
                      .toList();
    }
}

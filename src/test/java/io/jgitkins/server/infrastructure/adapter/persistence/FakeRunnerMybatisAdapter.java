package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.RunnerCommandPort;
import io.jgitkins.server.domain.model.Runner;
import org.springframework.context.annotation.Profile;

@Profile("test")
public class FakeRunnerMybatisAdapter implements RunnerCommandPort {

    @Override
    public Runner save(Runner runner) {
        return runner.withId(1L);
    }

    @Override
    public void deleteById(Long runnerId) {

    }

    @Override
    public Runner update(Runner runner) {
        return null;
    }
}

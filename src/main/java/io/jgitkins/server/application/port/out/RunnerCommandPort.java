package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.aggregate.Runner;
public interface RunnerCommandPort {
    Runner save(Runner runner);
    void deleteById(Long runnerId);
    Runner update(Runner runner);
}

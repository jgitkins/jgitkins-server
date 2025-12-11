package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.aggregate.Runner;
import java.util.List;
import java.util.Optional;

public interface RunnerQueryPort {
    Optional<Runner> findById(Long runnerId);
    List<Runner> findAll();
}

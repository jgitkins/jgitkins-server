package io.jgitkins.server.application.port.out;

import io.jgitkins.server.infrastructure.persistence.model.JobEntity;

public interface JobPersistencePort {
    void create(JobEntity jobEntity);
}

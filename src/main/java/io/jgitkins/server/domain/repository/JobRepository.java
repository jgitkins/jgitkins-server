package io.jgitkins.server.domain.repository;

import io.jgitkins.server.domain.model.Job;

public interface JobRepository {
    void save(Job job);
}

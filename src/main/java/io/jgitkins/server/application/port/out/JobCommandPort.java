package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.model.Job;

public interface JobCommandPort {
    void create(Job job);
}

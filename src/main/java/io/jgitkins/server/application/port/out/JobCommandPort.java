package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.aggregate.Job;

public interface JobCommandPort {
    void create(Job job);
}

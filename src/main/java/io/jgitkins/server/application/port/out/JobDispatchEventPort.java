package io.jgitkins.server.application.port.out;

import io.jgitkins.server.application.dto.JobDispatchMessage;

public interface JobDispatchEventPort {
    void publish(JobDispatchMessage message);
}

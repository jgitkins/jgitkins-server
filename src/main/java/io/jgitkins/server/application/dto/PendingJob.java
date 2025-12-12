package io.jgitkins.server.application.dto;

import io.jgitkins.server.domain.aggregate.Job;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PendingJob {
    private final Job job;
    private final Long organizeId;
    private final String repositoryClonePath;
}

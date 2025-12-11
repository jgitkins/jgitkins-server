package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.JobCommandPort;
import io.jgitkins.server.domain.aggregate.Job;
import io.jgitkins.server.infrastructure.persistence.mapper.JobEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.JobHistoryEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.JobEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobMybatisAdapter implements JobCommandPort {
    private final JobEntityMbgMapper jobEntityMbgMapper;
    private final JobHistoryEntityMbgMapper jobHistoryEntityMbgMapper;
    private final JobMapper jobMapper;

    @Override
    public void create(Job job) {

        JobEntity jobEntity = jobMapper.toEntity(job);
        jobEntityMbgMapper.insertSelective(jobEntity);

        Long jobId = jobEntity.getId();
        if (jobId == null) {
            throw new IllegalStateException("Generated job id is null after insert");
        }

        job.getHistories()
           .forEach(history -> jobHistoryEntityMbgMapper.insertSelective(jobMapper.toHistoryEntity(history, jobId)));

    }
}

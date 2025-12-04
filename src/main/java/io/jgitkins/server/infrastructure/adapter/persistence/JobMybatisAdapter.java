package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.JobPersistencePort;
import io.jgitkins.server.infrastructure.persistence.mapper.JobEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.JobEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobMybatisAdapter implements JobPersistencePort {
    private final JobEntityMbgMapper jobEntityMbgMapper;

    @Override
    public void create(JobEntity jobEntity) {
        jobEntityMbgMapper.insertSelective(jobEntity);
    }
}

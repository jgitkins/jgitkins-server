package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.JobCreateCommand;

public interface JobCreateUseCase {
    /**
     * Job 생성 요청 처리
     * 
     * @param command Job 생성에 필요한 정보
     */
    void createJob(JobCreateCommand command);
}

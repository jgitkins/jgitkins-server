package io.jgitkins.server.infrastructure.adapter.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jgitkins.server.application.dto.JobDispatchMessage;
import io.jgitkins.server.application.port.out.JobDispatchEventPort;
import io.jgitkins.server.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitJobDispatchEventAdapter implements JobDispatchEventPort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(JobDispatchMessage message) {

        try {

            String payload = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.JOB_DISPATCH_ROUTING_KEY, payload);

            log.info("Published job dispatch message: jobId=[{}] ", message.getJobId());
//            log.info("Published job dispatch message: jobId=[{}] runnerId=[{}] historyId=[{}]", message.getJobId(), message.getRunnerId(), message.getJobHistoryId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize job dispatch message for jobId={}", message.getJobId(), e);
            throw new IllegalStateException("Failed to publish job dispatch message", e);
        }
    }
}

package io.jgitkins.server.infrastructure.adapter.mq;

import io.jgitkins.server.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendTask(String message) {
        log.info("Sending task message: {}", message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "jgitkins.task.created", message);
    }
}

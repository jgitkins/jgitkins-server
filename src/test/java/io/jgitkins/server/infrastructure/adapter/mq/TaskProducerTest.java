package io.jgitkins.server.infrastructure.adapter.mq;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TaskProducerTest {

    @Autowired
    private TaskProducer taskProducer;

    @Test
    void sendTask() {
        taskProducer.sendTask("Hello RabbitMQ from JUnit!");
    }
}

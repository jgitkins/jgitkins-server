package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.PushEventCommand;

public interface HandlePushEventUseCase {
    void handle(PushEventCommand command);
}

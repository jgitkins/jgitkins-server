package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.CreateOrganizeCommand;
import io.jgitkins.server.application.dto.OrganizeResult;

public interface CreateOrganizeUseCase {
    OrganizeResult createOrganize(CreateOrganizeCommand command);
}

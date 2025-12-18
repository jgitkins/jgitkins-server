package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.OrganizeCreationCommand;
import io.jgitkins.server.application.dto.OrganizeCreationResult;

public interface OrganizeCreationUseCase {
    OrganizeCreationResult createOrganize(OrganizeCreationCommand command);
}

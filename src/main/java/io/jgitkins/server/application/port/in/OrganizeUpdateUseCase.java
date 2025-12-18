package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.OrganizeCreationResult;
import io.jgitkins.server.application.dto.UpdateOrganizeCommand;

public interface OrganizeUpdateUseCase {
    OrganizeCreationResult updateOrganize(Long organizeId, UpdateOrganizeCommand command);
}

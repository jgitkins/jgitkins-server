package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.OrganizeResult;
import io.jgitkins.server.application.dto.UpdateOrganizeCommand;

public interface UpdateOrganizeUseCase {
    OrganizeResult updateOrganize(Long organizeId, UpdateOrganizeCommand command);
}

package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.OrganizeResult;

public interface LoadOrganizeUseCase {
    OrganizeResult getOrganize(Long organizeId);
}

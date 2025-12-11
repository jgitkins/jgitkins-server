package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.OrganizeResult;

import java.util.List;

public interface ListOrganizeUseCase {
    List<OrganizeResult> getOrganizes();
}

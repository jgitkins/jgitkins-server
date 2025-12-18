package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.OrganizeCreationResult;

import java.util.List;

public interface OrganizeLoadUseCase {
    OrganizeCreationResult getOrganize(Long organizeId);
    List<OrganizeCreationResult> getOrganizes();

}

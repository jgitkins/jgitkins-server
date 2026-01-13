package io.jgitkins.server.infrastructure.config.git;

import io.jgitkins.server.domain.model.vo.OwnerType;

public record GitSmartHttpEvent(OwnerType ownerType, String ownerName, String repositoryName) {}

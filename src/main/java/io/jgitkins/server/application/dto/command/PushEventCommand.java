package io.jgitkins.server.application.dto.command;

import io.jgitkins.server.domain.model.vo.OwnerType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PushEventCommand {
    private final OwnerType ownerType;
    private final String namespace;
    private final String repositoryName;
    private final String branchName;
    private final boolean branchCreated;
    private final String commitHash;
    private final Long triggeredBy;
}

package io.jgitkins.server.application.dto.command;

import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.aggregate.Repository;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchCreationContext {

    private final Long repositoryId;
    private final String branchName;
    private final String sourceBranch;
    private final boolean physicalCreationRequired;
    private final String taskCd;
    private final String repositoryName;

    public static BranchCreationContext of(BranchCreateCommand command,
                                           Organize organize,
                                           Repository repository,
                                           String resolvedSourceBranch) {
        return BranchCreationContext.builder()
                .repositoryId(command.getRepositoryId())
                .branchName(command.getBranchName())
                .sourceBranch(resolvedSourceBranch)
                .physicalCreationRequired(command.isPhysicalCreationRequired())
                .taskCd(organize.getName().getValue())
                .repositoryName(repository.getName().getValue())
                .build();
    }
}

package io.jgitkins.server.application.dto;

import io.jgitkins.server.domain.model.vo.BranchName;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.domain.model.vo.RepositoryVisibility;
import io.jgitkins.server.domain.model.vo.UserId;

public record RepositoryCreationContext(OrganizeId organizeId,
                                        RepositoryName repositoryName,
                                        RepositoryPath repositoryPath,
                                        BranchName defaultBranch,
                                        RepositoryVisibility visibility,
                                        UserId owner,
                                        String clonePath,
                                        String organizeSlug) {
}

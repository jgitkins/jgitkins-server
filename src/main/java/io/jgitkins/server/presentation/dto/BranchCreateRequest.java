package io.jgitkins.server.presentation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchCreateRequest {

    private String branchName;
    private String sourceBranch;
}

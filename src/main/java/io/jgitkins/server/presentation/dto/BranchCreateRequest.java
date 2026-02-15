package io.jgitkins.server.presentation.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchCreateRequest {

    @JsonAlias("name")
    private String branchName;
    private String sourceBranch;
}

package io.jgitkins.server.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRepositoryCommand {
    private String repoName;
    private String mainBranch;    // e.g., "main" (default if null/blank)
    private boolean readme;       // create README.md on init
    private String authorName;    // initial commit author (fallback if null)
    private String authorEmail;   // initial commit email (fallback if null)
    private String message;       // initial commit message
    private Long organizeId;
    private String path;
    private String visibility;
    private String repositoryType;
    private Long ownerId;
    private String description;
    private String credentialId;
}

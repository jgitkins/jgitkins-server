package io.jgitkins.server.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRepositoryCommand {
    // required
    private String repoName;
    private Long organizeId;
    private String authorName;    // initial commit author (fallback if null)
    private String authorEmail;   // initial commit email (fallback if null)
    private Long ownerId;
    private String mainBranch;    // e.g., "main" (default if null/blank)
    private String path;
    private String visibility;
    private String description;
    private String credentialId;

    // optional
    private boolean readme;       // create README.md on init
    private String message;       // initial commit message
}

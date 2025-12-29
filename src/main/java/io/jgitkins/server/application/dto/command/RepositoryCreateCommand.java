package io.jgitkins.server.application.dto.command;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryCreateCommand {
    // required
    private String repoName;
    private String ownerType;
    private Long organizeId;
    private String authorName;    // initial commit author (fallback if null)
    private String authorEmail;   // initial commit email (fallback if null)
    private String mainBranch;    // e.g., "main" (default if null/blank)
    private String visibility;
    private String description;
    private String credentialId;

    // optional
    private boolean readme;       // create README.md on init
    private String message;       // initial commit message
}

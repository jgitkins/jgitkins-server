package io.jgitkins.server.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RepositoryCreateRequest {
    // required
    @Schema(description = "Repository name (unique within owner namespace).")
    @NotBlank(message = "repoName is required")
    private String repoName;
    private String mainBranch;
    private String username;
    private String email;
    private boolean readme;
    private String message;
    @Schema(description = "Owner type for the repository. Allowed values: USER, ORGANIZATION.")
    @NotBlank(message = "ownerType is required")
    private String ownerType;

    // optional
    @Schema(description = "Organization id when ownerType is ORGANIZATION.")
    private Long organizeId;
    private String visibility;
    private String description;
    private String credentialId;

    @AssertTrue(message = "ownerType requires organizeId only when ownerType is ORGANIZATION.")
    public boolean isOwnerSelectionValid() {
        if (ownerType == null || ownerType.isBlank()) {
            return false;
        }
        String normalized = ownerType.trim().toUpperCase();
        if ("USER".equals(normalized)) {
            return organizeId == null;
        }
        if ("ORGANIZATION".equals(normalized)) {
            return organizeId != null;
        }
        return false;
    }
}

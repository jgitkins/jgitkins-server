package io.jgitkins.server.infrastructure.config.git;

import io.jgitkins.server.domain.model.vo.OwnerType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GitSmartHttpEventParser {

    public GitSmartHttpEvent parse(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (!path.startsWith("/git/")) {
            return null;
        }
        String trimmed = path.substring("/git/".length());
        String[] parts = trimmed.split("/");
        if (parts.length < 3) {
            return null;
        }
        OwnerType ownerType = parseOwnerType(parts[0]);
        if (ownerType == null) {
            return null;
        }
        String ownerName = parts[1];
        String repoSegment = parts[2];
        String repositoryName = repoSegment.endsWith(".git")
                ? repoSegment.substring(0, repoSegment.length() - 4)
                : repoSegment;
        if (ownerName.isBlank() || repositoryName.isBlank()) {
            return null;
        }
        return new GitSmartHttpEvent(ownerType, ownerName, repositoryName);
    }

    private OwnerType parseOwnerType(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "user", "users" -> OwnerType.USER;
            case "organize", "organization", "org" -> OwnerType.ORGANIZATION;
            default -> null;
        };
    }
}

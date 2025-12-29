package io.jgitkins.server.infrastructure.config.git;

import jakarta.servlet.http.HttpServletRequest;

public class GitRepositoryRequestParser {

    public GitRepositoryRequest parse(HttpServletRequest request) {
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
        if (parts.length < 2) {
            return null;
        }
        String namespace = parts[0];
        String ownerSlug = null;
        String repoSegment;
        if ("users".equals(namespace)) {
            if (parts.length < 3) {
                return null;
            }
            ownerSlug = parts[1];
            repoSegment = parts[2];
        } else {
            repoSegment = parts[1];
        }
        String repositoryPath = repoSegment.endsWith(".git")
                ? repoSegment.substring(0, repoSegment.length() - 4)
                : repoSegment;
        if (namespace.isBlank() || repositoryPath.isBlank()) {
            return null;
        }
        return new GitRepositoryRequest(namespace, ownerSlug, repositoryPath);
    }

    public record GitRepositoryRequest(String namespace, String ownerSlug, String repositoryName) {}
}

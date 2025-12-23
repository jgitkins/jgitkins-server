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
        String organizeSlug = parts[0];
        String repoSegment = parts[1];
        String repositoryPath = repoSegment.endsWith(".git")
                ? repoSegment.substring(0, repoSegment.length() - 4)
                : repoSegment;
        if (organizeSlug.isBlank() || repositoryPath.isBlank()) {
            return null;
        }
        return new GitRepositoryRequest(organizeSlug, repositoryPath);
    }

    public record GitRepositoryRequest(String organizeSlug, String repositoryPath) {}
}

package io.jgitkins.server.infrastructure.support;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RepositoryPathHelper {

    public String buildClonePath(String namespace, String repoPath) {
        String namespaceSegment = trimSlashes(namespace);
        String repoSegment = trimSlashes(repoPath);
        if (!repoSegment.endsWith(".git")) {
            repoSegment = repoSegment + ".git";
        }
        return "/" + namespaceSegment + "/" + repoSegment;
    }

    public String buildUserNamespace(String username) {
        return "users/" + trimSlashes(username);
    }

    private String trimSlashes(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}

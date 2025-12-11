package io.jgitkins.server.application.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CloneUrlBuilder {

    private final String baseUrl;

    public CloneUrlBuilder(@Value("${jgitkins.clone-base-url:http://localhost:8084/git}") String baseUrl) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    public String build(String clonePath) {
        if (clonePath == null || clonePath.isBlank()) {
            return null;
        }
        if (clonePath.startsWith("/")) {
            return baseUrl + clonePath;
        }
        return baseUrl + "/" + clonePath;
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8084";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}

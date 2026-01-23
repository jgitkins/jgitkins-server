package io.jgitkins.server.application.common;

import io.jgitkins.server.infrastructure.config.RunnerRuntimeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CloneUrlBuilder {

    private final RunnerRuntimeProperties properties;

    public String build(String clonePath) {
        if (clonePath == null || clonePath.isBlank()) {
            return null;
        }

        String normalizedPath = clonePath.startsWith("/") ? clonePath : "/" + clonePath;
        return "%s://%s%s".formatted(
                properties.getRestScheme(),
                properties.getServiceHost(),
                normalizedPath
        );
    }
}

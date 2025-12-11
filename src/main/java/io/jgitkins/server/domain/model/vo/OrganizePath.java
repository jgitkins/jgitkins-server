package io.jgitkins.server.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Organize path(slug) VO
 */
@Getter
@EqualsAndHashCode
public class OrganizePath {

    private static final Pattern ALLOWED = Pattern.compile("^[a-z0-9]([a-z0-9-_]*[a-z0-9])?$");

    private final String value;

    private OrganizePath(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Organize path must not be null");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Organize path must not be blank");
        }
        if (!ALLOWED.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Organize path must match ^[a-z0-9]([a-z0-9-_]*[a-z0-9])?$");
        }
        this.value = normalized;
    }

    public static OrganizePath from(String value) {
        return new OrganizePath(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

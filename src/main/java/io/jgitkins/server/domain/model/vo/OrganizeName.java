package io.jgitkins.server.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Organize 표시 이름 VO
 */
@Getter
@EqualsAndHashCode
public class OrganizeName {

    private final String value;

    private OrganizeName(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Organize name must not be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Organize name must not be blank");
        }
        this.value = trimmed;
    }

    public static OrganizeName from(String value) {
        return new OrganizeName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

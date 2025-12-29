package io.jgitkins.server.domain.model.vo;

public enum OwnerType {
    USER,
    ORGANIZATION;

    public static OwnerType from(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.isEmpty()) {
            return null;
        }
        return OwnerType.valueOf(normalized);
    }
}

package io.jgitkins.server.domain;

import lombok.Getter;

@Getter
public class Branch {
    private final Long repositoryId;
    private final String name;
    private final boolean locked;
    private final boolean ciEnabled;

    // 생성자 및 비즈니스 로직
    private Branch(Long repositoryId, String name, boolean locked, boolean ciEnabled) {
        this.repositoryId = repositoryId;
        this.name = name;
        this.locked = locked;
        this.ciEnabled = ciEnabled;
    }

    public static Branch create(Long repositoryId, String name) {
        return create(repositoryId, name, false, false);
    }

    public static Branch create(Long repositoryId, String name, boolean locked, boolean ciEnabled) {
        // 비즈니스 로직 (예: 유효성 검사, 규칙 검증 등)
        if (repositoryId == null) {
            throw new IllegalArgumentException("Repository ID cannot be null.");
        }

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Branch name cannot be empty.");
        }
        return new Branch(repositoryId, name, locked, ciEnabled);
    }

    public static Branch rehydrate(Long repositoryId, String name, boolean locked, boolean ciEnabled) {
        return new Branch(repositoryId, name, locked, ciEnabled);
    }
}

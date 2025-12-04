package io.jgitkins.server.domain;

import lombok.Getter;

@Getter
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Branch {
    private final Long repositoryId;
    private final String name;

    // 생성자 및 비즈니스 로직
    private Branch(Long repositoryId, String name) {
        this.repositoryId = repositoryId;
        this.name = name;
    }

    public static Branch create(Long repositoryId, String name) {
        // 비즈니스 로직 (예: 유효성 검사, 규칙 검증 등)
        if (repositoryId == null) {
            throw new IllegalArgumentException("Repository ID cannot be null.");
        }

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Branch name cannot be empty.");
        }
        return new Branch(repositoryId, name);
    }
}

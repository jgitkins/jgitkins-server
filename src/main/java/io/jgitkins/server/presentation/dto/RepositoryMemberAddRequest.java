package io.jgitkins.server.presentation.dto;

import io.jgitkins.server.domain.model.vo.RepositoryMemberRole;
import lombok.Getter;

@Getter
public class RepositoryMemberAddRequest {
    private Long userId;
    private RepositoryMemberRole role;
}

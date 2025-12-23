package io.jgitkins.server.presentation.dto;

import io.jgitkins.server.domain.model.vo.OrganizeMemberRole;
import lombok.Getter;

@Getter
public class OrganizeMemberAddRequest {
    private Long userId;
    private OrganizeMemberRole role;
}

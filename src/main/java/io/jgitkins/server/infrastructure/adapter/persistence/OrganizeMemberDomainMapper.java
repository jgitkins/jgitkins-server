package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.domain.model.OrganizeMember;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizeMemberRole;
import io.jgitkins.server.domain.model.vo.UserId;
import io.jgitkins.server.infrastructure.persistence.model.OrganizeMemberEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizeMemberDomainMapper {

    public OrganizeMemberEntity toEntity(OrganizeMember member) {
        if (member == null) {
            return null;
        }
        OrganizeMemberEntity entity = new OrganizeMemberEntity();
        entity.setOrganizeId(member.getOrganizeId().getValue());
        entity.setUserId(member.getUserId().getValue());
        entity.setRole(member.getRole().name());
        entity.setJoinedAt(member.getJoinedAt());
        return entity;
    }

    public OrganizeMember toDomain(OrganizeMemberEntity entity) {
        if (entity == null) {
            return null;
        }
        return OrganizeMember.create(
                OrganizeId.of(entity.getOrganizeId()),
                UserId.of(entity.getUserId()),
                OrganizeMemberRole.from(entity.getRole()),
                entity.getJoinedAt()
        );
    }
}

package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.domain.model.OrganizeMember;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.UserId;
import io.jgitkins.server.infrastructure.persistence.mapper.OrganizeMemberEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.OrganizeMemberEntity;
import io.jgitkins.server.infrastructure.persistence.model.OrganizeMemberEntityCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizeMemberAdapter implements OrganizeMemberPort {

    private final OrganizeMemberEntityMbgMapper organizeMemberMapper;

    private final OrganizeMemberDomainMapper organizeMemberDomainMapper;

    @Override
    public OrganizeMember save(OrganizeMember member) {
        OrganizeMemberEntity entity = organizeMemberDomainMapper.toEntity(member);
        organizeMemberMapper.insertSelective(entity);
        return organizeMemberDomainMapper.toDomain(entity);
    }

    @Override
    public boolean existsByOrganizeAndUser(OrganizeId organizeId, UserId userId) {
        OrganizeMemberEntityCondition condition = new OrganizeMemberEntityCondition();
        condition.createCriteria()
                .andOrganizeIdEqualTo(organizeId.getValue())
                .andUserIdEqualTo(userId.getValue());
        return organizeMemberMapper.countByCondition(condition) > 0;
    }

    @Override
    public void deleteByOrganizeAndUser(OrganizeId organizeId, UserId userId) {
        OrganizeMemberEntityCondition condition = new OrganizeMemberEntityCondition();
        condition.createCriteria()
                .andOrganizeIdEqualTo(organizeId.getValue())
                .andUserIdEqualTo(userId.getValue());
        organizeMemberMapper.deleteByCondition(condition);
    }

    @Override
    public java.util.List<OrganizeMember> findAllByOrganize(OrganizeId organizeId) {
        OrganizeMemberEntityCondition condition = new OrganizeMemberEntityCondition();
        condition.createCriteria().andOrganizeIdEqualTo(organizeId.getValue());
        condition.setOrderByClause("joined_at desc");
        return organizeMemberMapper.selectByCondition(condition)
                .stream()
                .map(organizeMemberDomainMapper::toDomain)
                .toList();
    }
}

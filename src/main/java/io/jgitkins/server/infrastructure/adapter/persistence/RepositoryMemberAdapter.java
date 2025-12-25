package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.RepositoryMemberPort;
import io.jgitkins.server.domain.model.RepositoryMember;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.UserId;
import io.jgitkins.server.infrastructure.persistence.mapper.RepositoryMemberEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryMemberEntity;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryMemberEntityCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepositoryMemberAdapter implements RepositoryMemberPort {

    private final RepositoryMemberEntityMbgMapper repositoryMemberEntityMbgMapper;
    private final RepositoryMemberDomainMapper repositoryMemberDomainMapper;


    @Override
    public boolean existsByRepositoryAndUser(RepositoryId repositoryId, UserId userId) {
        if (repositoryId == null || userId == null) {
            return false;
        }
        RepositoryMemberEntityCondition condition = new RepositoryMemberEntityCondition();
        condition.createCriteria()
                .andRepositoryIdEqualTo(repositoryId.getValue())
                .andUserIdEqualTo(userId.getValue());
        return repositoryMemberEntityMbgMapper.countByCondition(condition) > 0;
    }

    @Override
    public RepositoryMember save(RepositoryMember member) {
        RepositoryMemberEntity entity = repositoryMemberDomainMapper.toEntity(member);
        repositoryMemberEntityMbgMapper.insertSelective(entity);
        return repositoryMemberDomainMapper.toDomain(entity);
    }

    @Override
    public void deleteByRepositoryAndUser(RepositoryId repositoryId, UserId userId) {
        RepositoryMemberEntityCondition condition = new RepositoryMemberEntityCondition();
        condition.createCriteria()
                .andRepositoryIdEqualTo(repositoryId.getValue())
                .andUserIdEqualTo(userId.getValue());
        repositoryMemberEntityMbgMapper.deleteByCondition(condition);
    }

    @Override
    public java.util.List<RepositoryMember> findAllByRepository(RepositoryId repositoryId) {
        RepositoryMemberEntityCondition condition = new RepositoryMemberEntityCondition();
        condition.createCriteria().andRepositoryIdEqualTo(repositoryId.getValue());
        condition.setOrderByClause("added_at desc");
        return repositoryMemberEntityMbgMapper.selectByCondition(condition)
                .stream()
                .map(repositoryMemberDomainMapper::toDomain)
                .toList();
    }

}

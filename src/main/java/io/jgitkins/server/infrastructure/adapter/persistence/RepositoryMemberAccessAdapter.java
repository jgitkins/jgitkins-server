package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.RepositoryMemberAccessPort;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.UserId;
import io.jgitkins.server.infrastructure.persistence.mapper.RepositoryMemberEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryMemberEntityCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepositoryMemberAccessAdapter implements RepositoryMemberAccessPort {

    private final RepositoryMemberEntityMbgMapper repositoryMemberEntityMbgMapper;

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
}

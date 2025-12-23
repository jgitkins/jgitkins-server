package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.UserLookupPort;
import io.jgitkins.server.infrastructure.persistence.mapper.UserEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.UserEntityCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserLookupMybatisAdapter implements UserLookupPort {

    private final UserEntityMbgMapper userEntityMbgMapper;

    @Override
    public Optional<Long> findUserIdByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        UserEntityCondition condition = new UserEntityCondition();
        condition.createCriteria().andUsernameEqualTo(username);
        condition.setOrderByClause("id desc limit 1");
        return userEntityMbgMapper.selectByCondition(condition)
                .stream()
                .findFirst()
                .map(entity -> entity.getId());
    }
}

package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.infrastructure.mapper.UserDomainMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.UserEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.UserEntity;
import io.jgitkins.server.infrastructure.persistence.model.UserEntityCondition;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMybatisAdapter implements UserPort {

    private final UserEntityMbgMapper userEntityMbgMapper;
    private final UserDomainMapper userDomainMapper;

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        UserEntityCondition condition = new UserEntityCondition();
        condition.createCriteria().andEmailEqualTo(email.trim());
        condition.setOrderByClause("id desc limit 1");
        return userEntityMbgMapper.selectByCondition(condition)
                .stream()
                .findFirst()
                .map(userDomainMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        UserEntityCondition condition = new UserEntityCondition();
        condition.createCriteria().andUsernameEqualTo(username.trim());
        condition.setOrderByClause("id desc limit 1");
        return userEntityMbgMapper.selectByCondition(condition)
                .stream()
                .findFirst()
                .map(userDomainMapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        UserEntity entity = userEntityMbgMapper.selectByPrimaryKey(id);
        return Optional.ofNullable(userDomainMapper.toDomain(entity));
    }

    @Override
    public User save(User user) {
        UserEntity entity = userDomainMapper.toEntity(user);
        if (user.getId() == null) {
            userEntityMbgMapper.insertSelective(entity);
            return userDomainMapper.toDomain(entity);
        }
        userEntityMbgMapper.updateByPrimaryKeySelective(entity);
        UserEntity updated = userEntityMbgMapper.selectByPrimaryKey(user.getId());
        return userDomainMapper.toDomain(updated);
    }

    @Override
    public java.util.List<User> findAll() {
        io.jgitkins.server.infrastructure.persistence.model.UserEntityCondition condition =
                new io.jgitkins.server.infrastructure.persistence.model.UserEntityCondition();
        condition.setOrderByClause("id desc");
        return userEntityMbgMapper.selectByCondition(condition)
                .stream()
                .map(userDomainMapper::toDomain)
                .toList();
    }

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

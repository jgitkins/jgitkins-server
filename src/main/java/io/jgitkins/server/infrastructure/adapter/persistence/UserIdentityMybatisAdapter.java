package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.UserIdentityPersistencePort;
import io.jgitkins.server.domain.model.UserIdentity;
import io.jgitkins.server.infrastructure.persistence.mapper.UserIdentitiesEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.UserIdentitiesEntity;
import io.jgitkins.server.infrastructure.persistence.model.UserIdentitiesEntityCondition;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserIdentityMybatisAdapter implements UserIdentityPersistencePort {

    private final UserIdentitiesEntityMbgMapper userIdentitiesEntityMbgMapper;
    private final UserIdentityDomainMapper userIdentityDomainMapper;

    @Override
    public Optional<UserIdentity> findByProvider(String providerName, String providerSub) {
        if (providerName == null || providerName.isBlank() || providerSub == null || providerSub.isBlank()) {
            return Optional.empty();
        }
        UserIdentitiesEntityCondition condition = new UserIdentitiesEntityCondition();
        condition.createCriteria()
                .andProviderNameEqualTo(providerName.trim())
                .andProviderSubEqualTo(providerSub.trim());
        condition.setOrderByClause("id desc limit 1");
        return userIdentitiesEntityMbgMapper.selectByCondition(condition)
                .stream()
                .findFirst()
                .map(userIdentityDomainMapper::toDomain);
    }

    @Override
    public UserIdentity save(UserIdentity identity) {
        UserIdentitiesEntity entity = userIdentityDomainMapper.toEntity(identity);
        if (identity.getId() == null) {
            userIdentitiesEntityMbgMapper.insertSelective(entity);
            return userIdentityDomainMapper.toDomain(entity);
        }
        userIdentitiesEntityMbgMapper.updateByPrimaryKeySelective(entity);
        UserIdentitiesEntity updated = userIdentitiesEntityMbgMapper.selectByPrimaryKey(identity.getId());
        return userIdentityDomainMapper.toDomain(updated);
    }

    @Override
    public java.util.List<UserIdentity> findAllByUserId(Long userId) {
        if (userId == null) {
            return java.util.List.of();
        }
        UserIdentitiesEntityCondition condition = new UserIdentitiesEntityCondition();
        condition.createCriteria().andUserIdEqualTo(userId);
        condition.setOrderByClause("id desc");
        return userIdentitiesEntityMbgMapper.selectByCondition(condition)
                .stream()
                .map(userIdentityDomainMapper::toDomain)
                .toList();
    }
}

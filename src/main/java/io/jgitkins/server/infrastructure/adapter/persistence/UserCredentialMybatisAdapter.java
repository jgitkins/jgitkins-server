package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.UserCredentialPersistencePort;
import io.jgitkins.server.domain.model.UserCredential;
import io.jgitkins.server.infrastructure.persistence.mapper.UserCredentialsEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.UserCredentialsEntity;
import io.jgitkins.server.infrastructure.persistence.model.UserCredentialsEntityCondition;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCredentialMybatisAdapter implements UserCredentialPersistencePort {

    private final UserCredentialsEntityMbgMapper userCredentialsEntityMbgMapper;
    private final UserCredentialDomainMapper userCredentialDomainMapper;

    @Override
    public UserCredential save(UserCredential credential) {
        UserCredentialsEntity entity = userCredentialDomainMapper.toEntity(credential);
        if (credential.getId() == null) {
            userCredentialsEntityMbgMapper.insertSelective(entity);
            return userCredentialDomainMapper.toDomain(entity);
        }
        userCredentialsEntityMbgMapper.updateByPrimaryKeySelective(entity);
        UserCredentialsEntity updated = userCredentialsEntityMbgMapper.selectByPrimaryKey(credential.getId());
        return userCredentialDomainMapper.toDomain(updated);
    }

    @Override
    public Optional<UserCredential> findByUserIdAndProvider(Long userId, String provider) {
        if (userId == null || provider == null || provider.isBlank()) {
            return Optional.empty();
        }
        UserCredentialsEntityCondition condition = new UserCredentialsEntityCondition();
        condition.createCriteria()
                .andUserIdEqualTo(userId)
                .andProviderEqualTo(provider);
        condition.setOrderByClause("id desc limit 1");
        return userCredentialsEntityMbgMapper.selectByCondition(condition)
                .stream()
                .findFirst()
                .map(userCredentialDomainMapper::toDomain);
    }

    @Override
    public List<UserCredential> findAllByUserIdAndProvider(Long userId, String provider) {
        if (userId == null || provider == null || provider.isBlank()) {
            return List.of();
        }
        UserCredentialsEntityCondition condition = new UserCredentialsEntityCondition();
        condition.createCriteria()
                .andUserIdEqualTo(userId)
                .andProviderEqualTo(provider);
        condition.setOrderByClause("id desc");
        return userCredentialsEntityMbgMapper.selectByCondition(condition)
                .stream()
                .map(userCredentialDomainMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteByIdAndUserId(Long id, Long userId) {
        if (id == null || userId == null) {
            return;
        }
        UserCredentialsEntityCondition condition = new UserCredentialsEntityCondition();
        condition.createCriteria()
                .andIdEqualTo(id)
                .andUserIdEqualTo(userId);
        userCredentialsEntityMbgMapper.deleteByCondition(condition);
    }
}

package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.infrastructure.mapper.RepositoryDomainMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.OrganizeEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.RepositoryEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.UserEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.OrganizeEntity;
import io.jgitkins.server.infrastructure.persistence.model.OrganizeEntityCondition;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryEntity;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryEntityCondition;
import io.jgitkins.server.infrastructure.persistence.model.UserEntity;
import io.jgitkins.server.infrastructure.persistence.model.UserEntityCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepositoryMybatisAdapter implements RepositoryPort {

    private final OrganizeEntityMbgMapper organizeEntityMbgMapper;
    private final RepositoryEntityMbgMapper repositoryEntityMbgMapper;
    private final UserEntityMbgMapper userEntityMbgMapper;

    private final RepositoryDomainMapper repositoryDomainMapper;

    @Override
    public Repository save(Repository repository) {

        RepositoryEntity entity = repositoryDomainMapper.toEntity(repository);
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        if (entity.getUpdatedAt() == null) {
            entity.setUpdatedAt(entity.getCreatedAt());
        }
        repositoryEntityMbgMapper.insertSelective(entity);
        log.debug("Repository saved. repositoryId={}, ownerType={}, ownerId={}, name={}",
                entity.getId(),
                entity.getOwnerType(),
                entity.getOwnerId(),
                entity.getName());
        return repository.withIdentity(RepositoryId.of(entity.getId()), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    @Override
    public Repository update(Repository repository) {
        if (repository.getId() == null) {
            throw new IllegalArgumentException("Repository ID required for update");
        }
        RepositoryEntity entity = repositoryDomainMapper.toEntity(repository);
        entity.setUpdatedAt(LocalDateTime.now());
        repositoryEntityMbgMapper.updateByPrimaryKeySelective(entity);
        return repository.withIdentity(repository.getId(), repository.getCreatedAt(), entity.getUpdatedAt());
    }

    @Override
    public void delete(RepositoryId id) {
        repositoryEntityMbgMapper.deleteByPrimaryKey(id.getValue());
    }

    @Override
    public Optional<Repository> findById(RepositoryId id) {
        RepositoryEntity entity = repositoryEntityMbgMapper.selectByPrimaryKey(id.getValue());
        return Optional.ofNullable(entity).map(repositoryDomainMapper::toDomain);
    }

    @Override
    public List<Repository> findAll() {
        return repositoryEntityMbgMapper.selectByConditionWithBLOBs(new RepositoryEntityCondition())
                .stream()
                .map(repositoryDomainMapper::toDomain)
                .toList();
//        return repositoryMapper.selectByConditionWithBLOBs(new RepositoryEntityCondition());
    }

    @Override
    public Optional<Repository> findByOwnerAndPath(OwnerType ownerType, OwnerId ownerId, RepositoryPath path) {
        RepositoryEntityCondition condition = new RepositoryEntityCondition();
        condition.createCriteria()
                .andOwnerTypeEqualTo(ownerType.name())
                .andOwnerIdEqualTo(ownerId.getValue())
                .andPathEqualTo(path.getValue());
        List<RepositoryEntity> entities = repositoryEntityMbgMapper.selectByConditionWithBLOBs(condition);
        return entities.stream().findFirst().map(repositoryDomainMapper::toDomain);
    }

    @Override
    public Optional<Repository> findByOwnerAndName(OwnerType ownerType, OwnerId ownerId, RepositoryName name) {
        RepositoryEntityCondition condition = new RepositoryEntityCondition();
        condition.createCriteria()
                .andOwnerTypeEqualTo(ownerType.name())
                .andOwnerIdEqualTo(ownerId.getValue())
                .andNameEqualTo(name.getValue());
        List<RepositoryEntity> entities = repositoryEntityMbgMapper.selectByConditionWithBLOBs(condition);
        return entities.stream().findFirst().map(repositoryDomainMapper::toDomain);
    }

    @Override
    public Optional<Long> findRepositoryId(String ownerNamespace, String repoName) {
        if (ownerNamespace == null || ownerNamespace.isBlank()) {
            return Optional.empty();
        }
        OwnerType ownerType;
        Long ownerId;
        if (ownerNamespace.startsWith("users/")) {
            ownerType = OwnerType.USER;
            String username = ownerNamespace.substring("users/".length());
            UserEntityCondition userCondition = new UserEntityCondition();
            userCondition.createCriteria().andUsernameEqualTo(username);
            List<UserEntity> users = userEntityMbgMapper.selectByCondition(userCondition);
            if (users.isEmpty()) {
                return Optional.empty();
            }
            ownerId = users.get(0).getId();
        } else {
            ownerType = OwnerType.ORGANIZATION;
            OrganizeEntityCondition organizeCondition = new OrganizeEntityCondition();
            organizeCondition.createCriteria().andNameEqualTo(ownerNamespace);
            List<OrganizeEntity> organizes = organizeEntityMbgMapper.selectByCondition(organizeCondition);
            if (organizes.isEmpty()) {
                return Optional.empty();
            }
            ownerId = organizes.get(0).getId();
        }
        RepositoryEntityCondition repositoryCondition = new RepositoryEntityCondition();
        repositoryCondition.createCriteria()
                .andOwnerTypeEqualTo(ownerType.name())
                .andOwnerIdEqualTo(ownerId)
                .andNameEqualTo(repoName);

        List<RepositoryEntity> repositories = repositoryEntityMbgMapper.selectByCondition(repositoryCondition);
        return repositories.stream()
                .findFirst()
                .map(RepositoryEntity::getId);
    }
}

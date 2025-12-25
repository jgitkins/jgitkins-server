package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.infrastructure.persistence.mapper.OrganizeEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.RepositoryEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.OrganizeEntity;
import io.jgitkins.server.infrastructure.persistence.model.OrganizeEntityCondition;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryEntity;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryEntityCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RepositoryAdapter implements RepositoryPort {

    private final OrganizeEntityMbgMapper organizeEntityMbgMapper;
    private final RepositoryEntityMbgMapper repositoryEntityMbgMapper;

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
    public Optional<Repository> findByOrganizeAndPath(OrganizeId organizeId, RepositoryPath path) {
        RepositoryEntityCondition condition = new RepositoryEntityCondition();
        condition.createCriteria()
                .andOrganizeIdEqualTo(organizeId.getValue())
                .andPathEqualTo(path.getValue());
        List<RepositoryEntity> entities = repositoryEntityMbgMapper.selectByConditionWithBLOBs(condition);
        return entities.stream().findFirst().map(repositoryDomainMapper::toDomain);
    }

    @Override
    public Optional<Repository> findByOrganizeAndName(OrganizeId organizeId, RepositoryName name) {
        RepositoryEntityCondition condition = new RepositoryEntityCondition();
        condition.createCriteria()
                .andOrganizeIdEqualTo(organizeId.getValue())
                .andNameEqualTo(name.getValue());
        List<RepositoryEntity> entities = repositoryEntityMbgMapper.selectByConditionWithBLOBs(condition);
        return entities.stream().findFirst().map(repositoryDomainMapper::toDomain);
    }

    @Override
    public Optional<Long> findRepositoryId(String taskCd, String repoName) {
        OrganizeEntityCondition organizeCondition = new OrganizeEntityCondition();
        organizeCondition.createCriteria().andNameEqualTo(taskCd);
        List<OrganizeEntity> organizes = organizeEntityMbgMapper.selectByCondition(organizeCondition);
        if (organizes.isEmpty()) {
            return Optional.empty();
        }

        Long organizeId = organizes.get(0).getId();
        RepositoryEntityCondition repositoryCondition = new RepositoryEntityCondition();
        repositoryCondition.createCriteria()
                .andOrganizeIdEqualTo(organizeId)
                .andNameEqualTo(repoName);

        List<RepositoryEntity> repositories = repositoryEntityMbgMapper.selectByCondition(repositoryCondition);
        return repositories.stream()
                .findFirst()
                .map(RepositoryEntity::getId);
    }
}

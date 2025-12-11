package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.OrganizePersistencePort;
import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizePath;
import io.jgitkins.server.infrastructure.persistence.mapper.OrganizeEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.OrganizeEntity;
import io.jgitkins.server.infrastructure.persistence.model.OrganizeEntityCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrganizePersistenceAdapter implements OrganizePersistencePort {

    private final OrganizeEntityMbgMapper organizeEntityMbgMapper;
    private final OrganizeDomainMapper organizeDomainMapper;

    @Override
    public Organize save(Organize organize) {
        OrganizeEntity entity = organizeDomainMapper.toEntity(organize);
        organizeEntityMbgMapper.insertSelective(entity);
        return organizeDomainMapper.toDomain(entity);
    }

    @Override
    public Organize update(Organize organize) {
        OrganizeEntity entity = organizeDomainMapper.toEntity(organize);
        organizeEntityMbgMapper.updateByPrimaryKeySelective(entity);
        return organizeDomainMapper.toDomain(entity);
    }

    @Override
    public Optional<Organize> findById(OrganizeId organizeId) {
        if (organizeId == null) {
            return Optional.empty();
        }
        OrganizeEntity entity = organizeEntityMbgMapper.selectByPrimaryKey(organizeId.getValue());
        return Optional.ofNullable(organizeDomainMapper.toDomain(entity));
    }

    @Override
    public Optional<Organize> findByPath(OrganizePath path) {
        OrganizeEntityCondition condition = new OrganizeEntityCondition();
        condition.createCriteria().andPathEqualTo(path.getValue());
        List<OrganizeEntity> entities = organizeEntityMbgMapper.selectByCondition(condition);
        return entities.stream().findFirst().map(organizeDomainMapper::toDomain);
    }

    @Override
    public List<Organize> findAll() {
        OrganizeEntityCondition condition = new OrganizeEntityCondition();
        List<OrganizeEntity> entities = organizeEntityMbgMapper.selectByCondition(condition);
        return entities.stream().map(organizeDomainMapper::toDomain).toList();
    }

    @Override
    public void delete(OrganizeId organizeId) {
        organizeEntityMbgMapper.deleteByPrimaryKey(organizeId.getValue());
    }
}

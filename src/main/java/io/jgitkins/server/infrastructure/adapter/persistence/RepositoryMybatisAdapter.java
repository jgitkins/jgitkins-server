package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.RepositoryLoadPort;
import io.jgitkins.server.infrastructure.persistence.mapper.OrganizeEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.mapper.RepositoryEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.OrganizeEntity;
import io.jgitkins.server.infrastructure.persistence.model.OrganizeEntityCondition;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryEntity;
import io.jgitkins.server.infrastructure.persistence.model.RepositoryEntityCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that resolves repository identities by traversing organize information.
 */
@Component
@RequiredArgsConstructor
public class RepositoryMybatisAdapter implements RepositoryLoadPort {

    private final OrganizeEntityMbgMapper organizeEntityMbgMapper;
    private final RepositoryEntityMbgMapper repositoryEntityMbgMapper;

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

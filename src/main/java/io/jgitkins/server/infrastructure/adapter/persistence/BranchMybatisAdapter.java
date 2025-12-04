package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.BranchPersistencePort;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.infrastructure.persistence.mapper.BranchEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.BranchEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BranchMybatisAdapter implements BranchPersistencePort {

    private final BranchMapper branchMapper;
    private final BranchEntityMbgMapper branchEntityMbgMapper;


    @Override
    public void create(Long repositoryId, String name) {
        Branch branch = Branch.create(repositoryId, name);
        BranchEntity branchEntity = branchMapper.toEntity(branch);
        branchEntityMbgMapper.insertSelective(branchEntity);
    }

    @Override
    public void delete(Long id) {
        branchEntityMbgMapper.deleteByPrimaryKey(id);
    }
}

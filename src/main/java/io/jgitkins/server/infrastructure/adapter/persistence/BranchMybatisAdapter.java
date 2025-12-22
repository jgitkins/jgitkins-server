package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.application.port.out.BranchPersistenceCommandPort;
import io.jgitkins.server.application.port.out.BranchPersistenceLoadPort;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.infrastructure.persistence.mapper.BranchEntityMbgMapper;
import io.jgitkins.server.infrastructure.persistence.model.BranchEntity;
import io.jgitkins.server.infrastructure.persistence.model.BranchEntityCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BranchMybatisAdapter implements BranchPersistenceCommandPort, BranchPersistenceLoadPort {

    private final BranchDomainMapper branchDomainMapper;
    private final BranchEntityMbgMapper branchEntityMbgMapper;


    @Override
    public void create(Branch branch) {
        BranchEntity branchEntity = branchDomainMapper.toEntity(branch);
        branchEntityMbgMapper.insertSelective(branchEntity);
    }

    @Override
    public void delete(Long repositoryId, String branchName) {
        BranchEntityCondition condition = new BranchEntityCondition();
        condition.createCriteria()
                .andRepositoryIdEqualTo(repositoryId)
                .andNameEqualTo(branchName);
        branchEntityMbgMapper.deleteByCondition(condition);
    }


    /***
     * query
     */
    @Override
    public Optional<Branch> getBranch(Long repositoryId, String branch) {

        BranchEntityCondition condition = new BranchEntityCondition();
        condition.createCriteria()
                .andRepositoryIdEqualTo(repositoryId)
                .andNameEqualTo(branch);

        return branchEntityMbgMapper.selectByCondition(condition)
                .stream()
                .findFirst()
                .map(branchDomainMapper::toDomain);
    }

    @Override
    public List<Branch> getBranches(Long repositoryId) {

        BranchEntityCondition condition = new BranchEntityCondition();
        condition.createCriteria()
                .andRepositoryIdEqualTo(repositoryId);

        return branchEntityMbgMapper.selectByCondition(condition)
                .stream()
                .map(branchDomainMapper::toDomain)
                .toList();

    }

}

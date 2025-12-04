package io.jgitkins.server.infrastructure.adapter.persistence;

import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.infrastructure.persistence.model.BranchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    BranchMapper INSTANCE = Mappers.getMapper(BranchMapper.class);

    // 도메인 모델 -> 영속성 모델 변환
    BranchEntity toEntity(Branch branch);

    // 영속성 모델 -> 도메인 모델 변환
//    Branch fromEntity(BranchEntity branchEntity);
}


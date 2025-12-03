package io.jgitkins.server.infrastructure.persistence.mapper;

import io.jgitkins.server.infrastructure.persistence.model.UserEntity;
import io.jgitkins.server.infrastructure.persistence.model.UserEntityCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserEntityMbgMapper {
    long countByCondition(UserEntityCondition example);

    int deleteByCondition(UserEntityCondition example);

    int insert(UserEntity row);

    int insertSelective(UserEntity row);

    List<UserEntity> selectByCondition(UserEntityCondition example);

    int updateByConditionSelective(@Param("row") UserEntity row, @Param("example") UserEntityCondition example);

    int updateByCondition(@Param("row") UserEntity row, @Param("example") UserEntityCondition example);
}
package com.ape.apeadmin.mapper;

import com.ape.apeadmin.domain.ApePhysicsLawFavor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApePhysicsLawFavorMapper {
    int deleteByPrimaryKey(String id);
    int insert(ApePhysicsLawFavor record);
    int insertSelective(ApePhysicsLawFavor record);
    ApePhysicsLawFavor selectByPrimaryKey(String id);
    int updateByPrimaryKeySelective(ApePhysicsLawFavor record);
    int updateByPrimaryKey(ApePhysicsLawFavor record);
    List<ApePhysicsLawFavor> selectByUserId(String userId);
    ApePhysicsLawFavor selectByUserIdAndLawName(@Param("userId") String userId, @Param("lawName") String lawName);
}

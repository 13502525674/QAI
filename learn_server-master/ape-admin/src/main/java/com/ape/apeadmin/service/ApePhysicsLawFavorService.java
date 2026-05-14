package com.ape.apeadmin.service;

import com.ape.apeadmin.domain.ApePhysicsLawFavor;
import java.util.List;

public interface ApePhysicsLawFavorService {
    int deleteByPrimaryKey(String id);
    int insert(ApePhysicsLawFavor record);
    int insertSelective(ApePhysicsLawFavor record);
    ApePhysicsLawFavor selectByPrimaryKey(String id);
    int updateByPrimaryKeySelective(ApePhysicsLawFavor record);
    int updateByPrimaryKey(ApePhysicsLawFavor record);
    List<ApePhysicsLawFavor> selectByUserId(String userId);
    ApePhysicsLawFavor selectByUserIdAndLawName(String userId, String lawName);
}

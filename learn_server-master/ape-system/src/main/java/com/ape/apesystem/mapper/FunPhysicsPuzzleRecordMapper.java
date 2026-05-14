package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.FunPhysicsPuzzleRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FunPhysicsPuzzleRecordMapper extends BaseMapper<FunPhysicsPuzzleRecord> {

    int countCompletedByUser(@Param("userId") String userId);

    int countCompletedByUserWithoutHint(@Param("userId") String userId, @Param("difficulty") Integer difficulty);

    List<Map<String, Object>> getUserPuzzleStats(@Param("userId") String userId);
}

package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.FunPhysicsAnswerRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface FunPhysicsAnswerRecordMapper extends BaseMapper<FunPhysicsAnswerRecord> {

    int countCorrectByUser(@Param("userId") String userId);

    int countCorrectByUserToday(@Param("userId") String userId);

    int getConsecutiveDays(@Param("userId") String userId);

    List<Map<String, Object>> getUserAnswerStats(@Param("userId") String userId);
    
    int countDistinctChallengeDates(@Param("userId") String userId);
}

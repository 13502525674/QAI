package com.ape.apesystem.service;

import com.ape.apesystem.domain.FunPhysicsAnswerRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface FunPhysicsAnswerRecordService extends IService<FunPhysicsAnswerRecord> {

    int countCorrectByUser(String userId);

    int countCorrectByUserToday(String userId);

    int getConsecutiveDays(String userId);

    List<Map<String, Object>> getUserAnswerStats(String userId);

    boolean hasCompletedTodayChallenge(String userId);
    
    /**
     * 统计用户答题的天数（轮数）
     */
    int countDistinctChallengeDates(String userId);
}

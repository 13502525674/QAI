package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.FunPhysicsAnswerRecord;
import com.ape.apesystem.mapper.FunPhysicsAnswerRecordMapper;
import com.ape.apesystem.service.FunPhysicsAnswerRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class FunPhysicsAnswerRecordServiceImpl extends ServiceImpl<FunPhysicsAnswerRecordMapper, FunPhysicsAnswerRecord> implements FunPhysicsAnswerRecordService {

    @Override
    public int countCorrectByUser(String userId) {
        return baseMapper.countCorrectByUser(userId);
    }

    @Override
    public int countCorrectByUserToday(String userId) {
        return baseMapper.countCorrectByUserToday(userId);
    }

    @Override
    public int getConsecutiveDays(String userId) {
        return baseMapper.getConsecutiveDays(userId);
    }

    @Override
    public List<Map<String, Object>> getUserAnswerStats(String userId) {
        return baseMapper.getUserAnswerStats(userId);
    }

    @Override
    public boolean hasCompletedTodayChallenge(String userId) {
        QueryWrapper<FunPhysicsAnswerRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("challenge_date", LocalDate.now());
        return count(wrapper) > 0;
    }
    
    @Override
    public int countDistinctChallengeDates(String userId) {
        return baseMapper.countDistinctChallengeDates(userId);
    }
}

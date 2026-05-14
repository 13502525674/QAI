package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.FunPhysicsPuzzleRecord;
import com.ape.apesystem.mapper.FunPhysicsPuzzleRecordMapper;
import com.ape.apesystem.service.FunPhysicsPuzzleRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FunPhysicsPuzzleRecordServiceImpl extends ServiceImpl<FunPhysicsPuzzleRecordMapper, FunPhysicsPuzzleRecord> implements FunPhysicsPuzzleRecordService {

    @Override
    public int countCompletedByUser(String userId) {
        return baseMapper.countCompletedByUser(userId);
    }

    @Override
    public int countCompletedByUserWithoutHint(String userId, Integer difficulty) {
        return baseMapper.countCompletedByUserWithoutHint(userId, difficulty);
    }

    @Override
    public List<Map<String, Object>> getUserPuzzleStats(String userId) {
        return baseMapper.getUserPuzzleStats(userId);
    }

    @Override
    public boolean hasCompletedPuzzle(String userId, String puzzleId) {
        QueryWrapper<FunPhysicsPuzzleRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("puzzle_id", puzzleId);
        wrapper.eq("is_completed", 1);
        return count(wrapper) > 0;
    }
}

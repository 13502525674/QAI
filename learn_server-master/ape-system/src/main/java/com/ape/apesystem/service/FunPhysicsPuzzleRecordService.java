package com.ape.apesystem.service;

import com.ape.apesystem.domain.FunPhysicsPuzzleRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface FunPhysicsPuzzleRecordService extends IService<FunPhysicsPuzzleRecord> {

    int countCompletedByUser(String userId);

    int countCompletedByUserWithoutHint(String userId, Integer difficulty);

    List<Map<String, Object>> getUserPuzzleStats(String userId);

    boolean hasCompletedPuzzle(String userId, String puzzleId);
}

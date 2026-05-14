package com.ape.apeadmin.mapper;

import com.ape.apeadmin.domain.ApeStudyCountdownGoal;

import java.util.List;

public interface ApeStudyCountdownGoalMapper {
    int insert(ApeStudyCountdownGoal record);
    List<ApeStudyCountdownGoal> selectByUserId(String userId);
    int deleteByPrimaryKey(String id);
}

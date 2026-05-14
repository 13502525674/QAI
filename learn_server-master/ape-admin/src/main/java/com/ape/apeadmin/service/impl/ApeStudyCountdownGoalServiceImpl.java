package com.ape.apeadmin.service.impl;

import com.ape.apeadmin.domain.ApeStudyCountdownGoal;
import com.ape.apeadmin.mapper.ApeStudyCountdownGoalMapper;
import com.ape.apeadmin.service.ApeStudyCountdownGoalService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApeStudyCountdownGoalServiceImpl implements ApeStudyCountdownGoalService {

    @Autowired
    private ApeStudyCountdownGoalMapper mapper;

    @Override
    public int insert(ApeStudyCountdownGoal record) {
        if (record.getId() == null || record.getId().isEmpty()) {
            record.setId(IdWorker.get32UUID());
        }
        if (record.getCreateTime() == null) {
            record.setCreateTime(new Date());
        }
        return mapper.insert(record);
    }

    @Override
    public List<ApeStudyCountdownGoal> selectByUserId(String userId) {
        return mapper.selectByUserId(userId);
    }

    @Override
    public int deleteByPrimaryKey(String id) {
        return mapper.deleteByPrimaryKey(id);
    }
}

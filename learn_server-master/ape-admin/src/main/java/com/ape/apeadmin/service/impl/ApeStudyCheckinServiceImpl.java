package com.ape.apeadmin.service.impl;

import com.ape.apeadmin.domain.ApeStudyCheckin;
import com.ape.apeadmin.mapper.ApeStudyCheckinMapper;
import com.ape.apeadmin.service.ApeStudyCheckinService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApeStudyCheckinServiceImpl implements ApeStudyCheckinService {

    @Autowired
    private ApeStudyCheckinMapper checkinMapper;

    @Override
    public int insert(String userId, String checkinDate) {
        ApeStudyCheckin record = new ApeStudyCheckin();
        record.setId(IdWorker.get32UUID());
        record.setUserId(userId);
        record.setCheckinDate(checkinDate);
        record.setCreateTime(new Date());
        return checkinMapper.insert(record);
    }

    @Override
    public List<String> selectCheckinDatesByUserId(String userId) {
        return checkinMapper.selectCheckinDatesByUserId(userId);
    }

    @Override
    public boolean hasCheckedIn(String userId, String checkinDate) {
        return checkinMapper.countByUserIdAndDate(userId, checkinDate) > 0;
    }
}

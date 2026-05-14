package com.ape.apeadmin.service.impl;

import com.ape.apeadmin.domain.ApeStudySeat;
import com.ape.apeadmin.mapper.ApeStudySeatMapper;
import com.ape.apeadmin.service.ApeStudySeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApeStudySeatServiceImpl implements ApeStudySeatService {

    @Autowired
    private ApeStudySeatMapper seatMapper;

    @Override
    public int deleteByPrimaryKey(String id) {
        return seatMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ApeStudySeat record) {
        return seatMapper.insert(record);
    }

    @Override
    public int insertSelective(ApeStudySeat record) {
        return seatMapper.insertSelective(record);
    }

    @Override
    public ApeStudySeat selectByPrimaryKey(String id) {
        return seatMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ApeStudySeat record) {
        return seatMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ApeStudySeat record) {
        return seatMapper.updateByPrimaryKey(record);
    }

    @Override
    public List<ApeStudySeat> list() {
        return seatMapper.selectAll();
    }

    @Override
    public ApeStudySeat selectBySeatId(String seatId) {
        return seatMapper.selectBySeatId(seatId);
    }

    @Override
    public List<ApeStudySeat> selectByUserId(String userId) {
        return seatMapper.selectByUserId(userId);
    }

    @Override
    public List<ApeStudySeat> selectByStatus(Integer status) {
        return seatMapper.selectByStatus(status);
    }
}

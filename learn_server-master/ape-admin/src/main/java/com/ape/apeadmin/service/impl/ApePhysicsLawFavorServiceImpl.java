package com.ape.apeadmin.service.impl;

import com.ape.apeadmin.domain.ApePhysicsLawFavor;
import com.ape.apeadmin.mapper.ApePhysicsLawFavorMapper;
import com.ape.apeadmin.service.ApePhysicsLawFavorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApePhysicsLawFavorServiceImpl implements ApePhysicsLawFavorService {

    @Autowired
    private ApePhysicsLawFavorMapper lawFavorMapper;

    @Override
    public int deleteByPrimaryKey(String id) {
        return lawFavorMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ApePhysicsLawFavor record) {
        return lawFavorMapper.insert(record);
    }

    @Override
    public int insertSelective(ApePhysicsLawFavor record) {
        return lawFavorMapper.insertSelective(record);
    }

    @Override
    public ApePhysicsLawFavor selectByPrimaryKey(String id) {
        return lawFavorMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ApePhysicsLawFavor record) {
        return lawFavorMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ApePhysicsLawFavor record) {
        return lawFavorMapper.updateByPrimaryKey(record);
    }

    @Override
    public List<ApePhysicsLawFavor> selectByUserId(String userId) {
        return lawFavorMapper.selectByUserId(userId);
    }

    @Override
    public ApePhysicsLawFavor selectByUserIdAndLawName(String userId, String lawName) {
        return lawFavorMapper.selectByUserIdAndLawName(userId, lawName);
    }
}

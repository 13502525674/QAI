package com.ape.apeadmin.service.impl;

import com.ape.apeadmin.domain.ApeNotebookRecord;
import com.ape.apeadmin.mapper.ApeNotebookRecordMapper;
import com.ape.apeadmin.service.ApeNotebookRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApeNotebookRecordServiceImpl implements ApeNotebookRecordService {

    @Autowired
    private ApeNotebookRecordMapper notebookMapper;

    @Override
    public int deleteByPrimaryKey(String id) {
        return notebookMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ApeNotebookRecord record) {
        return notebookMapper.insert(record);
    }

    @Override
    public int insertSelective(ApeNotebookRecord record) {
        return notebookMapper.insertSelective(record);
    }

    @Override
    public ApeNotebookRecord selectByPrimaryKey(String id) {
        return notebookMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ApeNotebookRecord record) {
        return notebookMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ApeNotebookRecord record) {
        return notebookMapper.updateByPrimaryKey(record);
    }

    @Override
    public List<ApeNotebookRecord> selectByUserId(String userId) {
        return notebookMapper.selectByUserId(userId);
    }
}

package com.ape.apeadmin.service.impl;

import com.ape.apeadmin.domain.ApeStudyTodo;
import com.ape.apeadmin.mapper.ApeStudyTodoMapper;
import com.ape.apeadmin.service.ApeStudyTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApeStudyTodoServiceImpl implements ApeStudyTodoService {

    @Autowired
    private ApeStudyTodoMapper todoMapper;

    @Override
    public int deleteByPrimaryKey(String id) {
        return todoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ApeStudyTodo record) {
        return todoMapper.insert(record);
    }

    @Override
    public int insertSelective(ApeStudyTodo record) {
        return todoMapper.insertSelective(record);
    }

    @Override
    public ApeStudyTodo selectByPrimaryKey(String id) {
        return todoMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(ApeStudyTodo record) {
        return todoMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ApeStudyTodo record) {
        return todoMapper.updateByPrimaryKey(record);
    }

    @Override
    public List<ApeStudyTodo> selectByUserId(String userId) {
        return todoMapper.selectByUserId(userId);
    }

    @Override
    public List<ApeStudyTodo> selectByUserIdAndCompleted(String userId, Integer completed) {
        return todoMapper.selectByUserIdAndCompleted(userId, completed);
    }
}

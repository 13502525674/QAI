package com.ape.apeadmin.service;

import com.ape.apeadmin.domain.ApeStudyTodo;
import java.util.List;

public interface ApeStudyTodoService {
    int deleteByPrimaryKey(String id);
    int insert(ApeStudyTodo record);
    int insertSelective(ApeStudyTodo record);
    ApeStudyTodo selectByPrimaryKey(String id);
    int updateByPrimaryKeySelective(ApeStudyTodo record);
    int updateByPrimaryKey(ApeStudyTodo record);
    List<ApeStudyTodo> selectByUserId(String userId);
    List<ApeStudyTodo> selectByUserIdAndCompleted(String userId, Integer completed);
}

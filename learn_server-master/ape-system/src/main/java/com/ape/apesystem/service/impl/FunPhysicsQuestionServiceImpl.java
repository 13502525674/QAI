package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.FunPhysicsQuestion;
import com.ape.apesystem.mapper.FunPhysicsQuestionMapper;
import com.ape.apesystem.service.FunPhysicsQuestionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class FunPhysicsQuestionServiceImpl extends ServiceImpl<FunPhysicsQuestionMapper, FunPhysicsQuestion> implements FunPhysicsQuestionService {

    @Override
    public Page<FunPhysicsQuestion> getPage(FunPhysicsQuestion question) {
        Page<FunPhysicsQuestion> page = new Page<>(question.getPageNumber() != null ? question.getPageNumber() : 1,
                question.getPageSize() != null ? question.getPageSize() : 10);
        return baseMapper.getPage(page, question);
    }

    @Override
    public List<FunPhysicsQuestion> getDailyQuestions(int count) {
        return getRandomQuestions(count, null);
    }

    @Override
    public List<FunPhysicsQuestion> getRandomQuestions(int count, Integer type) {
        return baseMapper.getRandomQuestions(count, type);
    }

    @Override
    public Map<String, Object> getTodayChallengeStatus(String userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("date", LocalDate.now().toString());
        return result;
    }

    @Override
    public List<FunPhysicsQuestion> getTodayChallengeQuestions(String userId, int count) {
        QueryWrapper<FunPhysicsQuestion> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.orderByAsc("RAND()");
        wrapper.last("LIMIT " + count);
        return list(wrapper);
    }
}

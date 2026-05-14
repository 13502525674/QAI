package com.ape.apesystem.service;

import com.ape.apesystem.domain.FunPhysicsQuestion;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface FunPhysicsQuestionService extends IService<FunPhysicsQuestion> {

    Page<FunPhysicsQuestion> getPage(FunPhysicsQuestion question);

    List<FunPhysicsQuestion> getDailyQuestions(int count);

    List<FunPhysicsQuestion> getRandomQuestions(int count, Integer type);

    Map<String, Object> getTodayChallengeStatus(String userId);

    List<FunPhysicsQuestion> getTodayChallengeQuestions(String userId, int count);
}

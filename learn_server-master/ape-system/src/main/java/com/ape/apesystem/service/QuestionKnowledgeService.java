package com.ape.apesystem.service;

import com.ape.apesystem.domain.QuestionKnowledge;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface QuestionKnowledgeService extends IService<QuestionKnowledge> {
    
    List<QuestionKnowledge> getByQuestionId(String questionId);
    
    List<QuestionKnowledge> getByKpId(String kpId);
    
    void saveQuestionKps(String questionId, String questionType, List<String> kpIds);
    
    void deleteByQuestionId(String questionId);
}

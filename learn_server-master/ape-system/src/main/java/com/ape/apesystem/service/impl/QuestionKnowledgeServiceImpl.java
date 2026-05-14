package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.QuestionKnowledge;
import com.ape.apesystem.mapper.QuestionKnowledgeMapper;
import com.ape.apesystem.service.QuestionKnowledgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class QuestionKnowledgeServiceImpl extends ServiceImpl<QuestionKnowledgeMapper, QuestionKnowledge> 
        implements QuestionKnowledgeService {

    @Autowired
    private QuestionKnowledgeMapper questionKnowledgeMapper;

    @Override
    public List<QuestionKnowledge> getByQuestionId(String questionId) {
        return questionKnowledgeMapper.selectByQuestionId(questionId);
    }

    @Override
    public List<QuestionKnowledge> getByKpId(String kpId) {
        return questionKnowledgeMapper.selectByKpId(kpId);
    }

    @Override
    @Transactional
    public void saveQuestionKps(String questionId, String questionType, List<String> kpIds) {
        questionKnowledgeMapper.deleteByQuestionId(questionId);
        
        if (kpIds != null && !kpIds.isEmpty()) {
            List<QuestionKnowledge> list = new ArrayList<>();
            for (String kpId : kpIds) {
                QuestionKnowledge qk = new QuestionKnowledge();
                qk.setQuestionId(questionId);
                qk.setQuestionType(questionType);
                qk.setKpId(kpId);
                qk.setWeight(1.0f);
                qk.setCreateTime(new Date());
                list.add(qk);
            }
            saveBatch(list);
        }
    }

    @Override
    public void deleteByQuestionId(String questionId) {
        questionKnowledgeMapper.deleteByQuestionId(questionId);
    }
}

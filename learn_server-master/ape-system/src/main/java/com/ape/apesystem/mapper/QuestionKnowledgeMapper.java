package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.QuestionKnowledge;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface QuestionKnowledgeMapper extends BaseMapper<QuestionKnowledge> {
    
    List<QuestionKnowledge> selectByQuestionId(@Param("questionId") String questionId);
    
    List<QuestionKnowledge> selectByKpId(@Param("kpId") String kpId);
    
    void deleteByQuestionId(@Param("questionId") String questionId);
}

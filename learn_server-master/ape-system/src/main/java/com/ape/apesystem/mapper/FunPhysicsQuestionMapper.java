package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.FunPhysicsQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FunPhysicsQuestionMapper extends BaseMapper<FunPhysicsQuestion> {

    Page<FunPhysicsQuestion> getPage(Page<FunPhysicsQuestion> page, @Param("ew")FunPhysicsQuestion question);

    List<FunPhysicsQuestion> getRandomQuestions(@Param("count") int count, @Param("type") Integer type);
}

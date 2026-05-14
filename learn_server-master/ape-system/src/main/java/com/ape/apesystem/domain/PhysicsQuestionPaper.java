package com.ape.apesystem.domain;

import com.ape.apecommon.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 物理试卷
 * @date 2024-01-24 10:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("physics_question_paper")
public class PhysicsQuestionPaper extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 试卷标题
     */
    private String paperTitle;

    /**
     * 物理分支：运动学/力学/电学/光学/热学
     */
    private String subjectBranch;

    /**
     * 原始文档路径
     */
    private String documentPath;

    /**
     * 总题数
     */
    private Integer totalQuestions;

    /**
     * 选择题数量
     */
    private Integer choiceQuestionsCount;

    /**
     * 填空题数量
     */
    private Integer fillInQuestionsCount;

    /**
     * 计算题数量
     */
    private Integer calculationQuestionsCount;

    /**
     * 试卷题目内容（解析后的）
     */
    private String questionContent;

    /**
     * 试卷答案内容（解析后的）
     */
    private String answerContent;

    /**
     * 创建者ID
     */
    private String creatorId;
    // createBy, createTime, updateBy, updateTime 字段从父类BaseEntity继承

    @TableField(exist = false)
    private Integer pageNumber;

    @TableField(exist = false)
    private Integer pageSize;
}
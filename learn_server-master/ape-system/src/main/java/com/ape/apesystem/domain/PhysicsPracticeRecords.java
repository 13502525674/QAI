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
 * @description: 物理练习记录
 * @date 2024-01-24 10:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("physics_practice_records")
public class PhysicsPracticeRecords extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 试卷ID
     */
    private String paperId;

    /**
     * 试卷名称 (冗余字段，便于快速查询)
     */
    private String paperName;

    /**
     * 用户答案
     */
    private String userAnswers;

    /**
     * 提交时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submittedAt;

    /**
     * 得分（待人工评分）
     */
    private Double score;

    /**
     * 状态：submitted-已提交，graded-已评分
     */
    private String status;

    /**
     * 评分员ID
     */
    private String graderId;

    /**
     * 评分人名称 (e.g. AI, 老师姓名)
     */
    private String gradedBy;

    /**
     * 评分时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gradedAt;

    /**
     * 评分详情（JSON格式存储逐题评分信息）
     */
    private String gradingDetails;

    // createBy, createTime, updateBy, updateTime 字段从父类BaseEntity继承

    @TableField(exist = false)
    private Integer pageNumber;

    @TableField(exist = false)
    private Integer pageSize;
}
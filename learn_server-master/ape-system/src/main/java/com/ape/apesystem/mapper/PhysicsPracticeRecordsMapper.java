package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.PhysicsPracticeRecords;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 物理练习记录Mapper接口
 * @date 2024-01-24 10:00
 */
@Mapper
public interface PhysicsPracticeRecordsMapper extends BaseMapper<PhysicsPracticeRecords> {

    /**
     * 获取练习记录列表（包含试卷和学生信息）
     */
    @Select("<script>" +
            "SELECT r.*, " +
            "u.user_name as studentName, " +
            "p.paper_title as paperTitle, " +
            "p.question_content as questionContent " +
            "FROM physics_practice_records r " +
            "LEFT JOIN ape_user u ON r.user_id = u.id " +
            "LEFT JOIN physics_question_paper p ON r.paper_id = p.id " +
            "WHERE 1=1 " +
            "<if test='status != null and status != \"\"'>" +
            "AND r.status = #{status} " +
            "</if>" +
            "<if test='status == null or status == \"\"'>" +
            "AND r.status IN ('submitted', 'graded') " +
            "</if>" +
            "<if test='paperTitle != null and paperTitle != \"\"'>" +
            "AND p.paper_title LIKE CONCAT('%', #{paperTitle}, '%') " +
            "</if>" +
            "<if test='studentName != null and studentName != \"\"'>" +
            "AND u.user_name LIKE CONCAT('%', #{studentName}, '%') " +
            "</if>" +
            "ORDER BY r.submitted_at DESC " +
            "</script>")
    IPage<Map<String, Object>> getGradingListWithDetails(
            Page<Map<String, Object>> page,
            @Param("paperTitle") String paperTitle,
            @Param("studentName") String studentName,
            @Param("status") String status
    );

    /**
     * 根据ID获取练习记录（包含试卷和学生信息）
     */
    @Select("SELECT r.id, r.user_id as userId, r.paper_id as paperId, r.user_answers as userAnswers, " +
            "r.status, r.score, r.grader_id as graderId, r.graded_at as gradedAt, " +
            "r.grading_details as gradingDetails, r.submitted_at as submittedAt, " +
            "r.create_time as createTime, r.update_time as updateTime, r.create_by as createBy, r.update_by as updateBy, " +
            "u.user_name as studentName, " +
            "p.paper_title as paperTitle, " +
            "p.question_content as questionContent " +
            "FROM physics_practice_records r " +
            "LEFT JOIN ape_user u ON r.user_id = u.id " +
            "LEFT JOIN physics_question_paper p ON r.paper_id = p.id " +
            "WHERE r.id = #{id}")
    Map<String, Object> getRecordWithDetails(@Param("id") String id);

    /**
     * 获取用户练习记录列表（包含试卷信息）
     */
    List<Map<String, Object>> getUserRecordsWithDetails(
            @Param("userId") String userId,
            @Param("paperId") String paperId
    );
}

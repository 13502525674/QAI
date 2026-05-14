package com.ape.apeadmin.controller.physics;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.PhysicsPracticeRecords;
import com.ape.apesystem.domain.PhysicsQuestionPaper;
import com.ape.apesystem.service.PhysicsPracticeRecordsService;
import com.ape.apesystem.service.PhysicsQuestionPaperService;
import com.ape.apesystem.service.StudentMasteryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson2.JSON;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 物理练习记录控制器
 * @date 2024-01-24 10:00
 */
@Controller
@ResponseBody
@RequestMapping("physics/practiceRecords")
public class PhysicsPracticeRecordsController {

    @Autowired
    private PhysicsPracticeRecordsService physicsPracticeRecordsService;

    @Autowired
    private PhysicsQuestionPaperService physicsQuestionPaperService;

    @Autowired
    private StudentMasteryService studentMasteryService;

    /** 分页获取物理练习记录 */
    @Log(name = "分页获取物理练习记录", type = BusinessType.OTHER)
    @PostMapping("getPage")
    public Result getPage(@RequestBody PhysicsPracticeRecords physicsPracticeRecords) {
        // 确保页码和页面大小不为空且有效
        Integer pageNumber = physicsPracticeRecords.getPageNumber();
        Integer pageSize = physicsPracticeRecords.getPageSize();
        
        if (pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
        
        Page<PhysicsPracticeRecords> page = new Page<>(pageNumber, pageSize);
        QueryWrapper<PhysicsPracticeRecords> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(physicsPracticeRecords.getUserId()), PhysicsPracticeRecords::getUserId, physicsPracticeRecords.getUserId())
                .eq(StringUtils.isNotBlank(physicsPracticeRecords.getPaperId()), PhysicsPracticeRecords::getPaperId, physicsPracticeRecords.getPaperId())
                .eq(StringUtils.isNotBlank(physicsPracticeRecords.getStatus()), PhysicsPracticeRecords::getStatus, physicsPracticeRecords.getStatus())
                .orderByDesc(PhysicsPracticeRecords::getSubmittedAt);
        Page<PhysicsPracticeRecords> physicsPracticeRecordsPage = physicsPracticeRecordsService.page(page, queryWrapper);
        return Result.success(physicsPracticeRecordsPage);
    }

    /** 根据id获取物理练习记录（包含试卷和学生信息） */
    @Log(name = "根据id获取物理练习记录", type = BusinessType.OTHER)
    @GetMapping("getById")
    public Result getById(@RequestParam("id") String id) {
        Map<String, Object> record = physicsPracticeRecordsService.getRecordWithDetails(id);
        return Result.success(record);
    }

    /** 保存物理练习记录 */
    @Log(name = "保存物理练习记录", type = BusinessType.INSERT)
    @PostMapping("save")
    public Result save(@RequestBody PhysicsPracticeRecords physicsPracticeRecords) {
        physicsPracticeRecords.setUserId(ShiroUtils.getUserInfo().getId());
        physicsPracticeRecords.setSubmittedAt(new Date());
        
        // Populate paperName if missing
        if (StringUtils.isBlank(physicsPracticeRecords.getPaperName()) && StringUtils.isNotBlank(physicsPracticeRecords.getPaperId())) {
            PhysicsQuestionPaper paper = physicsQuestionPaperService.getById(physicsPracticeRecords.getPaperId());
            if (paper != null) {
                physicsPracticeRecords.setPaperName(paper.getPaperTitle());
            }
        }
        
        boolean save = physicsPracticeRecordsService.save(physicsPracticeRecords);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 提交物理练习 */
    @Log(name = "提交物理练习", type = BusinessType.INSERT)
    @PostMapping("submitPractice")
    public Result submitPractice(@RequestBody PhysicsPracticeRecords physicsPracticeRecords) {
        // 设置用户ID和提交时间
        physicsPracticeRecords.setUserId(ShiroUtils.getUserInfo().getId());
        physicsPracticeRecords.setSubmittedAt(new Date());
        physicsPracticeRecords.setStatus("submitted"); // 设置为已提交状态
        
        // Populate paperName if missing
        if (StringUtils.isBlank(physicsPracticeRecords.getPaperName()) && StringUtils.isNotBlank(physicsPracticeRecords.getPaperId())) {
            PhysicsQuestionPaper paper = physicsQuestionPaperService.getById(physicsPracticeRecords.getPaperId());
            if (paper != null) {
                physicsPracticeRecords.setPaperName(paper.getPaperTitle());
            }
        }
        
        boolean save = physicsPracticeRecordsService.save(physicsPracticeRecords);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 评分物理练习 */
    @Log(name = "评分物理练习", type = BusinessType.UPDATE)
    @PostMapping("gradePractice")
    public Result gradePractice(@RequestBody PhysicsPracticeRecords physicsPracticeRecords) {
        PhysicsPracticeRecords existingRecord = physicsPracticeRecordsService.getById(physicsPracticeRecords.getId());
        if (existingRecord == null) {
            return Result.fail("练习记录不存在");
        }
        
        // 设置评分信息
        existingRecord.setScore(physicsPracticeRecords.getScore());
        existingRecord.setStatus("graded");
        existingRecord.setGraderId(ShiroUtils.getUserInfo().getId());
        existingRecord.setGradedBy(ShiroUtils.getUserInfo().getUserName());
        existingRecord.setGradedAt(new Date());
        
        boolean update = physicsPracticeRecordsService.updateById(existingRecord);
        if (update) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 详细评分物理练习（支持逐题评分） */
    @Log(name = "详细评分物理练习", type = BusinessType.UPDATE)
    @PostMapping("gradePracticeDetailed")
    public Result gradePracticeDetailed(@RequestBody Map<String, Object> gradingData) {
        String recordId = (String) gradingData.get("id");
        if (StringUtils.isBlank(recordId)) {
            return Result.fail("练习记录ID不能为空");
        }
        
        PhysicsPracticeRecords existingRecord = physicsPracticeRecordsService.getById(recordId);
        if (existingRecord == null) {
            return Result.fail("练习记录不存在");
        }
        
        // 解析评分数据
        Double totalScoreDouble = gradingData.get("totalScore") != null ? 
            Double.parseDouble(gradingData.get("totalScore").toString()) : 0.0;
        String status = (String) gradingData.get("status");
        
        // 设置评分信息
        existingRecord.setScore(totalScoreDouble);
        existingRecord.setStatus(status);
        existingRecord.setGraderId(ShiroUtils.getUserInfo().getId());
        existingRecord.setGradedBy(ShiroUtils.getUserInfo().getUserName());
        existingRecord.setGradedAt(new Date());
        
        // 保存详细的评分信息（可以存储在扩展字段中）
        if (gradingData.get("questions") != null) {
            existingRecord.setGradingDetails(JSON.toJSONString(gradingData.get("questions")));
            
            // 更新学生掌握度
            List<Map<String, Object>> questions = (List<Map<String, Object>>) gradingData.get("questions");
            if (questions != null) {
                float totalScoreFloat = totalScoreDouble.floatValue();
                for (Map<String, Object> question : questions) {
                    String questionId = (String) question.get("id");
                    Boolean isCorrect = question.get("isCorrect") != null ? 
                        Boolean.parseBoolean(question.get("isCorrect").toString()) : false;
                    
                    // 计算得分比例
                    float scoreRatio = totalScoreFloat > 0 ? 
                        (question.get("score") != null ? 
                            Float.parseFloat(question.get("score").toString()) / totalScoreFloat : 0f) : 0f;
                    
                    // 更新掌握度
                    studentMasteryService.updateMasteryFromPractice(
                        existingRecord.getUserId(), 
                        questionId, 
                        isCorrect, 
                        scoreRatio
                    );
                }
            }
        }
        
        boolean update = physicsPracticeRecordsService.updateById(existingRecord);
        if (update) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 获取用户物理练习记录 */
    @GetMapping("getUserRecords")
    public Result getUserRecords(@RequestParam(value = "paperId", required = false) String paperId) {
        String userId = ShiroUtils.getUserInfo().getId();
        QueryWrapper<PhysicsPracticeRecords> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(PhysicsPracticeRecords::getUserId, userId)
                .eq(StringUtils.isNotBlank(paperId), PhysicsPracticeRecords::getPaperId, paperId)
                .orderByDesc(PhysicsPracticeRecords::getSubmittedAt);
        return Result.success(physicsPracticeRecordsService.list(queryWrapper));
    }

    /** 获取用户物理练习记录（包含试卷信息） */
    @GetMapping("getUserRecordsWithDetails")
    public Result getUserRecordsWithDetails(@RequestParam(value = "paperId", required = false) String paperId) {
        String userId = ShiroUtils.getUserInfo().getId();
        List<Map<String, Object>> records = physicsPracticeRecordsService.getUserRecordsWithDetails(userId, paperId);
        return Result.success(records);
    }

    /** 获取待评分的练习记录 */
    @GetMapping("getPendingGrading")
    public Result getPendingGrading() {
        QueryWrapper<PhysicsPracticeRecords> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(PhysicsPracticeRecords::getStatus, "submitted")
                .orderByDesc(PhysicsPracticeRecords::getSubmittedAt);
        return Result.success(physicsPracticeRecordsService.list(queryWrapper));
    }

    /** 获取教师批改列表（包含试卷和学生信息） */
    @Log(name = "获取教师批改列表", type = BusinessType.OTHER)
    @PostMapping("getGradingList")
    public Result getGradingList(@RequestBody Map<String, Object> queryParams) {
        Integer pageNumber = queryParams.get("pageNumber") != null ? 
            Integer.parseInt(queryParams.get("pageNumber").toString()) : 1;
        Integer pageSize = queryParams.get("pageSize") != null ? 
            Integer.parseInt(queryParams.get("pageSize").toString()) : 10;
        
        String paperTitle = (String) queryParams.get("paperTitle");
        String studentName = (String) queryParams.get("studentName");
        String status = (String) queryParams.get("status");
        
        Page<Map<String, Object>> page = new Page<>(pageNumber, pageSize);
        IPage<Map<String, Object>> recordsPage = physicsPracticeRecordsService.getGradingListWithDetails(
            page, paperTitle, studentName, status
        );
        
        return Result.success(recordsPage);
    }

    /** 更新练习记录评分（支持详细评分） */
    @Log(name = "更新练习记录评分", type = BusinessType.UPDATE)
    @PostMapping("updatePracticeRecord")
    public Result updatePracticeRecord(@RequestBody Map<String, Object> updateData) {
        String recordId = (String) updateData.get("id");
        if (StringUtils.isBlank(recordId)) {
            return Result.fail("练习记录ID不能为空");
        }
        
        PhysicsPracticeRecords existingRecord = physicsPracticeRecordsService.getById(recordId);
        if (existingRecord == null) {
            return Result.fail("练习记录不存在");
        }
        
        // 更新评分信息
        if (updateData.get("questions") != null || updateData.get("overallComment") != null) {
            Map<String, Object> gradingData = new java.util.HashMap<>();
            if (updateData.get("questions") != null) {
                gradingData.put("questions", updateData.get("questions"));
            }
            if (updateData.get("overallComment") != null) {
                gradingData.put("overallComment", updateData.get("overallComment"));
            }
            existingRecord.setGradingDetails(JSON.toJSONString(gradingData));
        }
        
        if (updateData.get("totalScore") != null) {
            existingRecord.setScore(Double.parseDouble(updateData.get("totalScore").toString()));
        }
        
        if (updateData.get("status") != null) {
            existingRecord.setStatus((String) updateData.get("status"));
        }
        
        if ("graded".equals(existingRecord.getStatus())) {
            existingRecord.setGraderId(ShiroUtils.getUserInfo().getId());
            existingRecord.setGradedBy(ShiroUtils.getUserInfo().getUserName());
            existingRecord.setGradedAt(new Date());
        }
        
        boolean update = physicsPracticeRecordsService.updateById(existingRecord);
        if (update) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }
}
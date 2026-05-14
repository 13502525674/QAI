package com.ape.apesystem.service;

import com.ape.apesystem.domain.PhysicsPracticeRecords;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 物理练习记录Service接口
 * @date 2024-01-24 10:00
 */
public interface PhysicsPracticeRecordsService extends IService<PhysicsPracticeRecords> {

    /**
     * 获取练习记录列表（包含试卷和学生信息）
     */
    IPage<Map<String, Object>> getGradingListWithDetails(
            Page<Map<String, Object>> page,
            String paperTitle,
            String studentName,
            String status
    );

    /**
     * 根据ID获取练习记录（包含试卷和学生信息）
     */
    Map<String, Object> getRecordWithDetails(String id);

    /**
     * 获取用户练习记录列表（包含试卷信息）
     */
    List<Map<String, Object>> getUserRecordsWithDetails(String userId, String paperId);
}
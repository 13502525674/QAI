package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.PhysicsPracticeRecords;
import com.ape.apesystem.mapper.PhysicsPracticeRecordsMapper;
import com.ape.apesystem.service.PhysicsPracticeRecordsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 物理练习记录Service实现
 * @date 2024-01-24 10:00
 */
@Service
public class PhysicsPracticeRecordsServiceImpl extends ServiceImpl<PhysicsPracticeRecordsMapper, PhysicsPracticeRecords> implements PhysicsPracticeRecordsService {

    @Override
    public IPage<Map<String, Object>> getGradingListWithDetails(
            Page<Map<String, Object>> page,
            String paperTitle,
            String studentName,
            String status
    ) {
        return baseMapper.getGradingListWithDetails(page, paperTitle, studentName, status);
    }

    @Override
    public Map<String, Object> getRecordWithDetails(String id) {
        return baseMapper.getRecordWithDetails(id);
    }

    @Override
    public List<Map<String, Object>> getUserRecordsWithDetails(String userId, String paperId) {
        return baseMapper.getUserRecordsWithDetails(userId, paperId);
    }
}
package com.ape.apesystem.service;

import com.ape.apesystem.domain.PhysicsQuestionPaper;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 文档解析服务接口
 * @date 2024-01-24 10:00
 */
public interface DocumentParseService {
    /**
     * 解析文档并提取题目
     * @param file 上传的文档文件
     * @return 解析结果，包含题目内容和答案内容
     */
    Map<String, Object> parseDocument(MultipartFile file);

    /**
     * 解析文档并保存为物理试卷
     * @param file 上传的文档文件
     * @param paperTitle 试卷标题
     * @param subjectBranch 物理分支
     * @return 保存结果
     */
    PhysicsQuestionPaper parseAndSave(MultipartFile file, String paperTitle, String subjectBranch);
}
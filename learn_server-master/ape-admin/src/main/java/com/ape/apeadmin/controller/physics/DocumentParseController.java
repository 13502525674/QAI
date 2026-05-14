package com.ape.apeadmin.controller.physics;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apesystem.domain.PhysicsQuestionPaper;
import com.ape.apesystem.service.DocumentParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 文档解析控制器
 * @date 2024-01-24 10:00
 */
@RestController
@RequestMapping("physics/document")
public class DocumentParseController {

    @Autowired
    private DocumentParseService documentParseService;

    /**
     * 解析文档
     */
    @Log(name = "解析文档", type = BusinessType.OTHER)
    @PostMapping("parse")
    public Result parseDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("请选择要上传的文件");
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.fail("文件名无效");
        }
        
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        if (!Arrays.asList("pdf", "doc", "docx").contains(fileExtension)) {
            return Result.fail("不支持的文件格式，仅支持PDF、DOC、DOCX格式");
        }
        
        // 检查文件大小（限制为10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.fail("文件大小不能超过10MB");
        }
        
        try {
            Map<String, Object> result = documentParseService.parseDocument(file);
            if (result != null && result.containsKey("success") && (Boolean) result.get("success")) {
                return Result.success(result.get("data"));
            } else {
                String errorMessage = (String) result.get("message");
                if (errorMessage == null) {
                    errorMessage = "文档解析失败";
                }
                return Result.fail(errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("文档解析失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 解析并保存文档为试卷
     */
    @Log(name = "解析并保存文档为试卷", type = BusinessType.INSERT)
    @PostMapping("parseAndSave")
    public Result parseAndSave(
            @RequestParam("file") MultipartFile file,
            @RequestParam("paperTitle") String paperTitle,
            @RequestParam("subjectBranch") String subjectBranch) {
        
        if (file.isEmpty()) {
            return Result.fail("请选择要上传的文件");
        }
        
        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.fail("文件名无效");
        }
        
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        if (!Arrays.asList("pdf", "doc", "docx").contains(fileExtension)) {
            return Result.fail("不支持的文件格式，仅支持PDF、DOC、DOCX格式");
        }
        
        // 检查文件大小（限制为10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.fail("文件大小不能超过10MB");
        }
        
        if (paperTitle == null || paperTitle.trim().isEmpty()) {
            return Result.fail("请填写试卷标题");
        }
        
        if (subjectBranch == null || subjectBranch.trim().isEmpty()) {
            return Result.fail("请选择物理分支");
        }
        
        try {
            PhysicsQuestionPaper paper = documentParseService.parseAndSave(file, paperTitle, subjectBranch);
            return Result.success(paper);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("文档解析和保存失败：" + e.getMessage());
        }
    }
}
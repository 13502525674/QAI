package com.ape.apeadmin.controller.physics;

import com.ape.apesystem.service.impl.DocumentParseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试文档解析的控制器
 * 用于调试Word文档内容 - 绕过认证
 */
@RestController
@RequestMapping("/api/test")
public class TestDocumentController {

    @Autowired
    private DocumentParseServiceImpl documentParseService;

    @GetMapping("/run-test")
    public Map<String, Object> runTest() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 开始运行测试方法 ===");
            documentParseService.testReadPhysicsDocument();
            result.put("success", true);
            result.put("message", "测试方法运行完成，请查看控制台输出");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "测试方法运行失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}
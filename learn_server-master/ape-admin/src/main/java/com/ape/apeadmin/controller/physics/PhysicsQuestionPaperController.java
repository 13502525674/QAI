package com.ape.apeadmin.controller.physics;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.PhysicsQuestionPaper;
import com.ape.apesystem.domain.PhysicsPracticeRecords;
import com.ape.apesystem.service.PhysicsQuestionPaperService;
import com.ape.apesystem.service.PhysicsPracticeRecordsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 物理试卷控制器
 * @date 2024-01-24 10:00
 */
@Controller
@ResponseBody
@RequestMapping("physics/questionPaper")
public class PhysicsQuestionPaperController {

    @Autowired
    private PhysicsQuestionPaperService physicsQuestionPaperService;

    @Autowired
    private PhysicsPracticeRecordsService physicsPracticeRecordsService;

    /** 分页获取物理试卷 */
    @Log(name = "分页获取物理试卷", type = BusinessType.OTHER)
    @PostMapping("getPage")
    public Result getPage(@RequestBody PhysicsQuestionPaper physicsQuestionPaper) {
        // 确保页码和页面大小不为空且有效
        Integer pageNumber = physicsQuestionPaper.getPageNumber();
        Integer pageSize = physicsQuestionPaper.getPageSize();
        
        if (pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
        
        Page<PhysicsQuestionPaper> page = new Page<>(pageNumber, pageSize);
        QueryWrapper<PhysicsQuestionPaper> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(physicsQuestionPaper.getPaperTitle()), PhysicsQuestionPaper::getPaperTitle, physicsQuestionPaper.getPaperTitle())
                .eq(StringUtils.isNotBlank(physicsQuestionPaper.getSubjectBranch()), PhysicsQuestionPaper::getSubjectBranch, physicsQuestionPaper.getSubjectBranch())
                .like(StringUtils.isNotBlank(physicsQuestionPaper.getCreateBy()), PhysicsQuestionPaper::getCreateBy, physicsQuestionPaper.getCreateBy());
        Page<PhysicsQuestionPaper> physicsQuestionPaperPage = physicsQuestionPaperService.page(page, queryWrapper);
        return Result.success(physicsQuestionPaperPage);
    }

    /** 根据id获取物理试卷 */
    @Log(name = "根据id获取物理试卷", type = BusinessType.OTHER)
    @GetMapping("getById")
    public Result getById(@RequestParam("id") String id) {
        PhysicsQuestionPaper physicsQuestionPaper = physicsQuestionPaperService.getById(id);
        return Result.success(physicsQuestionPaper);
    }

    /** 获取物理试卷及格式化后的题目内容 */
    @Log(name = "获取物理试卷及格式化题目", type = BusinessType.OTHER)
    @GetMapping("getFormattedQuestions")
    public Result getFormattedQuestions(@RequestParam("id") String id) {
        try {
            PhysicsQuestionPaper physicsQuestionPaper = physicsQuestionPaperService.getById(id);
            if (physicsQuestionPaper == null) {
                return Result.fail("试卷不存在");
            }

            // 解析题目内容为格式化的结构
            try {
                List<Map<String, Object>> questions = JSON.parseObject(physicsQuestionPaper.getQuestionContent(), 
                    new TypeReference<List<Map<String, Object>>>() {});
                
                // 确保每个题目都有正确的结构，特别是type字段
                if (questions != null) {
                    for (Map<String, Object> question : questions) {
                        // 确保type字段存在且为数字类型
                        Object typeObj = question.get("type");
                        Integer type = 0; // 默认为选择题
                        if (typeObj instanceof Integer) {
                            type = (Integer) typeObj;
                        } else if (typeObj instanceof String) {
                            try {
                                type = Integer.parseInt((String) typeObj);
                            } catch (NumberFormatException e) {
                                type = 0; // 默认为选择题
                            }
                        } else if (typeObj != null) {
                            // 如果是其他类型，尝试转换
                            type = 0;
                        }
                        question.put("type", type);
                        
                        // 确保title字段存在
                        if (!question.containsKey("title")) {
                            question.put("title", "");
                        }
                        
                        // 根据不同题型进行特定处理
                        switch (type) {
                            case 0: // 选择题
                                // 确保options字段存在
                                if (!question.containsKey("options")) {
                                    question.put("options", new ArrayList<>());
                                }
                                // 如果title包含选项信息(A.B.C.D)，尝试解析选项
                                String title = (String) question.get("title");
                                if (title != null && !title.isEmpty() && 
                                    (title.contains("A.") || title.contains("B.") || title.contains("C.") || title.contains("D."))) {
                                    // 如果还没有options或options为空，则尝试从title中解析选项
                                    @SuppressWarnings("unchecked")
                                    List<Object> options = (List<Object>) question.get("options");
                                    if (options == null || options.isEmpty()) {
                                        options = parseOptionsFromTitle(title);
                                        question.put("options", options);
                                    }
                                }
                                // 确保填空题和计算题的特定字段被移除
                                question.remove("fillBlankContent");
                                question.remove("calculationSteps");
                                break;
                            case 1: // 填空题
                                // 确保没有不必要的options字段
                                if (question.containsKey("options")) {
                                    question.remove("options");
                                }
                                // 确保选择题和计算题的特定字段被移除
                                question.remove("options");
                                question.remove("calculationSteps");
                                // 确保填空题有适当的格式
                                if (!question.containsKey("fillBlankContent")) {
                                    // 从title中提取填空内容
                                    String fillBlankTitle = extractFillBlankContent((String) question.get("title"));
                                    question.put("fillBlankContent", fillBlankTitle);
                                }
                                // 确保title字段不为空
                                if (question.get("title") == null || ((String) question.get("title")).trim().isEmpty()) {
                                    question.put("title", "填空题");
                                }
                                break;
                            case 2: // 计算题
                                // 确保没有不必要的options字段
                                if (question.containsKey("options")) {
                                    question.remove("options");
                                }
                                // 确保选择题和填空题的特定字段被移除
                                question.remove("options");
                                question.remove("fillBlankContent");
                                // 确保计算题有适当的格式
                                if (!question.containsKey("calculationSteps")) {
                                    // 从title中提取计算题要点
                                    String calcContent = extractCalculationContent((String) question.get("title"));
                                    question.put("calculationSteps", calcContent);
                                }
                                // 为计算题设置特定属性，便于前端识别
                                question.put("requiresDetailedAnswer", true); // 需要详细解答
                                question.put("expectedAnswerType", "calculation"); // 期望的答案类型
                                // 确保title字段不为空
                                if (question.get("title") == null || ((String) question.get("title")).trim().isEmpty()) {
                                    question.put("title", "计算题");
                                }
                                break;
                            case 3: // 判断题（根据前端逻辑，type=3是判断题）
                                // 确保没有不必要的options字段
                                if (question.containsKey("options")) {
                                    question.remove("options");
                                }
                                // 确保其他题型的特定字段被移除
                                question.remove("options");
                                question.remove("fillBlankContent");
                                question.remove("calculationSteps");
                                // 为判断题设置特定属性
                                question.put("isTrueFalse", true); // 标记为判断题
                                // 确保title字段不为空
                                if (question.get("title") == null || ((String) question.get("title")).trim().isEmpty()) {
                                    question.put("title", "判断题");
                                }
                                break;
                            case 4: // 简答题
                                // 确保没有不必要的options字段
                                if (question.containsKey("options")) {
                                    question.remove("options");
                                }
                                // 确保其他题型的特定字段被移除
                                question.remove("options");
                                question.remove("fillBlankContent");
                                question.remove("calculationSteps");
                                // 确保title字段不为空
                                if (question.get("title") == null || ((String) question.get("title")).trim().isEmpty()) {
                                    question.put("title", "简答题");
                                }
                                break;
                            case 5: // 证明题
                                // 确保没有不必要的options字段
                                if (question.containsKey("options")) {
                                    question.remove("options");
                                }
                                // 确保其他题型的特定字段被移除
                                question.remove("options");
                                question.remove("fillBlankContent");
                                question.remove("calculationSteps");
                                // 确保title字段不为空
                                if (question.get("title") == null || ((String) question.get("title")).trim().isEmpty()) {
                                    question.put("title", "证明题");
                                }
                                break;
                            default:
                                // 其他题型，移除所有特定字段
                                question.remove("options");
                                question.remove("fillBlankContent");
                                question.remove("calculationSteps");
                                break;
                        }
                        
                        // 确保maxScore字段存在，根据题型设置默认分数
                        if (!question.containsKey("maxScore") || question.get("maxScore") == null) {
                            int defaultMaxScore;
                            switch (type) {
                                case 0: defaultMaxScore = 5; break;  // 选择题
                                case 1: defaultMaxScore = 8; break;  // 填空题
                                case 2: defaultMaxScore = 10; break; // 计算题
                                case 3: defaultMaxScore = 5; break;  // 判断题
                                case 4: defaultMaxScore = 10; break; // 简答题
                                default: defaultMaxScore = 10; break;
                            }
                            question.put("maxScore", defaultMaxScore);
                        }
                        
                        // 确保difficulty字段存在
                        if (!question.containsKey("difficulty")) {
                            question.put("difficulty", 1);
                        }
                        
                        // 确保answer字段存在
                        if (!question.containsKey("answer")) {
                            question.put("answer", "");
                        }
                        
                        // 确保explanation字段存在
                        if (!question.containsKey("explanation")) {
                            question.put("explanation", "");
                        }
                        
                        // 确保userAnswer字段存在（用于学生作答）
                        if (!question.containsKey("userAnswer")) {
                            question.put("userAnswer", "");
                        }
                    }
                }
                
                // 返回包含格式化内容的试卷信息
                Map<String, Object> result = new HashMap<>();
                result.put("paperInfo", physicsQuestionPaper);
                result.put("formattedQuestions", questions);
                
                return Result.success(result);
            } catch (Exception e) {
                return Result.fail("题目内容解析失败: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取试卷信息异常：" + e.getMessage());
        }
    }
    
    /**
      * 从题目文本中解析选项
      */
     private List<Object> parseOptionsFromTitle(String title) {
         List<Object> options = new ArrayList<>();
         
         // 按换行符分割标题
         String[] lines = title.split("\\\\n|\\n");
         java.util.regex.Pattern optionPattern = java.util.regex.Pattern.compile("^[A-D][\\.．、]\\s*(.*)");
         
         for (String line : lines) {
             java.util.regex.Matcher matcher = optionPattern.matcher(line.trim());
             if (matcher.matches()) {
                 String optionContent = matcher.group(1).trim();
                 if (!optionContent.isEmpty()) {
                     Map<String, String> option = new HashMap<>();
                     option.put("text", optionContent);
                     options.add(option);
                 }
             }
         }
         
         // 如果通过行分割没找到选项，尝试在整段文本中查找
         if (options.isEmpty()) {
             // 使用正则表达式查找所有选项
             java.util.regex.Pattern fullPattern = java.util.regex.Pattern.compile("[A-D][\\.．、]\\s*([^A-D]+?)(?=[A-D][\\.．、]|$)");
             java.util.regex.Matcher matcher = fullPattern.matcher(title);
             
             while (matcher.find()) {
                 String optionMatch = matcher.group().trim();
                 if (optionMatch.length() > 2) { // 至少要有一个字母+标点
                     String letter = optionMatch.substring(0, 1);
                     String content = optionMatch.substring(1).replaceFirst("^[\\.．、]\\s*", "").trim();
                     if (!content.isEmpty()) {
                         Map<String, String> option = new HashMap<>();
                         option.put("text", content);
                         options.add(option);
                     }
                 }
             }
         }
         
         return options;
     }
     
     /**
      * 从题目文本中提取填空题内容
      */
     private String extractFillBlankContent(String title) {
         if (title == null) return "";
         
         // 提取包含下划线或括号的填空内容
         String content = title.replaceAll("_{4,}", "{}").replaceAll("（[^）]*）", "{}");
         return content;
     }
     
     /**
      * 从题目文本中提取计算题内容
      */
     private String extractCalculationContent(String title) {
         if (title == null) return title;
         
         return title;
     }

    /** 保存物理试卷 */
    @Log(name = "保存物理试卷", type = BusinessType.INSERT)
    @PostMapping("save")
    public Result save(@RequestBody PhysicsQuestionPaper physicsQuestionPaper) {
        try {
            // 检查字段长度，避免数据库字段长度限制
            if (physicsQuestionPaper.getPaperTitle() != null && physicsQuestionPaper.getPaperTitle().length() > 200) {
                return Result.fail("试卷标题长度不能超过200个字符！");
            }
            
            if (physicsQuestionPaper.getSubjectBranch() != null && physicsQuestionPaper.getSubjectBranch().length() > 50) {
                return Result.fail("物理分支长度不能超过50个字符！");
            }
            
            if (physicsQuestionPaper.getDocumentPath() != null && physicsQuestionPaper.getDocumentPath().length() > 500) {
                return Result.fail("文档路径长度不能超过500个字符！");
            }
            
            // 对于可能较长的字段进行截断处理或验证
            if (physicsQuestionPaper.getQuestionContent() != null && physicsQuestionPaper.getQuestionContent().length() > 50000) {
                return Result.fail("题目内容过长，请减少内容后再试！");
            }
            
            if (physicsQuestionPaper.getAnswerContent() != null && physicsQuestionPaper.getAnswerContent().length() > 50000) {
                return Result.fail("答案内容过长，请减少内容后再试！");
            }

            physicsQuestionPaper.setCreatorId(ShiroUtils.getUserInfo().getId());
            boolean save = physicsQuestionPaperService.save(physicsQuestionPaper);
            if (save) {
                return Result.success();
            } else {
                return Result.fail("物理试卷保存失败，请检查数据完整性！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("物理试卷保存异常：" + e.getMessage());
        }
    }

    /** 编辑物理试卷（支持新增和编辑） */
    @PostMapping("edit")
    public Result edit(@RequestBody PhysicsQuestionPaper physicsQuestionPaper) {
        try {
            // 检查字段长度，避免数据库字段长度限制
            if (physicsQuestionPaper.getPaperTitle() != null && physicsQuestionPaper.getPaperTitle().length() > 200) {
                return Result.fail("试卷标题长度不能超过200个字符！");
            }
            
            if (physicsQuestionPaper.getSubjectBranch() != null && physicsQuestionPaper.getSubjectBranch().length() > 50) {
                return Result.fail("物理分支长度不能超过50个字符！");
            }
            
            if (physicsQuestionPaper.getDocumentPath() != null && physicsQuestionPaper.getDocumentPath().length() > 500) {
                return Result.fail("文档路径长度不能超过500个字符！");
            }
            
            // 对于可能较长的字段进行截断处理或验证
            if (physicsQuestionPaper.getQuestionContent() != null && physicsQuestionPaper.getQuestionContent().length() > 50000) {
                return Result.fail("题目内容过长，请减少内容后再试！");
            }
            
            if (physicsQuestionPaper.getAnswerContent() != null && physicsQuestionPaper.getAnswerContent().length() > 50000) {
                return Result.fail("答案内容过长，请减少内容后再试！");
            }

            boolean success;
            if (physicsQuestionPaper.getId() == null || physicsQuestionPaper.getId().trim().isEmpty()) {
                // ID为空，执行新增操作
                physicsQuestionPaper.setCreatorId(ShiroUtils.getUserInfo().getId());
                success = physicsQuestionPaperService.save(physicsQuestionPaper);
                if (success) {
                    return Result.success("物理试卷保存成功！");
                } else {
                    return Result.fail("物理试卷保存失败，请检查数据完整性！");
                }
            } else {
                // ID不为空，执行更新操作
                success = physicsQuestionPaperService.updateById(physicsQuestionPaper);
                if (success) {
                    return Result.success("物理试卷更新成功！");
                } else {
                    return Result.fail("物理试卷更新失败，请检查数据完整性！");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("物理试卷操作异常：" + e.getMessage());
        }
    }
    
    /** 保存物理试卷（保留原有方法，与edit方法功能类似但用于明确的新增操作） */
    @Log(name = "保存物理试卷", type = BusinessType.INSERT)
    @PostMapping("saveNew")
    public Result saveNew(@RequestBody PhysicsQuestionPaper physicsQuestionPaper) {
        try {
            // 检查字段长度，避免数据库字段长度限制
            if (physicsQuestionPaper.getPaperTitle() != null && physicsQuestionPaper.getPaperTitle().length() > 200) {
                return Result.fail("试卷标题长度不能超过200个字符！");
            }
            
            if (physicsQuestionPaper.getSubjectBranch() != null && physicsQuestionPaper.getSubjectBranch().length() > 50) {
                return Result.fail("物理分支长度不能超过50个字符！");
            }
            
            if (physicsQuestionPaper.getDocumentPath() != null && physicsQuestionPaper.getDocumentPath().length() > 500) {
                return Result.fail("文档路径长度不能超过500个字符！");
            }
            
            // 对于可能较长的字段进行截断处理或验证
            if (physicsQuestionPaper.getQuestionContent() != null && physicsQuestionPaper.getQuestionContent().length() > 50000) {
                return Result.fail("题目内容过长，请减少内容后再试！");
            }
            
            if (physicsQuestionPaper.getAnswerContent() != null && physicsQuestionPaper.getAnswerContent().length() > 50000) {
                return Result.fail("答案内容过长，请减少内容后再试！");
            }

            physicsQuestionPaper.setCreatorId(ShiroUtils.getUserInfo().getId());
            boolean save = physicsQuestionPaperService.save(physicsQuestionPaper);
            if (save) {
                return Result.success("物理试卷保存成功！");
            } else {
                return Result.fail("物理试卷保存失败，请检查数据完整性！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("物理试卷保存异常：" + e.getMessage());
        }
    }

    /** 删除物理试卷 */
    @GetMapping("remove")
    @Log(name = "删除物理试卷", type = BusinessType.DELETE)
    public Result remove(@RequestParam("ids") String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                // 先删除相关的练习记录（级联删除）
                QueryWrapper<PhysicsPracticeRecords> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(PhysicsPracticeRecords::getPaperId, id);
                physicsPracticeRecordsService.remove(queryWrapper);
                
                // 再删除试卷
                physicsQuestionPaperService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("物理试卷id不能为空！");
        }
    }

    /** 获取用户可做的物理试卷 */
    @GetMapping("getUserPapers")
    public Result getUserPapers() {
        QueryWrapper<PhysicsQuestionPaper> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .orderByDesc(PhysicsQuestionPaper::getCreateTime);
        return Result.success(physicsQuestionPaperService.list(queryWrapper));
    }

    /** 按物理分支获取试卷 */
    @GetMapping("getPapersByBranch")
    public Result getPapersByBranch(@RequestParam("branch") String branch) {
        QueryWrapper<PhysicsQuestionPaper> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(PhysicsQuestionPaper::getSubjectBranch, branch)
                .orderByDesc(PhysicsQuestionPaper::getCreateTime);
        return Result.success(physicsQuestionPaperService.list(queryWrapper));
    }
}
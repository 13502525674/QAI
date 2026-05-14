package com.ape.apeadmin.controller.funphysics;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.FunPhysicsQuestion;
import com.ape.apesystem.domain.FunPhysicsAnswerRecord;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.FunPhysicsQuestionService;
import com.ape.apesystem.service.FunPhysicsAnswerRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@ResponseBody
@RequestMapping("/funphysics/question")
public class FunPhysicsQuestionController {

    @Autowired
    private FunPhysicsQuestionService questionService;

    @Autowired
    private FunPhysicsAnswerRecordService answerRecordService;

    @Log(name = "获取趣味问题列表", type = BusinessType.OTHER)
    @PostMapping("getPage")
    public Result getPage(@RequestBody FunPhysicsQuestion question) {
        Page<FunPhysicsQuestion> page = questionService.getPage(question);
        return Result.success(page);
    }

    @Log(name = "获取每日挑战问题", type = BusinessType.OTHER)
    @GetMapping("getDailyChallenge")
    public Result getDailyChallenge(@RequestParam(value = "count", defaultValue = "5") int count) {
        List<FunPhysicsQuestion> questions = questionService.getTodayChallengeQuestions(null, count);
        return Result.success(questions);
    }

    @Log(name = "获取随机趣味问题", type = BusinessType.OTHER)
    @GetMapping("getRandom")
    public Result getRandom(@RequestParam(value = "count", defaultValue = "10") int count,
                           @RequestParam(value = "type", required = false) Integer type) {
        List<FunPhysicsQuestion> questions = questionService.getRandomQuestions(count, type);
        return Result.success(questions);
    }

    @Log(name = "获取问题详情", type = BusinessType.OTHER)
    @GetMapping("getById")
    public Result getById(@RequestParam("id") String id) {
        FunPhysicsQuestion question = questionService.getById(id);
        return Result.success(question);
    }

    @Log(name = "提交答案", type = BusinessType.OTHER)
    @PostMapping("submitAnswer")
    public Result submitAnswer(@RequestBody FunPhysicsAnswerRecord record) {
        ApeUser user = ShiroUtils.getUserInfo();
        record.setUserId(user.getId());
        record.setCreateTime(new Date());
        
        // 设置挑战日期为今天的日期（去掉时间部分）
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        record.setChallengeDate(today);
        
        FunPhysicsQuestion question = questionService.getById(record.getQuestionId());
        if (question != null) {
            String correctAnswer = question.getCorrectAnswer();
            String userAnswer = record.getUserAnswer();
            System.out.println("正确答案: " + correctAnswer + ", 用户答案: " + userAnswer + ", 类型: " + (userAnswer != null ? userAnswer.getClass().getName() : "null"));
            boolean isCorrect = correctAnswer != null && correctAnswer.trim().equals(userAnswer != null ? userAnswer.trim() : "");
            System.out.println("比较结果: " + isCorrect);
            record.setIsCorrect(isCorrect ? 1 : 0);
        }
        
        // 检查是否已存在记录
        QueryWrapper<FunPhysicsAnswerRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", record.getUserId())
                   .eq("question_id", record.getQuestionId())
                   .eq("DATE(challenge_date)", today.toString());
        
        FunPhysicsAnswerRecord existingRecord = answerRecordService.getOne(queryWrapper);
        
        if (existingRecord != null) {
            // 更新已有记录
            existingRecord.setUserAnswer(record.getUserAnswer());
            existingRecord.setIsCorrect(record.getIsCorrect());
            existingRecord.setCreateTime(new Date());
            answerRecordService.updateById(existingRecord);
        } else {
            // 插入新记录
            answerRecordService.save(record);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("isCorrect", record.getIsCorrect());
        result.put("correctAnswer", question != null ? question.getCorrectAnswer() : null);
        result.put("explanation", question != null ? question.getExplanation() : null);
        
        return Result.success(result);
    }
    
    /**
     * 完成一轮答题
     */
    @Log(name = "完成一轮答题", type = BusinessType.OTHER)
    @PostMapping("completeRound")
    public Result completeRound(@RequestBody Map<String, Object> params) {
        ApeUser user = ShiroUtils.getUserInfo();
        Integer questionCount = params.get("questionCount") != null ? (Integer) params.get("questionCount") : 5;
        Integer correctCount = params.get("correctCount") != null ? (Integer) params.get("correctCount") : 0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("correctCount", correctCount);
        result.put("totalQuestions", questionCount);
        result.put("isPerfect", correctCount.equals(questionCount));
        
        return Result.success(result);
    }

    @Log(name = "获取用户答题统计", type = BusinessType.OTHER)
    @GetMapping("getUserStats")
    public Result getUserStats() {
        ApeUser user = ShiroUtils.getUserInfo();
        Map<String, Object> stats = new HashMap<>();
        stats.put("correctCount", answerRecordService.countCorrectByUser(user.getId()));
        stats.put("todayCorrect", answerRecordService.countCorrectByUserToday(user.getId()));
        stats.put("consecutiveDays", answerRecordService.getConsecutiveDays(user.getId()));
        return Result.success(stats);
    }

    @Log(name = "新增趣味问题", type = BusinessType.INSERT)
    @PostMapping("save")
    public Result save(@RequestBody FunPhysicsQuestion question) {
        question.setCreateTime(new Date());
        question.setStatus(0);
        questionService.save(question);
        return Result.success();
    }

    @Log(name = "编辑趣味问题", type = BusinessType.UPDATE)
    @PostMapping("update")
    public Result update(@RequestBody FunPhysicsQuestion question) {
        question.setUpdateTime(new Date());
        questionService.updateById(question);
        return Result.success();
    }

    @Log(name = "删除趣味问题", type = BusinessType.DELETE)
    @GetMapping("remove")
    public Result remove(@RequestParam("id") String id) {
        questionService.removeById(id);
        return Result.success();
    }
}

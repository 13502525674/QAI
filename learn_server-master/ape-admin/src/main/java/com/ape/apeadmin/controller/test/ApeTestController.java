package com.ape.apeadmin.controller.test;

import com.alibaba.fastjson2.JSONObject;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.*;
import com.ape.apesystem.service.ApeTaskService;
import com.ape.apesystem.service.ApeTaskStudentService;
import com.ape.apesystem.service.ApeTestService;
import com.ape.apesystem.service.ApeTestStudentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 考试controller
 * @date 2023/11/20 11:28
 */
@Controller
@ResponseBody
@RequestMapping("test")
public class ApeTestController {

    @Autowired
    private ApeTestService apeTestService;
    @Autowired
    private ApeTaskService apeTaskService;
    @Autowired
    private ApeTaskStudentService apeTaskStudentService;
    @Autowired
    private ApeTestStudentService apeTestStudentService;

    /** 分页获取考试 */
    @Log(name = "分页获取考试", type = BusinessType.OTHER)
    @PostMapping("getApeTestPage")
    public Result getApeTestPage(@RequestBody ApeTest apeTest) {
        Page<ApeTest> page = new Page<>(apeTest.getPageNumber(),apeTest.getPageSize());
        QueryWrapper<ApeTest> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(apeTest.getName()),ApeTest::getName,apeTest.getName())
                .like(StringUtils.isNotBlank(apeTest.getTaskName()),ApeTest::getTaskName,apeTest.getTaskName())
                .eq(apeTest.getState() != null,ApeTest::getState,apeTest.getState())
                .like(StringUtils.isNotBlank(apeTest.getCreateBy()),ApeTest::getCreateBy,apeTest.getCreateBy());
        if (apeTest.getType() != null && Integer.valueOf(1).equals(apeTest.getType())) {
            QueryWrapper<ApeTask> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeTask::getTeacherId,ShiroUtils.getUserInfo().getId());
            List<ApeTask> taskList = apeTaskService.list(wrapper);
            List<String> list = new ArrayList<String>();
            for (ApeTask apeTask : taskList) {
                list.add(apeTask.getId());
            }
            if (list.size()>0) {
                queryWrapper.lambda().in(ApeTest::getTaskId,list);
            } else {
                list.add(" ");
                queryWrapper.lambda().in(ApeTest::getTaskId,list);
            }
        }
        Page<ApeTest> apeTestPage = apeTestService.page(page, queryWrapper);
        return Result.success(apeTestPage);
    }

    /** 根据id获取考试 */
    @Log(name = "根据id获取考试", type = BusinessType.OTHER)
    @GetMapping("getApeTestById")
    public Result getApeTestById(@RequestParam("id")String id) {
        ApeTest apeTest = apeTestService.getById(id);
        return Result.success(apeTest);
    }

    /** 保存考试 */
    @Log(name = "保存考试", type = BusinessType.INSERT)
    @PostMapping("saveApeTest")
    public Result saveApeTest(@RequestBody ApeTest apeTest) {
        if (StringUtils.isNotBlank(apeTest.getTaskId())) {
            ApeTask task = apeTaskService.getById(apeTest.getTaskId());
            apeTest.setTaskName(task.getName());
        }
        // 设置考试状态为通过（0），管理员创建的考试直接生效
        if (apeTest.getState() == null) {
            apeTest.setState(0);
        }
        boolean save = apeTestService.save(apeTest);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑考试 */
    @Log(name = "编辑考试", type = BusinessType.UPDATE)
    @PostMapping("editApeTest")
    public Result editApeTest(@RequestBody ApeTest apeTest) {
        if (StringUtils.isNotBlank(apeTest.getTaskId())) {
            ApeTask task = apeTaskService.getById(apeTest.getTaskId());
            apeTest.setTaskName(task.getName());
        }
        boolean save = apeTestService.updateById(apeTest);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除考试 */
    @GetMapping("removeApeTest")
    @Log(name = "删除考试", type = BusinessType.DELETE)
    public Result removeApeTest(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                apeTestService.removeById(id);
                QueryWrapper<ApeTestStudent> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(ApeTestStudent::getTestId,id);
                apeTestStudentService.remove(queryWrapper);
            }
            return Result.success();
        } else {
            return Result.fail("考试id不能为空！");
        }
    }

    /** 获取用户考试 */
    @GetMapping("getTestListByUser")
    public Result getTestListByUser() {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        QueryWrapper<ApeTaskStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeTaskStudent::getUserId,userInfo.getId())
                .eq(ApeTaskStudent::getState,0);
        List<ApeTaskStudent> studentList = apeTaskStudentService.list(queryWrapper);
        List<ApeTest> apeTestList = new ArrayList<>();
        for (ApeTaskStudent apeTaskStudent : studentList) {
            QueryWrapper<ApeTest> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeTest::getTaskId,apeTaskStudent.getTaskId()).eq(ApeTest::getState,0);
            List<ApeTest> testList = apeTestService.list(wrapper);
            if (testList.size() > 0) {
                apeTestList.addAll(testList);
            }
        }
        for (ApeTest test : apeTestList) {
            QueryWrapper<ApeTestStudent> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.lambda().eq(ApeTestStudent::getTestId,test.getId())
                    .eq(ApeTestStudent::getUserId,userInfo.getId());
            List<ApeTestStudent> list = apeTestStudentService.list(queryWrapper1);
            if (list.size() <= 0) {
                test.setSchedule("未开始");
                test.setScoreTotal(0);
            } else {
                test.setSchedule("已完成");
                Integer total = 0;
                for (ApeTestStudent apeTestStudent : list) {
                    total += apeTestStudent.getPoint();
                }
                test.setScoreTotal(total);
            }
        }
        return Result.success(apeTestList);
    }

    @PostMapping("getTestStudent")
    public Result getTestStudent(@RequestBody JSONObject jsonObject) {
        String testId = jsonObject.getString("testId");
        ApeTest test = apeTestService.getById(testId);
        String userName = jsonObject.getString("userName");
        Integer pageNumber = jsonObject.getInteger("pageNumber");
        Integer pageSize = jsonObject.getInteger("pageSize");
        QueryWrapper<ApeTestStudent> queryWrapper =  new QueryWrapper<>();
        queryWrapper.lambda()
                .select(ApeTestStudent::getUserId)
                .eq(ApeTestStudent::getTestId,testId)
                .like(StringUtils.isNotBlank(userName),ApeTestStudent::getCreateBy,userName)
                .groupBy(ApeTestStudent::getUserId);
        List<ApeTestStudent> studentList = apeTestStudentService.list(queryWrapper);
        for (ApeTestStudent student : studentList) {
            QueryWrapper<ApeTestStudent> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeTestStudent::getTestId,testId)
                            .eq(ApeTestStudent::getUserId,student.getUserId());
            List<ApeTestStudent> testStudents = apeTestStudentService.list(wrapper);
            int score = 0;
            for (ApeTestStudent item : testStudents) {
                score += item.getPoint();
                if (item.getCreateBy() != null) {
                    student.setCreateBy(item.getCreateBy());
                }
                if (student.getUpdateTime() == null || item.getUpdateTime().after(student.getUpdateTime())) {
                    student.setUpdateTime(item.getUpdateTime());
                }
            }
            student.setTestName(test.getName());
            student.setTotalScore(test.getTotalScore());
            student.setTotalGetScore(score);
        }
        studentList.sort((a, b) -> {
            if (a.getUpdateTime() == null && b.getUpdateTime() == null) return 0;
            if (a.getUpdateTime() == null) return 1;
            if (b.getUpdateTime() == null) return -1;
            return a.getUpdateTime().compareTo(b.getUpdateTime());
        });
        int start = (pageNumber - 1) * pageSize;
        int end = Math.min(start + pageSize, studentList.size());
        List<ApeTestStudent> pageList = studentList.subList(start, end);
        Page<ApeTestStudent> studentPage = new Page<>(pageNumber, pageSize);
        studentPage.setRecords(pageList);
        studentPage.setTotal(studentList.size());
        return Result.success(studentPage);
    }

    /** 获取所有学生考试成绩数据（用于大屏可视化） */
    @GetMapping("getAllStudentScores")
    public Result getAllStudentScores() {
        List<JSONObject> result = new ArrayList<>();
        
        QueryWrapper<ApeTestStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .select(ApeTestStudent::getUserId, ApeTestStudent::getTestId);
        List<ApeTestStudent> allRecords = apeTestStudentService.list(queryWrapper);
        
        Map<String, String> userTestKeyMap = new HashMap<>();
        for (ApeTestStudent record : allRecords) {
            String key = record.getUserId() + "_" + record.getTestId();
            if (!userTestKeyMap.containsKey(key)) {
                userTestKeyMap.put(key, key);
                
                QueryWrapper<ApeTestStudent> scoreQuery = new QueryWrapper<>();
                scoreQuery.lambda()
                        .eq(ApeTestStudent::getUserId, record.getUserId())
                        .eq(ApeTestStudent::getTestId, record.getTestId());
                List<ApeTestStudent> testItems = apeTestStudentService.list(scoreQuery);
                
                if (testItems.isEmpty()) {
                    continue;
                }
                
                int totalScore = 0;
                int maxScore = 0;
                String studentName = null;
                Date updateTime = null;
                
                for (ApeTestStudent item : testItems) {
                    totalScore += (item.getPoint() != null ? item.getPoint() : 0);
                    maxScore += (item.getScore() != null ? item.getScore() : 0);
                    if (studentName == null && item.getCreateBy() != null) {
                        studentName = item.getCreateBy();
                    }
                    if (updateTime == null || (item.getUpdateTime() != null && item.getUpdateTime().after(updateTime))) {
                        updateTime = item.getUpdateTime();
                    }
                }
                
                if (maxScore > 0) {
                    ApeTest test = apeTestService.getById(record.getTestId());
                    JSONObject obj = new JSONObject();
                    obj.put("studentName", studentName != null ? studentName : "未知学生");
                    obj.put("testName", test != null ? test.getName() : "未知考试");
                    obj.put("totalScore", totalScore);
                    obj.put("maxScore", maxScore);
                    obj.put("percentage", (double) totalScore / maxScore * 100);
                    obj.put("updateTime", updateTime);
                    result.add(obj);
                }
            }
        }
        
        result.sort((a, b) -> {
            if (a.getDate("updateTime") == null && b.getDate("updateTime") == null) return 0;
            if (a.getDate("updateTime") == null) return 1;
            if (b.getDate("updateTime") == null) return -1;
            return a.getDate("updateTime").compareTo(b.getDate("updateTime"));
        });
        
        return Result.success(result);
    }

}
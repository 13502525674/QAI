package com.ape.apeadmin.controller.funphysics;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.FunPhysicsPuzzle;
import com.ape.apesystem.domain.FunPhysicsPuzzleRecord;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.FunPhysicsPuzzleService;
import com.ape.apesystem.service.FunPhysicsPuzzleRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@ResponseBody
@RequestMapping("/funphysics/puzzle")
public class FunPhysicsPuzzleController {

    @Autowired
    private FunPhysicsPuzzleService puzzleService;

    @Autowired
    private FunPhysicsPuzzleRecordService recordService;

    @Log(name = "获取拼图列表", type = BusinessType.OTHER)
    @PostMapping("getPage")
    public Result getPage(@RequestBody FunPhysicsPuzzle puzzle) {
        Page<FunPhysicsPuzzle> page = puzzleService.getPage(puzzle);
        return Result.success(page);
    }

    @Log(name = "获取随机拼图", type = BusinessType.OTHER)
    @GetMapping("getRandom")
    public Result getRandom(@RequestParam(value = "count", defaultValue = "5") int count,
                           @RequestParam(value = "difficulty", required = false) Integer difficulty,
                           @RequestParam(value = "type", required = false) Integer type) {
        List<FunPhysicsPuzzle> puzzles = puzzleService.getRandomPuzzles(count, difficulty, type);
        return Result.success(puzzles);
    }

    @Log(name = "获取拼图详情", type = BusinessType.OTHER)
    @GetMapping("getById")
    public Result getById(@RequestParam("id") String id) {
        FunPhysicsPuzzle puzzle = puzzleService.getById(id);
        if (puzzle == null) {
            return Result.fail("拼图不存在");
        }
        return Result.success(puzzle);
    }

    @Log(name = "开始拼图", type = BusinessType.OTHER)
    @PostMapping("start")
    public Result start(@RequestBody Map<String, String> params) {
        String puzzleId = params.get("puzzleId");
        ApeUser user = ShiroUtils.getUserInfo();
        
        FunPhysicsPuzzleRecord record = new FunPhysicsPuzzleRecord();
        record.setId(UUID.randomUUID().toString());
        record.setPuzzleId(puzzleId);
        record.setUserId(user.getId());
        record.setStartTime(new Date());
        record.setIsCompleted(0);
        record.setUseHint(0);
        recordService.save(record);
        
        FunPhysicsPuzzle puzzle = puzzleService.getById(puzzleId);
        Map<String, Object> result = new HashMap<>();
        result.put("recordId", record.getId());
        result.put("puzzle", puzzle);
        return Result.success(result);
    }

    @Log(name = "完成拼图", type = BusinessType.OTHER)
    @PostMapping("complete")
    public Result complete(@RequestBody FunPhysicsPuzzleRecord record) {
        ApeUser user = ShiroUtils.getUserInfo();
        record.setEndTime(new Date());
        record.setIsCompleted(1);
        
        if (record.getStartTime() != null && record.getEndTime() != null) {
            long timeSpent = (record.getEndTime().getTime() - record.getStartTime().getTime()) / 1000;
            record.setTimeSpent((int) timeSpent);
        }
        
        // 获取拼图信息以获取难度
        FunPhysicsPuzzle puzzle = puzzleService.getById(record.getPuzzleId());
        if (puzzle != null) {
            // 如果record没有difficulty，从puzzle获取
            if (record.getDifficulty() == null) {
                record.setDifficulty(puzzle.getDifficulty());
            }
        }
        
        recordService.updateById(record);
        
        Map<String, Object> result = new HashMap<>();
        result.put("timeSpent", record.getTimeSpent());
        return Result.success(result);
    }

    @Log(name = "使用提示", type = BusinessType.OTHER)
    @PostMapping("useHint")
    public Result useHint(@RequestBody Map<String, String> params) {
        String recordId = params.get("recordId");
        FunPhysicsPuzzleRecord record = recordService.getById(recordId);
        if (record != null) {
            record.setUseHint(1);
            recordService.updateById(record);
            
            FunPhysicsPuzzle puzzle = puzzleService.getById(record.getPuzzleId());
            return Result.success(puzzle.getHint());
        }
        return Result.fail("记录不存在");
    }

    @Log(name = "获取用户拼图统计", type = BusinessType.OTHER)
    @GetMapping("getUserStats")
    public Result getUserStats() {
        ApeUser user = ShiroUtils.getUserInfo();
        Map<String, Object> stats = new HashMap<>();
        stats.put("completedCount", recordService.countCompletedByUser(user.getId()));
        stats.put("noHintCount", recordService.countCompletedByUserWithoutHint(user.getId(), null));
        return Result.success(stats);
    }

    @Log(name = "新增拼图", type = BusinessType.INSERT)
    @PostMapping("save")
    public Result save(@RequestBody FunPhysicsPuzzle puzzle) {
        puzzle.setCreateTime(new Date());
        puzzle.setStatus(0);
        puzzleService.save(puzzle);
        return Result.success();
    }

    @Log(name = "编辑拼图", type = BusinessType.UPDATE)
    @PostMapping("update")
    public Result update(@RequestBody FunPhysicsPuzzle puzzle) {
        puzzle.setUpdateTime(new Date());
        puzzleService.updateById(puzzle);
        return Result.success();
    }

    @Log(name = "删除拼图", type = BusinessType.DELETE)
    @GetMapping("remove")
    public Result remove(@RequestParam("id") String id) {
        puzzleService.removeById(id);
        return Result.success();
    }
}

package com.ape.apeadmin.controller.knowledge;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apesystem.domain.KnowledgePoint;
import com.ape.apesystem.service.KnowledgePointService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/knowledge")
public class KnowledgePointController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Log(name = "分页查询知识点", type = BusinessType.OTHER)
    @PostMapping("/getPage")
    public Result getPage(@RequestBody KnowledgePoint knowledgePoint) {
        Page<KnowledgePoint> page = new Page<>(knowledgePoint.getPageNumber(), knowledgePoint.getPageSize());
        QueryWrapper<KnowledgePoint> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .like(knowledgePoint.getName() != null, KnowledgePoint::getName, knowledgePoint.getName())
                .eq(knowledgePoint.getBranch() != null, KnowledgePoint::getBranch, knowledgePoint.getBranch())
                .eq(knowledgePoint.getLevel() != null, KnowledgePoint::getLevel, knowledgePoint.getLevel())
                .eq(knowledgePoint.getParentId() != null, KnowledgePoint::getParentId, knowledgePoint.getParentId())
                .orderByAsc(KnowledgePoint::getLevel, KnowledgePoint::getName);
        Page<KnowledgePoint> result = knowledgePointService.page(page, wrapper);
        return Result.success(result);
    }

    @Log(name = "查询知识点列表", type = BusinessType.OTHER)
    @GetMapping("/getList")
    public Result getList() {
        List<KnowledgePoint> list = knowledgePointService.list();
        return Result.success(list);
    }

    @Log(name = "根据ID查询知识点", type = BusinessType.OTHER)
    @GetMapping("/getById")
    public Result getById(@RequestParam String id) {
        KnowledgePoint knowledgePoint = knowledgePointService.getById(id);
        return Result.success(knowledgePoint);
    }

    @Log(name = "新增知识点", type = BusinessType.INSERT)
    @PostMapping("/save")
    public Result save(@RequestBody KnowledgePoint knowledgePoint) {
        boolean success = knowledgePointService.save(knowledgePoint);
        return success ? Result.success("保存成功") : Result.fail("保存失败");
    }

    @Log(name = "更新知识点", type = BusinessType.UPDATE)
    @PostMapping("/edit")
    public Result edit(@RequestBody KnowledgePoint knowledgePoint) {
        boolean success = knowledgePointService.updateById(knowledgePoint);
        return success ? Result.success("更新成功") : Result.fail("更新失败");
    }

    @Log(name = "删除知识点", type = BusinessType.DELETE)
    @GetMapping("/remove")
    public Result remove(@RequestParam String id) {
        boolean success = knowledgePointService.removeById(id);
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }

    @Log(name = "根据分支查询知识点", type = BusinessType.OTHER)
    @GetMapping("/getByBranch")
    public Result getByBranch(@RequestParam String branch) {
        List<KnowledgePoint> list = knowledgePointService.getByBranch(branch);
        return Result.success(list);
    }

    @Log(name = "查询子知识点", type = BusinessType.OTHER)
    @GetMapping("/getChildren")
    public Result getChildren(@RequestParam(required = false) String parentId) {
        List<KnowledgePoint> list;
        if (parentId == null || parentId.isEmpty()) {
            QueryWrapper<KnowledgePoint> wrapper = new QueryWrapper<>();
            wrapper.isNull("parent_id").or().eq("parent_id", "").orderByAsc("name");
            list = knowledgePointService.list(wrapper);
        } else {
            list = knowledgePointService.getChildren(parentId);
        }
        return Result.success(list);
    }

    @Log(name = "获取知识点树", type = BusinessType.OTHER)
    @GetMapping("/getTree")
    public Result getTree() {
        List<KnowledgePoint> tree = knowledgePointService.getTree();
        return Result.success(tree);
    }

    @Log(name = "获取知识图谱数据", type = BusinessType.OTHER)
    @GetMapping("/getGraphData")
    public Result getGraphData() {
        List<Map<String, Object>> data = knowledgePointService.getKnowledgeGraphData();
        return Result.success(data);
    }

    @Log(name = "根据层级查询知识点", type = BusinessType.OTHER)
    @GetMapping("/getByLevel")
    public Result getByLevel(@RequestParam Integer level) {
        List<KnowledgePoint> list = knowledgePointService.getByLevel(level);
        return Result.success(list);
    }
}

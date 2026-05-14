package com.ape.apeadmin.controller.knowledge;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apesystem.domain.KnowledgeRelation;
import com.ape.apesystem.service.KnowledgeRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/knowledgeRelation")
public class KnowledgeRelationController {

    @Autowired
    private KnowledgeRelationService knowledgeRelationService;

    @Log(name = "分页查询知识点关系", type = BusinessType.OTHER)
    @PostMapping("/getPage")
    public Result getPage(@RequestBody KnowledgeRelation knowledgeRelation) {
        Page<KnowledgeRelation> page = new Page<>(
                knowledgeRelation.getId() != null ? 1 : 1, 
                100
        );
        List<KnowledgeRelation> list = knowledgeRelationService.getAllWithNames();
        return Result.success(list);
    }

    @Log(name = "查询所有关系(带名称)", type = BusinessType.OTHER)
    @GetMapping("/getAllWithNames")
    public Result getAllWithNames() {
        List<KnowledgeRelation> list = knowledgeRelationService.getAllWithNames();
        return Result.success(list);
    }

    @Log(name = "根据源知识点查询", type = BusinessType.OTHER)
    @GetMapping("/getByFromKpId")
    public Result getByFromKpId(@RequestParam String fromKpId) {
        List<KnowledgeRelation> list = knowledgeRelationService.getByFromKpId(fromKpId);
        return Result.success(list);
    }

    @Log(name = "根据目标知识点查询", type = BusinessType.OTHER)
    @GetMapping("/getByToKpId")
    public Result getByToKpId(@RequestParam String toKpId) {
        List<KnowledgeRelation> list = knowledgeRelationService.getByToKpId(toKpId);
        return Result.success(list);
    }

    @Log(name = "根据关系类型查询", type = BusinessType.OTHER)
    @GetMapping("/getByRelationType")
    public Result getByRelationType(@RequestParam String relationType) {
        List<KnowledgeRelation> list = knowledgeRelationService.getByRelationType(relationType);
        return Result.success(list);
    }

    @Log(name = "新增知识点关系", type = BusinessType.INSERT)
    @PostMapping("/save")
    public Result save(@RequestBody KnowledgeRelation knowledgeRelation) {
        boolean success = knowledgeRelationService.save(knowledgeRelation);
        return success ? Result.success("保存成功") : Result.fail("保存失败");
    }

    @Log(name = "更新知识点关系", type = BusinessType.UPDATE)
    @PostMapping("/edit")
    public Result edit(@RequestBody KnowledgeRelation knowledgeRelation) {
        boolean success = knowledgeRelationService.updateById(knowledgeRelation);
        return success ? Result.success("更新成功") : Result.fail("更新失败");
    }

    @Log(name = "删除知识点关系", type = BusinessType.DELETE)
    @GetMapping("/remove")
    public Result remove(@RequestParam Long id) {
        boolean success = knowledgeRelationService.removeById(id);
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }

    @Log(name = "获取知识点的前置依赖", type = BusinessType.OTHER)
    @GetMapping("/getPrerequisites")
    public Result getPrerequisites(@RequestParam String kpId) {
        List<KnowledgeRelation> list = knowledgeRelationService.getPrerequisites(kpId);
        return Result.success(list);
    }
}

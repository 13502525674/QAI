package com.ape.apeadmin.controller.knowledge;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apesystem.domain.KnowledgePoint;
import com.ape.apesystem.domain.KnowledgeRelation;
import com.ape.apesystem.service.KnowledgePointService;
import com.ape.apesystem.service.KnowledgeRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/knowledgeGraph")
public class KnowledgeGraphController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private KnowledgeRelationService knowledgeRelationService;

    @Log(name = "获取知识图谱可视化数据", type = BusinessType.OTHER)
    @GetMapping("/getGraphData")
    public Result getGraphData() {
        List<Map<String, Object>> nodes = knowledgePointService.getKnowledgeGraphData();
        List<KnowledgeRelation> relations = knowledgeRelationService.getAllWithNames();
        
        List<Map<String, Object>> links = new ArrayList<>();
        for (KnowledgeRelation rel : relations) {
            Map<String, Object> link = new HashMap<>();
            link.put("source", rel.getFromKpId());
            link.put("target", rel.getToKpId());
            link.put("type", rel.getRelationType());
            link.put("weight", rel.getWeight());
            links.add(link);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("links", links);
        
        return Result.success(result);
    }

    @Log(name = "获取知识点详情(含关系)", type = BusinessType.OTHER)
    @GetMapping("/getDetail")
    public Result getDetail(@RequestParam String kpId) {
        KnowledgePoint kp = knowledgePointService.getById(kpId);
        if (kp == null) {
            return Result.fail("知识点不存在");
        }
        
        List<KnowledgeRelation> prerequisites = knowledgeRelationService.getPrerequisites(kpId);
        List<KnowledgeRelation> dependencies = knowledgeRelationService.getDependencies(kpId);
        List<KnowledgePoint> children = knowledgePointService.getChildren(kpId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("knowledgePoint", kp);
        result.put("prerequisites", prerequisites);
        result.put("dependencies", dependencies);
        result.put("children", children);
        
        return Result.success(result);
    }

    @Log(name = "获取知识路径", type = BusinessType.OTHER)
    @GetMapping("/getLearningPath")
    public Result getLearningPath(@RequestParam String kpId) {
        List<KnowledgePoint> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        buildLearningPath(kpId, path, visited);
        Collections.reverse(path);
        return Result.success(path);
    }
    
    private void buildLearningPath(String kpId, List<KnowledgePoint> path, Set<String> visited) {
        if (visited.contains(kpId)) {
            return;
        }
        visited.add(kpId);
        
        List<KnowledgeRelation> prerequisites = knowledgeRelationService.getPrerequisites(kpId);
        for (KnowledgeRelation rel : prerequisites) {
            buildLearningPath(rel.getFromKpId(), path, visited);
        }
        
        KnowledgePoint kp = knowledgePointService.getById(kpId);
        if (kp != null) {
            path.add(kp);
        }
    }

    @Log(name = "获取分支统计", type = BusinessType.OTHER)
    @GetMapping("/getBranchStats")
    public Result getBranchStats() {
        List<Map<String, Object>> nodes = knowledgePointService.getKnowledgeGraphData();
        
        Map<String, Integer> stats = new HashMap<>();
        for (Map<String, Object> node : nodes) {
            String branch = (String) node.get("category");
            if (branch != null) {
                stats.merge(branch, 1, Integer::sum);
            }
        }
        
        return Result.success(stats);
    }
}

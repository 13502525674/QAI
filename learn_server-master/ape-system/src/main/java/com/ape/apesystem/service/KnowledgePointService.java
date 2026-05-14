package com.ape.apesystem.service;

import com.ape.apesystem.domain.KnowledgePoint;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface KnowledgePointService extends IService<KnowledgePoint> {
    
    List<KnowledgePoint> getByBranch(String branch);
    
    List<KnowledgePoint> getChildren(String parentId);
    
    List<KnowledgePoint> getTree();
    
    List<Map<String, Object>> getKnowledgeGraphData();
    
    List<KnowledgePoint> getByLevel(Integer level);
}

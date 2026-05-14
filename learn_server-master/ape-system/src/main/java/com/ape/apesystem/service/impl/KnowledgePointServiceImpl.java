package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.KnowledgePoint;
import com.ape.apesystem.mapper.KnowledgePointMapper;
import com.ape.apesystem.service.KnowledgePointService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KnowledgePointServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint> 
        implements KnowledgePointService {

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Override
    public List<KnowledgePoint> getByBranch(String branch) {
        return knowledgePointMapper.selectByBranch(branch);
    }

    @Override
    public List<KnowledgePoint> getChildren(String parentId) {
        return knowledgePointMapper.selectChildren(parentId);
    }

    @Override
    public List<KnowledgePoint> getTree() {
        QueryWrapper<KnowledgePoint> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("level", "name");
        List<KnowledgePoint> allPoints = list(wrapper);
        
        Map<String, List<KnowledgePoint>> childrenMap = allPoints.stream()
                .filter(kp -> kp.getParentId() != null)
                .collect(Collectors.groupingBy(KnowledgePoint::getParentId));
        
        List<KnowledgePoint> roots = allPoints.stream()
                .filter(kp -> kp.getParentId() == null)
                .collect(Collectors.toList());
        
        buildTree(roots, childrenMap);
        return roots;
    }
    
    private void buildTree(List<KnowledgePoint> nodes, Map<String, List<KnowledgePoint>> childrenMap) {
        if (nodes == null) return;
        for (KnowledgePoint node : nodes) {
            List<KnowledgePoint> children = childrenMap.get(node.getId());
            if (children != null && !children.isEmpty()) {
                buildTree(children, childrenMap);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getKnowledgeGraphData() {
        return knowledgePointMapper.selectKnowledgeGraphData();
    }

    @Override
    public List<KnowledgePoint> getByLevel(Integer level) {
        QueryWrapper<KnowledgePoint> wrapper = new QueryWrapper<>();
        wrapper.eq("level", level).orderByAsc("branch", "name");
        return list(wrapper);
    }
}

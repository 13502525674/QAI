package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.KnowledgeRelation;
import com.ape.apesystem.mapper.KnowledgeRelationMapper;
import com.ape.apesystem.service.KnowledgeRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KnowledgeRelationServiceImpl extends ServiceImpl<KnowledgeRelationMapper, KnowledgeRelation> 
        implements KnowledgeRelationService {

    @Autowired
    private KnowledgeRelationMapper knowledgeRelationMapper;

    @Override
    public List<KnowledgeRelation> getByFromKpId(String fromKpId) {
        return knowledgeRelationMapper.selectByFromKpId(fromKpId);
    }

    @Override
    public List<KnowledgeRelation> getByToKpId(String toKpId) {
        return knowledgeRelationMapper.selectByToKpId(toKpId);
    }

    @Override
    public List<KnowledgeRelation> getByRelationType(String relationType) {
        return knowledgeRelationMapper.selectByRelationType(relationType);
    }

    @Override
    public List<KnowledgeRelation> getAllWithNames() {
        return knowledgeRelationMapper.selectAllWithNames();
    }

    @Override
    public List<KnowledgeRelation> getPrerequisites(String kpId) {
        QueryWrapper<KnowledgeRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("to_kp_id", kpId).eq("relation_type", "prerequisite");
        return knowledgeRelationMapper.selectByToKpId(kpId).stream()
                .filter(r -> "prerequisite".equals(r.getRelationType()))
                .toList();
    }

    @Override
    public List<KnowledgeRelation> getDependencies(String kpId) {
        QueryWrapper<KnowledgeRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("from_kp_id", kpId).eq("relation_type", "prerequisite");
        return knowledgeRelationMapper.selectByFromKpId(kpId).stream()
                .filter(r -> "prerequisite".equals(r.getRelationType()))
                .toList();
    }
}

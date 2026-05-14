package com.ape.apesystem.service;

import com.ape.apesystem.domain.KnowledgeRelation;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface KnowledgeRelationService extends IService<KnowledgeRelation> {
    
    List<KnowledgeRelation> getByFromKpId(String fromKpId);
    
    List<KnowledgeRelation> getByToKpId(String toKpId);
    
    List<KnowledgeRelation> getByRelationType(String relationType);
    
    List<KnowledgeRelation> getAllWithNames();
    
    List<KnowledgeRelation> getPrerequisites(String kpId);
    
    List<KnowledgeRelation> getDependencies(String kpId);
}

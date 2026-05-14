package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.KnowledgeRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface KnowledgeRelationMapper extends BaseMapper<KnowledgeRelation> {
    
    List<KnowledgeRelation> selectByFromKpId(@Param("fromKpId") String fromKpId);
    
    List<KnowledgeRelation> selectByToKpId(@Param("toKpId") String toKpId);
    
    List<KnowledgeRelation> selectByRelationType(@Param("relationType") String relationType);
    
    List<KnowledgeRelation> selectAllWithNames();
}

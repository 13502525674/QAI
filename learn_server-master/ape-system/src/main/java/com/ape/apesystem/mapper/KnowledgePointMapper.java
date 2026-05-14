package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.KnowledgePoint;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface KnowledgePointMapper extends BaseMapper<KnowledgePoint> {
    
    List<KnowledgePoint> selectByBranch(@Param("branch") String branch);
    
    List<KnowledgePoint> selectChildren(@Param("parentId") String parentId);
    
    List<Map<String, Object>> selectKnowledgeGraphData();
    
    Integer countChildren(@Param("parentId") String parentId);
}

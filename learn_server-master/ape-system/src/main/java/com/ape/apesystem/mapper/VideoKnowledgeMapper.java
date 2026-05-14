package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.VideoKnowledge;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface VideoKnowledgeMapper extends BaseMapper<VideoKnowledge> {
    
    List<VideoKnowledge> selectByChapterId(@Param("chapterId") String chapterId);
    
    List<VideoKnowledge> selectByKpId(@Param("kpId") String kpId);
    
    void deleteByChapterId(@Param("chapterId") String chapterId);
}

package com.ape.apesystem.service;

import com.ape.apesystem.domain.VideoKnowledge;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface VideoKnowledgeService extends IService<VideoKnowledge> {
    
    List<VideoKnowledge> getByChapterId(String chapterId);
    
    List<VideoKnowledge> getByKpId(String kpId);
    
    void saveChapterKps(String chapterId, List<String> kpIds);
    
    void deleteByChapterId(String chapterId);
}

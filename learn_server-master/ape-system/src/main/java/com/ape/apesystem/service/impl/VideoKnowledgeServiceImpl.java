package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.VideoKnowledge;
import com.ape.apesystem.mapper.VideoKnowledgeMapper;
import com.ape.apesystem.service.VideoKnowledgeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class VideoKnowledgeServiceImpl extends ServiceImpl<VideoKnowledgeMapper, VideoKnowledge> 
        implements VideoKnowledgeService {

    @Autowired
    private VideoKnowledgeMapper videoKnowledgeMapper;

    @Override
    public List<VideoKnowledge> getByChapterId(String chapterId) {
        return videoKnowledgeMapper.selectByChapterId(chapterId);
    }

    @Override
    public List<VideoKnowledge> getByKpId(String kpId) {
        return videoKnowledgeMapper.selectByKpId(kpId);
    }

    @Override
    @Transactional
    public void saveChapterKps(String chapterId, List<String> kpIds) {
        videoKnowledgeMapper.deleteByChapterId(chapterId);
        
        if (kpIds != null && !kpIds.isEmpty()) {
            List<VideoKnowledge> list = new ArrayList<>();
            for (String kpId : kpIds) {
                VideoKnowledge vk = new VideoKnowledge();
                vk.setChapterId(chapterId);
                vk.setKpId(kpId);
                vk.setWeight(1.0f);
                vk.setCreateTime(new Date());
                list.add(vk);
            }
            saveBatch(list);
        }
    }

    @Override
    public void deleteByChapterId(String chapterId) {
        videoKnowledgeMapper.deleteByChapterId(chapterId);
    }
}

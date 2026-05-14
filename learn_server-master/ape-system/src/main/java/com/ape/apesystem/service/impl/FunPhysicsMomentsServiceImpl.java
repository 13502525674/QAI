package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.FunPhysicsMoments;
import com.ape.apesystem.domain.FunPhysicsMomentsLike;
import com.ape.apesystem.mapper.FunPhysicsMomentsLikeMapper;
import com.ape.apesystem.mapper.FunPhysicsMomentsMapper;
import com.ape.apesystem.service.FunPhysicsMomentsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FunPhysicsMomentsServiceImpl extends ServiceImpl<FunPhysicsMomentsMapper, FunPhysicsMoments> implements FunPhysicsMomentsService {

    @Autowired
    private FunPhysicsMomentsLikeMapper likeMapper;

    @Override
    public Page<FunPhysicsMoments> getPage(FunPhysicsMoments moments) {
        Page<FunPhysicsMoments> page = new Page<>(moments.getPageNumber() != null ? moments.getPageNumber() : 1,
                moments.getPageSize() != null ? moments.getPageSize() : 10);
        return baseMapper.getPage(page, moments);
    }

    @Override
    public List<FunPhysicsMoments> getRandomMoments(int count) {
        return baseMapper.getRandomMoments(count);
    }

    @Override
    public FunPhysicsMoments getMomentsWithComments(String momentsId, String userId) {
        FunPhysicsMoments moments = getById(momentsId);
        if (moments != null && userId != null) {
            QueryWrapper<FunPhysicsMomentsLike> wrapper = new QueryWrapper<>();
            wrapper.eq("moments_id", momentsId);
            wrapper.eq("user_id", userId);
            moments.setIsLiked(likeMapper.selectCount(wrapper) > 0);
        }
        return moments;
    }

    @Override
    public void incrementLikes(String momentsId) {
        UpdateWrapper<FunPhysicsMoments> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", momentsId);
        wrapper.setSql("likes_count = likes_count + 1");
        update(wrapper);
    }

    @Override
    public void decrementLikes(String momentsId) {
        UpdateWrapper<FunPhysicsMoments> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", momentsId);
        wrapper.setSql("likes_count = GREATEST(0, likes_count - 1)");
        update(wrapper);
    }
}

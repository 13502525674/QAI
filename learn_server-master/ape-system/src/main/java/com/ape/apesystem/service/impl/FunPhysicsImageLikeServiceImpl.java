package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.FunPhysicsImageLike;
import com.ape.apesystem.mapper.FunPhysicsImageLikeMapper;
import com.ape.apesystem.service.FunPhysicsImageLikeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FunPhysicsImageLikeServiceImpl extends ServiceImpl<FunPhysicsImageLikeMapper, FunPhysicsImageLike> implements FunPhysicsImageLikeService {

    @Override
    public boolean hasLiked(String imageId, String userId) {
        QueryWrapper<FunPhysicsImageLike> wrapper = new QueryWrapper<>();
        wrapper.eq("image_id", imageId);
        wrapper.eq("user_id", userId);
        return count(wrapper) > 0;
    }

    @Override
    public int countByUser(String userId) {
        return baseMapper.countByUser(userId);
    }
}

package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.FunPhysicsMomentsLike;
import com.ape.apesystem.mapper.FunPhysicsMomentsLikeMapper;
import com.ape.apesystem.service.FunPhysicsMomentsLikeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FunPhysicsMomentsLikeServiceImpl extends ServiceImpl<FunPhysicsMomentsLikeMapper, FunPhysicsMomentsLike> implements FunPhysicsMomentsLikeService {

    @Override
    public boolean hasLiked(String momentsId, String userId) {
        QueryWrapper<FunPhysicsMomentsLike> wrapper = new QueryWrapper<>();
        wrapper.eq("moments_id", momentsId);
        wrapper.eq("user_id", userId);
        return count(wrapper) > 0;
    }

    @Override
    public int countByUser(String userId) {
        return baseMapper.countByUser(userId);
    }
}

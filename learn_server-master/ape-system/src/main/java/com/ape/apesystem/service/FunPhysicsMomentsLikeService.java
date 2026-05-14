package com.ape.apesystem.service;

import com.ape.apesystem.domain.FunPhysicsMomentsLike;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FunPhysicsMomentsLikeService extends IService<FunPhysicsMomentsLike> {

    boolean hasLiked(String momentsId, String userId);

    int countByUser(String userId);
}

package com.ape.apesystem.service;

import com.ape.apesystem.domain.FunPhysicsImageLike;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FunPhysicsImageLikeService extends IService<FunPhysicsImageLike> {

    boolean hasLiked(String imageId, String userId);

    int countByUser(String userId);
}

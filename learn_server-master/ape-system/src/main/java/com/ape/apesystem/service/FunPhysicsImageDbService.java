package com.ape.apesystem.service;

import com.ape.apesystem.domain.FunPhysicsImage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface FunPhysicsImageDbService extends IService<FunPhysicsImage> {

    Page<FunPhysicsImage> getPage(FunPhysicsImage image);

    Page<FunPhysicsImage> getPublicGallery(int pageNum, int pageSize);

    List<FunPhysicsImage> getHotImages(int count);

    List<FunPhysicsImage> getUserImages(String userId);

    void incrementLikes(String imageId);

    void decrementLikes(String imageId);

    FunPhysicsImage getImageWithUser(String imageId, String userId);
}

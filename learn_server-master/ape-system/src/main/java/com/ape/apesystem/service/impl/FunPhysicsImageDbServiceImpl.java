package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.FunPhysicsImage;
import com.ape.apesystem.domain.FunPhysicsImageLike;
import com.ape.apesystem.mapper.FunPhysicsImageLikeMapper;
import com.ape.apesystem.mapper.FunPhysicsImageMapper;
import com.ape.apesystem.service.FunPhysicsImageDbService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FunPhysicsImageDbServiceImpl extends ServiceImpl<FunPhysicsImageMapper, FunPhysicsImage> implements FunPhysicsImageDbService {

    @Autowired
    private FunPhysicsImageLikeMapper likeMapper;

    @Override
    public Page<FunPhysicsImage> getPage(FunPhysicsImage image) {
        Page<FunPhysicsImage> page = new Page<>(1, 10);
        return page;
    }

    @Override
    public Page<FunPhysicsImage> getPublicGallery(int pageNum, int pageSize) {
        Page<FunPhysicsImage> page = new Page<>(pageNum, pageSize);
        Page<FunPhysicsImage> result = baseMapper.getPublicGallery(page);
        fixImagePaths(result.getRecords());
        return result;
    }

    @Override
    public List<FunPhysicsImage> getHotImages(int count) {
        List<FunPhysicsImage> images = baseMapper.getHotImages(count);
        fixImagePaths(images);
        return images;
    }

    @Override
    public List<FunPhysicsImage> getUserImages(String userId) {
        QueryWrapper<FunPhysicsImage> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        List<FunPhysicsImage> list = list(wrapper);
        fixImagePaths(list);
        return list;
    }

    private void fixImagePaths(List<FunPhysicsImage> images) {
        if (images == null) return;
        for (FunPhysicsImage img : images) {
            String path = img.getLocalPath();
            if (path != null && (path.contains(":") || path.contains("\\"))) {
                // It looks like an absolute path or Windows path
                int index = path.lastIndexOf("img");
                if (index != -1) {
                    // Extract from "img" onwards, e.g., /img/funphysics/xxx.png
                    String relativePath = "/" + path.substring(index).replace("\\", "/");
                    img.setLocalPath(relativePath);
                }
            }
        }
    }

    @Override
    public void incrementLikes(String imageId) {
        UpdateWrapper<FunPhysicsImage> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", imageId);
        wrapper.setSql("likes_count = likes_count + 1");
        update(wrapper);
    }

    @Override
    public void decrementLikes(String imageId) {
        UpdateWrapper<FunPhysicsImage> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", imageId);
        wrapper.setSql("likes_count = GREATEST(0, likes_count - 1)");
        update(wrapper);
    }

    @Override
    public FunPhysicsImage getImageWithUser(String imageId, String userId) {
        FunPhysicsImage image = getById(imageId);
        if (image != null && userId != null) {
            QueryWrapper<FunPhysicsImageLike> wrapper = new QueryWrapper<>();
            wrapper.eq("image_id", imageId);
            wrapper.eq("user_id", userId);
            image.setIsLiked(likeMapper.selectCount(wrapper) > 0);
        }
        return image;
    }
}

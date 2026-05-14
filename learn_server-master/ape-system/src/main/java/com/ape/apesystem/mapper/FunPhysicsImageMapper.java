package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.FunPhysicsImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FunPhysicsImageMapper extends BaseMapper<FunPhysicsImage> {

    Page<FunPhysicsImage> getPublicGallery(Page<FunPhysicsImage> page);

    List<FunPhysicsImage> getHotImages(@Param("count") int count);
}

package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.FunPhysicsImageLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface FunPhysicsImageLikeMapper extends BaseMapper<FunPhysicsImageLike> {

    int countByUser(@Param("userId") String userId);
}

package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.FunPhysicsMomentsLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

public interface FunPhysicsMomentsLikeMapper extends BaseMapper<FunPhysicsMomentsLike> {

    int countByUser(@Param("userId") String userId);
}

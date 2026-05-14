package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.FunPhysicsMoments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FunPhysicsMomentsMapper extends BaseMapper<FunPhysicsMoments> {

    Page<FunPhysicsMoments> getPage(Page<FunPhysicsMoments> page, @Param("ew")FunPhysicsMoments moments);

    List<FunPhysicsMoments> getRandomMoments(@Param("count") int count);
}

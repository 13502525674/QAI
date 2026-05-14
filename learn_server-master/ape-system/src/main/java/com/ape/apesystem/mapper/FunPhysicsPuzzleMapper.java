package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.FunPhysicsPuzzle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FunPhysicsPuzzleMapper extends BaseMapper<FunPhysicsPuzzle> {

    Page<FunPhysicsPuzzle> getPage(Page<FunPhysicsPuzzle> page, @Param("ew")FunPhysicsPuzzle puzzle);

    List<FunPhysicsPuzzle> getRandomPuzzles(@Param("count") int count, @Param("difficulty") Integer difficulty, @Param("type") Integer type);
}

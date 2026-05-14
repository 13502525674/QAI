package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.FunPhysicsMomentsComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FunPhysicsMomentsCommentMapper extends BaseMapper<FunPhysicsMomentsComment> {

    List<FunPhysicsMomentsComment> getCommentsByMomentsId(@Param("momentsId") String momentsId);
}

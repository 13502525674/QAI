package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.FunPhysicsMomentsComment;
import com.ape.apesystem.mapper.FunPhysicsMomentsCommentMapper;
import com.ape.apesystem.service.FunPhysicsMomentsCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FunPhysicsMomentsCommentServiceImpl extends ServiceImpl<FunPhysicsMomentsCommentMapper, FunPhysicsMomentsComment> implements FunPhysicsMomentsCommentService {

    @Override
    public List<FunPhysicsMomentsComment> getCommentsByMomentsId(String momentsId) {
        return baseMapper.getCommentsByMomentsId(momentsId);
    }
}

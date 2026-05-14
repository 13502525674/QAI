package com.ape.apesystem.service;

import com.ape.apesystem.domain.FunPhysicsMomentsComment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface FunPhysicsMomentsCommentService extends IService<FunPhysicsMomentsComment> {

    List<FunPhysicsMomentsComment> getCommentsByMomentsId(String momentsId);
}

package com.ape.apesystem.service;

import com.ape.apesystem.domain.FunPhysicsMoments;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface FunPhysicsMomentsService extends IService<FunPhysicsMoments> {

    Page<FunPhysicsMoments> getPage(FunPhysicsMoments moments);

    List<FunPhysicsMoments> getRandomMoments(int count);

    FunPhysicsMoments getMomentsWithComments(String momentsId, String userId);

    void incrementLikes(String momentsId);

    void decrementLikes(String momentsId);
}

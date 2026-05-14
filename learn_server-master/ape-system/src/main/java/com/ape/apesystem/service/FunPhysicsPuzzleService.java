package com.ape.apesystem.service;

import com.ape.apesystem.domain.FunPhysicsPuzzle;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface FunPhysicsPuzzleService extends IService<FunPhysicsPuzzle> {

    Page<FunPhysicsPuzzle> getPage(FunPhysicsPuzzle puzzle);

    List<FunPhysicsPuzzle> getByDifficulty(Integer difficulty);

    List<FunPhysicsPuzzle> getByBranch(String branch);

    List<FunPhysicsPuzzle> getRandomPuzzles(int count, Integer difficulty, Integer type);
}

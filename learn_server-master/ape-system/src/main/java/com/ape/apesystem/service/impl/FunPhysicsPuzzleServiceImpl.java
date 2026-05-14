package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.FunPhysicsPuzzle;
import com.ape.apesystem.mapper.FunPhysicsPuzzleMapper;
import com.ape.apesystem.service.FunPhysicsPuzzleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FunPhysicsPuzzleServiceImpl extends ServiceImpl<FunPhysicsPuzzleMapper, FunPhysicsPuzzle> implements FunPhysicsPuzzleService {

    @Override
    public Page<FunPhysicsPuzzle> getPage(FunPhysicsPuzzle puzzle) {
        Page<FunPhysicsPuzzle> page = new Page<>(puzzle.getPageNumber() != null ? puzzle.getPageNumber() : 1,
                puzzle.getPageSize() != null ? puzzle.getPageSize() : 10);
        return baseMapper.getPage(page, puzzle);
    }

    @Override
    public List<FunPhysicsPuzzle> getByDifficulty(Integer difficulty) {
        QueryWrapper<FunPhysicsPuzzle> wrapper = new QueryWrapper<>();
        wrapper.eq("difficulty", difficulty);
        wrapper.eq("status", 0);
        return list(wrapper);
    }

    @Override
    public List<FunPhysicsPuzzle> getByBranch(String branch) {
        QueryWrapper<FunPhysicsPuzzle> wrapper = new QueryWrapper<>();
        wrapper.eq("branch", branch);
        wrapper.eq("status", 0);
        return list(wrapper);
    }

    @Override
    public List<FunPhysicsPuzzle> getRandomPuzzles(int count, Integer difficulty, Integer type) {
        return baseMapper.getRandomPuzzles(count, difficulty, type);
    }
}

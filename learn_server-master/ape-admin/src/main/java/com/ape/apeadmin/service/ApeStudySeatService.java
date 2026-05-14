package com.ape.apeadmin.service;

import com.ape.apeadmin.domain.ApeStudySeat;
import java.util.List;

public interface ApeStudySeatService {
    int deleteByPrimaryKey(String id);
    int insert(ApeStudySeat record);
    int insertSelective(ApeStudySeat record);
    ApeStudySeat selectByPrimaryKey(String id);
    int updateByPrimaryKeySelective(ApeStudySeat record);
    int updateByPrimaryKey(ApeStudySeat record);
    List<ApeStudySeat> list();
    ApeStudySeat selectBySeatId(String seatId);
    List<ApeStudySeat> selectByUserId(String userId);
    List<ApeStudySeat> selectByStatus(Integer status);
}

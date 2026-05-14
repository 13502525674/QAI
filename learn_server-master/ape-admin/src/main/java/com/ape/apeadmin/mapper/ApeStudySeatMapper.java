package com.ape.apeadmin.mapper;

import com.ape.apeadmin.domain.ApeStudySeat;
import java.util.List;

public interface ApeStudySeatMapper {
    int deleteByPrimaryKey(String id);
    int insert(ApeStudySeat record);
    int insertSelective(ApeStudySeat record);
    ApeStudySeat selectByPrimaryKey(String id);
    int updateByPrimaryKeySelective(ApeStudySeat record);
    int updateByPrimaryKey(ApeStudySeat record);
    List<ApeStudySeat> selectAll();
    ApeStudySeat selectBySeatId(String seatId);
    List<ApeStudySeat> selectByUserId(String userId);
    List<ApeStudySeat> selectByStatus(Integer status);
}

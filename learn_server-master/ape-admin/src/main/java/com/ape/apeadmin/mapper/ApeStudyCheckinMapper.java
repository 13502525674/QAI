package com.ape.apeadmin.mapper;

import com.ape.apeadmin.domain.ApeStudyCheckin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApeStudyCheckinMapper {
    int insert(ApeStudyCheckin record);
    List<String> selectCheckinDatesByUserId(String userId);
    int countByUserIdAndDate(@Param("userId") String userId, @Param("checkinDate") String checkinDate);
}

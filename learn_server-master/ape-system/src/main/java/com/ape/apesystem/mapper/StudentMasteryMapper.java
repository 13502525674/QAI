package com.ape.apesystem.mapper;

import com.ape.apesystem.domain.StudentMastery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface StudentMasteryMapper extends BaseMapper<StudentMastery> {
    
    List<StudentMastery> selectByStudentId(@Param("studentId") String studentId);
    
    StudentMastery selectByStudentAndKp(@Param("studentId") String studentId, @Param("kpId") String kpId);
    
    List<Map<String, Object>> selectMasteryByBranch(@Param("studentId") String studentId);
    
    List<StudentMastery> selectWeakPoints(@Param("studentId") String studentId, @Param("limit") int limit);
    
    List<StudentMastery> selectStrongPoints(@Param("studentId") String studentId, @Param("limit") int limit);
    
    void updateMasteryScore(@Param("studentId") String studentId, @Param("kpId") String kpId, 
                            @Param("masteryScore") Float masteryScore);
    
    void incrementPracticeCount(@Param("studentId") String studentId, @Param("kpId") String kpId,
                                @Param("correct") boolean correct);
}

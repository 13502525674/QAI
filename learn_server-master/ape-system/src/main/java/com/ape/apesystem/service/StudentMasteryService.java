package com.ape.apesystem.service;

import com.ape.apesystem.domain.StudentMastery;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface StudentMasteryService extends IService<StudentMastery> {
    
    void calculateAndUpdateMastery(String studentId);
    
    Map<String, Float> getMasteryByBranch(String studentId);
    
    Map<String, Object> getRadarData(String studentId);
    
    List<StudentMastery> getWeakPoints(String studentId, int limit);
    
    List<StudentMastery> getStrongPoints(String studentId, int limit);
    
    void updateMasteryFromPractice(String studentId, String questionId, boolean isCorrect, float scoreRatio);
    
    void updateMasteryFromVideo(String studentId, String chapterId, float progress);
    
    StudentMastery getOrCreate(String studentId, String kpId);
    
    List<Map<String, Object>> getAllStudentsRadarData();
    
    Map<String, Object> getOverallAverageRadarData();
}

package com.ape.apesystem.service.impl;

import com.ape.apesystem.domain.*;
import com.ape.apesystem.mapper.StudentMasteryMapper;
import com.ape.apesystem.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class StudentMasteryServiceImpl extends ServiceImpl<StudentMasteryMapper, StudentMastery> 
        implements StudentMasteryService {

    @Autowired
    private StudentMasteryMapper studentMasteryMapper;
    
    @Autowired
    private KnowledgePointService knowledgePointService;
    
    @Autowired
    private KnowledgeRelationService knowledgeRelationService;
    
    @Autowired
    private QuestionKnowledgeService questionKnowledgeService;
    
    @Autowired
    private VideoKnowledgeService videoKnowledgeService;
    
    @Autowired
    private ApeUserService apeUserService;
    
    @Autowired
    private PhysicsPracticeRecordsService physicsPracticeRecordsService;
    
    @Autowired
    private PhysicsQuestionPaperService physicsQuestionPaperService;
    
    @Autowired
    private ApeTestStudentService apeTestStudentService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String MASTERY_CACHE_PREFIX = "student:mastery:";
    private static final String RADAR_CACHE_PREFIX = "student:radar:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    @Override
    @Transactional
    public void calculateAndUpdateMastery(String studentId) {
        clearCache(studentId);
    }

    @Override
    public Map<String, Float> getMasteryByBranch(String studentId) {
        String cacheKey = MASTERY_CACHE_PREFIX + "branch:" + studentId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            Map<String, Object> rawMap = JSON.parseObject(cached, Map.class);
            Map<String, Float> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Number) {
                    result.put(entry.getKey(), ((Number) value).floatValue());
                } else {
                    result.put(entry.getKey(), 0f);
                }
            }
            return result;
        }
        
        Map<String, Float> result = calculateMasteryFromPracticeAndTest(studentId);
        
        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(result), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        return result;
    }
    
    private Map<String, Float> calculateMasteryFromPracticeAndTest(String studentId) {
        Map<String, Float> result = new HashMap<>();
        String[] defaultBranches = {"力学", "电学", "热学", "光学", "运动学"};
        for (String branch : defaultBranches) {
            result.put(branch, 0f);
        }
        
        Map<String, List<Double>> branchScores = new HashMap<>();
        for (String branch : defaultBranches) {
            branchScores.put(branch, new ArrayList<>());
        }
        
        QueryWrapper<PhysicsPracticeRecords> practiceWrapper = new QueryWrapper<>();
        practiceWrapper.eq("user_id", studentId);
        practiceWrapper.eq("status", "graded");
        practiceWrapper.isNotNull("score");
        List<PhysicsPracticeRecords> practiceRecords = physicsPracticeRecordsService.list(practiceWrapper);
        
        for (PhysicsPracticeRecords record : practiceRecords) {
            if (record.getPaperId() == null) continue;
            
            PhysicsQuestionPaper paper = physicsQuestionPaperService.getById(record.getPaperId());
            if (paper == null || paper.getSubjectBranch() == null) continue;
            
            String branch = normalizeBranch(paper.getSubjectBranch());
            if (branch != null && record.getScore() != null) {
                double scoreRatio = record.getScore() / 100.0;
                scoreRatio = Math.max(0, Math.min(1, scoreRatio));
                branchScores.get(branch).add(scoreRatio);
            }
        }
        
        QueryWrapper<ApeTestStudent> testWrapper = new QueryWrapper<>();
        testWrapper.eq("user_id", studentId);
        testWrapper.isNotNull("point");
        List<ApeTestStudent> testRecords = apeTestStudentService.list(testWrapper);
        
        if (!testRecords.isEmpty()) {
            double totalTestScore = 0;
            double totalTestPoint = 0;
            for (ApeTestStudent test : testRecords) {
                if (test.getScore() != null && test.getScore() > 0) {
                    totalTestScore += test.getScore();
                }
                if (test.getPoint() != null && test.getPoint() > 0) {
                    totalTestPoint += test.getPoint();
                }
            }
            
            if (totalTestScore > 0) {
                double avgTestScore = totalTestPoint / totalTestScore;
                avgTestScore = Math.max(0, Math.min(1, avgTestScore));
                for (String branch : defaultBranches) {
                    branchScores.get(branch).add(avgTestScore);
                }
            }
        }
        
        for (String branch : defaultBranches) {
            List<Double> scores = branchScores.get(branch);
            if (!scores.isEmpty()) {
                double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                result.put(branch, (float) avg);
            }
        }
        
        return result;
    }
    
    private String normalizeBranch(String branch) {
        if (branch == null) return null;
        branch = branch.trim();
        String lowerBranch = branch.toLowerCase();
        
        switch (branch) {
            case "力学":
            case "运动学":
            case "电学":
            case "热学":
            case "光学":
                return branch;
            case "电磁学":
                return "电学";
        }
        
        switch (lowerBranch) {
            case "mechanics":
                return "力学";
            case "kinematics":
                return "运动学";
            case "electrics":
                return "电学";
            case "thermodynamics":
                return "热学";
            case "optics":
                return "光学";
            default:
                return null;
        }
    }

    @Override
    public Map<String, Object> getRadarData(String studentId) {
        String cacheKey = RADAR_CACHE_PREFIX + studentId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return JSON.parseObject(cached, Map.class);
        }
        
        Map<String, Float> branchMastery = getMasteryByBranch(studentId);
        
        List<Map<String, Object>> indicators = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        
        String[] branches = {"力学", "电学", "热学", "光学", "运动学"};
        for (String branch : branches) {
            Map<String, Object> indicator = new HashMap<>();
            indicator.put("name", branch);
            indicator.put("max", 1);
            indicators.add(indicator);
            values.add(branchMastery.getOrDefault(branch, 0f));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("indicators", indicators);
        
        List<Map<String, Object>> seriesData = new ArrayList<>();
        Map<String, Object> dataItem = new HashMap<>();
        dataItem.put("value", values);
        dataItem.put("name", "能力值");
        seriesData.add(dataItem);
        result.put("data", seriesData);
        
        ApeUser user = apeUserService.getById(studentId);
        if (user != null) {
            result.put("studentName", user.getUserName());
        }
        
        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(result), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        return result;
    }

    @Override
    public List<StudentMastery> getWeakPoints(String studentId, int limit) {
        return studentMasteryMapper.selectWeakPoints(studentId, limit);
    }

    @Override
    public List<StudentMastery> getStrongPoints(String studentId, int limit) {
        return studentMasteryMapper.selectStrongPoints(studentId, limit);
    }

    @Override
    @Transactional
    public void updateMasteryFromPractice(String studentId, String questionId, boolean isCorrect, float scoreRatio) {
        List<QuestionKnowledge> qkList = questionKnowledgeService.getByQuestionId(questionId);
        
        for (QuestionKnowledge qk : qkList) {
            StudentMastery mastery = getOrCreate(studentId, qk.getKpId());
            
            float practiceWeight = 0.6f;
            float contribution = isCorrect ? scoreRatio : (scoreRatio * 0.3f);
            float newScore = mastery.getMasteryScore() * (1 - practiceWeight) + contribution * practiceWeight;
            newScore = Math.max(0, Math.min(1, newScore));
            
            mastery.setMasteryScore(newScore);
            mastery.setPracticeCount(mastery.getPracticeCount() + 1);
            if (isCorrect) {
                mastery.setCorrectCount(mastery.getCorrectCount() + 1);
            }
            mastery.setLastPracticeTime(new Date());
            mastery.setUpdateTime(new Date());
            
            updateById(mastery);
        }
        
        clearCache(studentId);
    }

    @Override
    @Transactional
    public void updateMasteryFromVideo(String studentId, String chapterId, float progress) {
        List<VideoKnowledge> vkList = videoKnowledgeService.getByChapterId(chapterId);
        
        for (VideoKnowledge vk : vkList) {
            StudentMastery mastery = getOrCreate(studentId, vk.getKpId());
            
            float videoWeight = 0.2f;
            float currentProgress = mastery.getVideoProgress();
            float progressDelta = Math.max(0, progress - currentProgress);
            float newScore = mastery.getMasteryScore() + progressDelta * videoWeight;
            newScore = Math.max(0, Math.min(1, newScore));
            
            mastery.setMasteryScore(newScore);
            mastery.setVideoProgress(Math.max(currentProgress, progress));
            mastery.setUpdateTime(new Date());
            
            updateById(mastery);
        }
        
        clearCache(studentId);
    }

    @Override
    public StudentMastery getOrCreate(String studentId, String kpId) {
        StudentMastery mastery = studentMasteryMapper.selectByStudentAndKp(studentId, kpId);
        
        if (mastery == null) {
            mastery = new StudentMastery();
            mastery.setStudentId(studentId);
            mastery.setKpId(kpId);
            mastery.setMasteryScore(0f);
            mastery.setPracticeCount(0);
            mastery.setCorrectCount(0);
            mastery.setVideoProgress(0f);
            mastery.setUpdateTime(new Date());
            save(mastery);
        }
        
        return mastery;
    }

    @Override
    public List<Map<String, Object>> getAllStudentsRadarData() {
        QueryWrapper<ApeUser> userWrapper = new QueryWrapper<>();
        userWrapper.eq("user_type", 2);
        List<ApeUser> students = apeUserService.list(userWrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (ApeUser student : students) {
            Map<String, Object> radarData = getRadarData(student.getId());
            radarData.put("studentId", student.getId());
            radarData.put("studentName", student.getUserName());
            result.add(radarData);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getOverallAverageRadarData() {
        String cacheKey = RADAR_CACHE_PREFIX + "overall_average";
        
        redisTemplate.delete(cacheKey);
        
        Map<String, Float> branchMastery = calculateOverallAverageFromAllRecords();
        
        List<Map<String, Object>> indicators = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        
        String[] branches = {"力学", "电学", "热学", "光学", "运动学"};
        for (String branch : branches) {
            Map<String, Object> indicator = new HashMap<>();
            indicator.put("name", branch);
            indicator.put("max", 1);
            indicators.add(indicator);
            values.add(branchMastery.getOrDefault(branch, 0f));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("indicators", indicators);
        
        List<Map<String, Object>> seriesData = new ArrayList<>();
        Map<String, Object> dataItem = new HashMap<>();
        dataItem.put("value", values);
        dataItem.put("name", "整体平均能力");
        seriesData.add(dataItem);
        result.put("data", seriesData);
        result.put("studentName", "整体学生平均");
        
        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(result), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        return result;
    }
    
    private Map<String, Float> calculateOverallAverageFromAllRecords() {
        Map<String, Float> result = new HashMap<>();
        String[] defaultBranches = {"力学", "电学", "热学", "光学", "运动学"};
        for (String branch : defaultBranches) {
            result.put(branch, 0f);
        }
        
        Map<String, List<Double>> branchScores = new HashMap<>();
        for (String branch : defaultBranches) {
            branchScores.put(branch, new ArrayList<>());
        }
        
        QueryWrapper<PhysicsPracticeRecords> practiceWrapper = new QueryWrapper<>();
        practiceWrapper.eq("status", "graded");
        practiceWrapper.isNotNull("score");
        List<PhysicsPracticeRecords> practiceRecords = physicsPracticeRecordsService.list(practiceWrapper);
        
        for (PhysicsPracticeRecords record : practiceRecords) {
            if (record.getPaperId() == null) continue;
            
            PhysicsQuestionPaper paper = physicsQuestionPaperService.getById(record.getPaperId());
            if (paper == null || paper.getSubjectBranch() == null) continue;
            
            String branch = normalizeBranch(paper.getSubjectBranch());
            if (branch != null && record.getScore() != null) {
                double scoreRatio = record.getScore() / 100.0;
                scoreRatio = Math.max(0, Math.min(1, scoreRatio));
                branchScores.get(branch).add(scoreRatio);
            }
        }
        
        QueryWrapper<ApeTestStudent> testWrapper = new QueryWrapper<>();
        testWrapper.isNotNull("point");
        List<ApeTestStudent> testRecords = apeTestStudentService.list(testWrapper);
        
        if (!testRecords.isEmpty()) {
            double totalTestScore = 0;
            double totalTestPoint = 0;
            for (ApeTestStudent test : testRecords) {
                if (test.getScore() != null && test.getScore() > 0) {
                    totalTestScore += test.getScore();
                }
                if (test.getPoint() != null && test.getPoint() > 0) {
                    totalTestPoint += test.getPoint();
                }
            }
            
            if (totalTestScore > 0) {
                double avgTestScore = totalTestPoint / totalTestScore;
                avgTestScore = Math.max(0, Math.min(1, avgTestScore));
                for (String branch : defaultBranches) {
                    branchScores.get(branch).add(avgTestScore);
                }
            }
        }
        
        for (String branch : defaultBranches) {
            List<Double> scores = branchScores.get(branch);
            if (!scores.isEmpty()) {
                double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                result.put(branch, (float) avg);
            }
        }
        
        return result;
    }
    
    private void clearCache(String studentId) {
        redisTemplate.delete(MASTERY_CACHE_PREFIX + "branch:" + studentId);
        redisTemplate.delete(RADAR_CACHE_PREFIX + studentId);
    }
}

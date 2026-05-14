package com.ape.apeadmin.controller.knowledge;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.domain.StudentMastery;
import com.ape.apesystem.service.StudentMasteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentMasteryController {

    @Autowired
    private StudentMasteryService studentMasteryService;

    @Log(name = "获取学生能力雷达图", type = BusinessType.OTHER)
    @GetMapping("/radar/{studentId}")
    public Result getRadarData(@PathVariable String studentId) {
        Map<String, Object> radarData = studentMasteryService.getRadarData(studentId);
        return Result.success(radarData);
    }

    @Log(name = "获取当前登录学生能力雷达图", type = BusinessType.OTHER)
    @GetMapping("/radar/my")
    public Result getMyRadarData() {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        Map<String, Object> radarData = studentMasteryService.getRadarData(user.getId());
        return Result.success(radarData);
    }

    @Log(name = "获取所有学生雷达图数据", type = BusinessType.OTHER)
    @GetMapping("/radar/all")
    public Result getAllStudentsRadarData() {
        List<Map<String, Object>> radarDataList = studentMasteryService.getAllStudentsRadarData();
        return Result.success(radarDataList);
    }

    @Log(name = "获取整体学生平均能力雷达图", type = BusinessType.OTHER)
    @GetMapping("/radar/overall")
    public Result getOverallAverageRadarData() {
        Map<String, Object> radarData = studentMasteryService.getOverallAverageRadarData();
        return Result.success(radarData);
    }

    @Log(name = "获取学生各分支掌握度", type = BusinessType.OTHER)
    @GetMapping("/mastery/branch/{studentId}")
    public Result getMasteryByBranch(@PathVariable String studentId) {
        Map<String, Float> mastery = studentMasteryService.getMasteryByBranch(studentId);
        return Result.success(mastery);
    }

    @Log(name = "获取学生薄弱知识点", type = BusinessType.OTHER)
    @GetMapping("/weak/{studentId}")
    public Result getWeakPoints(@PathVariable String studentId, 
                                @RequestParam(defaultValue = "5") int limit) {
        List<StudentMastery> weakPoints = studentMasteryService.getWeakPoints(studentId, limit);
        return Result.success(weakPoints);
    }

    @Log(name = "获取学生优势知识点", type = BusinessType.OTHER)
    @GetMapping("/strong/{studentId}")
    public Result getStrongPoints(@PathVariable String studentId, 
                                  @RequestParam(defaultValue = "3") int limit) {
        List<StudentMastery> strongPoints = studentMasteryService.getStrongPoints(studentId, limit);
        return Result.success(strongPoints);
    }

    @Log(name = "重新计算学生掌握度", type = BusinessType.UPDATE)
    @PostMapping("/mastery/recalculate/{studentId}")
    public Result recalculateMastery(@PathVariable String studentId) {
        studentMasteryService.calculateAndUpdateMastery(studentId);
        return Result.success("掌握度计算完成");
    }

    @Log(name = "获取学生所有知识点掌握度", type = BusinessType.OTHER)
    @GetMapping("/mastery/all/{studentId}")
    public Result getAllMastery(@PathVariable String studentId) {
        List<StudentMastery> masteryList = studentMasteryService.getWeakPoints(studentId, 1000);
        return Result.success(masteryList);
    }
}

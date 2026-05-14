package com.ape.apeadmin.controller.study;

import com.ape.apeadmin.domain.*;
import com.ape.apeadmin.service.*;
import com.ape.apecommon.domain.Result;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeUserService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 自习室控制器
 */
@Controller
@ResponseBody
@RequestMapping("/studyRoom")
public class StudyRoomController {

    @Autowired
    private ApeStudySeatService seatService;
    
    @Autowired
    private ApePhysicsLawFavorService lawFavorService;
    
    @Autowired
    private ApeNotebookRecordService notebookService;
    
    @Autowired
    private ApeStudyTodoService todoService;
    
    @Autowired
    private ApeStudyCheckinService checkinService;
    
    @Autowired
    private ApeStudyCountdownGoalService countdownGoalService;
    
    @Autowired
    private ApeUserService apeUserService;

    private static String readString(Map<String, Object> body, String key) {
        if (body == null) return null;
        Object v = body.get(key);
        return v == null ? null : String.valueOf(v);
    }

    // ==================== 座位管理 ====================
    
    // 获取所有座位
    @GetMapping("/seats")
    public Result getAllSeats() {
        return Result.success(seatService.list());
    }
    
    // 选座（前端发送 JSON: { seatId, goal }）
    @PostMapping("/seat/select")
    public Result selectSeat(@RequestBody(required = false) Map<String, Object> body) {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String seatId = readString(body, "seatId");
        String goal = readString(body, "goal");
        String nicknameParam = readString(body, "nickname");
        if (seatId == null || seatId.isEmpty()) {
            return Result.fail("seatId不能为空");
        }
        String userId = user.getId();
        // 优先使用前端传来的昵称，否则从数据库查询
        String nickname = nicknameParam;
        if (nickname == null || nickname.isEmpty()) {
            ApeUser fullUser = apeUserService.getById(userId);
            nickname = fullUser != null ? fullUser.getUserName() : user.getUserName();
        }
        
        // 先检查这个座位是否存在，不存在则自动创建
        ApeStudySeat seat = seatService.selectBySeatId(seatId);
        if (seat == null) {
            seat = new ApeStudySeat();
            seat.setId(IdWorker.get32UUID());
            seat.setSeatId(seatId);
            seat.setStatus(0);
            Date now = new Date();
            seat.setCreateTime(now);
            seat.setUpdateTime(now);
            seatService.insert(seat);
        }
        
        if (seat.getStatus() != null && seat.getStatus() == 1) {
            return Result.fail("座位已被占用");
        }
        
        // 先释放用户之前的座位
        List<ApeStudySeat> userSeats = seatService.selectByUserId(userId);
        for (ApeStudySeat userSeat : userSeats) {
            userSeat.setUserId(null);
            userSeat.setNickname(null);
            userSeat.setGoal(null);
            userSeat.setStatus(0);
            userSeat.setEnterTime(null);
            seatService.updateByPrimaryKey(userSeat);
        }
        
        // 占用新座位
        seat.setUserId(userId);
        seat.setNickname(nickname);
        seat.setGoal(goal);
        seat.setStatus(1);
        seat.setEnterTime(new Date());
        seatService.updateByPrimaryKey(seat);
        
        return Result.success();
    }
    
    // 离开座位
    @PostMapping("/seat/leave")
    public Result leaveSeat() {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String userId = user.getId();
        
        List<ApeStudySeat> userSeats = seatService.selectByUserId(userId);
        for (ApeStudySeat userSeat : userSeats) {
            userSeat.setUserId(null);
            userSeat.setNickname(null);
            userSeat.setGoal(null);
            userSeat.setStatus(0);
            userSeat.setEnterTime(null);
            seatService.updateByPrimaryKey(userSeat);
        }
        
        return Result.success();
    }
    
    // 更新学习目标
    @PostMapping("/seat/goal")
    public Result updateGoal(@RequestBody(required = false) Map<String, Object> body) {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String goal = readString(body, "goal");
        if (goal == null || goal.trim().isEmpty()) {
            return Result.fail("goal不能为空");
        }
        String userId = user.getId();
        
        List<ApeStudySeat> userSeats = seatService.selectByUserId(userId);
        for (ApeStudySeat userSeat : userSeats) {
            userSeat.setGoal(goal);
            seatService.updateByPrimaryKey(userSeat);
        }
        
        return Result.success();
    }

    // 兼容前端旧接口：/studyRoom/seat/updateGoal
    @PostMapping("/seat/updateGoal")
    public Result updateGoalCompat(@RequestBody(required = false) Map<String, Object> body) {
        return updateGoal(body);
    }
    
    // 获取在线用户
    @GetMapping("/onlineUsers")
    public Result getOnlineUsers() {
        List<ApeStudySeat> seats = seatService.selectByStatus(1);
        
        List<Map<String, Object>> users = new ArrayList<>();
        for (ApeStudySeat seat : seats) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", seat.getUserId());
            user.put("nickname", seat.getNickname());
            user.put("seatId", seat.getSeatId());
            user.put("goal", seat.getGoal());
            users.add(user);
        }
        
        return Result.success(users);
    }

    // ==================== 物理定律收藏 ====================
    
    // 获取收藏列表
    @GetMapping("/lawFavors")
    public Result getLawFavors() {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String userId = user.getId();
        
        List<ApePhysicsLawFavor> favors = lawFavorService.selectByUserId(userId);
        
        return Result.success(favors);
    }
    
    // 收藏/取消收藏
    @PostMapping("/lawFavor/toggle")
    public Result toggleLawFavor(@RequestBody ApePhysicsLawFavor favor) {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String userId = user.getId();
        
        // 检查是否已收藏
        ApePhysicsLawFavor exist = lawFavorService.selectByUserIdAndLawName(userId, favor.getLawName());
        
        if (exist != null) {
            // 已收藏，取消收藏
            lawFavorService.deleteByPrimaryKey(exist.getId());
            return Result.success("取消收藏成功");
        } else {
            // 未收藏，添加收藏
            favor.setId(IdWorker.get32UUID());
            favor.setUserId(userId);
            favor.setCreateTime(new Date());
            lawFavorService.insert(favor);
            return Result.success("收藏成功");
        }
    }

    // ==================== 签到 ====================
    
    @GetMapping("/checkins")
    public Result getCheckins() {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        List<String> dates = checkinService.selectCheckinDatesByUserId(user.getId());
        return Result.success(dates);
    }
    
    @PostMapping("/checkin")
    public Result checkin(@RequestBody(required = false) Map<String, Object> body) {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String dateStr = readString(body, "date");
        if (dateStr == null || dateStr.isEmpty()) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            dateStr = sdf.format(new Date());
        }
        if (checkinService.hasCheckedIn(user.getId(), dateStr)) {
            return Result.fail("今日已签到");
        }
        checkinService.insert(user.getId(), dateStr);
        return Result.success();
    }

    // ==================== 目标倒计日 ====================
    
    @GetMapping("/countdownGoals")
    public Result getCountdownGoals() {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        List<ApeStudyCountdownGoal> list = countdownGoalService.selectByUserId(user.getId());
        return Result.success(list);
    }
    
    @PostMapping("/countdownGoal")
    public Result addCountdownGoal(@RequestBody(required = false) Map<String, Object> body) {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String goalName = readString(body, "goalName");
        String goalDate = readString(body, "goalDate");
        if (goalName == null || goalName.trim().isEmpty()) {
            return Result.fail("目标名称不能为空");
        }
        if (goalDate == null || goalDate.isEmpty()) {
            return Result.fail("目标日期不能为空");
        }
        ApeStudyCountdownGoal goal = new ApeStudyCountdownGoal();
        goal.setId(IdWorker.get32UUID());
        goal.setUserId(user.getId());
        goal.setGoalName(goalName.trim());
        goal.setGoalDate(goalDate);
        goal.setCreateTime(new Date());
        countdownGoalService.insert(goal);
        return Result.success(goal);
    }
    
    @DeleteMapping("/countdownGoal/{id}")
    public Result deleteCountdownGoal(@PathVariable("id") String id) {
        countdownGoalService.deleteByPrimaryKey(id);
        return Result.success();
    }

    // ==================== 草稿纸 ====================
    
    // 获取草稿列表
    @GetMapping("/notebooks")
    public Result getNotebooks() {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String userId = user.getId();
        
        List<ApeNotebookRecord> records = notebookService.selectByUserId(userId);
        
        return Result.success(records);
    }
    
    // 保存草稿
    @PostMapping("/notebook/save")
    public Result saveNotebook(@RequestBody ApeNotebookRecord record) {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String userId = user.getId();
        
        if (record.getId() == null || record.getId().isEmpty()) {
            // 新增
            record.setId(IdWorker.get32UUID());
            record.setUserId(userId);
            record.setCreateTime(new Date());
            record.setUpdateTime(new Date());
            notebookService.insert(record);
        } else {
            // 更新
            record.setUpdateTime(new Date());
            notebookService.updateByPrimaryKey(record);
        }
        
        return Result.success(record.getId());
    }
    
    // 删除草稿
    @DeleteMapping("/notebook/{id}")
    public Result deleteNotebook(@PathVariable("id") String id) {
        notebookService.deleteByPrimaryKey(id);
        return Result.success();
    }

    // ==================== 待办事项 ====================
    
    // 获取待办列表
    @GetMapping("/todos")
    public Result getTodos() {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String userId = user.getId();
        
        List<ApeStudyTodo> todos = todoService.selectByUserId(userId);
        
        return Result.success(todos);
    }
    
    // 添加待办
    @PostMapping("/todo/add")
    public Result addTodo(@RequestBody(required = false) Map<String, Object> body) {
        com.ape.apesystem.domain.ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        String content = readString(body, "content");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("content不能为空");
        }
        String userId = user.getId();
        
        ApeStudyTodo todo = new ApeStudyTodo();
        todo.setId(IdWorker.get32UUID());
        todo.setUserId(userId);
        todo.setContent(content);
        todo.setCompleted(0);
        todo.setCreateTime(new Date());
        
        todoService.insert(todo);
        return Result.success();
    }

    // ==================== 用户信息（同桌信息） ====================
    @GetMapping("/user/{userId}")
    public Result getUserById(@PathVariable("userId") String userId) {
        ApeUser u = apeUserService.getById(userId);
        if (u == null) {
            return Result.fail("用户不存在");
        }
        return Result.success(u);
    }
    
    // 完成待办
    @PostMapping("/todo/complete/{id}")
    public Result completeTodo(@PathVariable("id") String id) {
        ApeStudyTodo todo = todoService.selectByPrimaryKey(id);
        if (todo != null) {
            todo.setCompleted(todo.getCompleted() == 0 ? 1 : 0);
            todo.setCompleteTime(todo.getCompleted() == 1 ? new Date() : null);
            todoService.updateByPrimaryKey(todo);
        }
        return Result.success();
    }
    
    // 删除待办
    @DeleteMapping("/todo/{id}")
    public Result deleteTodo(@PathVariable("id") String id) {
        todoService.deleteByPrimaryKey(id);
        return Result.success();
    }
}

package com.estudy.task.controller;

import com.estudy.common.result.Result;
import com.estudy.task.dto.TaskCreateDTO;
import com.estudy.task.entity.Task;
import com.estudy.task.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    private Long getCurrentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    /** 我的任务列表 */
    @GetMapping("/my")
    public Result<List<Task>> myTasks(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(taskService.getMyTasks(userId));
    }

    /** 任务详情 */
    @GetMapping("/{taskId}")
    public Result<Task> taskDetail(@PathVariable Long taskId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(taskService.getTaskDetail(taskId, userId));
    }

    /** 创建任务（管理员） */
    @PostMapping
    public Result<Long> createTask(@RequestBody @Valid TaskCreateDTO dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(taskService.createTask(dto, userId));
    }

    /** 完成学习步骤 */
    @PostMapping("/{taskId}/complete-learn")
    public Result<Void> completeLearn(@PathVariable Long taskId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        taskService.completeLearn(userId, taskId);
        return Result.success(null);
    }

    /** 检查是否可以开始考试 */
    @GetMapping("/{taskId}/can-exam")
    public Result<Boolean> canStartExam(@PathVariable Long taskId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(taskService.canStartExam(userId, taskId));
    }

    /** 获取任务关联的试卷ID */
    @GetMapping("/{taskId}/paper")
    public Result<Long> getTaskPaperId(@PathVariable Long taskId) {
        return Result.success(taskService.getTaskPaperId(taskId));
    }
}

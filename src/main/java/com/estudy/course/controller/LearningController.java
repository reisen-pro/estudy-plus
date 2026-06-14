package com.estudy.course.controller;

import com.estudy.common.result.Result;
import com.estudy.course.dto.LearningProgressDTO;
import com.estudy.course.service.LearningProgressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningProgressService progressService;
    private final HttpServletRequest request;

    /** 记录学习进度 */
    @PostMapping("/progress")
    public Result<Void> recordProgress(@RequestBody @Valid LearningProgressDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        progressService.recordProgress(userId, dto);
        return Result.success();
    }

    /** 查询用户在某课程的总进度 */
    @GetMapping("/progress/course/{courseId}")
    public Result<Map<String, Object>> getCourseProgress(@PathVariable Long courseId) {
        Long userId = (Long) request.getAttribute("userId");
        int progress = progressService.getCourseProgress(userId, courseId);
        return Result.success(Map.of("courseId", courseId, "progress", progress));
    }
}

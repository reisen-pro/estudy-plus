package com.estudy.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.estudy.common.exception.BusinessException;
import com.estudy.common.result.Result;
import com.estudy.common.annotation.permission.RequirePermission;
import com.estudy.course.dto.CourseCreateDTO;
import com.estudy.course.dto.CourseQueryDTO;
import com.estudy.course.dto.LearningProgressDTO;
import com.estudy.course.entity.Course;
import com.estudy.course.entity.CourseChapter;
import com.estudy.course.service.CourseChapterService;
import com.estudy.course.service.CourseService;
import com.estudy.course.service.LearningProgressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 课程管理控制器
 * <p>
 * 提供课程的 CRUD、发布/下架、分类查询、学习进度管理等接口。
 * 部分操作需要相应的 RBAC 权限（如 course:upload、course:manage、course:delete）。
 * </p>
 */
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseChapterService chapterService;
    private final LearningProgressService learningProgressService;

    /**
     * 获取当前登录用户ID
     * <p>
     * 从 HTTP 请求属性中获取由 JWT 认证拦截器设置的用户ID。
     * 该值在请求预处理阶段由拦截器解析 Token 后存入。
     * </p>
     *
     * @param request HTTP 请求对象，包含已解析的用户信息
     * @return 当前登录用户的ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }


    // ========== 课程 CRUD ==========

    /**
     * 创建课程（需要 course:upload 权限）
     */
    @PostMapping
    @RequirePermission("course:upload")
    public Result<Long> create(@RequestBody @Valid CourseCreateDTO dto) {
        return Result.success(courseService.createCourse(dto));
    }

    /**
     * 更新课程（需要 course:manage 权限）
     */
    @PutMapping("/{id}")
    @RequirePermission("course:manage")
    public Result<Void> update(@PathVariable Long id, @RequestBody CourseCreateDTO dto) {
        Course course = courseService.getById(id);
        if (course == null) throw new BusinessException("课程不存在");
        course.setTitle(dto.getTitle());
        course.setSubtitle(dto.getSubtitle());
        course.setCoverUrl(dto.getCoverUrl());
        course.setCategoryId(dto.getCategoryId());
        course.setTeacherId(dto.getTeacherId());
        course.setDescription(dto.getDescription());
        course.setCourseType(dto.getCourseType());
        course.setPrice(dto.getPrice());
        courseService.updateById(course);
        return Result.success();
    }

    /**
     * 发布课程（需要 course:manage 权限）
     */
    @PutMapping("/{id}/publish")
    @RequirePermission("course:manage")
    public Result<Void> publish(@PathVariable Long id) {
        courseService.publishCourse(id);
        return Result.success();
    }

    /**
     * 下架课程（需要 course:manage 权限）
     */
    @PutMapping("/{id}/unpublish")
    @RequirePermission("course:manage")
    public Result<Void> unpublish(@PathVariable Long id) {
        courseService.unpublishCourse(id);
        return Result.success();
    }

    /**
     * 删除课程（需要 course:delete 权限）
     */
    @DeleteMapping("/{id}")
    @RequirePermission("course:delete")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.success();
    }

    /**
     * 我的课程 - 必须在 /{id} 之前
     */
    @GetMapping("/my")
    public Result<List<Course>> myCourses(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(courseService.getMyCourses(userId));
    }

    /**
     * 课程详情(含章节目录)
     */
    @GetMapping("/{id}")
    public Result<Course> detail(@PathVariable Long id) {
        Course course = courseService.getById(id);
        if (course == null) throw new BusinessException("课程不存在");
        // 填充分类名
        courseService.fillCategoryName(List.of(course));
        // 填充章节目录
        course.setChapters(chapterService.getCourseCatalog(id));
        return Result.success(course);
    }

    /**
     * 课程列表(兼容 /list 和 /page)
     */
    @GetMapping({"/list", "/page"})
    public Result<Page<Course>> list(CourseQueryDTO dto) {
        return Result.success(courseService.pageQuery(dto));
    }

    /**
     * 获取课程目录(章节+课时)
     */
    @GetMapping("/{id}/catalog")
    public Result<List<CourseChapter>> catalog(@PathVariable Long id) {
        return Result.success(chapterService.getCourseCatalog(id));
    }


    // ========== 学习进度（别名路由，映射到 /learning/progress） ==========

    /**
     * 记录学习进度
     */
    @PostMapping("/progress")
    public Result<Void> recordProgress(@RequestBody @Valid LearningProgressDTO dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        learningProgressService.recordProgress(userId, dto);
        return Result.success();
    }

    /**
     * 查询课程进度
     */
    @GetMapping("/{id}/progress")
    public Result<Map<String, Object>> courseProgress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        int progress = learningProgressService.getCourseProgress(userId, id);
        return Result.success(Map.of("courseId", id, "progress", progress));
    }
}

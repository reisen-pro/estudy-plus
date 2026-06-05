package com.estudy.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.common.exception.BusinessException;
import com.estudy.course.dto.CourseCreateDTO;
import com.estudy.course.dto.CourseQueryDTO;
import com.estudy.course.entity.Course;
import com.estudy.course.entity.CourseCategory;
import com.estudy.course.entity.LearningProgress;
import com.estudy.course.mapper.CourseCategoryMapper;
import com.estudy.course.mapper.CourseMapper;
import com.estudy.user.entity.SysUser;
import com.estudy.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    private final LearningProgressService learningProgressService;
    private final CourseCategoryMapper categoryMapper;
    private final SysUserMapper userMapper;

    /**
     * 创建课程
     */
    public Long createCourse(CourseCreateDTO dto) {
        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setSubtitle(dto.getSubtitle());
        course.setCoverUrl(dto.getCoverUrl());
        course.setCategoryId(dto.getCategoryId());
        course.setTeacherId(dto.getTeacherId());
        course.setDescription(dto.getDescription());
        course.setCourseType(dto.getCourseType());
        course.setPrice(dto.getPrice());
        course.setStatus(0); // 草稿
        course.setViewCount(0);
        save(course);
        return course.getId();
    }

    /**
     * 发布课程
     */
    public void publishCourse(Long courseId) {
        Course course = getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        course.setStatus(1);
        updateById(course);
    }

    /**
     * 下架课程
     */
    public void unpublishCourse(Long courseId) {
        Course course = getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        course.setStatus(2);
        updateById(course);
    }

    /**
     * 我的课程 - 查询当前用户有学习记录的课程
     */
    public List<Course> getMyCourses(Long userId) {
        LambdaQueryWrapper<LearningProgress> lpWrapper = new LambdaQueryWrapper<>();
        lpWrapper.eq(LearningProgress::getUserId, userId)
                 .select(LearningProgress::getCourseId);
        List<Long> courseIds = learningProgressService.list(lpWrapper)
                .stream()
                .map(LearningProgress::getCourseId)
                .distinct()
                .collect(Collectors.toList());
        if (courseIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Course::getId, courseIds)
               .eq(Course::getStatus, 1)
               .orderByDesc(Course::getCreateTime);
        List<Course> courses = list(wrapper);
        fillCategoryName(courses);
        return courses;
    }

    /**
     * 分页查询课程
     */
    public Page<Course> pageQuery(CourseQueryDTO dto) {
        Page<Course> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(Course::getTitle, dto.getKeyword());
        }
        if (dto.getCategoryId() != null) {
            wrapper.eq(Course::getCategoryId, dto.getCategoryId());
        }
        if (dto.getCourseType() != null) {
            wrapper.eq(Course::getCourseType, dto.getCourseType());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Course::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(Course::getCreateTime);
        Page<Course> result = page(page, wrapper);
        fillCategoryName(result.getRecords());
        return result;
    }

    /**
     * 填充课程分类名称 + 讲师信息
     */
    public void fillCategoryName(List<Course> courses) {
        if (courses == null || courses.isEmpty()) return;

        // 分类名
        List<Long> catIds = courses.stream()
                .map(Course::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> catNameMap = catIds.isEmpty() ? Map.of() : categoryMapper.selectBatchIds(catIds)
                .stream()
                .collect(Collectors.toMap(CourseCategory::getId, CourseCategory::getCategoryName));

        // 讲师信息
        List<Long> teacherIds = courses.stream()
                .map(Course::getTeacherId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, SysUser> teacherMap = teacherIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(teacherIds)
                .stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        for (Course c : courses) {
            if (c.getCategoryId() != null) {
                c.setCategoryName(catNameMap.get(c.getCategoryId()));
            }
            if (c.getTeacherId() != null) {
                SysUser teacher = teacherMap.get(c.getTeacherId());
                if (teacher != null) {
                    c.setTeacherName(teacher.getRealName());
                    c.setTeacherAvatar(teacher.getAvatar());
                }
            }
        }
    }
}

package com.estudy.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.course.entity.CourseChapter;
import com.estudy.course.entity.CourseLesson;
import com.estudy.course.mapper.CourseChapterMapper;
import com.estudy.course.mapper.CourseLessonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseChapterService extends ServiceImpl<CourseChapterMapper, CourseChapter> {

    private final CourseLessonMapper lessonMapper;

    /**
     * 获取课程的完整目录结构(章节+课时)
     */
    public List<CourseChapter> getCourseCatalog(Long courseId) {
        // 查所有章节
        List<CourseChapter> chapters = list(
                new LambdaQueryWrapper<CourseChapter>()
                        .eq(CourseChapter::getCourseId, courseId)
                        .orderByAsc(CourseChapter::getSort)
        );
        // 查所有课时
        List<CourseLesson> lessons = lessonMapper.selectList(
                new LambdaQueryWrapper<CourseLesson>()
                        .eq(CourseLesson::getCourseId, courseId)
                        .orderByAsc(CourseLesson::getSort)
        );
        // 课时挂到章节下（用transient字段存，前端直接用）
        for (CourseChapter chapter : chapters) {
            chapter.setLessons(lessons.stream()
                    .filter(l -> l.getChapterId().equals(chapter.getId()))
                    .toList());
        }
        return chapters;
    }
}

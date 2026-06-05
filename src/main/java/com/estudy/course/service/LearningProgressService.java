package com.estudy.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.course.dto.LearningProgressDTO;
import com.estudy.course.entity.CourseLesson;
import com.estudy.course.entity.LearningProgress;
import com.estudy.course.mapper.CourseLessonMapper;
import com.estudy.course.mapper.LearningProgressMapper;
import com.estudy.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningProgressService extends ServiceImpl<LearningProgressMapper, LearningProgress> {

    private final CourseLessonMapper lessonMapper;
    private final PointService pointService;
    private final com.estudy.task.service.TaskService taskService;
    private final com.estudy.task.mapper.UserTaskMapper userTaskMapper;
    private final com.estudy.task.mapper.TaskMapper taskMapper;

    /**
     * 记录/更新学习进度，并自动计积分
     * 积分规则：差量计算，只对新增学习时长计分
     */
    @Transactional
    public void recordProgress(Long userId, LearningProgressDTO dto) {
        LearningProgress progress = getOne(
                new LambdaQueryWrapper<LearningProgress>()
                        .eq(LearningProgress::getUserId, userId)
                        .eq(LearningProgress::getLessonId, dto.getLessonId())
        );

        int oldDuration = 0;
        boolean wasCompleted = false;

        if (progress == null) {
            progress = new LearningProgress();
            progress.setUserId(userId);
            progress.setCourseId(dto.getCourseId());
            progress.setLessonId(dto.getLessonId());
        } else {
            oldDuration = progress.getLearnDuration() != null ? progress.getLearnDuration() : 0;
            wasCompleted = progress.getCompleted() != null && progress.getCompleted() == 1;
        }

        if (dto.getProgress() != null) {
            progress.setProgress(dto.getProgress());
        }
        if (dto.getLearnDuration() != null) {
            progress.setLearnDuration(dto.getLearnDuration());
        }
        if (dto.getLastPosition() != null) {
            progress.setLastPosition(dto.getLastPosition());
        }

        // 进度达到100%自动标记完成
        boolean justCompleted = false;
        if (progress.getProgress() != null && progress.getProgress() >= 100) {
            // 文档类课时：校验阅读时长是否达标
            CourseLesson lesson = lessonMapper.selectById(dto.getLessonId());
            if (lesson != null && lesson.getLessonType() == 2) {
                // 文档课时，检查是否有任务指定了更长的阅读时长
                int requiredDuration = getDocRequiredDuration(userId, dto.getCourseId(), lesson);
                int actualDuration = progress.getLearnDuration() != null ? progress.getLearnDuration() : 0;
                if (actualDuration < requiredDuration) {
                    // 未达要求，不标记完成，但保存进度
                    saveOrUpdate(progress);
                    log.info("用户{}文档课时{}阅读时长{}/{}秒，未达要求", userId, dto.getLessonId(), actualDuration, requiredDuration);
                    return;
                }
            }
            if (!wasCompleted) justCompleted = true;
            progress.setCompleted(1);
        }

        saveOrUpdate(progress);

        // ===== 计算积分：只对增量时长计分 =====
        int newDuration = progress.getLearnDuration() != null ? progress.getLearnDuration() : 0;
        int deltaSeconds = newDuration - oldDuration;
        if (deltaSeconds > 0) {
            try {
                BigDecimal earned = pointService.earnLearnPoints(userId, dto.getCourseId(), dto.getLessonId(), deltaSeconds);
                if (earned.compareTo(BigDecimal.ZERO) > 0) {
                    log.info("用户{}学习课时{}获得{}积分", userId, dto.getLessonId(), earned);
                }
            } catch (Exception e) {
                log.error("积分计算失败", e);
            }
        }

        // ===== 完成课时额外积分 =====
        if (justCompleted) {
            try {
                pointService.earnCompletePoints(userId, dto.getCourseId());
            } catch (Exception e) {
                log.error("完成积分计算失败", e);
            }
        }
    }

    /**
     * 查询用户在某课程下的总进度
     */
    public int getCourseProgress(Long userId, Long courseId) {
        long totalLessons = count(
                new LambdaQueryWrapper<LearningProgress>()
                        .eq(LearningProgress::getUserId, userId)
                        .eq(LearningProgress::getCourseId, courseId)
        );
        if (totalLessons == 0) return 0;

        long completedLessons = count(
                new LambdaQueryWrapper<LearningProgress>()
                        .eq(LearningProgress::getUserId, userId)
                        .eq(LearningProgress::getCourseId, courseId)
                        .eq(LearningProgress::getCompleted, 1)
        );
        return (int) (completedLessons * 100 / totalLessons);
    }

    /**
     * 获取文档类课时的要求阅读时长
     * 默认：10页文档需3分钟(180秒)，按比例计算
     * 如果该课程关联了任务，以任务指定的docReadDuration为准
     */
    private int getDocRequiredDuration(Long userId, Long courseId, CourseLesson lesson) {
        // 先检查是否有任务关联该课程
        try {
            com.estudy.task.entity.Task task = taskMapper.selectOne(
                    new LambdaQueryWrapper<com.estudy.task.entity.Task>()
                            .eq(com.estudy.task.entity.Task::getCourseId, courseId)
                            .eq(com.estudy.task.entity.Task::getStatus, 1)
                            .last("LIMIT 1")
            );
            if (task != null && task.getDocReadDuration() != null) {
                return task.getDocReadDuration();
            }
        } catch (Exception e) {
            log.warn("查询任务文档时长失败", e);
        }

        // 默认：根据duration字段（秒）计算，文档类duration存的是建议阅读时长
        return lesson.getDuration() != null ? lesson.getDuration() : 180;
    }

    /**
     * 检查用户是否完成了课程的所有课时（包括文档阅读时长达标）
     */
    public boolean isCourseCompleted(Long userId, Long courseId) {
        return getCourseProgress(userId, courseId) >= 100;
    }
}

package com.estudy.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.common.exception.BusinessException;
import com.estudy.course.entity.Course;
import com.estudy.course.mapper.CourseMapper;
import com.estudy.exam.entity.ExamPaper;
import com.estudy.exam.mapper.ExamPaperMapper;
import com.estudy.task.dto.TaskCreateDTO;
import com.estudy.task.entity.Task;
import com.estudy.task.entity.UserTask;
import com.estudy.task.mapper.TaskMapper;
import com.estudy.task.mapper.UserTaskMapper;
import com.estudy.user.entity.SysUser;
import com.estudy.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService extends ServiceImpl<TaskMapper, Task> {

    private final UserTaskMapper userTaskMapper;
    private final CourseMapper courseMapper;
    private final ExamPaperMapper paperMapper;
    private final SysUserMapper userMapper;

    /**
     * 创建培训任务并推送给部门人员
     */
    @Transactional
    public Long createTask(TaskCreateDTO dto, Long createBy) {
        // 校验
        if (dto.getTaskType() == 1 || dto.getTaskType() == 3) {
            if (dto.getCourseId() == null) throw new BusinessException("学习类任务必须关联课程");
        }
        if (dto.getTaskType() == 1 || dto.getTaskType() == 2) {
            if (dto.getPaperId() == null) throw new BusinessException("考试类任务必须关联试卷");
        }

        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setTaskType(dto.getTaskType());
        task.setDeptId(dto.getDeptId());
        task.setCourseId(dto.getCourseId());
        task.setPaperId(dto.getPaperId());
        task.setDocReadDuration(dto.getDocReadDuration() != null ? dto.getDocReadDuration() : 300);
        task.setDeadline(dto.getDeadline());
        task.setStatus(1);
        task.setCreateBy(createBy);
        save(task);

        // 推送给部门人员：查询该部门所有用户，创建user_task
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        if (dto.getDeptId() != null) {
            userWrapper.eq(SysUser::getDeptId, dto.getDeptId());
        }
        userWrapper.eq(SysUser::getStatus, 1);
        List<SysUser> users = userMapper.selectList(userWrapper);
        for (SysUser u : users) {
            UserTask ut = new UserTask();
            ut.setUserId(u.getId());
            ut.setTaskId(task.getId());
            ut.setLearnStatus(0);
            ut.setExamStatus(0);
            ut.setTaskStatus(0);
            userTaskMapper.insert(ut);
        }

        return task.getId();
    }

    /**
     * 查询我的任务列表
     */
    public List<Task> getMyTasks(Long userId) {
        // 查询用户的user_task
        LambdaQueryWrapper<UserTask> utWrapper = new LambdaQueryWrapper<>();
        utWrapper.eq(UserTask::getUserId, userId)
                 .orderByDesc(UserTask::getCreateTime);
        List<UserTask> userTasks = userTaskMapper.selectList(utWrapper);

        if (userTasks.isEmpty()) return List.of();

        List<Long> taskIds = userTasks.stream().map(UserTask::getTaskId).toList();
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Task::getId, taskIds)
               .orderByDesc(Task::getCreateTime);
        List<Task> tasks = list(wrapper);

        // 填充信息
        fillTaskInfo(tasks, userTasks);
        return tasks;
    }

    /**
     * 查询任务详情
     */
    public Task getTaskDetail(Long taskId, Long userId) {
        Task task = getById(taskId);
        if (task == null) throw new BusinessException("任务不存在");

        UserTask ut = userTaskMapper.selectOne(
                new LambdaQueryWrapper<UserTask>()
                        .eq(UserTask::getUserId, userId)
                        .eq(UserTask::getTaskId, taskId));
        if (ut == null) throw new BusinessException("无权查看此任务");

        fillTaskInfo(List.of(task), List.of(ut));
        return task;
    }

    /**
     * 完成学习步骤
     */
    @Transactional
    public void completeLearn(Long userId, Long taskId) {
        Task task = getById(taskId);
        if (task == null) throw new BusinessException("任务不存在");
        if (task.getTaskType() == 2) throw new BusinessException("纯考试任务无需学习");

        UserTask ut = getUserTask(userId, taskId);
        ut.setLearnStatus(2);
        ut.setLearnCompleteTime(LocalDateTime.now());
        updateTaskStatus(ut, task);
        userTaskMapper.updateById(ut);
    }

    /**
     * 完成考试步骤（由ExamService调用）
     */
    @Transactional
    public void completeExam(Long userId, Long taskId, boolean passed) {
        Task task = getById(taskId);
        if (task == null) return;

        UserTask ut = getUserTask(userId, taskId);
        ut.setExamStatus(passed ? 2 : 3);
        if (passed) {
            ut.setExamCompleteTime(LocalDateTime.now());
        }
        updateTaskStatus(ut, task);
        userTaskMapper.updateById(ut);
    }

    /**
     * 检查是否允许开始考试（学习+考试类需先完成学习）
     */
    public boolean canStartExam(Long userId, Long taskId) {
        Task task = getById(taskId);
        if (task == null) return false;
        if (task.getTaskType() == 2) return true; // 纯考试直接可考

        UserTask ut = getUserTask(userId, taskId);
        return ut.getLearnStatus() == 2; // 学习完成后才能考
    }

    /**
     * 获取任务关联的试卷ID（前端开始考试用）
     */
    public Long getTaskPaperId(Long taskId) {
        Task task = getById(taskId);
        return task != null ? task.getPaperId() : null;
    }

    private UserTask getUserTask(Long userId, Long taskId) {
        UserTask ut = userTaskMapper.selectOne(
                new LambdaQueryWrapper<UserTask>()
                        .eq(UserTask::getUserId, userId)
                        .eq(UserTask::getTaskId, taskId));
        if (ut == null) throw new BusinessException("未找到任务记录");
        return ut;
    }

    private void updateTaskStatus(UserTask ut, Task task) {
        if (task.getTaskType() == 1) {
            // 学习+考试：两者都完成才算完成
            if (ut.getLearnStatus() == 2 && ut.getExamStatus() == 2) {
                ut.setTaskStatus(2);
            } else {
                ut.setTaskStatus(1);
            }
        } else if (task.getTaskType() == 2) {
            // 纯考试：通过即完成
            ut.setTaskStatus(ut.getExamStatus() == 2 ? 2 : 1);
        } else {
            // 纯学习：学完即完成
            ut.setTaskStatus(ut.getLearnStatus() == 2 ? 2 : 1);
        }
    }

    private void fillTaskInfo(List<Task> tasks, List<UserTask> userTasks) {
        if (tasks.isEmpty()) return;

        Map<Long, UserTask> utMap = userTasks.stream()
                .collect(Collectors.toMap(UserTask::getTaskId, u -> u));

        // 批量查课程标题
        List<Long> courseIds = tasks.stream().map(Task::getCourseId).filter(id -> id != null).distinct().toList();
        Map<Long, String> courseMap = courseIds.isEmpty() ? Map.of() :
                courseMapper.selectBatchIds(courseIds).stream()
                        .collect(Collectors.toMap(Course::getId, Course::getTitle));

        // 批量查试卷标题
        List<Long> paperIds = tasks.stream().map(Task::getPaperId).filter(id -> id != null).distinct().toList();
        Map<Long, String> paperMap = paperIds.isEmpty() ? Map.of() :
                paperMapper.selectBatchIds(paperIds).stream()
                        .collect(Collectors.toMap(ExamPaper::getId, ExamPaper::getTitle));

        for (Task t : tasks) {
            if (t.getCourseId() != null) t.setCourseTitle(courseMap.get(t.getCourseId()));
            if (t.getPaperId() != null) t.setPaperTitle(paperMap.get(t.getPaperId()));
            UserTask ut = utMap.get(t.getId());
            if (ut != null) {
                t.setLearnStatus(ut.getLearnStatus());
                t.setExamStatus(ut.getExamStatus());
                t.setTaskStatus(ut.getTaskStatus());
            }
        }
    }
}

package com.estudy.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.common.exception.BusinessException;
import com.estudy.exam.dto.PaperCreateDTO;
import com.estudy.exam.dto.SubmitExamDTO;
import com.estudy.exam.entity.*;
import com.estudy.exam.mapper.ExamPaperMapper;
import com.estudy.exam.mapper.ExamPaperQuestionMapper;
import com.estudy.exam.mapper.ExamAnswerMapper;
import com.estudy.exam.mapper.ExamRecordMapper;
import com.estudy.exam.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService extends ServiceImpl<ExamPaperMapper, ExamPaper> {

    private final ExamPaperQuestionMapper paperQuestionMapper;
    private final ExamRecordMapper recordMapper;
    private final ExamAnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final com.estudy.task.service.TaskService taskService;

    /**
     * 创建试卷(含题目)
     */
    @Transactional
    public Long createPaper(PaperCreateDTO dto) {
        ExamPaper paper = new ExamPaper();
        paper.setTitle(dto.getTitle());
        paper.setCourseId(dto.getCourseId());
        paper.setDescription(dto.getDescription());
        paper.setTotalScore(dto.getTotalScore());
        paper.setPassScore(dto.getPassScore());
        paper.setDuration(dto.getDuration() != null ? dto.getDuration() : 60);
        paper.setExamType(dto.getExamType() != null ? dto.getExamType() : 1);
        paper.setMaxAttempts(dto.getMaxAttempts() != null ? dto.getMaxAttempts() : 1);
        paper.setStatus(0); // 草稿
        save(paper);

        // 保存试卷-题目关联
        if (dto.getQuestions() != null) {
            for (int i = 0; i < dto.getQuestions().size(); i++) {
                PaperCreateDTO.PaperQuestionItem item = dto.getQuestions().get(i);
                ExamPaperQuestion pq = new ExamPaperQuestion();
                pq.setPaperId(paper.getId());
                pq.setQuestionId(item.getQuestionId());
                pq.setSort(i);
                pq.setScore(item.getScore() != null ? item.getScore() : 2);
                paperQuestionMapper.insert(pq);
            }
        }
        return paper.getId();
    }

    /**
     * 发布试卷
     */
    public void publishPaper(Long paperId) {
        ExamPaper paper = getById(paperId);
        if (paper == null) throw new BusinessException("试卷不存在");
        paper.setStatus(1);
        updateById(paper);
    }

    /**
     * 开始考试：创建考试记录，返回题目列表(不含答案)
     */
    @Transactional
    public ExamRecord startExam(Long userId, Long paperId) {
        ExamPaper paper = getById(paperId);
        if (paper == null) throw new BusinessException("试卷不存在");
        if (paper.getStatus() != 1) throw new BusinessException("试卷未发布");

        // 检查考试次数（maxAttempts为-1或null表示不限）
        long attemptCount = recordMapper.selectCount(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .eq(ExamRecord::getPaperId, paperId)
        );
        int maxAttempts = paper.getMaxAttempts() != null ? paper.getMaxAttempts() : 1;
        if (maxAttempts > 0 && attemptCount >= maxAttempts) {
            throw new BusinessException("已达到最大考试次数");
        }

        // 创建考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setPaperId(paperId);
        record.setTotalScore(paper.getTotalScore());
        record.setStatus(0); // 进行中
        record.setStartTime(LocalDateTime.now());
        recordMapper.insert(record);

        return record;
    }

    /**
     * 获取试卷题目(考试时，不含正确答案)
     */
    public List<Question> getPaperQuestions(Long paperId) {
        List<ExamPaperQuestion> pqList = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamPaperQuestion>()
                        .eq(ExamPaperQuestion::getPaperId, paperId)
                        .orderByAsc(ExamPaperQuestion::getSort)
        );
        List<Long> qIds = pqList.stream().map(ExamPaperQuestion::getQuestionId).toList();
        if (qIds.isEmpty()) return List.of();

        List<Question> questions = questionMapper.selectBatchIds(qIds);
        // 隐藏答案和解析
        for (Question q : questions) {
            q.setAnswer(null);
            q.setAnalysis(null);
        }
        // 按试卷中的排序返回
        Map<Long, Integer> sortMap = pqList.stream()
                .collect(Collectors.toMap(ExamPaperQuestion::getQuestionId, ExamPaperQuestion::getSort));
        questions.sort((a, b) -> sortMap.getOrDefault(a.getId(), 0) - sortMap.getOrDefault(b.getId(), 0));
        return questions;
    }

    /**
     * 提交考试：自动判分(客观题)
     */
    @Transactional
    public ExamRecord submitExam(Long userId, SubmitExamDTO dto) {
        ExamRecord record = recordMapper.selectById(dto.getRecordId());
        if (record == null) throw new BusinessException("考试记录不存在");
        if (!record.getUserId().equals(userId)) throw new BusinessException("无权操作");
        if (record.getStatus() != 0) throw new BusinessException("已交卷，不可重复提交");

        // 获取试卷题目及正确答案
        List<ExamPaperQuestion> pqList = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamPaperQuestion>()
                        .eq(ExamPaperQuestion::getPaperId, record.getPaperId())
        );
        Map<Long, Integer> scoreMap = pqList.stream()
                .collect(Collectors.toMap(ExamPaperQuestion::getQuestionId, ExamPaperQuestion::getScore));

        List<Question> questions = questionMapper.selectBatchIds(
                pqList.stream().map(ExamPaperQuestion::getQuestionId).toList()
        );
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 逐题判分
        int totalScore = 0;
        for (SubmitExamDTO.AnswerItem item : dto.getAnswers()) {
            Question q = questionMap.get(item.getQuestionId());
            if (q == null) continue;

            ExamAnswer answer = new ExamAnswer();
            answer.setRecordId(record.getId());
            answer.setQuestionId(item.getQuestionId());
            answer.setUserAnswer(item.getUserAnswer());

            // 客观题自动判分：忽略大小写和空格
            boolean correct = q.getAnswer().trim().equalsIgnoreCase(
                    item.getUserAnswer() != null ? item.getUserAnswer().trim() : ""
            );
            answer.setIsCorrect(correct ? 1 : 0);
            int qScore = correct ? scoreMap.getOrDefault(item.getQuestionId(), 2) : 0;
            answer.setScore(qScore);
            totalScore += qScore;

            answerMapper.insert(answer);
        }

        // 更新考试记录
        ExamPaper paper = getById(record.getPaperId());
        record.setScore(totalScore);
        record.setPassed(totalScore >= paper.getPassScore() ? 1 : 0);
        record.setStatus(1); // 已交卷
        record.setSubmitTime(LocalDateTime.now());
        record.setDuration((int) java.time.Duration.between(
                record.getStartTime(), record.getSubmitTime()).getSeconds());
        recordMapper.updateById(record);

        // ===== 联动任务系统：如果有任务关联该试卷，更新考试状态 =====
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.estudy.task.entity.Task> taskWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.estudy.task.entity.Task>()
                            .eq(com.estudy.task.entity.Task::getPaperId, record.getPaperId())
                            .eq(com.estudy.task.entity.Task::getStatus, 1);
            com.estudy.task.entity.Task task = taskService.getOne(taskWrapper);
            if (task != null) {
                taskService.completeExam(userId, task.getId(), record.getPassed() == 1);
            }
        } catch (Exception e) {
            log.warn("联动任务系统失败", e);
        }

        return record;
    }

    /**
     * 查询考试结果(含答题详情)
     */
    public ExamRecord getExamResult(Long recordId) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new BusinessException("记录不存在");
        return record;
    }
}

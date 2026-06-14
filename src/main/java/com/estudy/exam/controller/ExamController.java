package com.estudy.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.estudy.common.result.Result;
import com.estudy.exam.dto.*;
import com.estudy.exam.entity.ExamAnswer;
import com.estudy.exam.entity.ExamPaper;
import com.estudy.exam.entity.ExamPaperQuestion;
import com.estudy.exam.entity.ExamRecord;
import com.estudy.exam.entity.Question;
import com.estudy.exam.mapper.ExamAnswerMapper;
import com.estudy.exam.mapper.ExamPaperQuestionMapper;
import com.estudy.exam.mapper.ExamRecordMapper;
import com.estudy.exam.service.ExamService;
import com.estudy.exam.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final QuestionService questionService;
    private final ExamRecordMapper recordMapper;
    private final ExamAnswerMapper answerMapper;
    private final ExamPaperQuestionMapper paperQuestionMapper;

    private Long getCurrentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    // ========== 题库 ==========

    /** 创建题目 */
    @PostMapping("/question")
    public Result<Long> createQuestion(@RequestBody @Valid QuestionCreateDTO dto) {
        return Result.success(questionService.createQuestion(dto));
    }

    /** 分页查询题目（兼容 /list） */
    @GetMapping({"/question/list", "/question/page"})
    public Result<Page<Question>> pageQuestion(QuestionQueryDTO dto) {
        return Result.success(questionService.pageQuery(dto));
    }

    /** 题目详情 */
    @GetMapping("/question/{id}")
    public Result<Question> questionDetail(@PathVariable Long id) {
        return Result.success(questionService.getById(id));
    }

    // ========== 试卷 ==========

    /** 创建试卷 */
    @PostMapping("/paper")
    public Result<Long> createPaper(@RequestBody @Valid PaperCreateDTO dto) {
        return Result.success(examService.createPaper(dto));
    }

    /** 发布试卷 */
    @PutMapping("/paper/{id}/publish")
    public Result<Void> publishPaper(@PathVariable Long id) {
        examService.publishPaper(id);
        return Result.success();
    }

    /** 试卷列表（兼容 /list） */
    @GetMapping({"/paper/list", "/paper/page"})
    public Result<Page<ExamPaper>> paperList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ExamPaper> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExamPaper::getCreateTime);
        Page<ExamPaper> result = examService.page(page, wrapper);
        fillPaperExtra(result.getRecords(), null);
        return Result.success(result);
    }

    /** 可考试卷列表 */
    @GetMapping("/paper/available")
    public Result<List<ExamPaper>> availablePapers(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamPaper::getStatus, 1)
               .orderByDesc(ExamPaper::getCreateTime);
        List<ExamPaper> papers = examService.list(wrapper);
        fillPaperExtra(papers, userId);
        return Result.success(papers);
    }

    /** 试卷详情 */
    @GetMapping("/paper/{id}")
    public Result<?> paperDetail(@PathVariable Long id) {
        return Result.success(examService.getById(id));
    }

    // ========== 考试流程 ==========

    /** 开始考试 */
    @PostMapping("/paper/{paperId}/start")
    public Result<ExamRecord> startExam(@PathVariable Long paperId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(examService.startExam(userId, paperId));
    }

    /** 获取试卷题目(考试时) */
    @GetMapping("/paper/{paperId}/questions")
    public Result<List<Question>> paperQuestions(@PathVariable Long paperId) {
        return Result.success(examService.getPaperQuestions(paperId));
    }

    /** 保存单题答案(中途保存) */
    @PostMapping("/answer")
    public Result<Void> saveAnswer(@RequestBody AnswerDTO dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        ExamRecord record = recordMapper.selectById(dto.getRecordId());
        if (record == null || !record.getUserId().equals(userId)) {
            return Result.error("无权操作");
        }
        if (record.getStatus() != 0) {
            return Result.error("已交卷，不可修改");
        }
        ExamAnswer existing = answerMapper.selectOne(
                new LambdaQueryWrapper<ExamAnswer>()
                        .eq(ExamAnswer::getRecordId, dto.getRecordId())
                        .eq(ExamAnswer::getQuestionId, dto.getQuestionId())
        );
        if (existing != null) {
            existing.setUserAnswer(dto.getUserAnswer());
            answerMapper.updateById(existing);
        } else {
            ExamAnswer answer = new ExamAnswer();
            answer.setRecordId(dto.getRecordId());
            answer.setQuestionId(dto.getQuestionId());
            answer.setUserAnswer(dto.getUserAnswer());
            answerMapper.insert(answer);
        }
        return Result.success();
    }

    /** 提交考试(整体交卷) */
    @PostMapping("/submit")
    public Result<ExamRecord> submitExam(@RequestBody @Valid SubmitExamDTO dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(examService.submitExam(userId, dto));
    }

    /** 按记录ID交卷(兼容 /record/{recordId}/submit) */
    @PostMapping("/record/{recordId}/submit")
    public Result<ExamRecord> submitByRecord(@PathVariable Long recordId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        SubmitExamDTO dto = new SubmitExamDTO();
        dto.setRecordId(recordId);
        List<ExamAnswer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>()
                        .eq(ExamAnswer::getRecordId, recordId)
        );
        dto.setAnswers(answers.stream().map(a -> {
            SubmitExamDTO.AnswerItem item = new SubmitExamDTO.AnswerItem();
            item.setQuestionId(a.getQuestionId());
            item.setUserAnswer(a.getUserAnswer());
            return item;
        }).toList());
        return Result.success(examService.submitExam(userId, dto));
    }

    // ========== 考试记录 ==========

    /** 我的考试记录 - 必须在 /record/{id} 之前声明 */
    @GetMapping("/record/my")
    public Result<List<ExamRecord>> myRecords(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getUserId, userId)
               .orderByDesc(ExamRecord::getCreateTime);
        List<ExamRecord> records = recordMapper.selectList(wrapper);
        // 填充试卷标题和格式化用时
        for (ExamRecord r : records) {
            ExamPaper paper = examService.getById(r.getPaperId());
            if (paper != null) {
                r.setPaperTitle(paper.getTitle());
            }
            if (r.getDuration() != null) {
                int mins = r.getDuration() / 60;
                int secs = r.getDuration() % 60;
                r.setTimeUsed(mins + "分" + secs + "秒");
            }
        }
        return Result.success(records);
    }

    /** 查看考试结果 */
    @GetMapping("/record/{recordId}")
    public Result<ExamRecord> examResult(@PathVariable Long recordId) {
        return Result.success(examService.getExamResult(recordId));
    }

    // ========== 私有方法 ==========

    /** 填充试卷的题目数和剩余次数 */
    private void fillPaperExtra(List<ExamPaper> papers, Long userId) {
        if (papers == null || papers.isEmpty()) return;
        for (ExamPaper p : papers) {
            // 题目数
            Long qCount = paperQuestionMapper.selectCount(
                    new LambdaQueryWrapper<ExamPaperQuestion>()
                            .eq(ExamPaperQuestion::getPaperId, p.getId())
            );
            p.setQuestionCount(qCount.intValue());
            // 剩余次数
            if (userId != null) {
                int max = p.getMaxAttempts() != null ? p.getMaxAttempts() : 1;
                if (max <= 0) {
                    // -1 表示不限
                    p.setRemainAttempts(-1);
                } else {
                    Long attemptCount = recordMapper.selectCount(
                            new LambdaQueryWrapper<ExamRecord>()
                                    .eq(ExamRecord::getUserId, userId)
                                    .eq(ExamRecord::getPaperId, p.getId())
                    );
                    p.setRemainAttempts(Math.max(0, max - attemptCount.intValue()));
                }
            } else {
                p.setRemainAttempts(p.getMaxAttempts());
            }
        }
    }
}

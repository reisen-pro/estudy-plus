package com.estudy.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.estudy.exam.dto.QuestionCreateDTO;
import com.estudy.exam.dto.QuestionQueryDTO;
import com.estudy.exam.entity.Question;
import com.estudy.exam.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class QuestionService extends ServiceImpl<QuestionMapper, Question> {

    public Long createQuestion(QuestionCreateDTO dto) {
        Question q = new Question();
        q.setCourseId(dto.getCourseId());
        q.setCategoryId(dto.getCategoryId());
        q.setContent(dto.getContent());
        q.setQuestionType(dto.getQuestionType());
        q.setOptions(dto.getOptions());
        q.setAnswer(dto.getAnswer());
        q.setAnalysis(dto.getAnalysis());
        q.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 1);
        q.setScore(dto.getScore() != null ? dto.getScore() : 2);
        save(q);
        return q.getId();
    }

    public Page<Question> pageQuery(QuestionQueryDTO dto) {
        Page<Question> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (dto.getCategoryId() != null) {
            wrapper.eq(Question::getCategoryId, dto.getCategoryId());
        }
        if (dto.getQuestionType() != null) {
            wrapper.eq(Question::getQuestionType, dto.getQuestionType());
        }
        if (dto.getDifficulty() != null) {
            wrapper.eq(Question::getDifficulty, dto.getDifficulty());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(Question::getContent, dto.getKeyword());
        }
        wrapper.orderByDesc(Question::getCreateTime);
        return page(page, wrapper);
    }
}

package com.estudy.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.estudy.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}

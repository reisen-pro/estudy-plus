package com.estudy.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.estudy.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}

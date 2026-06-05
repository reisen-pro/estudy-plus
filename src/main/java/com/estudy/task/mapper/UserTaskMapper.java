package com.estudy.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.estudy.task.entity.UserTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserTaskMapper extends BaseMapper<UserTask> {
}

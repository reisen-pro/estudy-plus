package com.estudy.dingtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dingtalk_message_log")
public class DingtalkMessageLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String dingUserId;

    /** 消息类型 training_notice/exam_notice/course_assign */
    private String msgType;

    private String title;

    private String content;

    /** 业务ID(课程ID/考试ID等) */
    private String bizId;

    /** 发送状态 0待发送 1已发送 2发送失败 */
    private Integer status;

    private String dingTaskId;

    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

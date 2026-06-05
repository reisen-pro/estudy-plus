USE estudy_plus;

-- ----------------------------
-- 钉钉配置表
-- ----------------------------
DROP TABLE IF EXISTS dingtalk_config;
CREATE TABLE dingtalk_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键(app_key/app_secret/callback_url等)',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB COMMENT='钉钉配置表';

-- ----------------------------
-- 钉钉用户绑定表
-- ----------------------------
DROP TABLE IF EXISTS dingtalk_user_bind;
CREATE TABLE dingtalk_user_bind (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '系统用户ID',
    ding_user_id VARCHAR(100) NOT NULL COMMENT '钉钉用户ID',
    ding_name VARCHAR(100) COMMENT '钉钉昵称',
    ding_avatar VARCHAR(500) COMMENT '钉钉头像',
    bind_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_ding_user_id (ding_user_id)
) ENGINE=InnoDB COMMENT='钉钉用户绑定表';

-- ----------------------------
-- 钉钉消息推送记录
-- ----------------------------
DROP TABLE IF EXISTS dingtalk_message_log;
CREATE TABLE dingtalk_message_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT COMMENT '系统用户ID',
    ding_user_id VARCHAR(100) COMMENT '钉钉用户ID',
    msg_type VARCHAR(50) NOT NULL COMMENT '消息类型(training_notice/exam_notice/course_assign等)',
    title VARCHAR(200) COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    biz_id VARCHAR(100) COMMENT '业务ID(课程ID/考试ID等)',
    status TINYINT DEFAULT 0 COMMENT '发送状态 0待发送 1已发送 2发送失败',
    ding_task_id VARCHAR(200) COMMENT '钉钉异步任务ID',
    error_msg VARCHAR(500) COMMENT '失败原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='钉钉消息推送记录';

-- 初始配置(需替换真实值)
INSERT INTO dingtalk_config (config_key, config_value, remark) VALUES
('app_key', 'ding_xxxxxxx', '钉钉应用AppKey'),
('app_secret', 'xxxxxx', '钉钉应用AppSecret'),
('agent_id', '123456789', '钉钉应用AgentId'),
('callback_url', 'http://localhost:8080/api/dingtalk/callback', '钉钉回调地址');

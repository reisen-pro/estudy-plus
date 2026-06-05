SET NAMES utf8mb4;
USE estudy_plus;

-- ----------------------------
-- 培训任务表（部门推送的学习/考试任务）
-- ----------------------------
DROP TABLE IF EXISTS task;
CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务说明',
    task_type TINYINT NOT NULL COMMENT '任务类型 1=学习+考试 2=纯考试 3=纯学习',
    dept_id BIGINT COMMENT '推送部门ID，NULL=全员',
    course_id BIGINT COMMENT '关联课程ID（学习+考试/纯学习时必填）',
    paper_id BIGINT COMMENT '关联试卷ID（学习+考试/纯考试时必填）',
    doc_read_duration INT DEFAULT 300 COMMENT '文档课时要求阅读时长(秒)，默认5分钟',
    deadline DATETIME COMMENT '截止时间',
    status TINYINT DEFAULT 1 COMMENT '状态 1进行中 2已结束',
    create_by BIGINT COMMENT '创建人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='培训任务表';

-- ----------------------------
-- 用户任务表（用户与任务的关联和状态）
-- ----------------------------
DROP TABLE IF EXISTS user_task;
CREATE TABLE user_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    learn_status TINYINT DEFAULT 0 COMMENT '学习状态 0=未开始 1=进行中 2=已完成',
    exam_status TINYINT DEFAULT 0 COMMENT '考试状态 0=未开始 1=进行中 2=已通过 3=未通过',
    task_status TINYINT DEFAULT 0 COMMENT '整体状态 0=未开始 1=进行中 2=已完成 3=已过期',
    learn_complete_time DATETIME COMMENT '学习完成时间',
    exam_complete_time DATETIME COMMENT '考试完成时间',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_task (user_id, task_id)
) ENGINE=InnoDB COMMENT='用户任务表';

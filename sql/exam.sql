USE estudy_plus;

-- ----------------------------
-- 题库表
-- ----------------------------
DROP TABLE IF EXISTS question;
CREATE TABLE question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    course_id BIGINT COMMENT '关联课程ID(可选)',
    category_id BIGINT COMMENT '分类ID',
    content TEXT NOT NULL COMMENT '题目内容',
    question_type TINYINT NOT NULL COMMENT '题型 1单选 2多选 3判断 4填空',
    options JSON COMMENT '选项JSON [{\"label\":\"A\",\"content\":\"选项A\"},...]',
    answer VARCHAR(500) NOT NULL COMMENT '正确答案(单选:A 多选:A,B 判断:对/错 填空:答案)',
    analysis TEXT COMMENT '解析',
    difficulty TINYINT DEFAULT 1 COMMENT '难度 1简单 2中等 3困难',
    score INT DEFAULT 2 COMMENT '分值',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='题库表';

-- ----------------------------
-- 试卷表
-- ----------------------------
DROP TABLE IF EXISTS exam_paper;
CREATE TABLE exam_paper (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '试卷ID',
    title VARCHAR(200) NOT NULL COMMENT '试卷标题',
    course_id BIGINT COMMENT '关联课程ID',
    description VARCHAR(500) COMMENT '试卷说明',
    total_score INT NOT NULL COMMENT '总分',
    pass_score INT NOT NULL COMMENT '及格分',
    duration INT DEFAULT 60 COMMENT '考试时长(分钟)',
    exam_type TINYINT DEFAULT 1 COMMENT '考试类型 1练习 2正式考试',
    status TINYINT DEFAULT 0 COMMENT '状态 0草稿 1已发布 2已关闭',
    max_attempts INT DEFAULT 1 COMMENT '最大允许次数',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='试卷表';

-- ----------------------------
-- 试卷题目关联表
-- ----------------------------
DROP TABLE IF EXISTS exam_paper_question;
CREATE TABLE exam_paper_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    paper_id BIGINT NOT NULL COMMENT '试卷ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    sort INT DEFAULT 0 COMMENT '排序',
    score INT DEFAULT 2 COMMENT '该题分值(可覆盖题目默认分值)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='试卷题目关联表';

-- ----------------------------
-- 考试记录表
-- ----------------------------
DROP TABLE IF EXISTS exam_record;
CREATE TABLE exam_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    paper_id BIGINT NOT NULL COMMENT '试卷ID',
    score INT DEFAULT 0 COMMENT '得分',
    total_score INT COMMENT '总分',
    passed TINYINT DEFAULT 0 COMMENT '是否及格 0否 1是',
    status TINYINT DEFAULT 0 COMMENT '状态 0进行中 1已交卷 2已批改',
    start_time DATETIME COMMENT '开始时间',
    submit_time DATETIME COMMENT '交卷时间',
    duration INT DEFAULT 0 COMMENT '用时(秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='考试记录表';

-- ----------------------------
-- 答题记录表
-- ----------------------------
DROP TABLE IF EXISTS exam_answer;
CREATE TABLE exam_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    record_id BIGINT NOT NULL COMMENT '考试记录ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    user_answer VARCHAR(500) COMMENT '用户答案',
    is_correct TINYINT DEFAULT 0 COMMENT '是否正确 0否 1是',
    score INT DEFAULT 0 COMMENT '得分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='答题记录表';

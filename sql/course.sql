USE estudy_plus;

-- ----------------------------
-- 课程分类表
-- ----------------------------
DROP TABLE IF EXISTS course_category;
CREATE TABLE course_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    sort INT DEFAULT 0 COMMENT '排序',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='课程分类表';

-- ----------------------------
-- 课程表
-- ----------------------------
DROP TABLE IF EXISTS course;
CREATE TABLE course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程ID',
    title VARCHAR(200) NOT NULL COMMENT '课程标题',
    subtitle VARCHAR(500) COMMENT '课程副标题',
    cover_url VARCHAR(500) COMMENT '封面图URL',
    category_id BIGINT COMMENT '分类ID',
    teacher_id BIGINT COMMENT '讲师用户ID',
    description TEXT COMMENT '课程介绍',
    course_type TINYINT DEFAULT 1 COMMENT '课程类型 1点播 2直播 3混合',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '价格',
    status TINYINT DEFAULT 0 COMMENT '状态 0草稿 1已发布 2已下架',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='课程表';

-- ----------------------------
-- 课程章节表
-- ----------------------------
DROP TABLE IF EXISTS course_chapter;
CREATE TABLE course_chapter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '章节ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父章节ID(0为大章节)',
    title VARCHAR(200) NOT NULL COMMENT '章节标题',
    sort INT DEFAULT 0 COMMENT '排序',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='课程章节表';

-- ----------------------------
-- 课时表(视频/文档等)
-- ----------------------------
DROP TABLE IF EXISTS course_lesson;
CREATE TABLE course_lesson (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课时ID',
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    title VARCHAR(200) NOT NULL COMMENT '课时标题',
    lesson_type TINYINT DEFAULT 1 COMMENT '课时类型 1视频 2文档 3链接',
    media_url VARCHAR(500) COMMENT '媒体资源URL',
    duration INT DEFAULT 0 COMMENT '视频时长(秒)',
    sort INT DEFAULT 0 COMMENT '排序',
    is_free TINYINT DEFAULT 0 COMMENT '是否免费试看 0否 1是',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='课时表';

-- ----------------------------
-- 学习进度表
-- ----------------------------
DROP TABLE IF EXISTS learning_progress;
CREATE TABLE learning_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    lesson_id BIGINT NOT NULL COMMENT '课时ID',
    progress INT DEFAULT 0 COMMENT '学习进度百分比(0-100)',
    learn_duration INT DEFAULT 0 COMMENT '已学时长(秒)',
    last_position INT DEFAULT 0 COMMENT '上次播放位置(秒)',
    completed TINYINT DEFAULT 0 COMMENT '是否完成 0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_lesson (user_id, lesson_id)
) ENGINE=InnoDB COMMENT='学习进度表';

-- 初始分类
INSERT INTO course_category (id, parent_id, category_name, sort) VALUES
(1, 0, '新员工入职培训', 1),
(2, 0, '专业技能培训', 2),
(3, 0, '管理能力培训', 3),
(4, 0, '合规与安全', 4),
(5, 2, '药物研发', 1),
(6, 2, '实验操作规范', 2);

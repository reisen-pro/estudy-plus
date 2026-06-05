USE estudy_plus;

-- ----------------------------
-- 帖子表
-- ----------------------------
DROP TABLE IF EXISTS post;
CREATE TABLE post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '作者用户ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    category VARCHAR(50) DEFAULT '讨论' COMMENT '分类(讨论/分享/提问/公告)',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶 0否 1是',
    status TINYINT DEFAULT 1 COMMENT '状态 0隐藏 1正常',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='帖子表';

-- ----------------------------
-- 评论表
-- ----------------------------
DROP TABLE IF EXISTS comment;
CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '评论者用户ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID(0为顶级评论)',
    reply_to_user_id BIGINT COMMENT '回复目标用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态 0隐藏 1正常',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='评论表';

-- ----------------------------
-- 点赞表(多态，可给帖子/评论点赞)
-- ----------------------------
DROP TABLE IF EXISTS `like`;
CREATE TABLE `like` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type TINYINT NOT NULL COMMENT '目标类型 1帖子 2评论',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_target (user_id, target_type, target_id)
) ENGINE=InnoDB COMMENT='点赞表';

-- ----------------------------
-- 通知表
-- ----------------------------
DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '接收通知的用户ID',
    from_user_id BIGINT COMMENT '触发通知的用户ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型(like/comment/reply/system)',
    title VARCHAR(200) COMMENT '通知标题',
    content VARCHAR(500) COMMENT '通知内容',
    biz_id VARCHAR(100) COMMENT '业务ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读 0未读 1已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='通知表';

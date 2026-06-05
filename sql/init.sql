-- e学网+ 数据库初始化脚本
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS estudy_plus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE estudy_plus;

-- ----------------------------
-- 部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='部门表';

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像URL',
    dept_id BIGINT COMMENT '部门ID',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='用户表';

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '描述',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB COMMENT='角色表';

-- ----------------------------
-- 用户-角色关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- ----------------------------
-- 插入初始数据
-- ----------------------------
-- 密码是 BCrypt 加密的 "admin123"
INSERT INTO sys_user (username, password, real_name, phone, dept_id, status) VALUES
('admin', '$2a$10$eaKSnCVUWfnAsYJefiXC6eqKninpva.nvIQfM6Ez6ENr1Jt/IAhuW', '管理员', '13800000000', 1, 1);

INSERT INTO sys_dept (id, parent_id, dept_name) VALUES
(1, 0, '总公司'),
(2, 1, '研发部'),
(3, 1, '培训部'),
(4, 1, '人力资源部');

INSERT INTO sys_role (role_name, role_code, description) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限'),
('培训管理员', 'TRAIN_ADMIN', '管理课程和考试'),
('普通学员', 'STUDENT', '学习课程和参加考试');

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- RBAC 权限系统
SET NAMES utf8mb4;

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `permission_code` varchar(100) NOT NULL COMMENT '权限编码',
  `permission_name` varchar(100) NOT NULL COMMENT '权限名称',
  `resource_type` varchar(20) NOT NULL DEFAULT 'button' COMMENT '资源类型 menu/button/api',
  `parent_id` bigint DEFAULT NULL COMMENT '父权限ID',
  `sort` int DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 权限申请表
CREATE TABLE IF NOT EXISTS `sys_permission_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '申请人',
  `permission_code` varchar(100) NOT NULL COMMENT '申请的权限编码',
  `reason` varchar(500) DEFAULT NULL COMMENT '申请理由',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待审批 1已通过 2已拒绝',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审批人',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '审批备注',
  `review_time` datetime DEFAULT NULL COMMENT '审批时间',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限申请表';

-- 初始化角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`) VALUES
(1, 'admin', '系统管理员', '拥有所有权限'),
(2, 'teacher', '讲师', '可上传课程、管理自己课程'),
(3, 'student', '学员', '默认角色，只能学习和考试');

-- 初始化权限
INSERT INTO `sys_permission` (`id`, `permission_code`, `permission_name`, `resource_type`, `parent_id`, `sort`) VALUES
(1, 'course:upload', '上传课程', 'button', NULL, 1),
(2, 'course:manage', '管理课程', 'button', NULL, 2),
(3, 'course:delete', '删除课程', 'button', NULL, 3),
(4, 'exam:manage', '管理考试', 'button', NULL, 4),
(5, 'user:manage', '用户管理', 'button', NULL, 5),
(6, 'permission:approve', '审批权限', 'button', NULL, 6),
(7, 'community:manage', '管理社区', 'button', NULL, 7);

-- 角色-权限关联
-- admin: 所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),
-- teacher: 上传+管理课程+管理考试
(2,1),(2,2),(2,4),
-- student: 无特殊权限
-- (3, none)
-- 无需插入

-- 用户-角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin -> 管理员
(2, 2),  -- 张三 -> 讲师
(3, 3);  -- 李四 -> 学员

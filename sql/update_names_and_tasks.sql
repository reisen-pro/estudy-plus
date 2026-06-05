SET NAMES utf8mb4;
USE estudy_plus;

-- ========== 更新人名 ==========
UPDATE sys_user SET real_name='周明远', username='zhoumingyuan', email='zhoumingyuan@estudy.com' WHERE id=2;
UPDATE sys_user SET real_name='林嘉欣', username='linjiaxin', email='linjiaxin@estudy.com' WHERE id=3;
UPDATE sys_user SET real_name='赵文博', username='zhaowenbo', email='zhaowenbo@estudy.com' WHERE id=4;
UPDATE sys_user SET real_name='孙晓峰', username='sunxiaofeng', email='sunxiaofeng@estudy.com' WHERE id=5;
UPDATE sys_user SET real_name='刘思远', username='liusiyuan', email='liusiyuan@estudy.com' WHERE id=6;
UPDATE sys_user SET real_name='杨慧敏', username='yanghuimin', email='yanghuimin@estudy.com' WHERE id=7;
UPDATE sys_user SET real_name='吴建国', username='wujianguo', email='wujianguo@estudy.com' WHERE id=8;

UPDATE post SET content=REPLACE(content, '张三', '周明远') WHERE content LIKE '%张三%';
UPDATE post SET content=REPLACE(content, '李四', '林嘉欣') WHERE content LIKE '%李四%';
UPDATE notification SET content=REPLACE(content, '李四', '林嘉欣') WHERE content LIKE '%李四%';
UPDATE notification SET content=REPLACE(content, '张三', '周明远') WHERE content LIKE '%张三%';
UPDATE notification SET content=REPLACE(content, '陈七', '刘思远') WHERE content LIKE '%陈七%';
UPDATE notification SET content=REPLACE(content, '孙八', '杨慧敏') WHERE content LIKE '%孙八%';

-- ========== 任务种子数据 ==========
INSERT INTO task (id, title, description, task_type, dept_id, course_id, paper_id, doc_read_duration, deadline, status, create_by) VALUES
(1, '新员工入职培训', '新员工必修：学习入职指南课程并通过安全考核', 1, 2, 1, 1, 300, '2026-06-30 23:59:59', 1, 1),
(2, 'GMP规范培训', '学习GMP课程并完成知识测验', 1, 2, 2, 2, 600, '2026-06-15 23:59:59', 1, 1),
(3, '实验室安全考核', '纯考试任务：通过实验室安全考核', 2, 3, NULL, 1, NULL, '2026-06-20 23:59:59', 1, 1);

INSERT INTO user_task (user_id, task_id, learn_status, exam_status, task_status) VALUES
(2, 1, 1, 0, 0),
(2, 2, 0, 0, 0),
(3, 1, 2, 1, 0),
(3, 3, 0, 0, 0),
(7, 1, 0, 0, 0),
(7, 3, 0, 0, 0);

-- ========== RBAC权限种子数据 ==========
INSERT INTO sys_permission (id, permission_code, permission_name, resource_type, parent_id, sort, status) VALUES
(1, 'course', '课程管理', 'menu', NULL, 1, 1),
(2, 'course:view', '查看课程', 'button', 1, 1, 1),
(3, 'course:create', '创建课程', 'button', 1, 2, 1),
(4, 'course:edit', '编辑课程', 'button', 1, 3, 1),
(5, 'course:delete', '删除课程', 'button', 1, 4, 1),
(6, 'course:upload', '上传视频', 'button', 1, 5, 1),
(10, 'exam', '考试管理', 'menu', NULL, 2, 1),
(11, 'exam:view', '查看试卷', 'button', 10, 1, 1),
(12, 'exam:create', '创建试卷', 'button', 10, 2, 1),
(13, 'exam:publish', '发布试卷', 'button', 10, 3, 1),
(20, 'task', '任务管理', 'menu', NULL, 3, 1),
(21, 'task:view', '查看任务', 'button', 20, 1, 1),
(22, 'task:create', '创建任务', 'button', 20, 2, 1),
(23, 'task:assign', '分配任务', 'button', 20, 3, 1),
(30, 'user', '用户管理', 'menu', NULL, 4, 1),
(31, 'user:view', '查看用户', 'button', 30, 1, 1),
(32, 'user:edit', '编辑用户', 'button', 30, 2, 1),
(40, 'community', '社区管理', 'menu', NULL, 5, 1),
(41, 'community:view', '查看帖子', 'button', 40, 1, 1),
(42, 'community:manage', '管理帖子', 'button', 40, 2, 1)
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- 管理员角色权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) SELECT 1, id FROM sys_permission;
-- 培训管理员权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1),(2, 2),(2, 3),(2, 4),(2, 5),(2, 6),
(2, 10),(2, 11),(2, 12),(2, 13),
(2, 20),(2, 21),(2, 22),(2, 23),
(2, 30),(2, 31),(2, 32),
(2, 40),(2, 41),(2, 42);
-- 讲师权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
(3, 1),(3, 2),(3, 3),(3, 4),(3, 6),
(3, 10),(3, 11),
(3, 40),(3, 41);

SET NAMES utf8mb4;
USE estudy_plus;

-- ========== 用户 ==========
-- admin已存在(id=1)，追加普通用户，密码均为 admin123
INSERT INTO sys_user (id, username, password, real_name, phone, email, dept_id, status) VALUES
(2, 'zhangsan', '$2a$10$eaKSnCVUWfnAsYJefiXC6eqKninpva.nvIQfM6Ez6ENr1Jt/IAhuW', '张三', '13800000001', 'zhangsan@estudy.com', 2, 1),
(3, 'lisi', '$2a$10$eaKSnCVUWfnAsYJefiXC6eqKninpva.nvIQfM6Ez6ENr1Jt/IAhuW', '李四', '13800000002', 'lisi@estudy.com', 2, 1),
(4, 'wangwu', '$2a$10$eaKSnCVUWfnAsYJefiXC6eqKninpva.nvIQfM6Ez6ENr1Jt/IAhuW', '王五', '13800000003', 'wangwu@estudy.com', 3, 1),
(5, 'zhaoliu', '$2a$10$eaKSnCVUWfnAsYJefiXC6eqKninpva.nvIQfM6Ez6ENr1Jt/IAhuW', '赵六', '13800000004', 'zhaoliu@estudy.com', 3, 1),
(6, 'chenqi', '$2a$10$eaKSnCVUWfnAsYJefiXC6eqKninpva.nvIQfM6Ez6ENr1Jt/IAhuW', '陈七', '13800000005', 'chenqi@estudy.com', 4, 1),
(7, 'sunba', '$2a$10$eaKSnCVUWfnAsYJefiXC6eqKninpva.nvIQfM6Ez6ENr1Jt/IAhuW', '孙八', '13800000006', 'sunba@estudy.com', 2, 1),
(8, 'zhoujiu', '$2a$10$eaKSnCVUWfnAsYJefiXC6eqKninpva.nvIQfM6Ez6ENr1Jt/IAhuW', '周九', '13800000007', 'zhoujiu@estudy.com', 4, 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(2, 3), (3, 3), (4, 3), (5, 2), (6, 3), (7, 3), (8, 3);

-- ========== 课程 ==========
INSERT INTO course (id, title, subtitle, category_id, teacher_id, description, course_type, price, status, view_count) VALUES
(1, '新员工入职指南', '快速融入药明康德', 1, 5, '本课程帮助新入职员工快速了解公司文化、规章制度、福利待遇等基本信息，助你顺利度过试用期。', 1, 0.00, 1, 328),
(2, 'GMP规范与实操', '药品生产质量管理规范', 4, 1, '系统学习GMP法规要求，掌握药品生产全流程的质量管理要点，确保合规生产。', 1, 0.00, 1, 215),
(3, '药物研发流程概览', '从靶点到上市', 5, 1, '全面了解药物从靶点发现、先导化合物优化、临床前研究到临床试验的完整研发链条。', 1, 0.00, 1, 176),
(4, '实验室安全培训', '安全第一，预防为主', 6, 5, '涵盖化学安全、生物安全、消防安全等实验室常见安全知识，通过考核方可进入实验室。', 1, 0.00, 1, 412),
(5, '高效沟通与团队协作', '管理能力提升系列', 3, 1, '学习跨部门沟通技巧、项目管理方法论和团队协作最佳实践，提升管理效能。', 1, 0.00, 1, 143),
(6, '生物分析技术进阶', '专业能力提升', 5, 1, '深入讲解LC-MS/MS、ELISA、流式细胞术等生物分析技术的原理与应用场景。', 1, 0.00, 1, 98);

-- 课程1的章节和课时
INSERT INTO course_chapter (id, course_id, parent_id, title, sort) VALUES
(1, 1, 0, '公司文化与价值观', 1),
(2, 1, 0, '规章制度与福利', 2),
(3, 1, 0, '入职流程指引', 3);

INSERT INTO course_lesson (id, chapter_id, course_id, title, lesson_type, duration, sort, is_free) VALUES
(1, 1, 1, '公司发展历程', 1, 900, 1, 1),
(2, 1, 1, '核心价值观解读', 1, 1200, 2, 1),
(3, 2, 1, '考勤与休假制度', 1, 600, 1, 0),
(4, 2, 1, '薪酬福利体系', 1, 800, 2, 0),
(5, 3, 1, '入职材料清单', 2, 300, 1, 1),
(6, 3, 1, 'IT系统账号开通', 2, 450, 2, 0);

-- 课程2的章节和课时
INSERT INTO course_chapter (id, course_id, parent_id, title, sort) VALUES
(4, 2, 0, 'GMP法规基础', 1),
(5, 2, 0, '生产过程管控', 2);

INSERT INTO course_lesson (id, chapter_id, course_id, title, lesson_type, duration, sort, is_free) VALUES
(7, 4, 2, 'GMP法规概述', 1, 1500, 1, 1),
(8, 4, 2, '质量管理体系', 1, 1800, 2, 0),
(9, 5, 2, '生产环境控制', 1, 1200, 1, 0),
(10, 5, 2, '批记录与放行', 1, 900, 2, 0);

-- 课程3的章节和课时
INSERT INTO course_chapter (id, course_id, parent_id, title, sort) VALUES
(6, 3, 0, '药物发现', 1),
(7, 3, 0, '临床前与临床研究', 2);

INSERT INTO course_lesson (id, chapter_id, course_id, title, lesson_type, duration, sort, is_free) VALUES
(11, 6, 3, '靶点发现与验证', 1, 1800, 1, 1),
(12, 6, 3, '先导化合物优化', 1, 1500, 2, 0),
(13, 7, 3, '临床前安全评价', 1, 1200, 1, 0),
(14, 7, 3, 'I/II/III期临床', 1, 2000, 2, 0);

-- 课程4的章节
INSERT INTO course_chapter (id, course_id, parent_id, title, sort) VALUES
(8, 4, 0, '化学安全', 1),
(9, 4, 0, '生物安全', 2);

INSERT INTO course_lesson (id, chapter_id, course_id, title, lesson_type, duration, sort, is_free) VALUES
(15, 8, 4, '化学品分类与标识', 1, 800, 1, 1),
(16, 8, 4, '应急处置流程', 1, 1000, 2, 0),
(17, 9, 4, '生物安全等级', 1, 600, 1, 0),
(18, 9, 4, 'PPE使用规范', 1, 750, 2, 0);

-- 学习进度(admin学了部分)
INSERT INTO learning_progress (user_id, course_id, lesson_id, progress, completed) VALUES
(1, 1, 1, 100, 1),
(1, 1, 2, 100, 1),
(1, 1, 3, 60, 0),
(1, 2, 7, 100, 1),
(2, 1, 1, 100, 1),
(2, 1, 2, 100, 1),
(2, 1, 3, 100, 1),
(2, 1, 4, 100, 1),
(3, 4, 15, 100, 1),
(3, 4, 16, 80, 0);

-- ========== 题库 ==========
INSERT INTO question (id, course_id, category_id, content, question_type, options, answer, analysis, difficulty, score) VALUES
-- 实验室安全题目
(1, 4, 6, '以下哪种标识表示化学品具有强腐蚀性？', 1, '[{"key":"A","value":"火焰图标"},{"key":"B","value":"腐蚀图标"},{"key":"C","value":"骷髅图标"},{"key":"D","value":"爆炸图标"}]', 'B', '腐蚀图标用于标识具有腐蚀性的化学品，通常为液体倒在手和金属上的图案。', 1, 5),
(2, 4, 6, '发现化学品泄漏时应采取的措施包括？', 2, '[{"key":"A","value":"立即撤离现场"},{"key":"B","value":"报告主管"},{"key":"C","value":"自行清理"},{"key":"D","value":"穿戴防护装备后处理"}]', 'A,B,D', '发现泄漏应先撤离、报告、在做好防护的前提下处理，不应自行无防护清理。', 2, 5),
(3, 4, 6, '实验室内可以饮食和存放食物。', 3, '[{"key":"A","value":"对"},{"key":"B","value":"错"}]', 'B', '实验室严禁饮食和存放食物，防止化学品污染。', 1, 5),
(4, 4, 6, 'BSL-2实验室需要配备哪种设备？', 1, '[{"key":"A","value":"通风橱"},{"key":"B","value":"生物安全柜"},{"key":"C","value":"超净工作台"},{"key":"D","value":"干燥箱"}]', 'B', 'BSL-2实验室必须配备生物安全柜以防止气溶胶扩散。', 2, 5),
(5, 4, 6, '以下哪些属于PPE（个人防护装备）？', 2, '[{"key":"A","value":"实验手套"},{"key":"B","value":"护目镜"},{"key":"C","value":"实验服"},{"key":"D","value":"手机"}]', 'A,B,C', '手机不属于PPE，实验手套、护目镜、实验服都是标准PPE。', 1, 5),

-- GMP题目
(6, 2, 4, 'GMP的中文全称是什么？', 1, '[{"key":"A","value":"良好农业规范"},{"key":"B","value":"药品生产质量管理规范"},{"key":"C","value":"良好实验室规范"},{"key":"D","value":"良好供应规范"}]', 'B', 'GMP = Good Manufacturing Practice，即药品生产质量管理规范。', 1, 5),
(7, 2, 4, '以下哪些属于GMP文件管理的要求？', 2, '[{"key":"A","value":"文件需经审批后生效"},{"key":"B","value":"批记录需实时填写"},{"key":"C","value":"过期文件可自行销毁"},{"key":"D","value":"修改需划线签名并注明日期"}]', 'A,B,D', '过期文件需按规程销毁并记录，不可自行销毁。', 2, 5),
(8, 2, 4, '洁净区的压差应大于等于10Pa。', 3, '[{"key":"A","value":"对"},{"key":"B","value":"错"}]', 'A', 'GMP要求不同级别洁净区之间压差≥10Pa。', 2, 5),
(9, 2, 4, '批记录的保存期限通常为？', 1, '[{"key":"A","value":"1年"},{"key":"B","value":"3年"},{"key":"C","value":"产品有效期后1年"},{"key":"D","value":"产品有效期后1年且不少于3年"}]', 'D', '批记录保存至产品有效期后1年，且不少于3年。', 2, 5),
(10, 2, 4, '关于偏差处理，以下正确的是？', 1, '[{"key":"A","value":"偏差可以事后补记"},{"key":"B","value":"所有偏差都需调查并记录"},{"key":"C","value":"小偏差无需记录"},{"key":"D","value":"偏差只需口头报告"}]', 'B', '所有偏差都需要书面调查、记录并采取纠正预防措施。', 2, 5),

-- 药物研发题目
(11, 3, 5, '药物I期临床试验的主要目的是？', 1, '[{"key":"A","value":"验证有效性"},{"key":"B","value":"评估安全性耐受性"},{"key":"C","value":"比较不同治疗方案"},{"key":"D","value":"长期安全性观察"}]', 'B', 'I期临床主要评估药物在人体的安全性和耐受性，通常在小样本健康志愿者中进行。', 2, 5),
(12, 3, 5, '以下哪些是先导化合物优化的常用策略？', 2, '[{"key":"A","value":"生物电子等排替换"},{"key":"B","value":"前药设计"},{"key":"C","value":"盲法试验"},{"key":"D","value":"构效关系研究"}]', 'A,B,D', '盲法试验是临床设计方法，不属于先导化合物优化策略。', 2, 5),
(13, 3, 5, 'IND申请是指向监管机构提交开展临床试验的申请。', 3, '[{"key":"A","value":"对"},{"key":"B","value":"错"}]', 'A', 'IND = Investigational New Drug，即新药临床试验申请。', 1, 5),
(14, 3, 5, 'ADME中的M代表什么？', 1, '[{"key":"A","value":"膜转运"},{"key":"B","value":"代谢"},{"key":"C","value":"分子量"},{"key":"D","value":"修饰"}]', 'B', 'ADME = Absorption(吸收), Distribution(分布), Metabolism(代谢), Excretion(排泄)。', 1, 5),
(15, 3, 5, 'III期临床试验的受试者规模通常为？', 1, '[{"key":"A","value":"20-100人"},{"key":"B","value":"100-300人"},{"key":"C","value":"300-3000人"},{"key":"D","value":"10000人以上"}]', 'C', 'III期临床为大样本确证性试验，通常300-3000人。', 2, 5);

-- ========== 试卷 ==========
INSERT INTO exam_paper (id, title, course_id, description, total_score, pass_score, duration, exam_type, status, max_attempts) VALUES
(1, '实验室安全培训考核', 4, '新员工入职必考，通过后方可进入实验室', 50, 40, 30, 2, 1, 3),
(2, 'GMP基础知识测验', 2, 'GMP培训配套考核', 25, 20, 20, 1, 1, -1),
(3, '药物研发流程测试', 3, '了解药物研发基本流程', 25, 15, 25, 1, 1, -1);

INSERT INTO exam_paper_question (paper_id, question_id, sort, score) VALUES
(1, 1, 1, 5), (1, 2, 2, 5), (1, 3, 3, 5), (1, 4, 4, 5), (1, 5, 5, 5),
(2, 6, 1, 5), (2, 7, 2, 5), (2, 8, 3, 5), (2, 9, 4, 5), (2, 10, 5, 5),
(3, 11, 1, 5), (3, 12, 2, 5), (3, 13, 3, 5), (3, 14, 4, 5), (3, 15, 5, 5);

-- ========== 考试记录(已完成的) ==========
INSERT INTO exam_record (id, user_id, paper_id, score, total_score, passed, status, start_time, submit_time, duration) VALUES
(1, 2, 1, 45, 50, 1, 2, '2026-05-20 09:00:00', '2026-05-20 09:22:00', 1320),
(2, 3, 1, 40, 50, 1, 2, '2026-05-20 10:00:00', '2026-05-20 10:25:00', 1500),
(3, 2, 2, 25, 25, 1, 2, '2026-05-21 14:00:00', '2026-05-21 14:15:00', 900),
(4, 7, 1, 35, 50, 0, 2, '2026-05-22 09:30:00', '2026-05-22 09:58:00', 1680),
(5, 4, 3, 20, 25, 1, 2, '2026-05-22 15:00:00', '2026-05-22 15:18:00', 1080);

-- ========== 社区帖子 ==========
INSERT INTO post (id, user_id, title, content, category, view_count, like_count, comment_count, is_top, status) VALUES
(1, 5, '新员工入职注意事项总结', '作为培训管理员，整理一份新员工入职的注意事项：\n\n1. 入职当天请携带身份证原件、学历证书、离职证明\n2. 体检报告需在入职前完成（指定医院清单见附件）\n3. IT账号将在入职当天由IT部门统一开通\n4. 首周需完成安全培训和GMP培训\n5. 试用期3个月，转正答辩在第3个月\n\n有问题随时联系培训部！', '公告', 586, 42, 8, 1, 1),
(2, 2, 'GMP培训重点笔记分享', '最近参加了GMP培训，把重点整理出来分享给大家：\n\n- 批记录必须实时填写，不能事后补记\n- 偏差处理要书面调查并记录\n- 洁净区压差≥10Pa\n- 文件需审批后生效\n- 批记录保存至有效期后1年且不少于3年\n\n祝大家考试顺利通过！', '分享', 234, 28, 5, 0, 1),
(3, 7, 'ELISA实验操作请教', '各位大佬，做ELISA的时候标准曲线R²总是到不了0.99，已经重复了三次了，有什么技巧吗？\n\n我目前的操作：\n- 封闭时间30min\n- 洗板5次\n- 显色15min\n\n求指点！', '提问', 156, 15, 7, 0, 1),
(4, 3, '实验室安全考核经验贴', '刚通过实验室安全考核，45分（满分50，及格40），分享一下经验：\n\n- PPE那道题是必考的，一定记住实验手套+护目镜+实验服\n- 化学品泄漏处理：撤离→报告→防护后处理，不要自己上\n- BSL-2对应生物安全柜不是通风橱\n\n多刷几遍培训视频就稳了。', '分享', 312, 35, 3, 0, 1),
(5, 4, '跨部门沟通踩过的坑', '在研发和QC之间协调项目的时候，发现几个常见问题：\n\n1. 邮件信息不对称——建议抄送所有相关方\n2. 时间节点没对齐——提前在项目会上确认milestone\n3. 样品交接不规范——用LIMS系统走流程\n4. 需求变更没走变更控制——必须走Change Control\n\n大家有什么补充的吗？', '讨论', 198, 22, 6, 0, 1),
(6, 6, 'LC-MS/MS方法学验证要点', '分享一份LC-MS/MS生物分析方法学验证的要点清单：\n\n- 选择性与基质效应\n- 线性与范围（至少6个浓度点）\n- 准确度与精密度（批内批间）\n- 稳定性（室温、冻融、长期、自动进样器）\n- 稀释可靠性\n- 残留效应\n\n有需要详细SOP的可以私信我。', '分享', 278, 31, 4, 0, 1);

-- ========== 评论 ==========
INSERT INTO comment (id, post_id, user_id, parent_id, reply_to_user_id, content, like_count, status) VALUES
-- 帖子1的评论
(1, 1, 2, 0, NULL, '太实用了！收藏了，入职的时候对照着准备', 8, 1),
(2, 1, 3, 0, NULL, '请问体检可以提前做吗？还是必须等HR通知？', 3, 1),
(3, 1, 5, 2, 3, '可以提前做，但要在指定医院，名单我已经发邮件了', 5, 1),
(4, 1, 7, 0, NULL, 'IT账号开通大概需要多久？我入职当天下午才拿到', 2, 1),
-- 帖子2的评论
(5, 2, 4, 0, NULL, '感谢分享！偏差处理那块我老是记混', 6, 1),
(6, 2, 6, 0, NULL, '批记录保存期限那个D选项是经典陷阱题哈哈', 4, 1),
-- 帖子3的评论
(7, 3, 1, 0, NULL, '封闭时间可以试试延长到1h，洗板建议用洗板机，手动洗容易不均匀', 7, 1),
(8, 3, 8, 7, 1, '赞同，洗板机效果确实好很多', 3, 1),
(9, 3, 6, 0, NULL, '显色时间可以适当缩短到10min试试，过度显色反而影响线性', 5, 1),
-- 帖子4的评论
(10, 4, 2, 0, NULL, 'PPE那题我第一次也答错了😂', 4, 1),
-- 帖子5的评论
(11, 5, 5, 0, NULL, '补充一点：样品标签一定要规范，我见过好几次标签写错导致返工的', 6, 1),
(12, 5, 7, 0, NULL, '变更控制确实是重点，之前我们项目差点因为没走Change Control被审计抓', 4, 1),
-- 帖子6的评论
(13, 6, 1, 0, NULL, '残留效应经常被忽视，其实对定量影响很大', 5, 1),
(14, 6, 3, 0, NULL, '求SOP！我的邮箱lisi@estudy.com', 3, 1);

-- ========== 点赞 ==========
INSERT INTO `like` (user_id, target_type, target_id) VALUES
(2, 1, 1), (3, 1, 1), (4, 1, 1), (6, 1, 1), (7, 1, 1),
(3, 1, 2), (4, 1, 2), (7, 1, 2),
(2, 1, 3), (4, 1, 3), (6, 1, 3),
(1, 1, 4), (2, 1, 4), (6, 1, 4),
(1, 1, 5), (3, 1, 5), (8, 1, 5),
(2, 1, 6), (3, 1, 6), (7, 1, 6),
(3, 2, 1), (4, 2, 1), (7, 2, 3),
(1, 2, 7), (2, 2, 9), (6, 2, 13);

-- ========== 通知 ==========
INSERT INTO notification (user_id, from_user_id, type, title, content, biz_id, is_read) VALUES
(2, 3, 'comment', '新评论', '李四评论了你的帖子：GMP培训重点笔记分享', '2', 1),
(5, 2, 'like', '收到点赞', '张三赞了你的帖子：新员工入职注意事项总结', '1', 1),
(7, 1, 'comment', '新评论', '管理员回复了你的评论', '3', 0),
(2, 6, 'like', '收到点赞', '陈七赞了你的帖子', '2', 0),
(4, 7, 'comment', '新评论', '孙八评论了你的帖子：跨部门沟通踩过的坑', '5', 0),
(3, 1, 'comment', '新评论', '管理员回复了你在ELISA帖子下的评论', '3', 0);

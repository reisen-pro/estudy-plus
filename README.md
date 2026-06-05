# e学网+ 培训平台

复刻"e学网+"企业培训平台核心功能，用于简历项目展示。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus + JWT |
| 中间件 | MySQL 8.0 + Redis 7 |
| 容器化 | Docker + Docker Compose + Nginx |
| 钉钉集成 | REST API 直连（Hutool HTTP） |

## 模块总览

| 阶段 | 模块 | 说明 |
|---|---|---|
| ① | 基础骨架 | 用户登录注册、JWT 认证、全局异常处理 |
| ② | 课程模块 | 课程 CRUD、分类树、章节课时、学习进度 |
| ③ | 考试模块 | 题库管理、试卷发布、考试次数限制、自动判分 |
| ④ | 钉钉集成 | 免登、消息推送、回调事件 |
| ⑤ | 社区模块 | 帖子、评论(树形)、点赞(切换)、通知 |
| ⑥ | 容器化部署 | Dockerfile + Compose 编排 + Nginx 反代 |

## 快速启动

### 前置条件

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose

### 本地开发

```bash
# 1. 启动中间件
docker compose up -d mysql redis

# 2. 等待 MySQL 就绪后导入表结构（自动执行 sql/ 目录下脚本）

# 3. 启动后端
mvn spring-boot:run

# 4. 访问 http://localhost:8080
```

### Docker 一键部署

```bash
# Linux/Mac
chmod +x deploy.sh && ./deploy.sh

# Windows
deploy.bat
```

## 默认账号

- 管理员：`admin` / `admin123`

## 数据库

共 21 张表，SQL 脚本在 `sql/` 目录：

- `init.sql` — 用户/角色/部门基础表
- `course.sql` — 课程相关 5 表
- `exam.sql` — 考试相关 5 表
- `dingtalk.sql` — 钉钉集成 3 表
- `community.sql` — 社区 4 表

## API 概览

| 模块 | 前缀 | 核心接口 |
|---|---|---|
| 认证 | `/api/auth` | 登录、注册 |
| 课程 | `/api/course` | 课程 CRUD、分类树、学习进度 |
| 考试 | `/api/exam` | 题目管理、试卷、开始考试、交卷判分 |
| 钉钉 | `/api/dingtalk` | 免登、发消息、回调 |
| 社区 | `/api/community` | 帖子、评论、点赞、通知 |

## 项目结构

```
estudy-plus/
├── sql/                    # 数据库脚本
├── nginx/                  # Nginx 配置
├── frontend/               # 前端(预留)
├── src/main/java/com/estudy/
│   ├── common/             # 通用组件(Result, 异常, JWT, 拦截器)
│   ├── config/             # 配置类
│   ├── user/               # 用户模块
│   ├── course/             # 课程模块
│   ├── exam/               # 考试模块
│   ├── dingtalk/           # 钉钉集成
│   └── community/          # 社区模块
├── docker-compose.yml
├── Dockerfile
├── deploy.sh / deploy.bat
└── pom.xml
```

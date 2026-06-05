@echo off
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

echo ==========================================
echo   e学网+ 培训平台 - Docker 一键部署
echo ==========================================

if not exist .env (
    echo [INFO] 未找到 .env，从 .env.example 复制...
    copy .env.example .env >nul
    echo [WARN] 请编辑 .env 填入真实的钉钉配置后再启动！
    pause
    exit /b 1
)

echo [STEP 1/4] 构建 Spring Boot JAR...
if not exist target\estudy-plus-1.0.0.jar (
    echo   执行 mvn package -DskipTests...
    call mvn package -DskipTests
) else (
    echo   JAR 已存在，跳过构建
)

echo [STEP 2/4] 构建 Docker 镜像...
docker compose build app

echo [STEP 3/4] 启动所有服务...
docker compose up -d

echo [STEP 4/4] 等待服务就绪...
timeout /t 10 /nobreak >nul

echo.
echo ==========================================
echo   部署完成！
echo ==========================================
echo   前端：http://localhost
echo   后端：http://localhost:8080
echo   MySQL：localhost:3306
echo   Redis：localhost:6379
echo.
echo   默认管理员：admin / admin123
echo ==========================================
echo.
echo 常用命令：
echo   查看日志：docker compose logs -f app
echo   停止服务：docker compose down
echo   重启应用：docker compose restart app
pause

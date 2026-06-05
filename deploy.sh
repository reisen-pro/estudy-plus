#!/bin/bash
# e学网+ 一键部署脚本

set -e

echo "=========================================="
echo "  e学网+ 培训平台 - Docker 一键部署"
echo "=========================================="

# 检查 .env
if [ ! -f .env ]; then
    echo "[INFO] 未找到 .env，从 .env.example 复制..."
    cp .env.example .env
    echo "[WARN] 请编辑 .env 填入真实的钉钉配置后再启动！"
    exit 1
fi

# 构建 JAR
echo "[STEP 1/4] 构建 Spring Boot JAR..."
if [ ! -f target/estudy-plus-1.0.0.jar ]; then
    echo "  执行 mvn package -DskipTests..."
    mvn package -DskipTests
else
    echo "  JAR 已存在，跳过构建"
fi

# 构建镜像
echo "[STEP 2/4] 构建 Docker 镜像..."
docker compose build app

# 启动服务
echo "[STEP 3/4] 启动所有服务..."
docker compose up -d

# 等待就绪
echo "[STEP 4/4] 等待服务就绪..."
sleep 10
echo ""
echo "=========================================="
echo "  部署完成！"
echo "=========================================="
echo "  前端：http://localhost"
echo "  后端：http://localhost:8080"
echo "  MySQL：localhost:3306"
echo "  Redis：localhost:6379"
echo ""
echo "  默认管理员：admin / admin123"
echo "=========================================="
echo ""
echo "常用命令："
echo "  查看日志：docker compose logs -f app"
echo "  停止服务：docker compose down"
echo "  重启应用：docker compose restart app"

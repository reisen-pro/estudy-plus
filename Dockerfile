FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# 需要maven wrapper或maven，这里用多阶段构建
# 实际构建在CI/CD完成，这里假设已有target目录
# 如果本地构建：mvn package -DskipTests 后再构建镜像

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 时区
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

COPY target/estudy-plus-1.0.0.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE="prod"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]

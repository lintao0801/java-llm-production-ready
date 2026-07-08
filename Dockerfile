# 多阶段构建：先构建，再运行，最终镜像更小
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先复制 pom 文件，利用 Docker 缓存加速依赖下载
COPY pom.xml ./
COPY llm-common/pom.xml ./llm-common/
COPY llm-service/pom.xml ./llm-service/
COPY llm-gateway/pom.xml ./llm-gateway/

# 下载依赖（仅当 pom 变化时才会重新执行）
RUN mvn dependency:go-offline -B -pl llm-service -am || true

# 复制源代码
COPY llm-common/src ./llm-common/src
COPY llm-service/src ./llm-service/src
COPY llm-gateway/src ./llm-gateway/src

# 构建
RUN mvn clean package -DskipTests -pl llm-service -am

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 安装 curl，用于健康检查
RUN apk add --no-cache curl

# 从构建阶段复制 jar
COPY --from=builder /build/llm-service/target/*.jar /app/app.jar

# 暴露端口
EXPOSE 8080

# JVM 参数：容器感知，限制内存
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError"

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

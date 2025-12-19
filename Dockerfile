# ======================== 阶段1：基础镜像选择 ========================
# 选择官方 Temurin JDK（Alpine 版本体积小，Spring Boot 3+ 推荐 JDK 17，2.x 用 JDK 11）
FROM eclipse-temurin:21-jre-alpine
  
  # ======================== 基础配置 ========================
  # 设置工作目录
WORKDIR /app
  
  # 配置时区（解决容器内时间与宿主机不一致问题）
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone
  
  # 创建非 root 用户（避免容器以 root 运行带来的安全风险）
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
  
  # ======================== 复制 Jar 包 ========================
  # 将本地的 Spring Boot Jar 包复制到容器内（注意：替换为你的 Jar 包名称，建议统一命名为 app.jar）
COPY your-spring-app.jar /app/app.jar
  
  # 更改工作目录权限为非 root 用户
RUN chown -R appuser:appgroup /app
  
  # 切换到非 root 用户
USER appuser m  
  
  # ======================== 暴露端口 & 启动命令 ========================
  # 暴露 Spring Boot 应用端口（替换为你的应用端口，如 8080/9090）
EXPOSE 8080
  
  # 启动命令（自定义 JVM 参数，根据服务器配置调整，比如内存大小）
  # -Xms/-Xmx：初始/最大堆内存，-XX:+UseContainerSupport：适配容器内存限制
ENTRYPOINT ["java", \
"-Xms256m", \
"-Xmx512m", \
"-XX:+UseContainerSupport", \
"-XX:+ExitOnOutOfMemoryError", \
"-jar", \
"/app/app.jar"]
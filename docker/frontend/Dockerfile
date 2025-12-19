# 校园闪配前端 Dockerfile
# 多阶段构建：构建阶段 + Nginx运行阶段

# ========== 构建阶段 ==========
FROM node:20-alpine AS builder

WORKDIR /app

# 复制 package.json 并安装依赖（利用缓存）
COPY frontend/package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码并构建
COPY frontend/ ./
RUN npm run build

# ========== 运行阶段 ==========
FROM nginx:alpine

# 删除默认配置
RUN rm /etc/nginx/conf.d/default.conf

# 复制自定义 nginx 配置
COPY docker/frontend/nginx.conf /etc/nginx/conf.d/

# 从构建阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost/ || exit 1

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]

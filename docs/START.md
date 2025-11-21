# 🚀 校园闪配 - 快速启动指南

## ✅ 问题已修复

已修复 IP 校验问题，现在支持：
1. 本地开发环境（127.0.0.1）
2. 校园网 IP（10.x.x.x）
3. 代理环境下的真实 IP 获取

## 📝 启动步骤

### 1️⃣ 启动后端服务

打开终端 1，运行：

```bash
cd "d:\java project\campus-flash-match"

# 使用 Maven
.\mvnw spring-boot:run

# 或使用 Gradle
.\gradlew bootRun
```

**等待看到：**
```
Started CampusFlashMatchApplication in X.XXX seconds
```

**验证后端：** 访问 http://localhost:8080

---

### 2️⃣ 启动前端服务

打开终端 2，运行：

```bash
cd "d:\java project\campus-flash-match\frontend"

# 首次运行需要安装依赖
npm install

# 启动开发服务器
npm run dev
```

**等待看到：**
```
VITE v5.1.5  ready in XXX ms
➜  Local:   http://localhost:3000/
```

**访问前端：** http://localhost:3000

---

## 🎯 测试流程

### 步骤 1：注册账号
1. 访问 http://localhost:3000/register
2. 填写信息：
   - 学号：20210001
   - 昵称：测试用户
   - 密码：123456
3. 点击注册
4. ✅ 应该显示"注册成功"

### 步骤 2：登录系统
1. 访问 http://localhost:3000/login
2. 输入学号和密码
3. 点击登录
4. ✅ 应该跳转到首页

### 步骤 3：测试功能
- 发起拼单/约伴
- 查看附近事件
- 发布信标
- 查看信用分

---

## 🔧 故障排查

### 问题 1：后端启动失败

**可能原因：**
- PostgreSQL 未启动
- Redis 未启动
- RabbitMQ 未启动
- 端口 8080 被占用

**解决方案：**
```bash
# 检查 PostgreSQL
psql -U postgres -c "SELECT version();"

# 检查 Redis
redis-cli ping

# 检查 RabbitMQ
rabbitmqctl status

# 检查端口占用
netstat -ano | findstr :8080
```

### 问题 2：前端无法访问后端

**检查项：**
1. 后端是否启动（http://localhost:8080）
2. 浏览器控制台是否有错误
3. 网络请求是否被拦截

**解决方案：**
- 确认后端日志中有 "本地开发环境，允许访问" 的输出
- 清除浏览器缓存
- 使用无痕模式测试

### 问题 3：注册时提示 IP 校验失败

**当前配置：**
- 本地开发：自动允许（127.0.0.1）
- 校园网：允许 10.x.x.x 开头的 IP

**如需修改：**
编辑 `src/main/resources/application.yml`：
```yaml
campus:
  ip-prefix: "你的IP前缀"  # 例如 "10.135"
```

### 问题 4：WebSocket 连接失败

**检查项：**
1. 用户是否已登录
2. 后端 WebSocket 配置是否正确
3. 浏览器是否支持 WebSocket

**查看连接状态：**
- 打开浏览器开发者工具
- 切换到 Network → WS 标签
- 查看 WebSocket 连接状态

---

## 📊 服务状态检查

### 后端健康检查
```bash
# 检查后端是否运行
curl http://localhost:8080

# 检查 API 是否可访问
curl http://localhost:8080/api/user/login
```

### 前端健康检查
```bash
# 检查前端是否运行
curl http://localhost:3000
```

---

## 🎨 开发建议

### 推荐的开发工具
- **IDE：** IntelliJ IDEA / VS Code
- **API 测试：** Postman / Apifox
- **数据库：** DBeaver / pgAdmin
- **Redis：** RedisInsight
- **浏览器：** Chrome（推荐开发者工具）

### 热重载
- **后端：** 修改代码后自动重启（Spring Boot DevTools）
- **前端：** 修改代码后自动刷新（Vite HMR）

---

## 📚 相关文档

- [前端 README](frontend/README.md) - 前端完整文档
- [API 映射](frontend/API_MAPPING.md) - API 接口详解
- [安装指南](frontend/INSTALLATION.md) - 详细安装步骤
- [项目总结](FRONTEND_SUMMARY.md) - 项目架构总结

---

## ✨ 快速命令

### 一键启动（Windows PowerShell）

创建 `start.ps1` 文件：
```powershell
# 启动后端
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'd:\java project\campus-flash-match'; .\mvnw spring-boot:run"

# 等待 5 秒
Start-Sleep -Seconds 5

# 启动前端
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'd:\java project\campus-flash-match\frontend'; npm run dev"

# 打开浏览器
Start-Sleep -Seconds 3
Start-Process "http://localhost:3000"
```

运行：
```bash
.\start.ps1
```

---

## 🎉 成功标志

当你看到以下内容时，说明系统已成功启动：

✅ 后端控制台：
```
Started CampusFlashMatchApplication
Tomcat started on port(s): 8080
```

✅ 前端控制台：
```
VITE v5.1.5  ready in XXX ms
➜  Local:   http://localhost:3000/
```

✅ 浏览器：
- 能看到登录页面
- 能成功注册和登录
- 能查看附近事件
- WebSocket 显示"已连接"

---

**祝你开发顺利！** 🚀

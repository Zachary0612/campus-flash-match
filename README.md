# 🎓 校园闪配 · Campus Flash Match

> 校园拼单 / 约伴 / 信用社交平台 · Spring Boot + Vue 3 全栈实现

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.x-brightgreen.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## 📚 目录

1. [项目简介](#项目简介)
2. [系统亮点](#系统亮点)
3. [整体架构](#整体架构)
4. [目录结构](#目录结构)
5. [环境准备](#环境准备)
6. [快速开始](#快速开始)
7. [配置说明](#配置说明)
8. [技术栈](#技术栈)
9. [核心模块与特性](#核心模块与特性)
10. [实时通信与消息流](#实时通信与消息流)
11. [文档与 api](#文档与-api)
12. [测试 / 构建 / 发布](#测试--构建--发布)
13. [故障排查](#故障排查)
14. [贡献指南](#贡献指南)
15. [许可证](#许可证)

---

## 项目简介

校园闪配是一个为校内用户量身打造的拼单约伴平台，核心目标是**让校园里的“临时需求”可以在 5 分钟内匹配成功**。平台提供：

- 🛒 **拼单协作**：食堂、外卖、快递代取快速成团
- 👥 **约伴活动**：学习、运动、约饭、社团活动招募
- 📍 **信标功能**：位置占位、排队提醒、临时集合
- ⭐ **信用体系**：学号认证、信用积分、互评机制
- 💬 **实时互动**：事件聊天室、通知推送、关注动态
- 📡 **位置服务**：Redis GEO 查找 1km 内热点事件

---

## 系统亮点

| 场景 | 能力 |
|------|------|
| 事件撮合 | Redis GEO + 事件缓存，实现毫秒级附近事件查询 |
| 信用闭环 | 信用分 + 举报 + 评价，保障真实可信 |
| 实时体验 | WebSocket 推送事件状态、聊天室消息、通知提醒 |
| 异步解耦 | RabbitMQ 处理事件结算、信用变更等耗时任务 |
| 低门槛启动 | 一键脚本 + 跨平台配置，开发 / 演示快捷 |

---

## 整体架构

```
┌──────────────┐        ┌─────────────┐
│   Vue 3 SPA   │<──────>│  REST / WS  │
│ (Vite + Pinia)│  HTTPS │ Spring Boot │
└──────┬───────┘        └──────┬──────┘
       │                        │
       │ WebSocket / REST       │
       │                        │
┌──────▼──────┐   Cache / GEO   ┌──────────────┐
│  Redis 6.x  │<───────────────>│ PostgreSQL 14 │
└──────▲──────┘                 └──────────────┘
       │ MQ                         ▲
┌──────▼─────────┐                  │
│ RabbitMQ 3.9.x │─事件结算/通知───┘
└────────────────┘
```

---

## 目录结构

```
campus-flash-match
├─ src/                     # Spring Boot 后端
├─ frontend/                # Vue 3 + Vite 前端
├─ docs/                    # 详细文档（架构 / 接口 / 指南）
├─ scripts/                 # 启动脚本
│   ├─ start-backend.cmd    # Windows 启动后端
│   ├─ start-frontend.cmd   # Windows 启动前端
│   └─ start-all.ps1        # 一键启动脚本
├─ Dockerfile               # Docker 容器化配置
├─ pom.xml                  # Maven 配置
└─ README.md                # 本文件
```

---

## 环境准备

| 组件 | 最低版本 | 备注 |
|------|----------|------|
| Java | 17 | 推荐使用 Temurin / Zulu |
| Maven | 3.6+ | 已提供 `mvnw` wrapper，可直接使用 |
| Node.js | 16+ | 推荐 18 LTS |
| PostgreSQL | 14+ | 需要启用 PostGIS 可选 |
| Redis | 6+ | 开启持久化可避免 GEO 丢失 |
| RabbitMQ | 3.9+ | 默认虚拟主机 `/` |

---

## 快速开始

### 方式 1：脚本一键启动（推荐）

```powershell
# 后端
.\scripts\start-backend.cmd

# 前端
.\scripts\start-frontend.cmd

# 一键启动（后端 + 前端 + 浏览器）
.\scripts\start-all.ps1
```

### 方式 2：手动启动（用于开发）

1. **启动后端**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **启动前端**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. **访问**
   - 前端：http://localhost:3000
   - 后端：http://localhost:8080
   - Swagger：http://localhost:8080/swagger-ui.html

---

## 配置说明

### 后端（`src/main/resources/application.yml`）

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: 123456
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672

jwt:
  secret: campus-flash-match-secret-key
  expire: 7200000    # 2 小时

campus:
  ip-prefix: "10"    # 校园网 IP 前缀
  event:
    default-radius: 1000
```

### 前端（`frontend/vite.config.js`）

```ts
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Spring Boot 3.1 | 核心框架，整合 Web / Security / Actuator |
| Spring Security 6 | JWT + 自定义过滤器保护 API |
| MyBatis-Plus 3.5 | ORM，简化 CRUD 与分页 |
| PostgreSQL 14 | 关系数据库，JSONB 扩展事件元数据 |
| Redis 6 | GEO 空间搜索、缓存、分布式锁 |
| RabbitMQ 3.9 | 异步事件结算、通知推送 |
| WebSocket | 事件聊天室、实时通知 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 + Composition API | 单页应用核心 |
| Vite 5 | 极速开发构建 |
| Pinia | 全局状态管理（用户、WebSocket） |
| Element Plus + TailwindCSS | UI 组件与现代化视觉 |
| Axios | API 请求封装，统一拦截处理 |

---

## 核心模块与特性

| 模块 | 功能亮点 |
|------|----------|
| 用户 & 认证 | 学号注册、邮箱验证码、JWT 登录、校内 IP 校验、资料编辑 |
| 事件中心 | 创建 / 搜索 / 附近事件、参与 / 退出、收藏、评论、评级 |
| 信用体系 | 初始信用分、行为加减分、信用记录查询 |
| 关注与通知 | 双向关注、消息盒子、消息已读、关注动态提醒 |
| 信标 | 临时位置标记、附近信标浏览、举报虚假信标 |
| 文件 & 多媒体 | 头像 / 图片上传、本地存储映射 |

---

## 实时通信与消息流

1. **事件聊天室与状态广播**  
   - 前端通过 WebSocket 订阅事件 ID  
   - 服务器将参与者列表、状态变更、聊天内容推送给订阅者

2. **RabbitMQ 异步结算**  
   - 事件到期或手动确认 → 投递结算消息  
   - 消费者更新事件历史、信用分、通知提醒

3. **Redis GEO + 缓存层**  
   - 用户经纬度与事件位置使用 GEO 存储  
   - 查询附近事件时仅与 Redis 交互，数据库作为最终落地

---

## 文档与 API

| 文档 | 描述 |
|------|------|
| `docs/PROJECT_DOCUMENTATION.md` | 项目技术白皮书（架构 / 模块 / 设计） |
| `docs/FILE_REFERENCE.md` | 每个目录 / 文件作用速查 |
| `docs/API_MAPPING.md` | 前后端接口映射 & 请求示例 |
| `docs/START.md` | 详细启动步骤 & 故障排查 |
| `frontend/README.md` | 前端开发指南 |

---

## 测试 / 构建 / 发布

| 命令 | 作用 |
|------|------|
| `./mvnw test` | 运行后端单元测试 |
| `./mvnw clean package` | 打包 Spring Boot JAR |
| `cd frontend && npm run test` | （如使用 Vitest）运行前端测试 |
| `cd frontend && npm run build` | 产出生产静态资源 |
| `npm run preview` | 本地预览构建结果 |

部署时可将前端 `dist` 上传至 Nginx，并通过反向代理 `/api` 和 `/ws` 到后端。

---

## 故障排查

| 症状 | 排查建议 |
|------|----------|
| `POST /api/user/register` 返回 302 | 检查 Spring Security 是否按照文档配置 |
| 前端报 CORS 错误 | 确认后端已放行 `OPTIONS` 并启用 `CorsFilter` |
| Redis GEO 查询为空 | 确认事件已写入 GEO 集合、Redis 未清空 |
| WebSocket 401 | 检查前端是否携带最新 JWT Token |
| RabbitMQ 消费失败 | 查看 `event.settlement` 队列日志，确认消费者运行 |

更多问题可参考 `docs/START.md` 的 FAQ 部分。

---

## 贡献指南

1. Fork 仓库并新建分支（例如 `feat/websocket-heartbeat`）
2. 遵循现有代码风格（Java 使用 Google Style，Vue 使用 ESLint 默认）
3. 补充必要的单元测试 / 文档
4. 提交 Pull Request，并在描述中关联 Issue

欢迎反馈 bug、提出新功能或提供 UX 改进建议。

---

## 许可证

项目基于 [MIT License](LICENSE) 开源，企业或个人可自由使用、修改、二次开发。请在衍生项目中保留原始版权声明。

---

**💬 有任何问题？**  
查看 [docs/PROJECT_DOCUMENTATION.md](docs/PROJECT_DOCUMENTATION.md) 或提交 Issue，我们会第一时间响应。🚀

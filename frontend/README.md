# 校园闪配前端界面

这是一个为校园闪配项目设计的现代化前端界面，基于Vue3和Element Plus构建。

## 功能特点

1. 响应式设计，适配不同屏幕尺寸
2. 现代化的UI界面，提供良好的用户体验

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架 (Composition API)
- **Vite** - 新一代前端构建工具
- **Vue Router** - 官方路由管理器
- **Pinia** - 新一代状态管理库
- **Element Plus** - 基于 Vue 3 的组件库
- **Axios** - HTTP 请求库
- **TailwindCSS** - 实用优先的 CSS 框架
- **WebSocket** - 实时通信
- **Day.js** - 轻量级日期处理库

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口封装
│   │   ├── request.js    # Axios 实例配置
│   │   ├── user.js       # 用户相关接口
│   │   ├── event.js      # 事件相关接口
│   │   ├── beacon.js     # 信标相关接口
│   │   └── credit.js     # 信用分相关接口
│   ├── components/       # 公共组件
│   │   └── Layout.vue    # 布局组件
│   ├── stores/           # Pinia 状态管理
│   │   ├── user.js       # 用户状态
│   │   └── websocket.js  # WebSocket 管理
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── Register.vue  # 注册页
│   │   ├── Home.vue      # 首页
│   │   ├── Events.vue    # 事件历史
│   │   ├── Beacon.vue    # 信标管理
│   │   ├── Credit.vue    # 信用记录
│   │   └── Profile.vue   # 个人中心
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义
│   ├── App.vue           # 根组件
│   ├── main.js           # 入口文件
│   └── style.css         # 全局样式
├── index.html            # HTML 模板
├── vite.config.js        # Vite 配置
├── tailwind.config.js    # TailwindCSS 配置
├── postcss.config.js     # PostCSS 配置
└── package.json          # 项目依赖

```

## 核心功能

### 1. 用户管理
- ✅ 用户注册（校园 IP 校验）
- ✅ 用户登录（JWT 认证）
- ✅ 校园位置绑定
- ✅ 信用分查询

### 2. 事件管理
- ✅ 发起拼单/约伴事件
- ✅ 查询附近事件（1公里内）
- ✅ 参与/退出事件
- ✅ 事件历史记录

### 3. 信标功能
- ✅ 发布占位信标
- ✅ 查看附近信标
- ✅ 举报虚假信标

### 4. 信用分体系
- ✅ 信用分实时显示
- ✅ 信用分变更记录
- ✅ 信用分规则说明

### 5. 实时通知
- ✅ WebSocket 连接管理
- ✅ 事件满员通知
- ✅ 事件结算通知
- ✅ 新成员加入提醒

## API 接口说明

### 用户接口 (`/api/user`)
- `POST /register` - 用户注册
- `POST /login` - 用户登录
- `PUT /location` - 绑定位置
- `GET /credit` - 查询信用分

### 事件接口 (`/api/event`)
- `POST /create` - 创建事件
- `GET /nearby` - 查询附近事件
- `POST /join` - 参与事件
- `POST /quit` - 退出事件
- `GET /history` - 事件历史

### 信标接口 (`/api/beacon`)
- `POST /publish` - 发布信标
- `POST /report` - 举报信标

### 信用分接口 (`/api/credit`)
- `GET /records` - 信用分记录

### WebSocket 接口
- `ws://localhost:8080/ws/{userId}` - WebSocket 连接

## 安装和运行

### 前置要求
- Node.js >= 16.0.0
- npm >= 8.0.0
- 后端服务运行在 http://localhost:8080

### 安装依赖
```bash
cd frontend
npm install
```

### 开发模式
```bash
npm run dev
```
访问 http://localhost:3000

### 生产构建
```bash
npm run build
```

### 预览生产构建
```bash
npm run preview
```

## 开发说明

### 状态管理
使用 Pinia 进行状态管理，主要包括：
- `useUserStore` - 用户信息、登录状态、信用分
- `useWebSocketStore` - WebSocket 连接状态、消息处理

### 路由守卫
- 未登录用户访问需要认证的页面会自动跳转到登录页
- 已登录用户访问登录/注册页会自动跳转到首页

### WebSocket 连接
- 登录成功后自动连接 WebSocket
- 断线自动重连（5秒间隔）
- 心跳保活（30秒间隔）

### 请求拦截
- 自动添加 JWT Token 到请求头
- 统一错误处理
- 401 自动跳转登录页

## 注意事项

1. **校园 IP 校验**：注册功能需要在校园网络环境下使用
2. **信用分限制**：信用分低于 60 分将限制发起事件
3. **退出限制**：参与事件后 10 分钟内可退出，退出会扣除信用分
4. **信标诚信**：发布虚假信标会被扣除信用分

## 浏览器支持

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

## 开发团队

校园闪配开发团队

## 许可证

MIT License
# 校园闪配前端 - 快速启动指南

## 一、环境准备

### 1. 安装 Node.js
确保已安装 Node.js 16.0 或更高版本：
```bash
node --version
npm --version
```

### 2. 确认后端服务运行
确保后端服务已启动并运行在 `http://localhost:8080`

## 二、安装依赖

进入前端目录并安装依赖：
```bash
cd frontend
npm install
```

## 三、启动开发服务器

```bash
npm run dev
```

启动成功后，访问：http://localhost:3000

## 四、功能测试流程

### 1. 用户注册
- 访问注册页面
- 输入学号、昵称、密码
- 点击注册（需要校园网络环境）

### 2. 用户登录
- 输入学号和密码
- 登录成功后自动跳转到首页
- WebSocket 自动连接

### 3. 发起事件
- 在首页点击"发起拼单"或"发起约伴"
- 填写事件信息（标题、人数、时长、点位）
- 创建成功后可在附近事件中看到

### 4. 参与事件
- 在首页查看附近事件列表
- 点击"参与"按钮加入事件
- 事件满员时会收到实时通知

### 5. 发布信标
- 点击"发布信标"按钮
- 填写位置描述和有效时长
- 发布成功后在信标页面可见

### 6. 查看信用分
- 在个人中心查看当前信用分
- 在信用记录页面查看变更历史

## 五、常见问题

### Q1: 无法连接后端服务
**A:** 检查后端服务是否启动，确认端口为 8080

### Q2: WebSocket 连接失败
**A:** 
- 检查后端 WebSocket 配置
- 确认用户已登录
- 查看浏览器控制台错误信息

### Q3: 注册时提示 IP 校验失败
**A:** 注册功能需要在校园网络环境下使用，或修改后端 IP 校验配置

### Q4: 页面样式异常
**A:** 
- 清除浏览器缓存
- 重新安装依赖：`rm -rf node_modules && npm install`
- 重启开发服务器

## 六、开发建议

### 1. 代码规范
- 使用 Composition API
- 组件命名使用 PascalCase
- 文件命名使用 kebab-case

### 2. 状态管理
- 全局状态使用 Pinia
- 组件内状态使用 ref/reactive

### 3. API 调用
- 统一使用 api 目录下的封装方法
- 错误处理在拦截器中统一处理

### 4. 样式开发
- 优先使用 TailwindCSS 工具类
- 组件特定样式使用 scoped style

## 七、生产部署

### 1. 构建生产版本
```bash
npm run build
```

### 2. 预览生产构建
```bash
npm run preview
```

### 3. 部署到服务器
将 `dist` 目录部署到 Nginx 或其他 Web 服务器

### Nginx 配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /path/to/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
    }
    
    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

## 八、技术支持

如有问题，请查看：
- 项目 README.md
- 后端 API 文档
- Element Plus 官方文档：https://element-plus.org/
- Vue 3 官方文档：https://cn.vuejs.org/

# 校园闪配前端 - 安装验证指南

## 📋 文件清单

### ✅ 配置文件
- [x] `package.json` - 项目依赖配置
- [x] `vite.config.js` - Vite 构建配置
- [x] `tailwind.config.js` - TailwindCSS 配置
- [x] `postcss.config.js` - PostCSS 配置
- [x] `index.html` - HTML 入口
- [x] `.gitignore` - Git 忽略文件

### ✅ 源代码文件
#### API 层 (5个文件)
- [x] `src/api/request.js` - Axios 配置
- [x] `src/api/user.js` - 用户接口
- [x] `src/api/event.js` - 事件接口
- [x] `src/api/beacon.js` - 信标接口
- [x] `src/api/credit.js` - 信用分接口

#### 状态管理 (2个文件)
- [x] `src/stores/user.js` - 用户状态
- [x] `src/stores/websocket.js` - WebSocket 管理

#### 路由 (1个文件)
- [x] `src/router/index.js` - 路由配置

#### 组件 (1个文件)
- [x] `src/components/Layout.vue` - 主布局

#### 页面 (7个文件)
- [x] `src/views/Login.vue` - 登录页
- [x] `src/views/Register.vue` - 注册页
- [x] `src/views/Home.vue` - 首页
- [x] `src/views/Events.vue` - 事件历史
- [x] `src/views/Beacon.vue` - 信标管理
- [x] `src/views/Credit.vue` - 信用记录
- [x] `src/views/Profile.vue` - 个人中心

#### 入口文件 (3个文件)
- [x] `src/App.vue` - 根组件
- [x] `src/main.js` - 入口 JS
- [x] `src/style.css` - 全局样式

### ✅ 文档文件
- [x] `README.md` - 项目文档
- [x] `SETUP.md` - 快速启动
- [x] `API_MAPPING.md` - API 映射
- [x] `INSTALLATION.md` - 本文档

## 🚀 安装步骤

### 第一步：检查环境
```bash
# 检查 Node.js 版本（需要 >= 16.0.0）
node --version

# 检查 npm 版本（需要 >= 8.0.0）
npm --version
```

### 第二步：进入项目目录
```bash
cd "d:\java project\campus-flash-match\frontend"
```

### 第三步：安装依赖
```bash
npm install
```

预期输出：
```
added XXX packages in XXs
```

### 第四步：验证安装
```bash
# 检查 node_modules 是否存在
dir node_modules

# 检查关键依赖
npm list vue
npm list element-plus
npm list axios
npm list pinia
```

## 🔍 验证步骤

### 1. 启动开发服务器
```bash
npm run dev
```

预期输出：
```
  VITE v5.1.5  ready in XXX ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
  ➜  press h to show help
```

### 2. 浏览器访问
打开浏览器访问：http://localhost:3000

应该看到登录页面，包含：
- 标题："校园闪配"
- 学号输入框
- 密码输入框
- 登录按钮
- "还没有账号？立即注册"链接

### 3. 检查控制台
按 F12 打开浏览器开发者工具，检查：
- 无 JavaScript 错误
- 无 CSS 加载错误
- 无 404 资源错误

### 4. 测试路由
手动访问以下路径，确认页面正常：
- http://localhost:3000/login - 登录页
- http://localhost:3000/register - 注册页
- http://localhost:3000/home - 应重定向到登录页（未登录）

## 🧪 功能测试

### 测试 1：注册功能
1. 访问注册页面
2. 填写表单：
   - 学号：20210001
   - 昵称：测试用户
   - 密码：123456
   - 确认密码：123456
3. 点击注册按钮
4. 预期：显示成功消息（或 IP 校验失败提示）

### 测试 2：登录功能
1. 访问登录页面
2. 填写表单：
   - 学号：已注册的学号
   - 密码：对应密码
3. 点击登录按钮
4. 预期：跳转到首页

### 测试 3：首页功能
登录后应该看到：
- 顶部导航栏（显示用户昵称和信用分）
- 左侧菜单栏
- 快速操作区（发起拼单、发起约伴、发布信标）
- 附近事件列表

### 测试 4：WebSocket 连接
1. 登录成功后
2. 打开浏览器控制台
3. 查看 Network → WS 标签
4. 应该看到 WebSocket 连接成功

## ⚠️ 常见问题

### 问题 1：npm install 失败
**解决方案：**
```bash
# 清除缓存
npm cache clean --force

# 删除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

### 问题 2：端口 3000 被占用
**解决方案：**
修改 `vite.config.js`：
```javascript
server: {
  port: 3001, // 改为其他端口
  // ...
}
```

### 问题 3：无法连接后端
**检查项：**
1. 后端服务是否启动（http://localhost:8080）
2. 检查 `vite.config.js` 中的 proxy 配置
3. 查看浏览器控制台网络请求

### 问题 4：Element Plus 样式不显示
**解决方案：**
检查 `src/main.js` 中是否正确导入：
```javascript
import 'element-plus/dist/index.css'
```

### 问题 5：WebSocket 连接失败
**检查项：**
1. 用户是否已登录
2. 后端 WebSocket 服务是否启动
3. 浏览器是否支持 WebSocket
4. 检查控制台错误信息

## 📊 性能检查

### 开发模式
```bash
npm run dev
```
- 启动时间：< 3秒
- 热更新：< 1秒

### 生产构建
```bash
npm run build
```
- 构建时间：< 30秒
- 输出目录：`dist/`
- 文件大小：< 2MB

### 预览构建
```bash
npm run preview
```
访问：http://localhost:4173

## ✅ 验证清单

完成以下检查项，确认项目正常运行：

### 环境检查
- [ ] Node.js 版本 >= 16.0.0
- [ ] npm 版本 >= 8.0.0
- [ ] 后端服务运行正常

### 安装检查
- [ ] npm install 成功
- [ ] node_modules 目录存在
- [ ] 所有依赖安装完成

### 启动检查
- [ ] npm run dev 成功启动
- [ ] 浏览器能访问 http://localhost:3000
- [ ] 无控制台错误

### 功能检查
- [ ] 登录页面显示正常
- [ ] 注册页面显示正常
- [ ] 路由跳转正常
- [ ] API 请求正常
- [ ] WebSocket 连接正常

### UI 检查
- [ ] Element Plus 组件显示正常
- [ ] TailwindCSS 样式生效
- [ ] 响应式布局正常
- [ ] 图标显示正常

## 🎯 下一步

验证通过后，可以：
1. 阅读 `README.md` 了解项目详情
2. 查看 `API_MAPPING.md` 了解 API 对接
3. 参考 `SETUP.md` 进行开发
4. 开始功能测试和开发

## 📞 技术支持

如遇到问题：
1. 检查本文档的常见问题部分
2. 查看浏览器控制台错误信息
3. 检查后端服务日志
4. 参考项目文档

---

**祝开发顺利！** 🎉

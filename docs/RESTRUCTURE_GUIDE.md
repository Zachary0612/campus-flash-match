# 🏗️ 项目结构重构指南

## 📊 当前结构问题

### 问题 1：根目录混乱
- ❌ 有旧的 `package.json`（Node.js 服务器）
- ❌ 有旧的 `server.js`（不再需要）
- ❌ 前后端文件混在一起

### 问题 2：启动混乱
- 容易在错误的目录运行命令
- 不清楚哪个是前端，哪个是后端

## 🎯 重构方案

### 方案 A：保持当前结构，清理冗余文件（推荐）

**优点：** 改动最小，不影响现有配置

```
campus-flash-match/
├── src/                        # 后端源码（Spring Boot）
├── frontend/                   # 前端项目（Vue 3）
├── pom.xml                     # Maven 配置
├── README.md                   # 项目文档
└── START.md                    # 启动指南
```

**需要删除的文件：**
- `package.json`（根目录的，保留 frontend 里的）
- `package-lock.json`（根目录的）
- `server.js`（旧的 Node.js 服务器）
- `run.cmd`（如果不需要）

### 方案 B：完全分离前后端（标准做法）

**优点：** 结构最清晰，适合团队协作

```
campus-flash-match/
├── backend/                    # 后端项目
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── ...
├── frontend/                   # 前端项目
│   ├── src/
│   ├── package.json
│   └── ...
├── docs/                       # 文档
│   ├── API.md
│   ├── SETUP.md
│   └── ...
└── README.md                   # 总文档
```

## 🚀 推荐执行方案 A（最小改动）

### 步骤 1：清理根目录冗余文件

```powershell
# 删除旧的 Node.js 相关文件
Remove-Item "d:\java project\campus-flash-match\package.json"
Remove-Item "d:\java project\campus-flash-match\package-lock.json"
Remove-Item "d:\java project\campus-flash-match\server.js"
Remove-Item "d:\java project\campus-flash-match\run.cmd"
```

### 步骤 2：整理文档

```powershell
# 创建 docs 目录
New-Item -ItemType Directory -Path "d:\java project\campus-flash-match\docs"

# 移动文档文件
Move-Item "d:\java project\campus-flash-match\START.md" "d:\java project\campus-flash-match\docs\"
Move-Item "d:\java project\campus-flash-match\FRONTEND_SUMMARY.md" "d:\java project\campus-flash-match\docs\"
Move-Item "d:\java project\campus-flash-match\HELP.md" "d:\java project\campus-flash-match\docs\"
```

### 步骤 3：更新 README.md

创建清晰的项目说明文档。

### 步骤 4：创建启动脚本

创建统一的启动脚本，避免混淆。

## 📝 重构后的目录结构

```
campus-flash-match/
│
├── src/                        # 后端源码
│   ├── main/
│   │   ├── java/
│   │   │   └── com/campus/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── entity/
│   │   │       ├── config/
│   │   │       └── ...
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── schema.sql
│   │       └── data.sql
│   └── test/
│
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/               # API 接口
│   │   ├── components/        # 组件
│   │   ├── stores/            # 状态管理
│   │   ├── views/             # 页面
│   │   ├── router/            # 路由
│   │   ├── App.vue
│   │   └── main.js
│   ├── public/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── README.md
│
├── docs/                       # 文档目录
│   ├── START.md               # 启动指南
│   ├── API_MAPPING.md         # API 文档
│   ├── FRONTEND_SUMMARY.md    # 前端总结
│   └── SETUP.md               # 安装指南
│
├── scripts/                    # 脚本目录
│   ├── start-backend.cmd      # 启动后端
│   ├── start-frontend.cmd     # 启动前端
│   └── start-all.ps1          # 启动全部
│
├── .gitignore                 # Git 忽略文件
├── pom.xml                    # Maven 配置
├── mvnw                       # Maven Wrapper
├── README.md                  # 项目主文档
└── LICENSE                    # 许可证
```

## 🎯 启动命令标准化

### 后端启动
```bash
# 在项目根目录
.\mvnw spring-boot:run
```

### 前端启动
```bash
# 进入前端目录
cd frontend
npm run dev
```

## ✅ 重构检查清单

- [ ] 删除根目录的 `package.json`
- [ ] 删除根目录的 `server.js`
- [ ] 创建 `docs` 目录
- [ ] 移动文档文件到 `docs`
- [ ] 创建启动脚本
- [ ] 更新 README.md
- [ ] 更新 .gitignore
- [ ] 测试后端启动
- [ ] 测试前端启动
- [ ] 验证功能正常

## 🚨 注意事项

1. **备份重要文件**：重构前先备份
2. **逐步执行**：不要一次性删除所有文件
3. **测试验证**：每一步后都要测试
4. **Git 提交**：重构完成后提交代码

## 📚 相关文档

- [前端文档](../frontend/README.md)
- [API 文档](./API_MAPPING.md)
- [启动指南](./START.md)

# 前后端 API 映射文档

本文档详细说明了前端如何调用后端的各个 API 接口。

## 一、用户模块 (UserController)

### 1. 用户注册
**后端接口：** `POST /api/user/register`

**前端调用：**
```javascript
// 文件：src/api/user.js
import { register } from '@/api/user'

// 使用示例：src/views/Register.vue
const success = await userStore.register({
  studentId: '20210001',
  nickname: '张三',
  password: '123456'
})
```

**请求参数：**
```json
{
  "studentId": "学号",
  "nickname": "昵称",
  "password": "密码"
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": "token字符串"
}
```

---

### 2. 用户登录
**后端接口：** `POST /api/user/login`

**前端调用：**
```javascript
// 文件：src/api/user.js
import { login } from '@/api/user'

// 使用示例：src/views/Login.vue
const success = await userStore.login({
  studentId: '20210001',
  password: '123456'
})
```

**请求参数：**
```json
{
  "studentId": "学号",
  "password": "密码"
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "token": "jwt_token",
    "nickname": "张三",
    "creditScore": 80
  }
}
```

---

### 3. 绑定校园位置
**后端接口：** `PUT /api/user/location?pointId={pointId}`

**前端调用：**
```javascript
// 文件：src/api/user.js
import { bindLocation } from '@/api/user'

// 使用示例：src/views/Profile.vue
await bindLocation(1) // 1 为点位ID
```

**请求参数：** URL参数 `pointId`

**响应数据：**
```json
{
  "code": 200,
  "message": "位置绑定成功",
  "data": null
}
```

---

### 4. 查询信用分
**后端接口：** `GET /api/user/credit`

**前端调用：**
```javascript
// 文件：src/api/user.js
import { getCreditInfo } from '@/api/user'

// 使用示例：src/views/Credit.vue
const res = await getCreditInfo()
```

**响应数据：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userId": 1,
    "currentCredit": 85,
    "recentRecords": [...]
  }
}
```

---

## 二、事件模块 (EventController)

### 1. 发起事件
**后端接口：** `POST /api/event/create`

**前端调用：**
```javascript
// 文件：src/api/event.js
import { createEvent } from '@/api/event'

// 使用示例：src/views/Home.vue
const res = await createEvent({
  title: '食堂拼单',
  eventType: 'group_buy',
  targetNum: 4,
  expireMinutes: 60,
  pointId: 2,
  extMeta: {}
})
```

**请求参数：**
```json
{
  "title": "事件标题",
  "eventType": "group_buy|meetup|beacon",
  "targetNum": 4,
  "expireMinutes": 60,
  "pointId": 2,
  "extMeta": {}
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "事件发起成功",
  "data": "event_id_string"
}
```

---

### 2. 查询附近事件
**后端接口：** `GET /api/event/nearby?eventType={type}&radius={radius}`

**前端调用：**
```javascript
// 文件：src/api/event.js
import { getNearbyEvents } from '@/api/event'

// 使用示例：src/views/Home.vue
const res = await getNearbyEvents('group_buy', 1000)
```

**请求参数：**
- `eventType`: 事件类型（可选）
- `radius`: 半径（米，默认1000）

**响应数据：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "eventId": "event_123",
      "eventType": "group_buy",
      "title": "食堂拼单",
      "distance": 150,
      "currentNum": 2,
      "targetNum": 4,
      "createTime": "2024-01-01T12:00:00"
    }
  ]
}
```

---

### 3. 参与事件
**后端接口：** `POST /api/event/join?eventId={eventId}`

**前端调用：**
```javascript
// 文件：src/api/event.js
import { joinEvent } from '@/api/event'

// 使用示例：src/views/Home.vue
const res = await joinEvent('event_123')
```

**请求参数：** URL参数 `eventId`

**响应数据：**
```json
{
  "code": 200,
  "message": "参与成功",
  "data": {
    "currentParticipants": 3,
    "maxParticipants": 4
  }
}
```

---

### 4. 退出事件
**后端接口：** `POST /api/event/quit?eventId={eventId}`

**前端调用：**
```javascript
// 文件：src/api/event.js
import { quitEvent } from '@/api/event'

// 使用示例：src/views/Events.vue
const res = await quitEvent('event_123')
```

**请求参数：** URL参数 `eventId`

**响应数据：**
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

---

### 5. 查询事件历史
**后端接口：** `GET /api/event/history?pageNum={num}&pageSize={size}`

**前端调用：**
```javascript
// 文件：src/api/event.js
import { getEventHistory } from '@/api/event'

// 使用示例：src/views/Events.vue
const res = await getEventHistory(1, 10)
```

**请求参数：**
- `pageNum`: 页码（默认1）
- `pageSize`: 每页数量（默认10）

**响应数据：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "eventId": "event_123",
      "eventType": "group_buy",
      "title": "食堂拼单",
      "targetNum": 4,
      "currentNum": 4,
      "status": "settled",
      "createTime": "2024-01-01T12:00:00",
      "settleTime": "2024-01-01T13:00:00"
    }
  ]
}
```

---

## 三、信标模块 (BeaconController)

### 1. 发布信标
**后端接口：** `POST /api/beacon/publish`

**前端调用：**
```javascript
// 文件：src/api/beacon.js
import { publishBeacon } from '@/api/beacon'

// 使用示例：src/views/Beacon.vue
const res = await publishBeacon({
  locationDesc: '图书馆3楼靠窗座位',
  expireMinutes: 120,
  pointId: 1
})
```

**请求参数：**
```json
{
  "locationDesc": "位置描述",
  "expireMinutes": 120,
  "pointId": 1
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "信标发布成功",
  "data": "beacon_id_string"
}
```

---

### 2. 举报虚假信标
**后端接口：** `POST /api/beacon/report?eventId={eventId}`

**前端调用：**
```javascript
// 文件：src/api/beacon.js
import { reportBeacon } from '@/api/beacon'

// 使用示例：src/views/Beacon.vue
const res = await reportBeacon('event_123')
```

**请求参数：** URL参数 `eventId`

**响应数据：**
```json
{
  "code": 200,
  "message": "举报成功，已扣除发布者信用分",
  "data": null
}
```

---

## 四、信用分模块 (CreditController)

### 1. 查询信用分记录
**后端接口：** `GET /api/credit/records?pageNum={num}&pageSize={size}`

**前端调用：**
```javascript
// 文件：src/api/credit.js
import { getCreditRecords } from '@/api/credit'

// 使用示例：src/views/Credit.vue
const res = await getCreditRecords(1, 10)
```

**请求参数：**
- `pageNum`: 页码（默认1）
- `pageSize`: 每页数量（默认10）

**响应数据：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "recordId": 1,
      "eventId": "event_123",
      "changeScore": 2,
      "reason": "成功参与事件",
      "createTime": 1704096000000
    }
  ]
}
```

---

## 五、WebSocket 实时通知

### 连接方式
**WebSocket URL：** `ws://localhost:8080/ws/{userId}`

**前端实现：**
```javascript
// 文件：src/stores/websocket.js
const wsUrl = `ws://localhost:8080/ws/${userStore.userId}`
ws.value = new WebSocket(wsUrl)
```

### 消息格式

#### 1. 连接成功
```json
{
  "type": "connected",
  "timestamp": 1704096000000,
  "data": "WebSocket连接成功"
}
```

#### 2. 事件满员通知
```json
{
  "type": "event_full",
  "timestamp": 1704096000000,
  "data": {
    "eventId": "event_123",
    "message": "您参与的事件已满员"
  }
}
```

#### 3. 事件结算通知
```json
{
  "type": "event_settled",
  "timestamp": 1704096000000,
  "data": {
    "eventId": "event_123",
    "message": "事件已结算",
    "creditChange": 2
  }
}
```

#### 4. 新成员加入
```json
{
  "type": "new_participant",
  "timestamp": 1704096000000,
  "data": {
    "eventId": "event_123",
    "message": "有新成员加入事件"
  }
}
```

#### 5. 心跳包
**客户端发送：**
```json
{
  "type": "ping"
}
```

**服务端响应：**
```json
{
  "type": "pong",
  "timestamp": 1704096000000,
  "data": null
}
```

#### 6. 订阅事件
**客户端发送：**
```json
{
  "type": "subscribe_event",
  "eventId": "event_123"
}
```

---

## 六、请求拦截器配置

### Token 自动添加
```javascript
// 文件：src/api/request.js
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

### 错误统一处理
```javascript
// 文件：src/api/request.js
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
  },
  error => {
    // 401 自动跳转登录页
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

---

## 七、状态管理

### 用户状态 (Pinia)
```javascript
// 文件：src/stores/user.js
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  
  // 登录
  async function login(loginForm) { ... }
  
  // 注册
  async function register(registerForm) { ... }
  
  // 登出
  function logout() { ... }
  
  return { token, userInfo, login, register, logout }
})
```

### WebSocket 状态 (Pinia)
```javascript
// 文件：src/stores/websocket.js
export const useWebSocketStore = defineStore('websocket', () => {
  const ws = ref(null)
  const connected = ref(false)
  
  // 连接
  function connect() { ... }
  
  // 断开
  function disconnect() { ... }
  
  // 发送消息
  function send(message) { ... }
  
  return { connected, connect, disconnect, send }
})
```

---

## 八、路由配置

```javascript
// 文件：src/router/index.js
const routes = [
  { path: '/login', component: Login, meta: { requiresAuth: false } },
  { path: '/register', component: Register, meta: { requiresAuth: false } },
  { path: '/home', component: Home, meta: { requiresAuth: true } },
  { path: '/events', component: Events, meta: { requiresAuth: true } },
  { path: '/beacon', component: Beacon, meta: { requiresAuth: true } },
  { path: '/credit', component: Credit, meta: { requiresAuth: true } },
  { path: '/profile', component: Profile, meta: { requiresAuth: true } }
]

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})
```

---

## 九、环境配置

### 开发环境
- 前端：http://localhost:3000
- 后端：http://localhost:8080
- WebSocket：ws://localhost:8080/ws/{userId}

### Vite 代理配置
```javascript
// 文件：vite.config.js
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    }
  }
})
```

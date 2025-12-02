# 校园闪聊匹配系统 - 新增功能API文档

## 概述

本文档描述了新增的功能模块及其API接口。

---

## 1. 用户个人资料模块 (`/api/profile`)

### 1.1 获取当前用户资料
- **URL**: `GET /api/profile/me`
- **认证**: 需要
- **响应**: `UserProfileVO`

### 1.2 获取指定用户资料（他人主页）
- **URL**: `GET /api/profile/{userId}`
- **认证**: 可选
- **响应**: `UserProfileVO`

### 1.3 更新个人资料
- **URL**: `PUT /api/profile/update`
- **认证**: 需要
- **请求体**:
```json
{
  "nickname": "新昵称",
  "bio": "个人简介",
  "gender": 1,
  "major": "计算机科学",
  "grade": "大三",
  "interests": "编程,篮球,音乐"
}
```

### 1.4 上传头像
- **URL**: `POST /api/profile/avatar`
- **认证**: 需要
- **请求**: `multipart/form-data`, 参数名 `file`
- **响应**: 头像URL

### 1.5 获取用户统计数据
- **URL**: `GET /api/profile/statistics`
- **认证**: 需要
- **响应**: `UserStatisticsVO`

---

## 2. 消息通知模块 (`/api/notification`)

### 2.1 获取通知列表
- **URL**: `GET /api/notification/list`
- **认证**: 需要
- **参数**:
  - `type` (可选): 通知类型
  - `pageNum` (默认1)
  - `pageSize` (默认20)
- **响应**: `List<NotificationVO>`

### 2.2 获取未读消息数
- **URL**: `GET /api/notification/unread-count`
- **认证**: 需要
- **响应**: `Integer`

### 2.3 标记消息已读
- **URL**: `POST /api/notification/read`
- **认证**: 需要
- **参数**:
  - `notificationId` (可选): 不传则标记全部已读

---

## 3. 用户关注模块 (`/api/follow`)

### 3.1 关注用户
- **URL**: `POST /api/follow/{userId}`
- **认证**: 需要

### 3.2 取消关注
- **URL**: `DELETE /api/follow/{userId}`
- **认证**: 需要

### 3.3 获取关注列表
- **URL**: `GET /api/follow/following`
- **认证**: 需要
- **参数**: `pageNum`, `pageSize`
- **响应**: `List<UserFollowVO>`

### 3.4 获取粉丝列表
- **URL**: `GET /api/follow/followers`
- **认证**: 需要
- **参数**: `pageNum`, `pageSize`
- **响应**: `List<UserFollowVO>`

### 3.5 检查是否已关注
- **URL**: `GET /api/follow/check/{userId}`
- **认证**: 需要
- **响应**: `Boolean`

---

## 4. 事件评价模块 (`/api/rating`)

### 4.1 提交评价
- **URL**: `POST /api/rating/submit`
- **认证**: 需要
- **请求体**:
```json
{
  "eventId": "event_xxx",
  "ratedUserId": 123,
  "score": 5,
  "comment": "非常靠谱的队友",
  "tags": "准时,友好,靠谱"
}
```

### 4.2 获取收到的评价
- **URL**: `GET /api/rating/received`
- **认证**: 需要
- **参数**: `pageNum`, `pageSize`
- **响应**: `List<EventRatingVO>`

### 4.3 获取发出的评价
- **URL**: `GET /api/rating/given`
- **认证**: 需要
- **响应**: `List<EventRatingVO>`

### 4.4 获取事件的评价列表
- **URL**: `GET /api/rating/event/{eventId}`
- **认证**: 不需要
- **响应**: `List<EventRatingVO>`

### 4.5 获取用户平均评分
- **URL**: `GET /api/rating/average/{userId}`
- **认证**: 不需要
- **响应**: `Double`

### 4.6 检查是否可以评价
- **URL**: `GET /api/rating/can-rate`
- **认证**: 需要
- **参数**: `eventId`, `ratedUserId`
- **响应**: `Boolean`

---

## 5. 事件收藏模块 (`/api/favorite`)

### 5.1 收藏事件
- **URL**: `POST /api/favorite/{eventId}`
- **认证**: 需要

### 5.2 取消收藏
- **URL**: `DELETE /api/favorite/{eventId}`
- **认证**: 需要

### 5.3 获取收藏列表
- **URL**: `GET /api/favorite/list`
- **认证**: 需要
- **参数**: `pageNum`, `pageSize`
- **响应**: `List<EventFavoriteVO>`

### 5.4 检查是否已收藏
- **URL**: `GET /api/favorite/check/{eventId}`
- **认证**: 需要
- **响应**: `Boolean`

---

## 6. 事件评论模块 (`/api/comment`)

### 6.1 发表评论
- **URL**: `POST /api/comment/add`
- **认证**: 需要
- **请求体**:
```json
{
  "eventId": "event_xxx",
  "content": "评论内容",
  "parentId": null,
  "replyToUserId": null
}
```
- **响应**: 评论ID

### 6.2 删除评论
- **URL**: `DELETE /api/comment/{commentId}`
- **认证**: 需要

### 6.3 获取事件评论列表
- **URL**: `GET /api/comment/event/{eventId}`
- **认证**: 不需要
- **参数**: `pageNum`, `pageSize`
- **响应**: `List<EventCommentVO>` (树形结构)

### 6.4 点赞评论
- **URL**: `POST /api/comment/like/{commentId}`
- **认证**: 不需要

### 6.5 获取事件评论数
- **URL**: `GET /api/comment/count/{eventId}`
- **认证**: 不需要
- **响应**: `Integer`

---

## 7. 事件扩展接口 (`/api/event`)

### 7.1 获取事件详情
- **URL**: `GET /api/event/detail`
- **认证**: 可选
- **参数**: `eventId`
- **响应**: `EventDetailVO`

### 7.2 搜索事件
- **URL**: `POST /api/event/search`
- **认证**: 不需要
- **请求体**:
```json
{
  "keyword": "搜索关键词",
  "eventType": "group_buy",
  "status": "active",
  "ownerId": null,
  "minTargetNum": 2,
  "maxTargetNum": 10,
  "startTime": "2024-01-01",
  "endTime": "2024-12-31",
  "pageNum": 1,
  "pageSize": 10,
  "sortBy": "create_time",
  "sortOrder": "desc"
}
```
- **响应**: `List<EventHistoryVO>`

### 7.3 取消事件
- **URL**: `POST /api/event/cancel`
- **认证**: 需要
- **参数**: `eventId`
- **说明**: 仅发起者可取消

---

## 通知类型说明

| 类型 | 说明 |
|------|------|
| `event_join` | 有人加入事件 |
| `event_full` | 事件满员 |
| `event_settle` | 事件结算 |
| `follow` | 被关注 |
| `comment` | 收到评论 |
| `rating` | 收到评价 |
| `system` | 系统通知 |

---

## 数据库迁移

运行 `V2__add_new_features.sql` 脚本以创建新表和字段。

---

## 注意事项

1. 所有需要认证的接口需要在请求头中携带 `Authorization: Bearer <token>`
2. 评价功能仅对已完成的事件开放
3. 用户只能评价同一事件中的其他参与者
4. 评论支持二级回复结构

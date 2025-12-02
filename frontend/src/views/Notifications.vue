<template>
  <Layout>
    <div class="notifications-page relative z-10">
      <!-- 页面标题 -->
      <div class="glass rounded-2xl p-6 mb-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up">
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <el-icon class="text-2xl mr-3 text-primary"><Bell /></el-icon>
            <h1 class="text-2xl font-bold text-gray-800">消息通知</h1>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="ml-3" />
          </div>
          <el-button type="primary" plain @click="handleMarkAllRead" :disabled="unreadCount === 0">
            全部已读
          </el-button>
        </div>
      </div>

      <!-- 通知类型筛选 -->
      <div class="glass rounded-2xl p-4 mb-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.1s">
        <el-radio-group v-model="currentType" @change="loadNotifications">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="event_join">事件加入</el-radio-button>
          <el-radio-button label="event_full">事件满员</el-radio-button>
          <el-radio-button label="event_settle">事件结算</el-radio-button>
          <el-radio-button label="follow">关注</el-radio-button>
          <el-radio-button label="comment">评论</el-radio-button>
          <el-radio-button label="rating">评价</el-radio-button>
          <el-radio-button label="system">系统</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 通知列表 -->
      <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.2s">
        <div v-if="loading" class="text-center py-12">
          <el-icon class="is-loading text-4xl text-primary"><Loading /></el-icon>
          <p class="mt-4 text-gray-500">加载中...</p>
        </div>

        <div v-else-if="notifications.length === 0" class="text-center py-12">
          <el-icon class="text-6xl text-gray-300"><BellFilled /></el-icon>
          <p class="mt-4 text-gray-500">暂无通知消息</p>
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-item p-4 rounded-xl transition-all cursor-pointer hover:shadow-md"
            :class="item.isRead ? 'bg-white/40' : 'bg-blue-50/60 border-l-4 border-primary'"
            @click="handleNotificationClick(item)"
          >
            <div class="flex items-start gap-4">
              <!-- 通知图标 -->
              <div class="notification-icon p-3 rounded-full" :class="getTypeIconClass(item.type)">
                <el-icon class="text-xl"><component :is="getTypeIcon(item.type)" /></el-icon>
              </div>

              <!-- 通知内容 -->
              <div class="flex-1">
                <div class="flex items-center justify-between mb-1">
                  <span class="font-semibold text-gray-800">{{ item.title }}</span>
                  <span class="text-xs text-gray-400">{{ formatTime(item.createTime) }}</span>
                </div>
                <p class="text-sm text-gray-600">{{ item.content }}</p>
                <div v-if="item.senderNickname" class="mt-2 text-xs text-gray-400">
                  来自: {{ item.senderNickname }}
                </div>
              </div>

              <!-- 未读标记 -->
              <div v-if="!item.isRead" class="w-2 h-2 rounded-full bg-primary animate-pulse"></div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="notifications.length > 0" class="mt-6 flex justify-center">
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadNotifications"
          />
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import { 
  Bell, BellFilled, Loading, UserFilled, ChatDotRound, 
  Star, Check, Warning, InfoFilled 
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const loading = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const currentType = ref('')
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await getNotifications({
      type: currentType.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      notifications.value = res.data || []
      total.value = res.data?.length || 0
    }
  } catch (error) {
    console.error('加载通知失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载未读数
const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('获取未读数失败:', error)
  }
}

// 点击通知
const handleNotificationClick = async (item) => {
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  
  // 根据通知类型跳转
  if (item.relatedId) {
    // 可以根据type跳转到对应页面
  }
}

// 全部已读
const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    notifications.value.forEach(item => item.isRead = true)
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('标记全部已读失败:', error)
  }
}

// 获取类型图标
const getTypeIcon = (type) => {
  const icons = {
    'event_join': UserFilled,
    'event_full': Check,
    'event_settle': Star,
    'follow': UserFilled,
    'comment': ChatDotRound,
    'rating': Star,
    'system': Warning
  }
  return icons[type] || InfoFilled
}

// 获取类型图标样式
const getTypeIconClass = (type) => {
  const classes = {
    'event_join': 'bg-blue-100 text-blue-500',
    'event_full': 'bg-green-100 text-green-500',
    'event_settle': 'bg-purple-100 text-purple-500',
    'follow': 'bg-pink-100 text-pink-500',
    'comment': 'bg-yellow-100 text-yellow-600',
    'rating': 'bg-orange-100 text-orange-500',
    'system': 'bg-gray-100 text-gray-500'
  }
  return classes[type] || 'bg-gray-100 text-gray-500'
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).fromNow()
}

onMounted(() => {
  loadNotifications()
  loadUnreadCount()
})
</script>

<style scoped>
.notifications-page {
  max-width: 800px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.notification-item:hover {
  transform: translateX(4px);
}
</style>

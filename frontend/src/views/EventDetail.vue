<template>
  <Layout>
    <div class="event-detail-page relative z-10">
      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-20">
        <el-icon class="is-loading text-5xl text-primary"><Loading /></el-icon>
        <p class="mt-4 text-gray-500">加载中...</p>
      </div>

      <template v-else-if="event">
        <!-- 事件头部 -->
        <div class="glass rounded-2xl p-8 mb-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up">
          <div class="flex justify-between items-start mb-6">
            <div>
              <div class="flex items-center gap-3 mb-3">
                <el-tag :type="event.eventType === 'group_buy' ? 'success' : 'primary'" size="large">
                  {{ event.eventType === 'group_buy' ? '拼单' : '约伴' }}
                </el-tag>
                <el-tag :type="getStatusTag(event.status)">{{ getStatusName(event.status) }}</el-tag>
              </div>
              <h1 class="text-3xl font-bold text-gray-800 mb-2">{{ event.title }}</h1>
              <p v-if="event.description" class="text-gray-600">{{ event.description }}</p>
            </div>

            <!-- 操作按钮 -->
            <div class="flex gap-3">
              <el-button
                :type="isFavorite ? 'warning' : 'default'"
                :icon="isFavorite ? StarFilled : Star"
                @click="handleFavorite"
                :loading="favoriteLoading"
              >
                {{ isFavorite ? '已收藏' : '收藏' }}
              </el-button>
              
              <template v-if="event.status === 'active'">
                <el-button
                  v-if="isOwner"
                  type="danger"
                  @click="handleCancelEvent"
                >
                  取消事件
                </el-button>
                <el-button
                  v-else-if="!isJoined"
                  type="primary"
                  @click="handleJoinEvent"
                  :loading="joinLoading"
                >
                  参与事件
                </el-button>
                <el-button
                  v-else
                  type="warning"
                  @click="handleQuitEvent"
                  :loading="quitLoading"
                >
                  退出事件
                </el-button>
              </template>
              
              <!-- 待确认状态：显示确认按钮 -->
              <template v-if="event.status === 'pending_confirm' && (isOwner || isJoined)">
                <el-button
                  v-if="!confirmationStatus.currentUserConfirmed"
                  type="success"
                  @click="handleConfirmEvent"
                  :loading="confirmLoading"
                >
                  <el-icon class="mr-1"><Check /></el-icon>
                  确认完成
                </el-button>
                <el-tag v-else type="success" effect="plain" size="large">
                  <el-icon class="mr-1"><Check /></el-icon>
                  已确认
                </el-tag>
              </template>
            </div>
          </div>
          
          <!-- 待确认状态提示 -->
          <div v-if="event.status === 'pending_confirm'" class="mt-4 bg-orange-50 border border-orange-200 rounded-lg p-4">
            <div class="flex items-center text-orange-600">
              <el-icon class="mr-2 text-xl"><Bell /></el-icon>
              <span class="font-medium">事件已满员，等待所有成员确认完成</span>
            </div>
            <div class="mt-2 text-sm text-orange-500">
              确认进度：{{ confirmationStatus.confirmedCount }}/{{ confirmationStatus.totalCount }} 人已确认
            </div>
          </div>

          <!-- 集合地点 -->
          <div v-if="eventLocation" class="mb-6 bg-blue-50/80 border border-blue-100 rounded-xl p-4">
            <div class="flex items-start">
              <el-icon class="mr-3 mt-1 text-blue-500 text-xl"><Location /></el-icon>
              <div class="flex-1">
                <div class="text-sm font-semibold text-blue-700 mb-1">集合地点</div>
                <div class="text-blue-900 font-medium">{{ eventLocation }}</div>
              </div>
            </div>
          </div>

          <!-- 事件信息 -->
          <div class="grid grid-cols-4 gap-6 bg-white/40 rounded-xl p-6">
            <div class="text-center">
              <div class="text-2xl font-bold text-primary">{{ event.currentNum }}/{{ event.targetNum }}</div>
              <div class="text-sm text-gray-500 mt-1">参与人数</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-gray-800">{{ event.expireMinutes }}分钟</div>
              <div class="text-sm text-gray-500 mt-1">有效时长</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-gray-800">{{ commentCount }}</div>
              <div class="text-sm text-gray-500 mt-1">评论数</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-gray-800">{{ event.favoriteCount || 0 }}</div>
              <div class="text-sm text-gray-500 mt-1">收藏数</div>
            </div>
          </div>
        </div>

        <el-row :gutter="20">
          <!-- 左侧：发起者和参与者 -->
          <el-col :span="8">
            <!-- 发起者信息 -->
            <div class="glass rounded-2xl p-6 mb-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.1s">
              <h3 class="font-bold text-gray-800 mb-4 flex items-center">
                <el-icon class="mr-2 text-primary"><User /></el-icon>
                发起者
              </h3>
              <div class="flex items-center gap-4 cursor-pointer hover:opacity-80" @click="goToProfile(event.ownerId)">
                <el-avatar :size="50" :src="event.ownerAvatar">
                  {{ event.ownerNickname?.charAt(0) }}
                </el-avatar>
                <div>
                  <div class="font-medium text-gray-800">{{ event.ownerNickname }}</div>
                  <div class="text-sm text-gray-500">
                    信用分: <span :class="getScoreClass(event.ownerCreditScore)">{{ event.ownerCreditScore }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 参与者列表 -->
            <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.15s">
              <h3 class="font-bold text-gray-800 mb-4 flex items-center">
                <el-icon class="mr-2 text-green-500"><UserFilled /></el-icon>
                参与者 ({{ event.participants?.length || 0 }})
              </h3>
              <div v-if="!event.participants?.length" class="text-center py-4 text-gray-400">
                暂无参与者
              </div>
              <div v-else class="space-y-3">
                <div
                  v-for="p in event.participants"
                  :key="p.userId"
                  class="flex items-center gap-3 p-2 rounded-lg hover:bg-white/50 cursor-pointer"
                  @click="goToProfile(p.userId)"
                >
                  <el-avatar :size="36" :src="p.avatar">{{ p.nickname?.charAt(0) }}</el-avatar>
                  <div class="flex-1">
                    <span class="text-gray-800">{{ p.nickname }}</span>
                    <el-tag v-if="p.isOwner" size="small" type="warning" class="ml-2">发起者</el-tag>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 即时交流（仅参与者可见） -->
            <div v-if="isJoined || isOwner" class="glass rounded-2xl p-6 mt-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.2s">
              <h3 class="font-bold text-gray-800 mb-4 flex items-center">
                <el-icon class="mr-2 text-orange-500"><ChatLineSquare /></el-icon>
                即时交流
                <span class="ml-2 text-xs text-gray-400">(仅参与者可见)</span>
              </h3>
              
              <!-- 聊天消息列表 -->
              <div ref="chatContainerRef" class="chat-messages h-64 overflow-y-auto bg-white/50 rounded-lg p-3 mb-3 space-y-2">
                <div v-if="chatMessages.length === 0" class="text-center py-8 text-gray-400 text-sm">
                  暂无消息，快来打个招呼吧~
                </div>
                <div
                  v-for="msg in chatMessages"
                  :key="msg.id"
                  class="flex gap-2"
                  :class="{ 'flex-row-reverse': msg.userId === userStore.userId }"
                >
                  <el-avatar :size="28" :src="msg.avatar">{{ msg.nickname?.charAt(0) }}</el-avatar>
                  <div :class="msg.userId === userStore.userId ? 'text-right' : ''">
                    <div class="text-xs text-gray-400 mb-1">
                      <span class="font-medium text-gray-600">{{ msg.nickname }}</span>
                      <span class="ml-2">{{ formatChatTime(msg.time) }}</span>
                    </div>
                    <div
                      class="inline-block px-3 py-2 rounded-lg text-sm max-w-[200px] break-words"
                      :class="msg.userId === userStore.userId ? 'bg-primary text-white' : 'bg-gray-100 text-gray-800'"
                    >
                      {{ msg.content }}
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 发送消息 -->
              <div class="flex gap-2">
                <el-input
                  v-model="chatInput"
                  placeholder="输入消息..."
                  @keyup.enter="sendChatMessage"
                  size="small"
                  class="flex-1"
                />
                <el-button type="primary" size="small" @click="sendChatMessage" :disabled="!chatInput.trim()">
                  发送
                </el-button>
              </div>
            </div>
          </el-col>

          <!-- 右侧：路线导航和评论区 -->
          <el-col :span="16">
            <!-- 路线导航（仅参与者可见） -->
            <div v-if="(isJoined || isOwner) && destinationLocation" class="glass rounded-2xl p-6 mb-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.18s">
              <h3 class="font-bold text-gray-800 mb-4 flex items-center justify-between">
                <div class="flex items-center">
                  <el-icon class="mr-2 text-green-500"><Position /></el-icon>
                  前往集合点
                </div>
                <el-button 
                  v-if="currentLocation" 
                  size="small" 
                  @click="getCurrentLocation" 
                  :loading="gettingLocation"
                  text
                >
                  <el-icon class="mr-1"><Aim /></el-icon>
                  重新定位
                </el-button>
              </h3>
              <div v-if="!currentLocation" class="text-center py-8">
                <el-button type="primary" @click="getCurrentLocation" :loading="gettingLocation" size="large">
                  <el-icon class="mr-2"><Aim /></el-icon>
                  获取我的位置
                </el-button>
                <p class="text-sm text-gray-400 mt-3">
                  {{ userStore.isLoggedIn ? '未绑定位置，点击获取当前位置' : '获取位置后可查看导航路线' }}
                </p>
              </div>
              <RouteMap 
                v-else
                :origin="currentLocation"
                :destination="destinationLocation"
                height="500px"
              />
            </div>
            
            <!-- 评论区 -->
            <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.2s">
              <h3 class="font-bold text-gray-800 mb-4 flex items-center">
                <el-icon class="mr-2 text-blue-500"><ChatDotRound /></el-icon>
                评论区
              </h3>

              <!-- 发表评论 -->
              <div class="mb-6">
                <el-input
                  v-model="commentContent"
                  type="textarea"
                  :rows="3"
                  placeholder="说点什么..."
                  class="mb-3"
                />
                <div class="flex justify-end">
                  <el-button type="primary" @click="handleAddComment" :loading="commentLoading" :disabled="!commentContent.trim()">
                    发表评论
                  </el-button>
                </div>
              </div>

              <!-- 评论列表 -->
              <div v-if="comments.length === 0" class="text-center py-8 text-gray-400">
                暂无评论，快来抢沙发吧~
              </div>
              <div v-else class="space-y-4">
                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                  <div class="flex gap-3">
                    <el-avatar :size="40" :src="comment.avatar" @click="goToProfile(comment.userId)" class="cursor-pointer">
                      {{ comment.nickname?.charAt(0) }}
                    </el-avatar>
                    <div class="flex-1">
                      <div class="flex items-center justify-between mb-1">
                        <span class="font-medium text-gray-800">{{ comment.nickname }}</span>
                        <span class="text-xs text-gray-400">{{ formatTime(comment.createTime) }}</span>
                      </div>
                      <p class="text-gray-600 mb-2">{{ comment.content }}</p>
                      <div class="flex items-center gap-4 text-sm text-gray-400">
                        <span class="cursor-pointer hover:text-primary flex items-center" @click="handleLikeComment(comment)">
                          <el-icon class="mr-1"><Pointer /></el-icon>
                          {{ comment.likeCount || 0 }}
                        </span>
                        <span class="cursor-pointer hover:text-primary" @click="handleReply(comment)">
                          回复
                        </span>
                      </div>

                      <!-- 回复列表 -->
                      <div v-if="comment.replies?.length" class="mt-3 pl-4 border-l-2 border-gray-100 space-y-3">
                        <div v-for="reply in comment.replies" :key="reply.id" class="flex gap-2">
                          <el-avatar :size="28" :src="reply.avatar">{{ reply.nickname?.charAt(0) }}</el-avatar>
                          <div class="flex-1">
                            <div class="text-sm">
                              <span class="font-medium text-gray-800">{{ reply.nickname }}</span>
                              <span v-if="reply.replyToNickname" class="text-gray-400">
                                回复 <span class="text-primary">@{{ reply.replyToNickname }}</span>
                              </span>
                            </div>
                            <p class="text-sm text-gray-600">{{ reply.content }}</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 加载更多 -->
              <div v-if="comments.length > 0 && hasMoreComments" class="text-center mt-4">
                <el-button text @click="loadMoreComments">加载更多</el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </template>

      <!-- 事件不存在 -->
      <div v-else class="text-center py-20">
        <el-icon class="text-6xl text-gray-300"><Warning /></el-icon>
        <p class="mt-4 text-gray-500">事件不存在或已被删除</p>
        <el-button type="primary" class="mt-4" @click="$router.push('/home')">返回首页</el-button>
      </div>

      <!-- 回复对话框 -->
      <el-dialog v-model="showReplyDialog" title="回复评论" width="400px">
        <p class="text-gray-500 mb-3">回复 @{{ replyTarget?.nickname }}</p>
        <el-input v-model="replyContent" type="textarea" :rows="3" placeholder="输入回复内容..." />
        <template #footer>
          <el-button @click="showReplyDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitReply" :loading="replyLoading">回复</el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import RouteMap from '@/components/RouteMap.vue'
import { useUserStore } from '@/stores/user'
import { getEventDetail, joinEvent, quitEvent, cancelEvent, confirmEventCompletion, getConfirmationStatus } from '@/api/event'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { getEventComments, addComment, likeComment, getCommentCount } from '@/api/comment'
import { getChatMessages } from '@/api/chat'
import { getMyProfile } from '@/api/profile'
import { useWebSocketStore } from '@/stores/websocket'
import { 
  Loading, Star, StarFilled, User, UserFilled, ChatDotRound, 
  Pointer, Warning, ChatLineSquare, Check, Bell, Position, Aim, Location 
} from '@element-plus/icons-vue'
import { loadAMap } from '@/utils/mapLoader'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const event = ref(null)
const isFavorite = ref(false)
const favoriteLoading = ref(false)
const joinLoading = ref(false)
const quitLoading = ref(false)
const comments = ref([])
const commentContent = ref('')
const commentLoading = ref(false)
const commentCount = ref(0)
const commentPageNum = ref(1)
const hasMoreComments = ref(false)
const showReplyDialog = ref(false)
const replyTarget = ref(null)
const replyContent = ref('')
const replyLoading = ref(false)

// 聊天相关
const wsStore = useWebSocketStore()
const chatMessages = ref([])
const chatInput = ref('')
const chatContainerRef = ref(null)

// 确认相关
const confirmLoading = ref(false)
const confirmationStatus = ref({
  confirmedCount: 0,
  totalCount: 0,
  currentUserConfirmed: false
})

// 路线导航相关
const currentLocation = ref(null)
const gettingLocation = ref(false)
const destinationLocation = computed(() => {
  if (!event.value) return null
  
  // 优先使用地图选点的位置
  if (event.value.extMeta?.mapLocation) {
    return {
      lng: event.value.extMeta.mapLocation.lng,
      lat: event.value.extMeta.mapLocation.lat,
      address: event.value.extMeta.mapLocation.address || event.value.pointName || '集合点'
    }
  }
  
  // 使用校园点位的坐标（如果有）
  if (event.value.pointLng && event.value.pointLat) {
    return {
      lng: event.value.pointLng,
      lat: event.value.pointLat,
      address: event.value.pointName || '集合点'
    }
  }
  
  // 默认位置（可以设置为校园中心）
  if (event.value.pointName) {
    // 如果只有点位名称，使用默认坐标
    return {
      lng: 116.397428,
      lat: 39.90923,
      address: event.value.pointName
    }
  }
  
  return null
})

// 集合地点文本显示
const eventLocation = computed(() => {
  if (!event.value) return null
  
  // 优先使用地图选点的地址
  if (event.value.extMeta?.mapLocation?.address) {
    return event.value.extMeta.mapLocation.address
  }
  
  // 否则使用点位名称
  if (event.value.pointName) {
    return event.value.pointName
  }
  
  return null
})

// 注：已从 mapLoader.js 导入 loadAMap 统一加载函数

// 加载用户绑定的位置
const loadUserBindLocation = async () => {
  try {
    const res = await getMyProfile()
    if (res.code === 200 && res.data.extMeta?.campusLocation) {
      const location = res.data.extMeta.campusLocation
      if (location.lng && location.lat) {
        currentLocation.value = {
          lng: location.lng,
          lat: location.lat,
          address: location.address || '我的位置'
        }
        console.log('已加载用户绑定位置:', currentLocation.value)
      }
    }
  } catch (error) {
    console.error('加载用户绑定位置失败:', error)
  }
}

// 使用高德地图定位（国内更可靠）
const getCurrentLocation = () => {
  gettingLocation.value = true
  ElMessage.info('正在获取位置信息，请稍候...')
  
  // 动态加载高德地图API
  if (!window.AMap) {
    ElMessage.error('地图服务未加载，请刷新页面重试')
    gettingLocation.value = false
    return
  }
  
  // 1. 尝试使用高精度定位
  window.AMap.plugin('AMap.Geolocation', () => {
    const geolocation = new window.AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 5000, // 缩短超时时间到5秒
      position: 'RB',
      offset: [10, 20],
      zoomToAccuracy: true,
      noIpLocate: 0 // 允许AMap内部尝试IP定位
    })
    
    geolocation.getCurrentPosition((status, result) => {
      if (status === 'complete') {
        // 定位成功
        handleLocationSuccess(result)
      } else {
        console.warn('高精度定位失败，尝试CitySearch IP定位:', result)
        // 2. 如果高精度定位失败，尝试使用CitySearch插件
        tryIpLocation()
      }
    })
  })
}

// IP定位作为备选方案
const tryIpLocation = () => {
  window.AMap.plugin('AMap.CitySearch', () => {
    const citySearch = new window.AMap.CitySearch()
    citySearch.getLocalCity((status, result) => {
      if (status === 'complete' && result.info === 'OK') {
        // CitySearch 成功（通常只返回城市矩形bounds）
        gettingLocation.value = false
        console.log('CitySearch定位成功:', result)
        ElMessage.warning('无法获取精确位置，已切换到当前城市中心')
        
        // 由于CitySearch没有直接返回lng/lat中心点，我们需要计算bounds中心或使用默认逻辑
        // 这里简单处理，如果CitySearch成功，我们尝试获取bounds的中心
        // 但为了稳妥，这里直接回退到默认校园位置，因为城市中心离学校太远也没意义
        useDefaultLocation('定位精度不足，已使用默认校园位置')
      } else {
        console.warn('IP定位也失败:', result)
        // 3. 所有定位都失败，使用默认位置
        useDefaultLocation('定位失败，已使用默认校园位置')
      }
    })
  })
}

// 使用默认位置（西南大学）
const useDefaultLocation = (msg) => {
  gettingLocation.value = false
  currentLocation.value = {
    lng: 106.419704,
    lat: 29.817324,
    address: '西南大学(默认位置)'
  }
  ElMessage.warning(msg || '已切换到默认位置')
}

// 处理定位成功
const handleLocationSuccess = (result) => {
  gettingLocation.value = false
  currentLocation.value = {
    lng: result.position.lng,
    lat: result.position.lat,
    address: result.formattedAddress || '我的位置'
  }
  ElMessage.success('定位成功')
  console.log('定位成功:', currentLocation.value)
}

const eventId = computed(() => route.params.eventId)
const isOwner = computed(() => event.value?.ownerId === userStore.userId)
const isJoined = computed(() => event.value?.isJoined)

// 加载事件详情
const loadEventDetail = async () => {
  loading.value = true
  try {
    const res = await getEventDetail(eventId.value)
    if (res.code === 200) {
      event.value = res.data
      isFavorite.value = res.data.isFavorite || false
      
      // 如果是待确认状态，加载确认状态
      if (res.data.status === 'pending_confirm') {
        loadConfirmationStatus()
      }
    }
  } catch (error) {
    console.error('加载事件详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载确认状态
const loadConfirmationStatus = async () => {
  try {
    const res = await getConfirmationStatus(eventId.value)
    if (res.code === 200) {
      confirmationStatus.value = res.data
    }
  } catch (error) {
    console.error('加载确认状态失败:', error)
  }
}

// 确认事件完成
const handleConfirmEvent = async () => {
  confirmLoading.value = true
  try {
    const res = await confirmEventCompletion(eventId.value)
    if (res.code === 200) {
      ElMessage.success(res.message || '确认成功')
      // 重新加载事件详情和确认状态
      await loadEventDetail()
    }
  } catch (error) {
    console.error('确认失败:', error)
  } finally {
    confirmLoading.value = false
  }
}

// 加载评论
const loadComments = async () => {
  try {
    const res = await getEventComments(eventId.value, {
      pageNum: commentPageNum.value,
      pageSize: 20
    })
    if (res.code === 200) {
      if (commentPageNum.value === 1) {
        comments.value = res.data || []
      } else {
        comments.value.push(...(res.data || []))
      }
      hasMoreComments.value = (res.data?.length || 0) >= 20
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

// 加载评论数
const loadCommentCount = async () => {
  try {
    const res = await getCommentCount(eventId.value)
    if (res.code === 200) {
      commentCount.value = res.data || 0
    }
  } catch (error) {
    console.error('获取评论数失败:', error)
  }
}

// 加载更多评论
const loadMoreComments = () => {
  commentPageNum.value++
  loadComments()
}

// 收藏/取消收藏
const handleFavorite = async () => {
  favoriteLoading.value = true
  try {
    if (isFavorite.value) {
      await removeFavorite(eventId.value)
      isFavorite.value = false
      if (event.value.favoriteCount > 0) {
        event.value.favoriteCount--
      }
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(eventId.value)
      isFavorite.value = true
      event.value.favoriteCount = (event.value.favoriteCount || 0) + 1
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    favoriteLoading.value = false
  }
}

// 参与事件
const handleJoinEvent = async () => {
  joinLoading.value = true
  try {
    await joinEvent(eventId.value)
    ElMessage.success('参与成功')
    loadEventDetail()
  } catch (error) {
    console.error('参与失败:', error)
  } finally {
    joinLoading.value = false
  }
}

// 退出事件
const handleQuitEvent = async () => {
  try {
    await ElMessageBox.confirm('确定要退出该事件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    quitLoading.value = true
    await quitEvent(eventId.value)
    ElMessage.success('已退出事件')
    loadEventDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出失败:', error)
    }
  } finally {
    quitLoading.value = false
  }
}

// 取消事件
const handleCancelEvent = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该事件吗？此操作不可撤销。', '警告', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    
    await cancelEvent(eventId.value)
    ElMessage.success('事件已取消')
    loadEventDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消事件失败:', error)
    }
  }
}

// 发表评论
const handleAddComment = async () => {
  if (!commentContent.value.trim()) return
  
  commentLoading.value = true
  try {
    await addComment({
      eventId: eventId.value,
      content: commentContent.value
    })
    commentContent.value = ''
    commentPageNum.value = 1
    loadComments()
    loadCommentCount()
    ElMessage.success('评论成功')
  } catch (error) {
    console.error('评论失败:', error)
  } finally {
    commentLoading.value = false
  }
}

// 点赞评论
const handleLikeComment = async (comment) => {
  try {
    await likeComment(comment.id)
    comment.likeCount = (comment.likeCount || 0) + 1
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

// 回复评论
const handleReply = (comment) => {
  replyTarget.value = comment
  replyContent.value = ''
  showReplyDialog.value = true
}

// 提交回复
const handleSubmitReply = async () => {
  if (!replyContent.value.trim()) return
  
  replyLoading.value = true
  try {
    await addComment({
      eventId: eventId.value,
      content: replyContent.value,
      parentId: replyTarget.value.id,
      replyToUserId: replyTarget.value.userId
    })
    showReplyDialog.value = false
    commentPageNum.value = 1
    loadComments()
    ElMessage.success('回复成功')
  } catch (error) {
    console.error('回复失败:', error)
  } finally {
    replyLoading.value = false
  }
}

// 跳转到用户主页
const goToProfile = (userId) => {
  router.push(`/user/${userId}`)
}

// 获取状态名称
const getStatusName = (status) => {
  const statuses = {
    'active': '进行中',
    'pending_confirm': '待确认',
    'settled': '已完成',
    'completed': '已完成',
    'cancelled': '已取消',
    'expired': '已过期'
  }
  return statuses[status] || status
}

// 获取状态标签
const getStatusTag = (status) => {
  const tags = {
    'active': 'success',
    'pending_confirm': 'warning',
    'settled': 'info',
    'completed': 'info',
    'cancelled': 'danger',
    'expired': 'warning'
  }
  return tags[status] || 'info'
}

// 信用分颜色
const getScoreClass = (score) => {
  if (score >= 90) return 'text-emerald-500'
  if (score >= 80) return 'text-blue-500'
  if (score >= 60) return 'text-yellow-500'
  return 'text-red-500'
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).fromNow()
}

// 格式化聊天时间
const formatChatTime = (time) => {
  return dayjs(time).format('HH:mm')
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  try {
    const res = await checkFavorite(eventId.value)
    if (res.code === 200) {
      isFavorite.value = res.data
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 从后端加载聊天记录
const loadChatMessages = async () => {
  try {
    const res = await getChatMessages(eventId.value)
    if (res.code === 200 && res.data) {
      chatMessages.value = res.data.map(msg => ({
        id: msg.id,
        userId: msg.userId,
        nickname: msg.nickname,
        avatar: msg.avatar,
        content: msg.content,
        time: msg.createTime
      }))
    }
  } catch (e) {
    console.error('加载聊天记录失败:', e)
  }
}

// 添加消息（检查重复）
const addChatMessage = (msg) => {
  // 检查是否重复消息（根据时间和内容判断）
  const isDuplicate = chatMessages.value.some(
    m => m.content === msg.content && m.userId === msg.userId && 
         Math.abs(new Date(m.time).getTime() - new Date(msg.time).getTime()) < 2000
  )
  if (!isDuplicate) {
    chatMessages.value.push(msg)
  }
}

// 发送聊天消息
const sendChatMessage = () => {
  if (!chatInput.value.trim()) return
  
  const message = {
    type: 'event_chat',
    eventId: eventId.value,
    content: chatInput.value.trim()
  }
  
  wsStore.send(message)
  
  // 本地添加消息（乐观更新）
  const newMsg = {
    id: Date.now(),
    userId: userStore.userId,
    nickname: userStore.nickname,
    avatar: userStore.avatar,
    content: chatInput.value.trim(),
    time: new Date().toISOString()
  }
  addChatMessage(newMsg)
  
  chatInput.value = ''
  
  // 滚动到底部
  scrollToBottom()
}

// 滚动到底部
const scrollToBottom = () => {
  setTimeout(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  }, 100)
}

// 初始化聊天
const initChat = async () => {
  // 从后端加载历史消息
  await loadChatMessages()
  scrollToBottom()
  
  // 订阅事件聊天
  wsStore.subscribeEvent(eventId.value)
  
  // 监听聊天消息
  wsStore.onMessage('event_chat', (data) => {
    if (data.eventId === eventId.value && data.userId !== userStore.userId) {
      const newMsg = {
        id: Date.now(),
        userId: data.userId,
        nickname: data.nickname,
        avatar: data.avatar,
        content: data.content,
        time: data.time || new Date().toISOString()
      }
      addChatMessage(newMsg)
      scrollToBottom()
    }
  })
}

onMounted(async () => {
  // 预加载高德地图API（用于定位功能）
  try {
    await loadAMap()
  } catch (error) {
    console.error('加载地图服务失败:', error)
  }
  
  loadEventDetail()
  loadComments()
  loadCommentCount()
  
  // 加载用户绑定的位置（用于路线导航）
  if (userStore.isLoggedIn) {
    loadUserBindLocation()
  }
  
  if (userStore.isLoggedIn) {
    checkFavoriteStatus()
    // 加载聊天记录并初始化 WebSocket
    await loadChatMessages()
    scrollToBottom()
    if (wsStore.connected) {
      // 订阅事件聊天
      wsStore.subscribeEvent(eventId.value)
      wsStore.onMessage('event_chat', (data) => {
        if (data.eventId === eventId.value && data.userId !== userStore.userId) {
          const newMsg = {
            id: Date.now(),
            userId: data.userId,
            nickname: data.nickname,
            avatar: data.avatar,
            content: data.content,
            time: data.time || new Date().toISOString()
          }
          addChatMessage(newMsg)
          scrollToBottom()
        }
      })
    }
  }
})
</script>

<style scoped>
.event-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.comment-item {
  padding: 16px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
}
</style>

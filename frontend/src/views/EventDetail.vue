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
                  <el-avatar :size="36">{{ p.nickname?.charAt(0) }}</el-avatar>
                  <div class="flex-1">
                    <span class="text-gray-800">{{ p.nickname }}</span>
                    <el-tag v-if="p.isOwner" size="small" type="warning" class="ml-2">发起者</el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-col>

          <!-- 右侧：评论区 -->
          <el-col :span="16">
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
                    <el-avatar :size="40" @click="goToProfile(comment.userId)" class="cursor-pointer">
                      {{ comment.userNickname?.charAt(0) }}
                    </el-avatar>
                    <div class="flex-1">
                      <div class="flex items-center justify-between mb-1">
                        <span class="font-medium text-gray-800">{{ comment.userNickname }}</span>
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
                          <el-avatar :size="28">{{ reply.userNickname?.charAt(0) }}</el-avatar>
                          <div class="flex-1">
                            <div class="text-sm">
                              <span class="font-medium text-gray-800">{{ reply.userNickname }}</span>
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
        <p class="text-gray-500 mb-3">回复 @{{ replyTarget?.userNickname }}</p>
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
import { useUserStore } from '@/stores/user'
import { getEventDetail, joinEvent, quitEvent, cancelEvent } from '@/api/event'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { getEventComments, addComment, likeComment, getCommentCount } from '@/api/comment'
import { 
  Loading, Star, StarFilled, User, UserFilled, ChatDotRound, 
  Pointer, Warning 
} from '@element-plus/icons-vue'
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
    }
  } catch (error) {
    console.error('加载事件详情失败:', error)
  } finally {
    loading.value = false
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
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(eventId.value)
      isFavorite.value = true
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

onMounted(() => {
  loadEventDetail()
  loadComments()
  loadCommentCount()
  if (userStore.isLoggedIn) {
    checkFavoriteStatus()
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

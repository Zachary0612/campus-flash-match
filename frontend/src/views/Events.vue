<template>
  <Layout>
    <div class="events-page relative z-10">
      <div class="glass-card rounded-3xl p-8 shadow-glass backdrop-blur-xl bg-white/20 border-white/30 min-h-[85vh] animate-slide-up" style="animation-delay: 0.1s">
        <div class="flex items-center justify-between mb-8 border-b border-white/10 pb-6">
          <div class="flex-1">
             <div class="inline-flex bg-white/40 p-1.5 rounded-2xl border border-white/40 shadow-inner backdrop-blur-md">
                <button 
                  v-for="tab in [{name: 'history', label: '进行中 / 历史'}, {name: 'completed', label: '已完成事件'}]" 
                  :key="tab.name"
                  @click="activeTab = tab.name; handleTabChange(tab.name)"
                  class="px-8 py-2.5 rounded-xl text-sm font-bold transition-all duration-300 flex items-center"
                  :class="activeTab === tab.name ? 'bg-white text-primary shadow-md transform scale-105' : 'text-gray-500 hover:text-gray-800 hover:bg-white/30'"
                >
                  <el-icon class="mr-2 text-lg"><component :is="tab.name === 'history' ? 'List' : 'Trophy'" /></el-icon>
                  {{ tab.label }}
                </button>
             </div>
          </div>
          <el-button circle @click="handleRefresh" :loading="activeTab === 'history' ? loading : completedLoading" class="!bg-white/40 !border-white/40 hover:!bg-white/60 hover:!shadow-md transition-all !w-10 !h-10">
            <el-icon class="text-gray-700 font-bold"><Refresh /></el-icon>
          </el-button>
        </div>

        <div v-if="activeTab === 'history'" class="animate-fade-in">
          <div v-if="loading" class="text-center py-24">
            <div class="inline-block p-4 rounded-full bg-white/30 backdrop-blur-md shadow-lg animate-pulse-slow">
              <el-icon class="is-loading text-primary" :size="48"><Loading /></el-icon>
            </div>
            <p class="mt-4 text-gray-500 font-medium">加载历史记录...</p>
          </div>
          
          <div v-else-if="historyList.length === 0" class="text-center py-24 text-gray-500">
            <div class="bg-gray-100/30 w-32 h-32 rounded-full flex items-center justify-center mx-auto mb-6 backdrop-blur-md shadow-inner border border-white/20">
              <el-icon :size="56" class="text-gray-400/70"><DocumentDelete /></el-icon>
            </div>
            <p class="text-xl font-bold text-gray-700">暂无事件历史</p>
            <p class="text-base opacity-70 mt-2">这里空空如也，快去参加活动吧！</p>
          </div>
          
          <template v-else>
            <div class="overflow-hidden rounded-2xl border border-white/40 shadow-sm bg-white/20 backdrop-blur-md">
              <el-table 
                :data="historyList" 
                style="width: 100%" 
                :header-cell-style="{background: 'rgba(255,255,255,0.5)', color: '#4b5563', fontWeight: 'bold', fontSize: '14px', borderBottom: '1px solid rgba(255,255,255,0.3)'}" 
                :cell-style="{background: 'transparent', borderBottom: '1px solid rgba(255,255,255,0.1)'}"
                :row-class-name="'glass-row hover:bg-white/30 transition-colors duration-200'"
              >
                <el-table-column label="类型" width="120" align="center">
                  <template #default="{ row }">
                    <div class="flex justify-center">
                      <el-tag :type="getEventTypeTag(row.eventType)" size="default" effect="dark" class="!rounded-lg !font-bold !border-none shadow-sm w-20">
                        {{ getEventTypeName(row.eventType) }}
                      </el-tag>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column prop="title" label="标题" min-width="220">
                   <template #default="{ row }">
                     <div class="font-bold text-gray-800 text-base truncate pr-4">{{ row.title }}</div>
                   </template>
                </el-table-column>
                
                <el-table-column label="参与情况" width="140">
                  <template #default="{ row }">
                    <div class="flex items-center text-gray-600 bg-white/40 px-3 py-1 rounded-full w-fit">
                      <el-icon class="mr-1.5 text-primary"><User /></el-icon>
                      <span class="font-bold font-mono">{{ row.currentNum }}</span>
                      <span class="text-xs text-gray-400 mx-1">/</span>
                      <span class="text-xs text-gray-500">{{ row.targetNum }}</span>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="状态" width="120" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getStatusTag(row.status)" size="small" effect="plain" class="!rounded-full !px-3 !font-medium bg-white/50 backdrop-blur-sm">
                      {{ getStatusName(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column label="创建时间" width="180">
                  <template #default="{ row }">
                    <div class="text-gray-500 text-xs flex items-center">
                      <el-icon class="mr-1"><Clock /></el-icon>
                      {{ formatTime(row.createTime) }}
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="结算时间" width="180">
                  <template #default="{ row }">
                    <div class="text-gray-500 text-xs">{{ row.settleTime ? formatTime(row.settleTime) : '-' }}</div>
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="180" align="center">
                  <template #default="{ row }">
                    <div class="flex justify-center gap-2">
                      <el-button
                        v-if="row.status === 'active'"
                        type="danger"
                        size="small"
                        plain
                        class="!rounded-lg hover:!shadow-md transition-all"
                        @click="handleQuit(row.eventId)"
                      >
                        退出
                      </el-button>
                      <el-button
                        v-if="row.status === 'pending_confirm'"
                        type="success"
                        size="small"
                        class="!rounded-lg !font-bold hover:!shadow-md transition-all !bg-emerald-500 !border-emerald-500"
                        @click="handleConfirm(row.eventId)"
                        :loading="confirmingEventId === row.eventId"
                      >
                        确认完成
                      </el-button>
                      <el-button
                        v-if="row.status === 'pending_confirm' || row.status === 'active'"
                        type="primary"
                        size="small"
                        plain
                        class="!rounded-lg hover:!shadow-md transition-all"
                        @click="goToEventDetail(row.eventId)"
                      >
                        详情
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <div class="mt-6 flex justify-center">
              <el-pagination
                v-model:current-page="pageNum"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next"
                background
                @current-change="loadHistory"
              />
            </div>
          </template>
        </div>

        <div v-else class="animate-fade-in">
          <div v-if="completedLoading" class="text-center py-24">
            <div class="inline-block p-4 rounded-full bg-white/30 backdrop-blur-md shadow-lg animate-pulse-slow">
              <el-icon class="is-loading text-primary" :size="48"><Loading /></el-icon>
            </div>
            <p class="mt-4 text-gray-500 font-medium">加载已完成事件...</p>
          </div>

          <div v-else-if="completedList.length === 0" class="text-center py-24 text-gray-500">
            <div class="bg-gray-100/30 w-32 h-32 rounded-full flex items-center justify-center mx-auto mb-6 backdrop-blur-md shadow-inner border border-white/20">
              <el-icon :size="56" class="text-gray-400/70"><Trophy /></el-icon>
            </div>
            <p class="text-xl font-bold text-gray-700">暂无已完成事件</p>
            <p class="text-base opacity-70 mt-2">加油，完成更多挑战！</p>
          </div>

          <div v-else class="completed-wrapper grid grid-cols-1 gap-6">
            <div v-for="event in completedList" :key="event.eventId" class="glass-card group rounded-2xl p-6 border border-white/50 shadow-glass-sm hover:shadow-glass-hover transition-all duration-300 hover:-translate-y-1 bg-white/40">
              <div class="flex items-center justify-between mb-6 pb-4 border-b border-gray-100/50">
                <div class="flex items-center gap-4">
                  <div class="p-2 rounded-xl text-white shadow-sm" :class="event.eventType === 'group_buy' ? 'bg-gradient-to-br from-blue-400 to-blue-600' : 'bg-gradient-to-br from-emerald-400 to-emerald-600'">
                    <el-icon class="text-xl"><component :is="event.eventType === 'group_buy' ? 'ShoppingCart' : 'UserFilled'" /></el-icon>
                  </div>
                  <span class="font-black text-xl text-gray-800 tracking-tight">{{ event.title }}</span>
                </div>
                <div class="flex items-center gap-3">
                  <el-button type="primary" size="small" plain class="!rounded-lg !px-4 !font-bold" @click="goToEventChat(event.eventId)">
                    <el-icon class="mr-1"><ChatLineSquare /></el-icon>
                    聊天记录
                  </el-button>
                  <el-tag :type="getStatusTag(event.status)" size="default" effect="dark" class="!rounded-lg !px-3 shadow-sm font-bold">
                    {{ getStatusName(event.status) }}
                  </el-tag>
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                <div class="flex items-center text-gray-600 bg-white/50 px-4 py-3 rounded-xl border border-white/60">
                  <div class="w-8 h-8 rounded-full bg-blue-50 flex items-center justify-center mr-3 text-blue-500">
                    <el-icon><User /></el-icon>
                  </div>
                  <div>
                    <div class="text-xs text-gray-400 uppercase font-bold">参与人数</div>
                    <div class="font-bold text-gray-800">{{ event.currentNum }}<span class="text-gray-400 font-normal">/{{ event.targetNum }}</span></div>
                  </div>
                </div>
                <div class="flex items-center text-gray-600 bg-white/50 px-4 py-3 rounded-xl border border-white/60">
                   <div class="w-8 h-8 rounded-full bg-orange-50 flex items-center justify-center mr-3 text-orange-500">
                    <el-icon><Clock /></el-icon>
                  </div>
                  <div>
                    <div class="text-xs text-gray-400 uppercase font-bold">创建时间</div>
                    <div class="font-bold text-gray-800 text-sm">{{ formatTime(event.createTime) }}</div>
                  </div>
                </div>
                <div class="flex items-center text-gray-600 bg-white/50 px-4 py-3 rounded-xl border border-white/60">
                   <div class="w-8 h-8 rounded-full bg-green-50 flex items-center justify-center mr-3 text-green-500">
                    <el-icon><Check /></el-icon>
                  </div>
                  <div>
                    <div class="text-xs text-gray-400 uppercase font-bold">结算时间</div>
                    <div class="font-bold text-gray-800 text-sm">{{ event.settleTime ? formatTime(event.settleTime) : '自动结算' }}</div>
                  </div>
                </div>
              </div>

              <div class="mt-6 bg-white/30 rounded-2xl p-5 border border-white/40">
                <p class="font-bold text-gray-700 mb-4 flex items-center text-sm uppercase tracking-wide">
                  <el-icon class="mr-2 text-primary"><UserFilled /></el-icon>
                  参与者评价
                </p>
                <el-row :gutter="16">
                  <el-col :xs="24" :sm="12" :lg="8" v-for="participant in event.participants" :key="participant.userId" class="mb-4 lg:mb-0">
                    <div class="participant-card bg-white/80 p-4 rounded-xl border border-white shadow-sm transition-all duration-300 hover:shadow-md hover:-translate-y-0.5 group/card">
                      <div class="flex items-center justify-between mb-3">
                        <div class="flex items-center gap-3 font-bold text-gray-800">
                          <el-avatar :size="32" :src="participant.avatar" class="border border-white shadow-sm">{{ participant.nickname.charAt(0) }}</el-avatar>
                          <span>{{ participant.nickname }}</span>
                        </div>
                        <el-tag type="warning" effect="dark" v-if="participant.owner" size="small" class="scale-90 origin-right !rounded-md shadow-sm">发起者</el-tag>
                      </div>
                      <div class="text-xs text-gray-500 space-y-1 pl-11 mb-3 opacity-80">
                        <div class="flex justify-between"><span>状态:</span> <span class="font-medium text-gray-700">{{ getStatusName(participant.status) }}</span></div>
                        <div class="flex justify-between"><span>加入:</span> <span class="font-medium">{{ formatTime(participant.joinTime).split(' ')[1] }}</span></div>
                      </div>
                      <el-button 
                        v-if="participant.userId && userStore?.userId && participant.userId !== userStore.userId"
                        type="primary" 
                        size="small" 
                        plain
                        class="!rounded-lg w-full !font-bold group-hover/card:bg-primary group-hover/card:text-white transition-colors"
                        @click="handleRateUser(participant.userId, participant.nickname, event.eventId)"
                      >
                        <el-icon class="mr-1"><Star /></el-icon> 评价TA
                      </el-button>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 评价对话框 -->
    <el-dialog v-model="showRatingDialog" title="评价用户" width="500px">
      <div class="mb-4">
        <p class="text-gray-600">评价用户: <span class="font-bold">{{ ratingForm.targetNickname }}</span></p>
      </div>
      <el-form :model="ratingForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="ratingForm.score" :max="5" size="large" />
        </el-form-item>
        <el-form-item label="评价标签">
          <el-checkbox-group v-model="ratingForm.tags">
            <el-checkbox label="准时守信" />
            <el-checkbox label="友好热情" />
            <el-checkbox label="靠谱负责" />
            <el-checkbox label="沟通顺畅" />
            <el-checkbox label="值得推荐" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input 
            v-model="ratingForm.comment" 
            type="textarea" 
            :rows="4" 
            placeholder="说说你的评价吧..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRatingDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRating" :loading="ratingLoading">提交评价</el-button>
      </template>
    </el-dialog>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { useUserStore } from '@/stores/user'
import { getMyEvents, quitEvent, getCompletedEvents, confirmEventCompletion } from '@/api/event'
import { rateUser } from '@/api/rating'
import dayjs from 'dayjs'
import { Refresh, Loading, DocumentDelete, User, UserFilled, ChatLineSquare, Clock, Check, Star, List, Trophy, ShoppingCart } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const historyList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('history')
const completedList = ref([])
const completedLoading = ref(false)
const confirmingEventId = ref(null)

// 评价相关
const showRatingDialog = ref(false)
const ratingLoading = ref(false)
const ratingForm = reactive({
  targetUserId: null,
  targetNickname: '',
  eventId: '',
  score: 5,
  tags: [],
  comment: ''
})

onMounted(() => {
  loadHistory()
})

const handleTabChange = (name) => {
  if (name === 'completed' && completedList.value.length === 0) {
    loadCompleted()
  }
}

const handleRefresh = () => {
  if (activeTab.value === 'history') {
    loadHistory()
  } else {
    loadCompleted()
  }
}

const loadHistory = async () => {
  loading.value = true
  try {
    // 使用 getMyEvents 获取所有事件（包含进行中和历史，包括信标事件）
    const res = await getMyEvents('all', pageNum.value, pageSize.value)
    if (res.code === 200) {
      historyList.value = res.data || []
      total.value = historyList.value.length
    }
  } catch (error) {
    console.error('加载我的事件失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuit = async (eventId) => {
  try {
    await ElMessageBox.confirm(
      '退出事件将扣除信用分，确定要退出吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await quitEvent(eventId)
    if (res.code === 200) {
      ElMessage.success('已退出事件')
      loadHistory()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出事件失败:', error)
    }
  }
}

const loadCompleted = async () => {
  completedLoading.value = true
  try {
    const res = await getCompletedEvents()
    if (res.code === 200) {
      completedList.value = res.data || []
    }
  } catch (error) {
    console.error('加载已完成事件失败:', error)
  } finally {
    completedLoading.value = false
  }
}

const getEventTypeTag = (type) => {
  const map = {
    group_buy: 'primary',
    meetup: 'success',
    beacon: 'warning'
  }
  return map[type] || 'info'
}

const getEventTypeName = (type) => {
  const map = {
    group_buy: '拼单',
    meetup: '约伴',
    beacon: '信标'
  }
  return map[type] || type
}

const getStatusTag = (status) => {
  const map = {
    active: 'success',
    pending_confirm: 'warning',
    full: 'warning',
    settled: 'success',
    completed: 'success',
    expired: 'info'
  }
  return map[status] || 'info'
}

const getStatusName = (status) => {
  const map = {
    active: '进行中',
    pending_confirm: '待确认',
    full: '已满员',
    settled: '已完成',
    completed: '已完成',
    expired: '过期失败'
  }
  return map[status] || status
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const goToEventChat = (eventId) => {
  router.push(`/event/${eventId}`)
}

const goToEventDetail = (eventId) => {
  router.push(`/event/${eventId}`)
}

const handleConfirm = async (eventId) => {
  confirmingEventId.value = eventId
  try {
    const res = await confirmEventCompletion(eventId)
    if (res.code === 200) {
      ElMessage.success(res.message || '确认成功')
      loadHistory()
    }
  } catch (error) {
    console.error('确认失败:', error)
  } finally {
    confirmingEventId.value = null
  }
}

// 打开评价对话框
const handleRateUser = (userId, nickname, eventId) => {
  ratingForm.targetUserId = userId
  ratingForm.targetNickname = nickname
  ratingForm.eventId = eventId
  ratingForm.score = 5
  ratingForm.tags = []
  ratingForm.comment = ''
  showRatingDialog.value = true
}

// 提交评价
const submitRating = async () => {
  if (!ratingForm.comment.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  
  ratingLoading.value = true
  try {
    const res = await rateUser({
      targetUserId: ratingForm.targetUserId,
      eventId: ratingForm.eventId,
      score: ratingForm.score,
      tags: ratingForm.tags.join(','),
      comment: ratingForm.comment
    })
    if (res.code === 200) {
      ElMessage.success('评价成功')
      showRatingDialog.value = false
    }
  } catch (error) {
    console.error('评价失败:', error)
  } finally {
    ratingLoading.value = false
  }
}
</script>

<style scoped>
.events-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* Glass table customization */
:deep(.el-table) {
  background-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.5);
}

:deep(.el-table tr) {
  background-color: transparent;
}

:deep(.el-table th.el-table__cell) {
  background-color: rgba(255, 255, 255, 0.4);
}

:deep(.el-table__body-wrapper) {
  overflow-x: auto;
}

:deep(.el-table .el-table__cell) {
  padding: 12px 8px;
}
</style>
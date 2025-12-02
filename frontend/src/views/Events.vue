<template>
  <Layout>
    <div class="events-page relative z-10">
      <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 min-h-[80vh] animate-slide-up" style="animation-delay: 0.1s">
        <div class="flex items-center justify-between mb-6 border-b border-white/20 pb-4">
          <div class="flex-1">
             <div class="inline-flex bg-gray-100/50 p-1 rounded-xl">
                <button 
                  v-for="tab in [{name: 'history', label: '进行中 / 历史'}, {name: 'completed', label: '已完成事件'}]" 
                  :key="tab.name"
                  @click="activeTab = tab.name; handleTabChange(tab.name)"
                  class="px-6 py-2 rounded-lg text-sm font-medium transition-all duration-300"
                  :class="activeTab === tab.name ? 'bg-white text-primary shadow-sm' : 'text-gray-500 hover:text-gray-700'"
                >
                  {{ tab.label }}
                </button>
             </div>
          </div>
          <el-button circle @click="handleRefresh" :loading="activeTab === 'history' ? loading : completedLoading" class="!bg-white/50 !border-white/50 hover:!bg-white/80">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>

        <div v-if="activeTab === 'history'" class="animate-fade-in">
          <div v-if="loading" class="text-center py-16">
            <el-icon class="is-loading text-primary" :size="40"><Loading /></el-icon>
          </div>
          
          <div v-else-if="historyList.length === 0" class="text-center py-16 text-gray-500">
            <div class="bg-gray-100/50 w-24 h-24 rounded-full flex items-center justify-center mx-auto mb-4">
              <el-icon :size="40" class="text-gray-400"><DocumentDelete /></el-icon>
            </div>
            <p class="text-lg font-medium">暂无事件历史</p>
          </div>
          
          <template v-else>
            <div class="overflow-hidden rounded-xl border border-white/30">
              <el-table :data="historyList" style="width: 100%" :header-cell-style="{background: 'rgba(255,255,255,0.5)', color: '#606266'}" :row-class-name="'glass-row'">
                <el-table-column label="事件类型" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getEventTypeTag(row.eventType)" size="small" effect="dark" class="!rounded-md">
                      {{ getEventTypeName(row.eventType) }}
                    </el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column prop="title" label="标题" min-width="200">
                   <template #default="{ row }">
                     <span class="font-medium text-gray-800">{{ row.title }}</span>
                   </template>
                </el-table-column>
                
                <el-table-column label="人数" width="100">
                  <template #default="{ row }">
                    <div class="flex items-center text-gray-600">
                      <el-icon class="mr-1"><User /></el-icon>
                      {{ row.currentNum }}/{{ row.targetNum }}
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getStatusTag(row.status)" size="small" effect="plain" class="!rounded-full">
                      {{ getStatusName(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column label="创建时间" width="180">
                  <template #default="{ row }">
                    <div class="text-gray-500 text-sm">{{ formatTime(row.createTime) }}</div>
                  </template>
                </el-table-column>
                
                <el-table-column label="结算时间" width="180">
                  <template #default="{ row }">
                    <div class="text-gray-500 text-sm">{{ row.settleTime ? formatTime(row.settleTime) : '-' }}</div>
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="120" fixed="right">
                  <template #default="{ row }">
                    <el-button
                      v-if="row.status === 'active'"
                      type="danger"
                      size="small"
                      plain
                      class="!rounded-lg"
                      @click="handleQuit(row.eventId)"
                    >
                      退出
                    </el-button>
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
          <div v-if="completedLoading" class="text-center py-16">
            <el-icon class="is-loading text-primary" :size="40"><Loading /></el-icon>
          </div>

          <div v-else-if="completedList.length === 0" class="text-center py-16 text-gray-500">
            <div class="bg-gray-100/50 w-24 h-24 rounded-full flex items-center justify-center mx-auto mb-4">
              <el-icon :size="40" class="text-gray-400"><DocumentDelete /></el-icon>
            </div>
            <p class="text-lg font-medium">暂无已完成事件</p>
          </div>

          <div v-else class="completed-wrapper space-y-6">
            <div v-for="event in completedList" :key="event.eventId" class="bg-white/50 rounded-xl p-6 border border-white/60 shadow-sm hover:shadow-md transition-all">
              <div class="flex items-center justify-between mb-4 pb-4 border-b border-gray-100">
                <div class="flex items-center gap-3">
                  <el-tag :type="getEventTypeTag(event.eventType)" size="default" effect="dark" class="!rounded-lg">
                    {{ getEventTypeName(event.eventType) }}
                  </el-tag>
                  <span class="font-bold text-lg text-gray-800">{{ event.title }}</span>
                </div>
                <div class="flex items-center gap-2">
                  <el-button type="primary" size="small" plain class="!rounded-lg" @click="goToEventChat(event.eventId)">
                    <el-icon class="mr-1"><ChatLineSquare /></el-icon>
                    进入聊天
                  </el-button>
                  <el-tag :type="getStatusTag(event.status)" size="small" effect="plain" class="!rounded-full">
                    {{ getStatusName(event.status) }}
                  </el-tag>
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6 text-sm">
                <div class="flex items-center text-gray-600 bg-white/60 px-3 py-2 rounded-lg">
                  <span class="text-gray-400 mr-2">人数:</span>
                  <span class="font-medium">{{ event.currentNum }}/{{ event.targetNum }}</span>
                </div>
                <div class="flex items-center text-gray-600 bg-white/60 px-3 py-2 rounded-lg">
                   <span class="text-gray-400 mr-2">创建:</span>
                   <span class="font-medium">{{ formatTime(event.createTime) }}</span>
                </div>
                <div class="flex items-center text-gray-600 bg-white/60 px-3 py-2 rounded-lg">
                   <span class="text-gray-400 mr-2">结算:</span>
                   <span class="font-medium">{{ event.settleTime ? formatTime(event.settleTime) : '-' }}</span>
                </div>
              </div>

              <div class="mt-4">
                <p class="font-semibold text-gray-700 mb-3 flex items-center">
                  <el-icon class="mr-1"><UserFilled /></el-icon>
                  参与者名单
                </p>
                <el-row :gutter="12">
                  <el-col :xs="24" :sm="12" :lg="8" v-for="participant in event.participants" :key="participant.userId">
                    <div class="participant-card bg-white/80 p-3 rounded-lg border border-gray-100 mb-3 transition-all hover:bg-white hover:shadow-sm">
                      <div class="flex items-center justify-between mb-2">
                        <div class="flex items-center gap-2 font-medium text-gray-800">
                          <el-avatar :size="24" class="bg-gray-200 text-xs">{{ participant.nickname.charAt(0) }}</el-avatar>
                          <span>{{ participant.nickname }}</span>
                        </div>
                        <el-tag type="warning" effect="dark" v-if="participant.owner" size="small" class="scale-90 origin-right !rounded-full">发起者</el-tag>
                      </div>
                      <div class="text-xs text-gray-500 grid grid-cols-1 gap-1 pl-8">
                        <div class="flex justify-between"><span>状态:</span> <span class="text-gray-700">{{ getStatusName(participant.status) }}</span></div>
                        <div class="flex justify-between"><span>加入:</span> <span>{{ formatTime(participant.joinTime).split(' ')[1] }}</span></div>
                      </div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getEventHistory, quitEvent, getCompletedEvents } from '@/api/event'
import dayjs from 'dayjs'
import { Refresh, Loading, DocumentDelete, User, UserFilled, ChatLineSquare } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const historyList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('history')
const completedList = ref([])
const completedLoading = ref(false)

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
    const res = await getEventHistory(pageNum.value, pageSize.value)
    if (res.code === 200) {
      historyList.value = res.data || []
      total.value = historyList.value.length
    }
  } catch (error) {
    console.error('加载事件历史失败:', error)
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
    full: 'warning',
    settled: 'info',
    expired: 'danger'
  }
  return map[status] || 'info'
}

const getStatusName = (status) => {
  const map = {
    active: '进行中',
    full: '已满员',
    settled: '已结算',
    expired: '已过期'
  }
  return map[status] || status
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const goToEventChat = (eventId) => {
  router.push(`/event/${eventId}`)
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
</style>
<template>
  <Layout>
    <div class="events-page">
      <el-card shadow="hover">
        <template #header>
          <div class="flex items-center justify-between">
            <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="flex-1">
              <el-tab-pane label="进行中 / 历史" name="history" />
              <el-tab-pane label="已完成事件" name="completed" />
            </el-tabs>
            <el-button size="small" @click="handleRefresh" :loading="activeTab === 'history' ? loading : completedLoading">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </template>

        <div v-if="activeTab === 'history'">
          <div v-if="loading" class="text-center py-8">
            <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          </div>
          
          <div v-else-if="historyList.length === 0" class="text-center py-8 text-gray-400">
            <el-icon :size="48"><DocumentDelete /></el-icon>
            <p class="mt-2">暂无事件历史</p>
          </div>
          
          <template v-else>
            <el-table :data="historyList" stripe>
              <el-table-column label="事件类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="getEventTypeTag(row.eventType)" size="small">
                    {{ getEventTypeName(row.eventType) }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column prop="title" label="标题" min-width="200" />
              
              <el-table-column label="人数" width="100">
                <template #default="{ row }">
                  {{ row.currentNum }}/{{ row.targetNum }}
                </template>
              </el-table-column>
              
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusTag(row.status)" size="small">
                    {{ getStatusName(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column label="创建时间" width="160">
                <template #default="{ row }">
                  {{ formatTime(row.createTime) }}
                </template>
              </el-table-column>
              
              <el-table-column label="结算时间" width="160">
                <template #default="{ row }">
                  {{ row.settleTime ? formatTime(row.settleTime) : '-' }}
                </template>
              </el-table-column>
              
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="row.status === 'active'"
                    type="danger"
                    size="small"
                    @click="handleQuit(row.eventId)"
                  >
                    退出
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="mt-4 flex justify-center">
              <el-pagination
                v-model:current-page="pageNum"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next"
                @current-change="loadHistory"
              />
            </div>
          </template>
        </div>

        <div v-else>
          <div v-if="completedLoading" class="text-center py-8">
            <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          </div>

          <div v-else-if="completedList.length === 0" class="text-center py-8 text-gray-400">
            <el-icon :size="48"><DocumentDelete /></el-icon>
            <p class="mt-2">暂无已完成事件</p>
          </div>

          <div v-else class="completed-wrapper space-y-4">
            <el-card v-for="event in completedList" :key="event.eventId">
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <el-tag :type="getEventTypeTag(event.eventType)" size="small">
                    {{ getEventTypeName(event.eventType) }}
                  </el-tag>
                  <span class="font-semibold">{{ event.title }}</span>
                </div>
                <el-tag :type="getStatusTag(event.status)" size="small">
                  {{ getStatusName(event.status) }}
                </el-tag>
              </div>

              <el-descriptions :column="3" size="small" border>
                <el-descriptions-item label="人数">
                  {{ event.currentNum }}/{{ event.targetNum }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ formatTime(event.createTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="结算时间">
                  {{ event.settleTime ? formatTime(event.settleTime) : '-' }}
                </el-descriptions-item>
              </el-descriptions>

              <div class="mt-3">
                <p class="font-medium mb-2">参与者</p>
                <el-row :gutter="12">
                  <el-col :xs="24" :sm="12" :lg="8" v-for="participant in event.participants" :key="participant.userId">
                    <div class="participant-card">
                      <div class="flex items-center justify-between">
                        <div class="flex items-center gap-2">
                          <el-icon><User /></el-icon>
                          <span>{{ participant.nickname }}</span>
                        </div>
                        <el-tag type="info" v-if="participant.owner" size="small">发起者</el-tag>
                      </div>
                      <div class="text-sm text-gray-500 mt-2">
                        <div>状态：{{ getStatusName(participant.status) }}</div>
                        <div>加入：{{ formatTime(participant.joinTime) }}</div>
                        <div>结算：{{ participant.settleTime ? formatTime(participant.settleTime) : '-' }}</div>
                      </div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </el-card>
          </div>
        </div>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getEventHistory, quitEvent, getCompletedEvents } from '@/api/event'
import dayjs from 'dayjs'
import { Refresh, Loading, DocumentDelete, User } from '@element-plus/icons-vue'

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
</script>

<style scoped>
.events-page {
  max-width: 1200px;
  margin: 0 auto;
}

.participant-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 12px;
  background: #fafafa;
}
</style>
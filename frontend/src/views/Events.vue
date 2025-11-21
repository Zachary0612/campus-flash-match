<template>
  <Layout>
    <div class="events-page">
      <el-card shadow="hover">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="text-lg font-semibold">我的事件历史</span>
            <el-button size="small" @click="loadHistory" :loading="loading">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </template>
        
        <div v-if="loading" class="text-center py-8">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        </div>
        
        <div v-else-if="historyList.length === 0" class="text-center py-8 text-gray-400">
          <el-icon :size="48"><DocumentDelete /></el-icon>
          <p class="mt-2">暂无事件历史</p>
        </div>
        
        <el-table v-else :data="historyList" stripe>
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
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getEventHistory, quitEvent } from '@/api/event'
import dayjs from 'dayjs'
import { Refresh, Loading, DocumentDelete } from '@element-plus/icons-vue'

const loading = ref(false)
const historyList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadHistory()
})

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
</style>
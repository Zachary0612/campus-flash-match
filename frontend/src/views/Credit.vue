<template>
  <Layout>
    <div class="credit-page">
      <!-- 信用分概览 -->
      <el-card shadow="hover" class="mb-6">
        <div class="credit-overview">
          <div class="credit-score">
            <div class="score-label">当前信用分</div>
            <div class="score-value" :class="getScoreClass(creditInfo.currentCredit)">
              {{ creditInfo.currentCredit || 0 }}
            </div>
            <div class="score-desc">{{ getScoreDesc(creditInfo.currentCredit) }}</div>
          </div>
          
          <div class="credit-tips">
            <el-alert
              title="信用分规则"
              type="info"
              :closable="false"
            >
              <ul class="tips-list">
                <li>初始信用分：80分</li>
                <li>成功参与事件：+2分</li>
                <li>恶意退出事件：-5分</li>
                <li>发布虚假信标：-10分</li>
                <li>信用分低于60分将限制发起事件</li>
              </ul>
            </el-alert>
          </div>
        </div>
      </el-card>
      
      <!-- 信用分变更记录 -->
      <el-card shadow="hover">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="text-lg font-semibold">信用分变更记录</span>
            <el-button size="small" @click="loadRecords" :loading="loading">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </template>
        
        <div v-if="loading" class="text-center py-8">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        </div>
        
        <div v-else-if="records.length === 0" class="text-center py-8 text-gray-400">
          <el-icon :size="48"><DocumentDelete /></el-icon>
          <p class="mt-2">暂无变更记录</p>
        </div>
        
        <el-timeline v-else>
          <el-timeline-item
            v-for="record in records"
            :key="record.recordId"
            :timestamp="formatTime(record.createTime)"
            placement="top"
          >
            <el-card>
              <div class="record-item">
                <div class="record-info">
                  <div class="record-reason">{{ record.reason }}</div>
                  <div class="record-event" v-if="record.eventId">
                    事件ID: {{ record.eventId }}
                  </div>
                </div>
                <div
                  class="record-score"
                  :class="record.changeScore > 0 ? 'score-increase' : 'score-decrease'"
                >
                  {{ record.changeScore > 0 ? '+' : '' }}{{ record.changeScore }}
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        
        <div class="mt-4 flex justify-center">
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadRecords"
          />
        </div>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import { getCreditRecords } from '@/api/credit'
import dayjs from 'dayjs'
import { Refresh, Loading, DocumentDelete } from '@element-plus/icons-vue'

const loading = ref(false)
const records = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const creditInfo = reactive({
  userId: null,
  currentCredit: 0,
  recentRecords: []
})

onMounted(() => {
  loadCreditInfo()
  loadRecords()
})

const loadCreditInfo = async () => {
  try {
    const res = await getCreditInfo()
    if (res.code === 200 && res.data) {
      Object.assign(creditInfo, res.data)
    }
  } catch (error) {
    console.error('加载信用分信息失败:', error)
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getCreditRecords(pageNum.value, pageSize.value)
    if (res.code === 200) {
      records.value = res.data || []
      total.value = records.value.length
    }
  } catch (error) {
    console.error('加载信用分记录失败:', error)
  } finally {
    loading.value = false
  }
}

const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-normal'
  return 'score-low'
}

const getScoreDesc = (score) => {
  if (score >= 90) return '信用优秀'
  if (score >= 80) return '信用良好'
  if (score >= 60) return '信用一般'
  return '信用较低'
}

const formatTime = (timestamp) => {
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped>
.credit-page {
  max-width: 1000px;
  margin: 0 auto;
}

.credit-overview {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 32px;
}

.credit-score {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.score-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 12px;
}

.score-value {
  font-size: 64px;
  font-weight: bold;
  margin-bottom: 8px;
}

.score-desc {
  font-size: 16px;
  opacity: 0.9;
}

.credit-tips {
  display: flex;
  align-items: center;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 8px 0 0 0;
}

.tips-list li {
  padding: 4px 0;
  color: #606266;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-info {
  flex: 1;
}

.record-reason {
  font-size: 15px;
  color: #303133;
  margin-bottom: 4px;
}

.record-event {
  font-size: 13px;
  color: #909399;
}

.record-score {
  font-size: 24px;
  font-weight: bold;
  margin-left: 16px;
}

.score-increase {
  color: #67c23a;
}

.score-decrease {
  color: #f56c6c;
}

.score-excellent {
  color: #67c23a;
}

.score-good {
  color: #409eff;
}

.score-normal {
  color: #e6a23c;
}

.score-low {
  color: #f56c6c;
}
</style>

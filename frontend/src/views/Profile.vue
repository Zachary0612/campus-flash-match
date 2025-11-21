<template>
  <Layout>
    <div class="profile-page">
      <el-row :gutter="24">
        <!-- 用户信息卡片 -->
        <el-col :span="8">
          <el-card shadow="hover" class="user-card">
            <div class="user-avatar">
              <el-avatar :size="100">{{ userStore.nickname.charAt(0) }}</el-avatar>
            </div>
            <div class="user-name">{{ userStore.nickname }}</div>
            <div class="user-id">ID: {{ userStore.userId }}</div>
            
            <el-divider />
            
            <div class="user-stats">
              <div class="stat-item">
                <div class="stat-label">信用分</div>
                <div class="stat-value" :class="getScoreClass(userStore.creditScore)">
                  {{ userStore.creditScore }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 位置绑定 -->
        <el-col :span="16">
          <el-card shadow="hover" class="mb-6">
            <template #header>
              <span class="text-lg font-semibold">校园位置绑定</span>
            </template>
            
            <el-form :model="locationForm" label-width="100px">
              <el-form-item label="当前位置">
                <el-select
                  v-model="locationForm.pointId"
                  placeholder="请选择校园点位"
                  class="w-full"
                  :loading="pointsLoading"
                >
                  <el-option 
                    v-for="point in campusPoints" 
                    :key="point.id" 
                    :label="point.pointName" 
                    :value="point.id" 
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="handleBindLocation" :loading="bindLoading">
                  更新位置
                </el-button>
              </el-form-item>
            </el-form>
            
            <el-alert
              title="位置说明"
              type="info"
              :closable="false"
            >
              绑定校园位置后，系统将根据您的位置推荐附近的事件
            </el-alert>
          </el-card>
          
          <!-- WebSocket连接状态 -->
          <el-card shadow="hover">
            <template #header>
              <span class="text-lg font-semibold">实时通知</span>
            </template>
            
            <div class="connection-status">
              <div class="status-item">
                <span class="status-label">WebSocket状态：</span>
                <el-tag :type="wsStore.connected ? 'success' : 'danger'" size="small">
                  {{ wsStore.connected ? '已连接' : '未连接' }}
                </el-tag>
              </div>
              
              <div class="mt-4">
                <el-button
                  v-if="!wsStore.connected"
                  type="primary"
                  @click="wsStore.connect()"
                >
                  连接
                </el-button>
                <el-button
                  v-else
                  type="danger"
                  @click="wsStore.disconnect()"
                >
                  断开
                </el-button>
              </div>
            </div>
            
            <el-alert
              title="实时通知说明"
              type="info"
              :closable="false"
              class="mt-4"
            >
              <ul class="tips-list">
                <li>事件满员时会收到实时通知</li>
                <li>事件结算时会收到结算结果</li>
                <li>新成员加入时会收到提醒</li>
              </ul>
            </el-alert>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { useUserStore } from '@/stores/user'
import { useWebSocketStore } from '@/stores/websocket'
import { bindLocation, getCampusPoints } from '@/api/user'

const userStore = useUserStore()
const wsStore = useWebSocketStore()

const bindLoading = ref(false)
const pointsLoading = ref(false)
const campusPoints = ref([])

const locationForm = reactive({
  pointId: null
})

// 获取校园点位列表
const loadCampusPoints = async () => {
  pointsLoading.value = true
  try {
    const res = await getCampusPoints()
    if (res.code === 200) {
      campusPoints.value = res.data
    } else {
      ElMessage.error('加载校园点位失败: ' + res.message)
    }
  } catch (error) {
    console.error('加载校园点位失败:', error)
    ElMessage.error('加载校园点位失败: ' + (error.message || '未知错误'))
  } finally {
    pointsLoading.value = false
  }
}

const handleBindLocation = async () => {
  if (!locationForm.pointId) {
    ElMessage.warning('请选择校园点位')
    return
  }
  
  bindLoading.value = true
  try {
    const res = await bindLocation(locationForm.pointId)
    if (res.code === 200) {
      ElMessage.success('位置绑定成功')
    } else {
      ElMessage.error('绑定位置失败: ' + res.message)
    }
  } catch (error) {
    console.error('绑定位置失败:', error)
    ElMessage.error('绑定位置失败: ' + (error.message || '未知错误'))
  } finally {
    bindLoading.value = false
  }
}

const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-normal'
  return 'score-low'
}

// 组件挂载时加载校园点位
onMounted(() => {
  loadCampusPoints()
})
</script>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
}

.user-card {
  text-align: center;
}

.user-avatar {
  margin: 24px 0;
}

.user-name {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.user-id {
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
}

.user-stats {
  padding: 16px 0;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
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

.connection-status {
  padding: 16px 0;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-label {
  font-size: 15px;
  color: #606266;
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

.tips-list li::before {
  content: "• ";
  color: #409eff;
  font-weight: bold;
  margin-right: 8px;
}
</style>
<template>
  <Layout>
    <div class="profile-page relative z-10">
      <el-row :gutter="24">
        <!-- 用户信息卡片 -->
        <el-col :span="8" class="mb-6">
          <div class="glass rounded-2xl p-8 text-center shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.1s">
            <div class="relative inline-block mb-6 group">
              <div class="absolute inset-0 bg-gradient-to-br from-primary to-purple-500 rounded-full blur opacity-50"></div>
              <el-avatar :size="120" :src="userStore.avatar" class="relative border-4 border-white shadow-md text-4xl font-bold bg-gradient-to-br from-primary to-blue-400">
                {{ userStore.nickname?.charAt(0) }}
              </el-avatar>
              <el-upload
                class="avatar-uploader absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
                :action="uploadAction"
                :show-file-list="false"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <div class="w-full h-full rounded-full bg-black/50 flex items-center justify-center">
                  <el-icon class="text-white text-2xl"><Camera /></el-icon>
                </div>
              </el-upload>
            </div>
            <div class="user-name text-2xl font-bold text-gray-800 mb-2">{{ userStore.nickname }}</div>
            <div class="user-id text-sm text-gray-500 bg-gray-100/50 inline-block px-3 py-1 rounded-full mb-6">ID: {{ userStore.userId }}</div>
            
            <div class="border-t border-gray-200/50 pt-6">
              <div class="user-stats">
                <div class="stat-item">
                  <div class="stat-label text-gray-500 text-sm mb-2 uppercase tracking-wider">信用分</div>
                  <div class="stat-value text-5xl font-black" :class="getScoreClass(userStore.creditScore)">
                    {{ userStore.creditScore }}
                  </div>
                  <div class="mt-2 text-xs text-gray-400">信用良好</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
        
        <!-- 位置绑定 -->
        <el-col :span="16">
          <div class="glass rounded-2xl p-6 mb-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.2s">
            <div class="flex items-center mb-6 border-b border-white/20 pb-4">
              <el-icon class="text-xl mr-2 text-primary"><Location /></el-icon>
              <span class="text-lg font-bold text-gray-800">校园位置绑定</span>
            </div>
            
            <el-form :model="locationForm" label-width="0" class="mb-6">
              <div class="flex gap-4 items-end">
                <el-form-item class="flex-1 mb-0">
                  <div class="text-sm text-gray-600 mb-2 font-medium">当前位置</div>
                  <el-select
                    v-model="locationForm.pointId"
                    placeholder="请选择校园点位"
                    class="w-full glass-select"
                    :loading="pointsLoading"
                    size="large"
                  >
                    <el-option 
                      v-for="point in campusPoints" 
                      :key="point.id" 
                      :label="point.pointName" 
                      :value="point.id" 
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item class="mb-0">
                  <el-button type="primary" size="large" @click="handleBindLocation" :loading="bindLoading" class="!rounded-lg shadow-md shadow-blue-200">
                    更新位置
                  </el-button>
                </el-form-item>
              </div>
            </el-form>
            
            <div class="bg-blue-50/50 rounded-xl p-4 border border-blue-100">
               <div class="flex items-start">
                 <el-icon class="text-blue-500 mt-1 mr-2"><InfoFilled /></el-icon>
                 <div class="text-sm text-blue-700">
                   <p class="font-semibold mb-1">位置说明</p>
                   <p class="opacity-80">绑定校园位置后，系统将根据您的位置推荐附近的事件，助您更快找到组织。</p>
                 </div>
               </div>
            </div>
          </div>
          
          <!-- WebSocket连接状态 -->
          <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.3s">
            <div class="flex items-center mb-6 border-b border-white/20 pb-4">
              <el-icon class="text-xl mr-2 text-warning"><Bell /></el-icon>
              <span class="text-lg font-bold text-gray-800">实时通知</span>
            </div>
            
            <div class="flex items-center justify-between mb-6 bg-white/40 p-4 rounded-xl">
              <div class="status-item flex items-center gap-3">
                <span class="status-label font-medium text-gray-700">WebSocket状态</span>
                <div class="flex items-center px-3 py-1 rounded-full text-sm font-bold" :class="wsStore.connected ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'">
                  <div class="w-2 h-2 rounded-full mr-2" :class="wsStore.connected ? 'bg-green-500 animate-pulse' : 'bg-red-500'"></div>
                  {{ wsStore.connected ? '已连接' : '未连接' }}
                </div>
              </div>
              
              <div>
                <el-button
                  v-if="!wsStore.connected"
                  type="primary"
                  @click="wsStore.connect()"
                  class="!rounded-lg"
                >
                  连接服务
                </el-button>
                <el-button
                  v-else
                  type="danger"
                  plain
                  @click="wsStore.disconnect()"
                  class="!rounded-lg"
                >
                  断开连接
                </el-button>
              </div>
            </div>
            
            <div class="bg-gray-50/50 rounded-xl p-4 border border-gray-100">
               <div class="flex items-start">
                 <el-icon class="text-gray-500 mt-1 mr-2"><InfoFilled /></el-icon>
                 <div class="text-sm text-gray-600">
                   <p class="font-semibold mb-2">通知功能说明</p>
                   <ul class="space-y-1 pl-1">
                     <li class="flex items-center"><div class="w-1 h-1 rounded-full bg-gray-400 mr-2"></div>事件满员时会收到实时通知</li>
                     <li class="flex items-center"><div class="w-1 h-1 rounded-full bg-gray-400 mr-2"></div>事件结算时会收到结算结果</li>
                     <li class="flex items-center"><div class="w-1 h-1 rounded-full bg-gray-400 mr-2"></div>新成员加入时会收到提醒</li>
                   </ul>
                 </div>
               </div>
            </div>
          </div>
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
import { bindLocation, getCampusPoints, updateAvatar } from '@/api/user'
import { uploadFile } from '@/api/file'
import { Location, InfoFilled, Bell, Camera } from '@element-plus/icons-vue'

const userStore = useUserStore()
const wsStore = useWebSocketStore()

const bindLoading = ref(false)
const pointsLoading = ref(false)
const campusPoints = ref([])
const uploadAction = '/api/file/upload'

const locationForm = reactive({
  pointId: null
})

// 头像上传
const handleAvatarUpload = async (options) => {
  const { file, onError, onSuccess } = options
  const formData = new FormData()
  formData.append('file', file)
  try {
    // 先上传文件
    const uploadRes = await uploadFile(formData)
    const avatarUrl = uploadRes.data
    
    // 再更新用户头像
    const res = await updateAvatar(avatarUrl)
    if (res.code === 200) {
      userStore.setAvatar(avatarUrl)
      ElMessage.success('头像更新成功')
      onSuccess(res, file)
    } else {
      ElMessage.error('更新头像失败: ' + res.message)
      onError(new Error(res.message))
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error(error?.response?.data?.message || '上传失败')
    onError(error)
  }
}

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
  if (score >= 90) return 'text-emerald-500'
  if (score >= 80) return 'text-blue-500'
  if (score >= 60) return 'text-yellow-500'
  return 'text-red-500'
}

// 组件挂载时加载校园点位
onMounted(() => {
  loadCampusPoints()
})
</script>

<style scoped>
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
  padding-bottom: 40px;
}

/* Glass select customization */
:deep(.glass-select .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  box-shadow: none;
  border: 1px solid #e5e7eb;
}

:deep(.glass-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary);
  background-color: white;
}
</style>
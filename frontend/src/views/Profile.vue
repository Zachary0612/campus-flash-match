<template>
  <Layout>
    <div class="profile-page relative z-10">
      <el-row :gutter="24">
        <!-- 用户信息卡片 -->
        <el-col :span="8" class="mb-6">
          <div class="glass rounded-2xl p-8 text-center shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.1s">
            <div class="relative inline-block mb-4 group">
              <div class="absolute inset-0 bg-gradient-to-br from-primary to-purple-500 rounded-full blur opacity-50"></div>
              <el-avatar :size="100" :src="userStore.avatar" class="relative border-4 border-white shadow-md text-3xl font-bold bg-gradient-to-br from-primary to-blue-400">
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
            <div class="user-name text-xl font-bold text-gray-800 mb-1">{{ userStore.nickname }}</div>
            <div class="user-id text-xs text-gray-500 bg-gray-100/50 inline-block px-2 py-0.5 rounded-full mb-3">ID: {{ userStore.userId }}</div>
            
            <!-- 个人简介 -->
            <div v-if="profileData.bio" class="text-sm text-gray-600 mb-3 px-2">{{ profileData.bio }}</div>
            
            <!-- 基本信息标签 -->
            <div v-if="profileData.gender || profileData.grade || profileData.major" class="flex flex-wrap justify-center gap-2 mb-4">
              <el-tag v-if="profileData.gender === 1 || profileData.gender === 2" size="small" type="info">
                {{ profileData.gender === 1 ? '男' : '女' }}
              </el-tag>
              <el-tag v-if="profileData.grade" size="small" type="success">{{ profileData.grade }}</el-tag>
              <el-tag v-if="profileData.major" size="small" type="warning">{{ profileData.major }}</el-tag>
            </div>
            
            <!-- 兴趣标签 -->
            <div v-if="profileData.interests" class="flex flex-wrap justify-center gap-1 mb-4">
              <el-tag 
                v-for="interest in profileData.interests.split(',')" 
                :key="interest" 
                size="small" 
                effect="plain"
                class="!bg-blue-50 !text-blue-600 !border-blue-200"
              >
                {{ interest.trim() }}
              </el-tag>
            </div>
            
            <div class="border-t border-gray-200/50 pt-4">
              <div class="user-stats">
                <div class="stat-item">
                  <div class="stat-label text-gray-500 text-xs mb-1 uppercase tracking-wider">信用分</div>
                  <div class="stat-value text-4xl font-black" :class="getScoreClass(userStore.creditScore)">
                    {{ userStore.creditScore }}
                  </div>
                  <div class="mt-1 text-xs text-gray-400">{{ getCreditLevel(userStore.creditScore) }}</div>
                </div>
              </div>
            </div>
            
            <!-- 编辑资料按钮 -->
            <el-button type="primary" plain class="mt-4 !rounded-lg w-full" @click="showEditDialog = true">
              <el-icon class="mr-1"><Edit /></el-icon>
              编辑资料
            </el-button>
          </div>
        </el-col>
        
        <!-- 位置绑定 -->
        <el-col :span="16">
          <div class="glass rounded-2xl p-6 mb-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.2s">
            <div class="flex items-center mb-6 border-b border-white/20 pb-4">
              <el-icon class="text-xl mr-2 text-primary"><Location /></el-icon>
              <span class="text-lg font-bold text-gray-800">校园位置绑定</span>
            </div>
            
            <!-- 位置选择模式切换 -->
            <div class="mb-4">
              <el-radio-group v-model="profileLocationMode" size="small">
                <el-radio-button label="point">选择点位</el-radio-button>
                <el-radio-button label="map">地图选点</el-radio-button>
              </el-radio-group>
            </div>

            <el-form :model="locationForm" label-width="0" class="mb-6">
              <!-- 点位选择模式 -->
              <div v-if="profileLocationMode === 'point'" class="flex gap-4 items-end">
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

              <!-- 地图选点模式 -->
              <div v-else>
                <MapPicker 
                  v-model:location="profileMapLocation"
                  height="300px"
                  @change="handleProfileLocationChange"
                />
                <div class="mt-4 flex justify-end">
                  <el-button type="primary" size="large" @click="handleBindMapLocation" :loading="bindLoading" :disabled="!profileMapLocation" class="!rounded-lg shadow-md shadow-blue-200">
                    保存地图位置
                  </el-button>
                </div>
              </div>
            </el-form>
            
            <!-- 已保存的地图位置显示 -->
            <div v-if="savedMapLocation" class="mb-4 bg-green-50/50 rounded-xl p-4 border border-green-100">
              <div class="flex items-start">
                <el-icon class="text-green-500 mt-1 mr-2"><Location /></el-icon>
                <div class="flex-1">
                  <p class="text-sm font-semibold text-green-700 mb-1">已绑定位置</p>
                  <p class="text-sm text-green-600">{{ savedMapLocation.address }}</p>
                  <p class="text-xs text-gray-400 mt-1">经度: {{ savedMapLocation.lng?.toFixed(6) }}, 纬度: {{ savedMapLocation.lat?.toFixed(6) }}</p>
                </div>
              </div>
            </div>
            
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
    
    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑个人资料" width="500px" class="profile-edit-dialog">
      <el-form :model="editForm" label-width="80px" label-position="left">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
        </el-form-item>
        
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="0">保密</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="年级">
          <el-select v-model="editForm.grade" placeholder="请选择年级" class="w-full">
            <el-option label="大一" value="大一" />
            <el-option label="大二" value="大二" />
            <el-option label="大三" value="大三" />
            <el-option label="大四" value="大四" />
            <el-option label="研一" value="研一" />
            <el-option label="研二" value="研二" />
            <el-option label="研三" value="研三" />
            <el-option label="博士" value="博士" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="专业">
          <el-input v-model="editForm.major" placeholder="请输入专业" maxlength="30" />
        </el-form-item>
        
        <el-form-item label="个人简介">
          <el-input 
            v-model="editForm.bio" 
            type="textarea" 
            :rows="3" 
            placeholder="介绍一下自己吧~" 
            maxlength="200" 
            show-word-limit 
          />
        </el-form-item>
        
        <el-form-item label="兴趣标签">
          <div class="w-full">
            <div class="flex flex-wrap gap-2 mb-2">
              <el-tag 
                v-for="tag in interestTags" 
                :key="tag" 
                closable 
                @close="removeInterest(tag)"
                class="!bg-blue-50 !text-blue-600 !border-blue-200"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="flex gap-2">
              <el-input 
                v-model="newInterest" 
                placeholder="输入兴趣标签" 
                size="small"
                @keyup.enter="addInterest"
                class="flex-1"
              />
              <el-button size="small" type="primary" @click="addInterest">添加</el-button>
            </div>
            <div class="text-xs text-gray-400 mt-2">推荐：运动、音乐、游戏、美食、旅行、摄影、阅读、电影</div>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveProfile" :loading="saveLoading">保存</el-button>
      </template>
    </el-dialog>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import MapPicker from '@/components/MapPicker.vue'
import { useUserStore } from '@/stores/user'
import { useWebSocketStore } from '@/stores/websocket'
import { bindLocation, getCampusPoints, updateAvatar } from '@/api/user'
import { uploadFile } from '@/api/file'
import { getMyProfile, updateProfile } from '@/api/profile'
import { Location, InfoFilled, Bell, Camera, Edit } from '@element-plus/icons-vue'

const userStore = useUserStore()
const wsStore = useWebSocketStore()

const bindLoading = ref(false)
const pointsLoading = ref(false)
const campusPoints = ref([])
const uploadAction = '/api/file/upload'

const locationForm = reactive({
  pointId: null
})

// 地图选点相关
const profileLocationMode = ref('point')
const profileMapLocation = ref(null)

// 处理地图位置变化
const handleProfileLocationChange = (location) => {
  profileMapLocation.value = location
}

// 保存地图位置
const handleBindMapLocation = async () => {
  if (!profileMapLocation.value) {
    ElMessage.warning('请先在地图上选择位置')
    return
  }
  
  bindLoading.value = true
  try {
    // 将地图位置保存到用户资料中
    const res = await updateProfile({
      mapLocation: {
        lng: profileMapLocation.value.lng,
        lat: profileMapLocation.value.lat,
        address: profileMapLocation.value.address
      }
    })
    if (res.code === 200) {
      ElMessage.success('地图位置保存成功')
      // 重新加载个人资料以更新显示
      await loadProfile()
    }
  } catch (error) {
    console.error('保存地图位置失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    bindLoading.value = false
  }
}

// 计算属性：已保存的地图位置
const savedMapLocation = computed(() => {
  if (profileData.extMeta && profileData.extMeta.campusLocation) {
    return profileData.extMeta.campusLocation
  }
  return null
})

// 个人资料数据
const profileData = reactive({
  bio: '',
  gender: 0,
  major: '',
  grade: '',
  interests: '',
  extMeta: null
})

// 编辑对话框
const showEditDialog = ref(false)
const saveLoading = ref(false)
const newInterest = ref('')

const editForm = reactive({
  nickname: '',
  bio: '',
  gender: 0,
  major: '',
  grade: '',
  interests: ''
})

// 兴趣标签数组
const interestTags = computed(() => {
  if (!editForm.interests) return []
  return editForm.interests.split(',').map(t => t.trim()).filter(t => t)
})

// 添加兴趣标签
const addInterest = () => {
  const tag = newInterest.value.trim()
  if (!tag) return
  if (interestTags.value.includes(tag)) {
    ElMessage.warning('该标签已存在')
    return
  }
  if (interestTags.value.length >= 10) {
    ElMessage.warning('最多添加10个标签')
    return
  }
  const tags = [...interestTags.value, tag]
  editForm.interests = tags.join(',')
  newInterest.value = ''
}

// 移除兴趣标签
const removeInterest = (tag) => {
  const tags = interestTags.value.filter(t => t !== tag)
  editForm.interests = tags.join(',')
}

// 加载个人资料
const loadProfile = async () => {
  try {
    const res = await getMyProfile()
    if (res.code === 200) {
      const data = res.data
      profileData.bio = data.bio || ''
      profileData.gender = data.gender || 0
      profileData.major = data.major || ''
      profileData.grade = data.grade || ''
      profileData.interests = data.interests || ''
      profileData.extMeta = data.extMeta || null
      
      // 同步到编辑表单
      editForm.nickname = data.nickname || userStore.nickname
      editForm.bio = data.bio || ''
      editForm.gender = data.gender || 0
      editForm.major = data.major || ''
      editForm.grade = data.grade || ''
      editForm.interests = data.interests || ''
    }
  } catch (error) {
    console.error('加载个人资料失败:', error)
  }
}

// 保存个人资料
const handleSaveProfile = async () => {
  saveLoading.value = true
  try {
    const res = await updateProfile({
      nickname: editForm.nickname,
      bio: editForm.bio,
      gender: editForm.gender,
      major: editForm.major,
      grade: editForm.grade,
      interests: editForm.interests
    })
    if (res.code === 200) {
      ElMessage.success('资料更新成功')
      showEditDialog.value = false
      // 更新本地数据
      profileData.bio = editForm.bio
      profileData.gender = editForm.gender
      profileData.major = editForm.major
      profileData.grade = editForm.grade
      profileData.interests = editForm.interests
      // 更新store中的昵称
      if (editForm.nickname !== userStore.nickname) {
        userStore.setNickname(editForm.nickname)
      }
    } else {
      ElMessage.error('更新失败: ' + res.message)
    }
  } catch (error) {
    console.error('更新资料失败:', error)
    ElMessage.error('更新失败')
  } finally {
    saveLoading.value = false
  }
}

// 获取信用等级
const getCreditLevel = (score) => {
  if (score >= 90) return '信用极好'
  if (score >= 80) return '信用良好'
  if (score >= 60) return '信用一般'
  return '信用较差'
}

// 获取信用分颜色
const getScoreClass = (score) => {
  if (score >= 90) return 'text-emerald-500'
  if (score >= 80) return 'text-blue-500'
  if (score >= 60) return 'text-yellow-500'
  return 'text-red-500'
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

// 绑定位置
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

// 组件挂载时加载数据
onMounted(() => {
  loadCampusPoints()
  loadProfile()
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
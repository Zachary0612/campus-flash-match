<template>
  <Layout>
    <div class="beacon-page relative z-10">
      <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 min-h-[80vh] animate-slide-up" style="animation-delay: 0.1s">
        <div class="flex items-center justify-between mb-6 border-b border-white/20 pb-4">
          <span class="text-xl font-bold text-gray-800 flex items-center">
            <el-icon class="mr-2 text-warning"><LocationInformation /></el-icon>
            占位信标管理
          </span>
          <el-button type="primary" @click="showPublishDialog" class="!rounded-lg shadow-md shadow-blue-200">
            <el-icon class="mr-1"><Plus /></el-icon>
            发布信标
          </el-button>
        </div>
        
        <el-alert
          title="信标说明"
          type="warning"
          :closable="false"
          class="mb-6 !bg-orange-50/80 !border-orange-100"
        >
          <template #default>
            <div class="text-orange-800 text-sm">
              <p class="mb-1 flex items-center"><el-icon class="mr-1"><InfoFilled /></el-icon> 占位信标用于标记图书馆座位、食堂排队等临时占位信息</p>
              <p class="flex items-center"><el-icon class="mr-1"><Warning /></el-icon> 发布虚假信标将被扣除信用分，请诚信使用</p>
            </div>
          </template>
        </el-alert>
        
        <div v-if="loading" class="text-center py-16">
          <el-icon class="is-loading text-primary" :size="40"><Loading /></el-icon>
        </div>
        
        <div v-else-if="beaconList.length === 0" class="text-center py-16 text-gray-500">
          <div class="bg-gray-100/50 w-24 h-24 rounded-full flex items-center justify-center mx-auto mb-4">
            <el-icon :size="40" class="text-gray-400"><DocumentDelete /></el-icon>
          </div>
          <p class="text-lg font-medium">暂无信标信息</p>
        </div>
        
        <div v-else class="beacon-grid">
          <div
            v-for="(beacon, index) in beaconList"
            :key="beacon.eventId"
            class="beacon-card bg-white/80 backdrop-blur-sm rounded-xl p-5 border border-white/60 shadow-sm hover:shadow-xl transition-all duration-300 hover:-translate-y-1 group"
            :style="{ animationDelay: `${index * 0.05}s` }"
          >
            <div class="beacon-header flex justify-between items-start mb-3">
              <div class="flex items-center">
                 <div class="w-2 h-2 rounded-full bg-warning mr-2 animate-pulse"></div>
                 <el-tag type="warning" size="small" effect="dark" class="!rounded-full">信标</el-tag>
              </div>
              <el-button
                type="danger"
                size="small"
                text
                bg
                class="!px-2 opacity-0 group-hover:opacity-100 transition-opacity"
                @click="handleReport(beacon.eventId)"
              >
                举报
              </el-button>
            </div>
            
            <div class="beacon-content">
              <p class="location-desc text-gray-800 font-medium text-lg mb-4 leading-relaxed">{{ beacon.title }}</p>
              <div class="beacon-info flex gap-4 text-sm text-gray-500 bg-gray-50/80 p-3 rounded-lg">
                <div class="info-item flex items-center">
                  <el-icon class="mr-1"><Clock /></el-icon>
                  <span>{{ formatTime(beacon.createTime) }}</span>
                </div>
                <div class="info-item flex items-center">
                  <el-icon class="mr-1"><Location /></el-icon>
                  <span>{{ beacon.distance }}m</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 发布信标对话框 -->
      <el-dialog
        v-model="publishDialogVisible"
        title="发布占位信标"
        width="500px"
        class="custom-dialog rounded-2xl"
      >
        <el-form :model="beaconForm" :rules="beaconRules" ref="beaconFormRef" label-width="100px" class="pt-4">
          <el-form-item label="位置描述" prop="locationDesc">
            <el-input
              v-model="beaconForm.locationDesc"
              type="textarea"
              :rows="3"
              placeholder="请详细描述占位位置，如：图书馆3楼靠窗座位"
              class="custom-textarea"
            />
          </el-form-item>
          
          <el-form-item label="有效时长" prop="expireMinutes">
            <el-input-number v-model="beaconForm.expireMinutes" :min="10" :max="240" class="!w-[calc(100%-40px)]" />
            <span class="ml-2 text-gray-500">分钟</span>
          </el-form-item>
          
          <el-form-item label="校园点位" prop="pointId">
            <el-select v-model="beaconForm.pointId" placeholder="请选择点位" class="w-full custom-select">
              <el-option 
                v-for="point in campusPoints" 
                :key="point.id" 
                :label="point.pointName" 
                :value="point.id" 
              />
            </el-select>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="publishDialogVisible = false" class="!rounded-lg">取消</el-button>
            <el-button type="primary" @click="handlePublish" :loading="publishLoading" class="!rounded-lg shadow-lg shadow-blue-200">
              发布
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getNearbyEvents } from '@/api/event'
import { publishBeacon, reportBeacon } from '@/api/beacon'
import { getCampusPoints } from '@/api/user'
import dayjs from 'dayjs'
import { Plus, Loading, DocumentDelete, Clock, Location, LocationInformation, InfoFilled, Warning } from '@element-plus/icons-vue'

const loading = ref(false)
const publishLoading = ref(false)
const beaconList = ref([])
const publishDialogVisible = ref(false)
const beaconFormRef = ref(null)
const campusPoints = ref([])

const beaconForm = reactive({
  locationDesc: '',
  expireMinutes: 120,
  pointId: null
})

const beaconRules = {
  locationDesc: [{ required: true, message: '请输入位置描述', trigger: 'blur' }],
  expireMinutes: [{ required: true, message: '请输入有效时长', trigger: 'blur' }],
  pointId: [{ required: true, message: '请选择校园点位', trigger: 'change' }]
}

onMounted(() => {
  loadBeacons()
  loadCampusPoints()
})

const loadBeacons = async () => {
  loading.value = true
  try {
    const res = await getNearbyEvents('beacon')
    if (res.code === 200) {
      beaconList.value = res.data || []
    }
  } catch (error) {
    console.error('加载信标失败:', error)
  } finally {
    loading.value = false
  }
}

const loadCampusPoints = async () => {
  try {
    const res = await getCampusPoints()
    if (res.code === 200) {
      campusPoints.value = res.data
    }
  } catch (error) {
    console.error('加载校园点位失败:', error)
  }
}

const showPublishDialog = () => {
  beaconForm.locationDesc = ''
  beaconForm.expireMinutes = 120
  beaconForm.pointId = null
  publishDialogVisible.value = true
}

const handlePublish = async () => {
  if (!beaconFormRef.value) return
  
  await beaconFormRef.value.validate(async (valid) => {
    if (valid) {
      publishLoading.value = true
      try {
        const res = await publishBeacon(beaconForm)
        if (res.code === 200) {
          ElMessage.success('信标发布成功')
          publishDialogVisible.value = false
          loadBeacons()
        }
      } catch (error) {
        console.error('发布信标失败:', error)
      } finally {
        publishLoading.value = false
      }
    }
  })
}

const handleReport = async (eventId) => {
  try {
    await ElMessageBox.confirm(
      '确定要举报此信标为虚假信标吗？举报成功将扣除发布者信用分',
      '举报确认',
      {
        confirmButtonText: '确定举报',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await reportBeacon(eventId)
    if (res.code === 200) {
      ElMessage.success('举报成功')
      loadBeacons()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('举报失败:', error)
    }
  }
}

const formatTime = (time) => {
  return dayjs(time).format('MM-DD HH:mm')
}
</script>

<style scoped>
.beacon-page {
  max-width: 1200px;
  margin: 0 auto;
}

.beacon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.beacon-card {
  animation: fade-in 0.5s ease-out forwards;
  opacity: 0;
}

/* Custom Dialog Styles */
:deep(.custom-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f2f5;
}

:deep(.el-dialog__title) {
  font-weight: 700;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 24px;
}
</style>

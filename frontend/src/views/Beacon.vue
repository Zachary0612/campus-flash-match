<template>
  <Layout>
    <div class="beacon-page">
      <el-card shadow="hover">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="text-lg font-semibold">占位信标管理</span>
            <el-button type="primary" @click="showPublishDialog">
              <el-icon><Plus /></el-icon>
              发布信标
            </el-button>
          </div>
        </template>
        
        <el-alert
          title="信标说明"
          type="info"
          :closable="false"
          class="mb-4"
        >
          <p>占位信标用于标记图书馆座位、食堂排队等临时占位信息</p>
          <p>发布虚假信标将被扣除信用分，请诚信使用</p>
        </el-alert>
        
        <div v-if="loading" class="text-center py-8">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        </div>
        
        <div v-else-if="beaconList.length === 0" class="text-center py-8 text-gray-400">
          <el-icon :size="48"><DocumentDelete /></el-icon>
          <p class="mt-2">暂无信标信息</p>
        </div>
        
        <div v-else class="beacon-grid">
          <div
            v-for="beacon in beaconList"
            :key="beacon.eventId"
            class="beacon-card"
          >
            <div class="beacon-header">
              <el-tag type="warning" size="small">信标</el-tag>
              <el-button
                type="danger"
                size="small"
                text
                @click="handleReport(beacon.eventId)"
              >
                举报
              </el-button>
            </div>
            
            <div class="beacon-content">
              <p class="location-desc">{{ beacon.title }}</p>
              <div class="beacon-info">
                <div class="info-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(beacon.createTime) }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Location /></el-icon>
                  <span>{{ beacon.distance }}m</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 发布信标对话框 -->
      <el-dialog
        v-model="publishDialogVisible"
        title="发布占位信标"
        width="500px"
      >
        <el-form :model="beaconForm" :rules="beaconRules" ref="beaconFormRef" label-width="100px">
          <el-form-item label="位置描述" prop="locationDesc">
            <el-input
              v-model="beaconForm.locationDesc"
              type="textarea"
              :rows="3"
              placeholder="请详细描述占位位置，如：图书馆3楼靠窗座位"
            />
          </el-form-item>
          
          <el-form-item label="有效时长" prop="expireMinutes">
            <el-input-number v-model="beaconForm.expireMinutes" :min="10" :max="240" />
            <span class="ml-2 text-gray-500">分钟</span>
          </el-form-item>
          
          <el-form-item label="校园点位" prop="pointId">
            <el-select v-model="beaconForm.pointId" placeholder="请选择点位" class="w-full">
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
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePublish" :loading="publishLoading">
            发布
          </el-button>
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
import { Plus, Loading, DocumentDelete, Clock, Location } from '@element-plus/icons-vue'

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
  gap: 16px;
}

.beacon-card {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: white;
  transition: all 0.3s;
}

.beacon-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.beacon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.beacon-content {
  margin-top: 12px;
}

.location-desc {
  font-size: 15px;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.5;
}

.beacon-info {
  display: flex;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}
</style>

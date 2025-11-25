<template>
  <Layout>
    <div class="home-page">
      <!-- 快速操作区 -->
      <div class="quick-actions mb-6">
        <el-card shadow="hover">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="text-lg font-semibold">快速发起</span>
            </div>
          </template>
          
          <div class="grid grid-cols-3 gap-4">
            <el-button type="primary" @click="showCreateDialog('group_buy')" class="action-btn">
              <el-icon class="mr-2"><ShoppingCart /></el-icon>
              发起拼单
            </el-button>
            <el-button type="success" @click="showCreateDialog('meetup')" class="action-btn">
              <el-icon class="mr-2"><UserFilled /></el-icon>
              发起约伴
            </el-button>
            <el-button type="warning" @click="showBeaconDialog" class="action-btn">
              <el-icon class="mr-2"><LocationInformation /></el-icon>
              发布信标
            </el-button>
          </div>
        </el-card>
      </div>
      
      <!-- 附近事件 -->
      <el-card shadow="hover">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="text-lg font-semibold">附近事件</span>
            <div class="flex items-center gap-4">
              <el-select v-model="eventType" @change="loadNearbyEvents" size="small" style="width: 120px">
                <el-option label="全部" value="all" />
                <el-option label="拼单" value="group_buy" />
                <el-option label="约伴" value="meetup" />
                <el-option label="信标" value="beacon" />
              </el-select>
              <el-button size="small" @click="loadNearbyEvents" :loading="loading">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </div>
        </template>
        
        <div v-if="loading" class="text-center py-8">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        </div>
        
        <div v-else-if="nearbyEvents.length === 0" class="text-center py-8 text-gray-400">
          <el-icon :size="48"><DocumentDelete /></el-icon>
          <p class="mt-2">暂无附近事件</p>
        </div>
        
        <div v-else class="events-grid">
          <div
            v-for="event in nearbyEvents"
            :key="event.eventId"
            class="event-card"
          >
            <div class="event-header">
              <el-tag :type="getEventTypeTag(event.eventType)" size="small">
                {{ getEventTypeName(event.eventType) }}
              </el-tag>
              <span class="distance">{{ event.distance }}m</span>
            </div>
            
            <h3 class="event-title">{{ event.title }}</h3>
            
            <div class="event-info">
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span>{{ event.currentNum }}/{{ event.targetNum }}</span>
              </div>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(event.createTime) }}</span>
              </div>
            </div>

            <p v-if="event.description" class="event-desc">
              {{ event.description.substring(0, 60) }}
              <span v-if="event.description.length > 60">...</span>
            </p>

            <div v-if="event.mediaUrls && event.mediaUrls.length" class="media-preview">
              <template v-for="(url, mediaIndex) in event.mediaUrls.slice(0, 3)" :key="mediaIndex">
                <video
                  v-if="isVideo(url)"
                  :src="url"
                  controls
                  class="media-item video"
                />
                <img
                  v-else
                  :src="url"
                  alt="event media"
                  class="media-item image"
                />
              </template>
            </div>
            
            <el-button
              type="primary"
              size="small"
              class="w-full mt-3"
              @click="handleJoinEvent(event.eventId)"
              :disabled="event.currentNum >= event.targetNum"
            >
              {{ event.currentNum >= event.targetNum ? '已满员' : '参与' }}
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 创建事件对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="eventForm" :rules="eventRules" ref="eventFormRef" label-width="100px">
        <el-form-item label="事件标题" prop="title">
          <el-input v-model="eventForm.title" placeholder="请输入事件标题" />
        </el-form-item>
        
        <el-form-item label="目标人数" prop="targetNum">
          <el-input-number v-model="eventForm.targetNum" :min="2" :max="20" />
        </el-form-item>
        
        <el-form-item label="过期时间" prop="expireMinutes">
          <el-input-number v-model="eventForm.expireMinutes" :min="1" :max="1440" />
          <span class="ml-2 text-gray-500">分钟</span>
        </el-form-item>
        
        <el-form-item label="校园点位" prop="pointId">
          <el-select v-model="eventForm.pointId" placeholder="请选择点位" class="w-full">
            <el-option 
              v-for="point in campusPoints" 
              :key="point.id" 
              :label="point.pointName" 
              :value="point.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="事件说明">
          <el-input
            v-model="eventForm.description"
            type="textarea"
            :rows="3"
            placeholder="可填写补充说明，最多200字"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="图片/视频">
          <el-upload
            class="media-uploader"
            :action="uploadAction"
            list-type="picture-card"
            :file-list="mediaFileList"
            :limit="6"
            :on-remove="handleMediaRemove"
            :http-request="handleMediaUpload"
            :on-preview="handlePreview"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip text-gray-500 text-sm">
                支持 jpg/png/gif/webp 图片或 mp4/mov/webm 视频，单个不超过10MB，最多6个
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreateEvent">创建</el-button>
      </template>
    </el-dialog>

    <!-- 发布信标对话框 -->
    <el-dialog
      v-model="beaconDialogVisible"
      title="发布信标"
      width="500px"
    >
      <el-form :model="beaconForm" :rules="beaconRules" ref="beaconFormRef" label-width="100px">
        <el-form-item label="位置描述" prop="locationDesc">
          <el-input v-model="beaconForm.locationDesc" placeholder="请输入位置描述" />
        </el-form-item>
        
        <el-form-item label="有效时长" prop="expireMinutes">
          <el-input-number v-model="beaconForm.expireMinutes" :min="1" :max="1440" />
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
        <el-button @click="beaconDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="beaconLoading" @click="handlePublishBeacon">发布</el-button>
      </template>
    </el-dialog>

    <!-- 媒体预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="预览"
      width="600px"
      class="media-preview-dialog"
    >
      <video
        v-if="isVideo(previewUrl)"
        :src="previewUrl"
        controls
        class="preview-video"
      />
      <img
        v-else
        :src="previewUrl"
        alt="preview"
        class="preview-image"
      />
    </el-dialog>
  </Layout>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import dayjs from 'dayjs'
  import Layout from '@/components/Layout.vue'
  import { getNearbyEvents, createEvent, joinEvent } from '@/api/event'
  import { getCampusPoints } from '@/api/user'
  import { publishBeacon } from '@/api/beacon'
  import { uploadFile } from '@/api/file'
  import { ShoppingCart, UserFilled, LocationInformation, Refresh, Loading, DocumentDelete, User, Clock, Plus } from '@element-plus/icons-vue'

  const loading = ref(false)
  const createLoading = ref(false)
  const beaconLoading = ref(false)
  const eventType = ref('all')
  const nearbyEvents = ref([])
  const campusPoints = ref([])

  const createDialogVisible = ref(false)
  const beaconDialogVisible = ref(false)
  const dialogTitle = ref('')
  const eventFormRef = ref(null)
  const beaconFormRef = ref(null)

  const mediaFileList = ref([])
  const uploadAction = '/api/file/upload'
  const previewDialogVisible = ref(false)
  const previewUrl = ref('')

  const eventForm = reactive({
    title: '',
    eventType: '',
    targetNum: 2,
    expireMinutes: 60,
    pointId: null,
    description: '',
    mediaUrls: [],
    extMeta: {}
  })

  const beaconForm = reactive({
    locationDesc: '',
    expireMinutes: 120,
    pointId: null
  })

  const eventRules = {
    title: [{ required: true, message: '请输入事件标题', trigger: 'blur' }],
    targetNum: [{ required: true, message: '请输入目标人数', trigger: 'blur' }],
    expireMinutes: [{ required: true, message: '请输入过期时间', trigger: 'blur' }],
    pointId: [{ required: true, message: '请选择校园点位', trigger: 'change' }]
  }

  const beaconRules = {
    locationDesc: [{ required: true, message: '请输入位置描述', trigger: 'blur' }],
    expireMinutes: [{ required: true, message: '请输入有效时长', trigger: 'blur' }],
    pointId: [{ required: true, message: '请选择校园点位', trigger: 'change' }]
  }

  onMounted(() => {
    loadNearbyEvents()
    loadCampusPoints()
  })

  const loadNearbyEvents = async () => {
    loading.value = true
    try {
      const eventTypeParam = eventType.value === 'all' ? undefined : eventType.value;
      const res = await getNearbyEvents(eventTypeParam)
      if (res.code === 200) {
        nearbyEvents.value = res.data || []
      }
    } catch (error) {
      console.error('加载附近事件失败:', error)
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

  const showCreateDialog = (type) => {
    eventForm.eventType = type
    dialogTitle.value = type === 'group_buy' ? '发起拼单' : '发起约伴'
    eventForm.title = ''
    eventForm.targetNum = 2
    eventForm.expireMinutes = 60
    eventForm.pointId = null
    eventForm.description = ''
    eventForm.mediaUrls = []
    mediaFileList.value = []
    createDialogVisible.value = true
  }

  const showBeaconDialog = () => {
    beaconForm.locationDesc = ''
    beaconForm.expireMinutes = 120
    beaconForm.pointId = null
    beaconDialogVisible.value = true
  }

  const handleCreateEvent = async () => {
    if (!eventFormRef.value) return
    
    await eventFormRef.value.validate(async (valid) => {
      if (valid) {
        createLoading.value = true
        try {
          const res = await createEvent(eventForm)
          if (res.code === 200) {
            ElMessage.success('事件创建成功')
            createDialogVisible.value = false
            loadNearbyEvents()
          }
        } catch (error) {
          console.error('创建事件失败:', error)
        } finally {
          createLoading.value = false
        }
      }
    })
  }

  const handlePublishBeacon = async () => {
    if (!beaconFormRef.value) return
    
    await beaconFormRef.value.validate(async (valid) => {
      if (valid) {
        beaconLoading.value = true
        try {
          const res = await publishBeacon(beaconForm)
          if (res.code === 200) {
            ElMessage.success('信标发布成功')
            beaconDialogVisible.value = false
            loadNearbyEvents()
          }
        } catch (error) {
          console.error('发布信标失败:', error)
        } finally {
          beaconLoading.value = false
        }
      }
    })
  }

  const handleJoinEvent = async (eventId) => {
    try {
      const res = await joinEvent(eventId)
      if (res.code === 200) {
        ElMessage.success('参与成功')
        // 立即更新UI状态，避免等待接口返回
        const eventIndex = nearbyEvents.value.findIndex(event => event.eventId === eventId)
        if (eventIndex !== -1) {
          // 更新当前人数
          nearbyEvents.value[eventIndex].currentNum = res.data.currentParticipants
          // 如果满员，禁用按钮
          if (res.data.currentParticipants >= res.data.maxParticipants) {
            nearbyEvents.value[eventIndex].currentNum = res.data.maxParticipants
          }
        }
        // 重新加载附近事件列表，确保数据同步
        loadNearbyEvents()
      }
    } catch (error) {
      console.error('参与事件失败:', error)
      if (error.response?.data?.message) {
        ElMessage.error(error.response.data.message)
      } else if (error.message) {
        ElMessage.error(error.message)
      } else {
        ElMessage.error('参与事件失败，请重试')
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

  const formatTime = (time) => {
    return dayjs(time).format('MM-DD HH:mm')
  }

  const isVideo = (url) => {
    return /\.(mp4|mov|webm|avi|mkv)$/i.test(url)
  }

  const handleMediaUpload = async (options) => {
    const { file, onError, onSuccess } = options
    const formData = new FormData()
    formData.append('file', file)
    try {
      const res = await uploadFile(formData)
      const url = res.data
      eventForm.mediaUrls.push(url)
      mediaFileList.value.push({ name: file.name, url, status: 'success' })
      onSuccess(res, file)
    } catch (error) {
      console.error('上传文件失败:', error)
      ElMessage.error(error?.response?.data?.message || '上传失败')
      onError(error)
    }
  }

  const handleMediaRemove = (file) => {
    mediaFileList.value = mediaFileList.value.filter((item) => item.uid !== file.uid && item.url !== file.url)
    eventForm.mediaUrls = eventForm.mediaUrls.filter((url) => url !== file.url)
  }

  const handlePreview = (file) => {
    if (!file.url) return
    previewUrl.value = file.url
    previewDialogVisible.value = true
  }
</script>

<style scoped>
  .home-page {
    max-width: 1200px;
    margin: 0 auto;
  }

  .action-btn {
    height: 60px;
    font-size: 16px;
  }

  .events-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }

  .event-card {
    padding: 16px;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    background: white;
    transition: all 0.3s;
  }

  .event-card:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  .event-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .distance {
    color: #909399;
    font-size: 14px;
  }

  .event-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 12px;
    color: #303133;
  }

  .event-info {
    display: flex;
    gap: 16px;
    margin-bottom: 8px;
  }

  .info-item {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #606266;
    font-size: 14px;
  }

  .event-desc {
    margin: 8px 0;
    color: #606266;
    font-size: 14px;
    line-height: 1.4;
  }

  .media-preview {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
  }

  .media-item {
    width: 90px;
    height: 90px;
    border-radius: 6px;
    object-fit: cover;
    background: #f5f7fa;
  }

  .media-item.video {
    object-fit: contain;
  }

  .media-uploader :deep(.el-upload--picture-card) {
    width: 100px;
    height: 100px;
  }

  .media-preview-dialog .preview-image,
  .media-preview-dialog .preview-video {
    width: 100%;
    border-radius: 8px;
  }
</style>
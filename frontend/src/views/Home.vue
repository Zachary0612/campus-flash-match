<template>
  <Layout>
    <div class="home-page relative z-10">
      <!-- 快速操作区 -->
      <div class="quick-actions mb-8 animate-slide-up" style="animation-delay: 0.1s">
        <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40">
          <div class="flex items-center justify-between mb-6 border-b border-white/20 pb-4">
            <span class="text-xl font-bold text-gray-800 flex items-center">
              <el-icon class="mr-2 text-primary"><Lightning /></el-icon>
              快速发起
            </span>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <button @click="showCreateDialog('group_buy')" class="action-card group relative overflow-hidden rounded-xl p-6 text-left transition-all duration-300 hover:-translate-y-1 hover:shadow-lg bg-gradient-to-br from-blue-500 to-blue-600">
              <div class="absolute top-0 right-0 -mt-4 -mr-4 w-24 h-24 bg-white opacity-10 rounded-full transform group-hover:scale-150 transition-transform duration-500"></div>
              <div class="relative z-10">
                <div class="w-12 h-12 rounded-lg bg-white/20 flex items-center justify-center mb-4 backdrop-blur-sm">
                  <el-icon class="text-white text-2xl"><ShoppingCart /></el-icon>
                </div>
                <h3 class="text-white text-lg font-bold mb-1">发起拼单</h3>
                <p class="text-blue-100 text-sm">寻找伙伴一起拼单购物</p>
              </div>
            </button>

            <button @click="showCreateDialog('meetup')" class="action-card group relative overflow-hidden rounded-xl p-6 text-left transition-all duration-300 hover:-translate-y-1 hover:shadow-lg bg-gradient-to-br from-green-500 to-emerald-600">
              <div class="absolute top-0 right-0 -mt-4 -mr-4 w-24 h-24 bg-white opacity-10 rounded-full transform group-hover:scale-150 transition-transform duration-500"></div>
              <div class="relative z-10">
                <div class="w-12 h-12 rounded-lg bg-white/20 flex items-center justify-center mb-4 backdrop-blur-sm">
                  <el-icon class="text-white text-2xl"><UserFilled /></el-icon>
                </div>
                <h3 class="text-white text-lg font-bold mb-1">发起约伴</h3>
                <p class="text-green-100 text-sm">约饭、运动、学习搭子</p>
              </div>
            </button>

            <button @click="showBeaconDialog" class="action-card group relative overflow-hidden rounded-xl p-6 text-left transition-all duration-300 hover:-translate-y-1 hover:shadow-lg bg-gradient-to-br from-orange-400 to-orange-500">
              <div class="absolute top-0 right-0 -mt-4 -mr-4 w-24 h-24 bg-white opacity-10 rounded-full transform group-hover:scale-150 transition-transform duration-500"></div>
              <div class="relative z-10">
                <div class="w-12 h-12 rounded-lg bg-white/20 flex items-center justify-center mb-4 backdrop-blur-sm">
                  <el-icon class="text-white text-2xl"><LocationInformation /></el-icon>
                </div>
                <h3 class="text-white text-lg font-bold mb-1">发布信标</h3>
                <p class="text-orange-100 text-sm">标记位置，等待响应</p>
              </div>
            </button>
          </div>
        </div>
      </div>
      
      <!-- 附近事件 -->
      <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.3s">
        <div class="flex items-center justify-between mb-6 border-b border-white/20 pb-4">
          <span class="text-xl font-bold text-gray-800 flex items-center">
            <el-icon class="mr-2 text-primary"><Place /></el-icon>
            附近事件
          </span>
          <div class="flex items-center gap-4">
            <el-select v-model="eventType" @change="loadNearbyEvents" size="default" class="glass-select" style="width: 120px">
              <el-option label="全部" value="all" />
              <el-option label="拼单" value="group_buy" />
              <el-option label="约伴" value="meetup" />
              <el-option label="信标" value="beacon" />
            </el-select>
            <el-button circle @click="loadNearbyEvents" :loading="loading" class="!bg-white/50 !border-white/50 hover:!bg-white/80">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>
        
        <div v-if="loading" class="text-center py-16">
          <el-icon class="is-loading text-primary" :size="40"><Loading /></el-icon>
        </div>
        
        <div v-else-if="nearbyEvents.length === 0" class="text-center py-16 text-gray-500">
          <div class="bg-gray-100/50 w-24 h-24 rounded-full flex items-center justify-center mx-auto mb-4">
            <el-icon :size="40" class="text-gray-400"><DocumentDelete /></el-icon>
          </div>
          <p class="text-lg font-medium">暂无附近事件</p>
          <p class="text-sm opacity-70 mt-1">快去发起第一个活动吧！</p>
          <div class="mt-4 bg-blue-50 text-blue-600 px-4 py-2 rounded-lg inline-block text-xs">
             <el-icon class="mr-1 translate-y-0.5"><InfoFilled /></el-icon>
             没看到事件？请检查<router-link to="/profile" class="underline font-bold hover:text-blue-800">个人中心</router-link>是否已绑定校园位置
          </div>
        </div>
        
        <div v-else class="events-grid">
          <div
            v-for="(event, index) in nearbyEvents"
            :key="event.eventId"
            class="event-card bg-white/80 backdrop-blur-sm rounded-xl p-5 border border-white/60 shadow-sm hover:shadow-xl transition-all duration-300 hover:-translate-y-1 cursor-pointer"
            :style="{ animationDelay: `${index * 0.05}s` }"
            @click="goToEventDetail(event.eventId)"
          >
            <div class="event-header flex justify-between items-start mb-3">
              <el-tag :type="getEventTypeTag(event.eventType)" effect="dark" size="small" class="!rounded-lg shadow-sm">
                {{ getEventTypeName(event.eventType) }}
              </el-tag>
              <span class="distance bg-gray-100 px-2 py-0.5 rounded text-xs font-medium text-gray-600 flex items-center">
                <el-icon class="mr-1"><Location /></el-icon>
                {{ event.distance }}m
              </span>
            </div>
            
            <h3 class="event-title text-lg font-bold text-gray-800 mb-3 line-clamp-1" :title="event.title">{{ event.title }}</h3>
            
            <div class="event-info flex gap-4 mb-4 text-sm text-gray-500">
              <div class="info-item flex items-center bg-gray-50 px-2 py-1 rounded-md">
                <el-icon class="mr-1 text-primary"><User /></el-icon>
                <span :class="{'text-primary font-semibold': event.currentNum < event.targetNum}">{{ event.currentNum }}</span>
                <span class="mx-0.5">/</span>
                <span>{{ event.targetNum }}</span>
              </div>
              <div class="info-item flex items-center bg-gray-50 px-2 py-1 rounded-md">
                <el-icon class="mr-1 text-warning"><Clock /></el-icon>
                <span>{{ formatTime(event.createTime) }}</span>
              </div>
            </div>

            <p v-if="event.description" class="event-desc text-gray-600 text-sm mb-4 line-clamp-2 h-10">
              {{ event.description }}
            </p>
            <p v-else class="event-desc text-gray-400 text-sm mb-4 italic h-10 flex items-center">
              暂无详细描述...
            </p>

            <div v-if="event.mediaUrls && event.mediaUrls.length" class="media-preview grid grid-cols-3 gap-2 mb-4">
              <template v-for="(url, mediaIndex) in event.mediaUrls.slice(0, 3)" :key="mediaIndex">
                <div class="aspect-square rounded-lg overflow-hidden border border-gray-100 relative group cursor-pointer" @click="handlePreview({url: getMediaUrl(url)})">
                  <video
                    v-if="isVideo(url)"
                    :src="getMediaUrl(url)"
                    class="w-full h-full object-cover"
                  />
                  <img
                    v-else
                    :src="getMediaUrl(url)"
                    alt="event media"
                    class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                  />
                  <div v-if="isVideo(url)" class="absolute inset-0 flex items-center justify-center bg-black/20">
                     <el-icon class="text-white" size="20"><VideoPlay /></el-icon>
                  </div>
                </div>
              </template>
            </div>
            
            <el-button
              type="primary"
              class="w-full !rounded-lg !h-10 !font-medium shadow-md hover:shadow-primary/40 transition-all"
              :class="{'!bg-gray-300 !border-gray-300 !shadow-none': event.currentNum >= event.targetNum}"
              @click.stop="handleJoinEvent(event.eventId)"
              :disabled="event.currentNum >= event.targetNum"
            >
              {{ event.currentNum >= event.targetNum ? '已满员' : '立即参与' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 创建事件对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      :title="dialogTitle"
      width="500px"
      class="custom-dialog rounded-2xl overflow-hidden"
      destroy-on-close
    >
      <el-form :model="eventForm" :rules="eventRules" ref="eventFormRef" label-width="100px" class="pt-4">
        <el-form-item label="事件标题" prop="title">
          <el-input v-model="eventForm.title" placeholder="给活动起个响亮的名字" class="custom-input" />
        </el-form-item>
        
        <el-form-item label="目标人数" prop="targetNum">
          <el-input-number v-model="eventForm.targetNum" :min="2" :max="20" class="!w-full" />
        </el-form-item>
        
        <el-form-item label="过期时间" prop="expireMinutes">
          <el-input-number v-model="eventForm.expireMinutes" :min="1" :max="1440" class="!w-[calc(100%-40px)]" />
          <span class="ml-2 text-gray-500">分钟</span>
        </el-form-item>
        
        <el-form-item label="校园点位" prop="pointId">
          <el-select v-model="eventForm.pointId" placeholder="请选择集合点位" class="w-full custom-select">
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
            placeholder="描述一下活动的具体内容，最多200字"
            maxlength="200"
            show-word-limit
            class="custom-textarea"
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
          </el-upload>
           <div class="text-xs text-gray-400 mt-1 leading-relaxed">
              支持 jpg/png/gif/webp 图片或 mp4/mov/webm 视频<br>单个不超过10MB，最多6个
            </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false" class="!rounded-lg">取消</el-button>
          <el-button type="primary" :loading="createLoading" @click="handleCreateEvent" class="!rounded-lg shadow-lg shadow-blue-200">马上创建</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 发布信标对话框 -->
    <el-dialog
      v-model="beaconDialogVisible"
      title="发布信标"
      width="500px"
      class="custom-dialog rounded-2xl"
      destroy-on-close
    >
      <el-form :model="beaconForm" :rules="beaconRules" ref="beaconFormRef" label-width="100px" class="pt-4">
        <el-form-item label="位置描述" prop="locationDesc">
          <el-input v-model="beaconForm.locationDesc" placeholder="你在哪里？穿什么衣服？" class="custom-input" />
        </el-form-item>
        
        <el-form-item label="详细说明">
          <el-input
            v-model="beaconForm.description"
            type="textarea"
            :rows="2"
            placeholder="可以补充更多信息，如想找什么样的人等"
            maxlength="200"
            show-word-limit
            class="custom-textarea"
          />
        </el-form-item>
        
        <el-form-item label="有效时长" prop="expireMinutes">
          <el-input-number v-model="beaconForm.expireMinutes" :min="1" :max="1440" class="!w-[calc(100%-40px)]" />
          <span class="ml-2 text-gray-500">分钟</span>
        </el-form-item>
        
        <el-form-item label="校园点位" prop="pointId">
          <el-select v-model="beaconForm.pointId" placeholder="请选择大概位置" class="w-full custom-select">
            <el-option 
              v-for="point in campusPoints" 
              :key="point.id" 
              :label="point.pointName" 
              :value="point.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="图片/视频">
          <el-upload
            class="media-uploader"
            :action="uploadAction"
            list-type="picture-card"
            :file-list="beaconMediaFileList"
            :limit="3"
            :on-remove="handleBeaconMediaRemove"
            :http-request="handleBeaconMediaUpload"
            :on-preview="handlePreview"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="text-xs text-gray-400 mt-1">最多上传3张图片</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="beaconDialogVisible = false" class="!rounded-lg">取消</el-button>
          <el-button type="primary" :loading="beaconLoading" @click="handlePublishBeacon" class="!rounded-lg shadow-lg shadow-blue-200">立即发布</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 媒体预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="预览"
      width="800px"
      class="media-preview-dialog !bg-transparent shadow-none"
      :show-close="true"
    >
      <div class="relative rounded-lg overflow-hidden shadow-2xl bg-black">
        <video
          v-if="isVideo(previewUrl)"
          :src="previewUrl"
          controls
          class="preview-video w-full max-h-[80vh]"
        />
        <img
          v-else
          :src="previewUrl"
          alt="preview"
          class="preview-image w-full max-h-[80vh] object-contain"
        />
      </div>
    </el-dialog>
  </Layout>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import dayjs from 'dayjs'
  import Layout from '@/components/Layout.vue'
  import { getNearbyEvents, createEvent, joinEvent } from '@/api/event'
  import { getCampusPoints } from '@/api/user'
  import { publishBeacon } from '@/api/beacon'
  import { uploadFile } from '@/api/file'
  import { ShoppingCart, UserFilled, LocationInformation, Refresh, Loading, DocumentDelete, User, Clock, Plus, Lightning, Place, Location, VideoPlay, InfoFilled } from '@element-plus/icons-vue'

  const router = useRouter()

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
  const beaconMediaFileList = ref([])
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
    pointId: null,
    description: '',
    mediaUrls: []
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
      console.log('请求附近事件, eventType:', eventTypeParam)
      const res = await getNearbyEvents(eventTypeParam)
      console.log('附近事件响应:', res)
      if (res.code === 200) {
        nearbyEvents.value = res.data || []
        console.log('设置nearbyEvents:', nearbyEvents.value)
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
      console.log('校园点位响应:', res)
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
    beaconForm.description = ''
    beaconForm.mediaUrls = []
    beaconMediaFileList.value = []
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

  const goToEventDetail = (eventId) => {
    router.push(`/event/${eventId}`)
  }

  const getMediaUrl = (url) => {
    if (!url) return ''
    // 如果已经是完整URL，直接返回
    if (url.startsWith('http')) {
      return url
    }
    // /media/ 路径由后端静态资源处理器直接提供，不需要/api前缀
    return url
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

  const handleBeaconMediaUpload = async (options) => {
    const { file, onError, onSuccess } = options
    const formData = new FormData()
    formData.append('file', file)
    try {
      const res = await uploadFile(formData)
      const url = res.data
      beaconForm.mediaUrls.push(url)
      beaconMediaFileList.value.push({ name: file.name, url, status: 'success' })
      onSuccess(res, file)
    } catch (error) {
      console.error('上传文件失败:', error)
      ElMessage.error(error?.response?.data?.message || '上传失败')
      onError(error)
    }
  }

  const handleBeaconMediaRemove = (file) => {
    beaconMediaFileList.value = beaconMediaFileList.value.filter((item) => item.uid !== file.uid && item.url !== file.url)
    beaconForm.mediaUrls = beaconForm.mediaUrls.filter((url) => url !== file.url)
  }
</script>

<style scoped>
  .home-page {
    max-width: 1200px;
    margin: 0 auto;
    padding-bottom: 40px;
  }

  .events-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 24px;
  }
  
  .event-card {
    animation: fade-in 0.5s ease-out forwards;
    opacity: 0;
  }

  @keyframes fade-in {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .media-uploader :deep(.el-upload--picture-card) {
    width: 80px;
    height: 80px;
    border-radius: 12px;
    background-color: #f8f9fa;
    border: 2px dashed #e4e7ed;
  }
  
  .media-uploader :deep(.el-upload--picture-card:hover) {
    border-color: var(--el-color-primary);
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

  /* Glass select customization */
  :deep(.glass-select .el-input__wrapper) {
    background-color: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(5px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    box-shadow: none;
  }
  
  :deep(.glass-select .el-input__wrapper.is-focus) {
    background-color: white;
    box-shadow: 0 0 0 1px var(--el-color-primary);
  }
</style>
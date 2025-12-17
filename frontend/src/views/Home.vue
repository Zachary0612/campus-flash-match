<template>
  <Layout>
    <div class="home-page relative z-10">
      <!-- 快速操作区 -->
      <div class="quick-actions mb-8 animate-slide-up" style="animation-delay: 0.1s">
        <div class="glass rounded-3xl p-8 shadow-glass backdrop-blur-xl bg-white/20 border-white/30">
          <div class="flex items-center justify-between mb-8 border-b border-white/10 pb-4">
            <span class="text-2xl font-black text-gray-800 flex items-center tracking-tight">
              <div class="p-2 bg-gradient-to-tr from-yellow-400 to-orange-500 rounded-xl mr-3 shadow-lg shadow-orange-500/30">
                <el-icon class="text-white text-xl"><Lightning /></el-icon>
              </div>
              快速发起
            </span>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
            <button @click="showCreateDialog('group_buy')" class="group relative overflow-hidden rounded-2xl p-8 text-left transition-all duration-500 hover:-translate-y-2 hover:shadow-2xl bg-gradient-to-br from-blue-500 to-indigo-600 border border-white/20">
              <div class="absolute top-0 right-0 -mt-8 -mr-8 w-40 h-40 bg-white opacity-10 rounded-full transform group-hover:scale-150 transition-transform duration-700 ease-out blur-xl"></div>
              <div class="absolute bottom-0 left-0 -mb-8 -ml-8 w-32 h-32 bg-blue-400 opacity-20 rounded-full transform group-hover:scale-150 transition-transform duration-700 ease-out blur-xl"></div>
              <div class="relative z-10 flex flex-col h-full justify-between">
                <div class="w-14 h-14 rounded-2xl bg-white/20 flex items-center justify-center mb-6 backdrop-blur-md shadow-inner border border-white/30 group-hover:rotate-6 transition-transform duration-300">
                  <el-icon class="text-white text-3xl"><ShoppingCart /></el-icon>
                </div>
                <div>
                  <h3 class="text-white text-2xl font-black mb-2 tracking-wide">发起拼单</h3>
                  <p class="text-blue-100 text-sm font-medium leading-relaxed opacity-90">寻找伙伴一起拼单购物<br>省钱又省心</p>
                </div>
                <div class="mt-4 flex items-center text-white/80 text-sm font-semibold group-hover:translate-x-2 transition-transform duration-300">
                  立即开始 <el-icon class="ml-1"><ArrowRight /></el-icon>
                </div>
              </div>
            </button>

            <button @click="showCreateDialog('meetup')" class="group relative overflow-hidden rounded-2xl p-8 text-left transition-all duration-500 hover:-translate-y-2 hover:shadow-2xl bg-gradient-to-br from-emerald-500 to-teal-600 border border-white/20">
              <div class="absolute top-0 right-0 -mt-8 -mr-8 w-40 h-40 bg-white opacity-10 rounded-full transform group-hover:scale-150 transition-transform duration-700 ease-out blur-xl"></div>
              <div class="absolute bottom-0 left-0 -mb-8 -ml-8 w-32 h-32 bg-emerald-400 opacity-20 rounded-full transform group-hover:scale-150 transition-transform duration-700 ease-out blur-xl"></div>
              <div class="relative z-10 flex flex-col h-full justify-between">
                <div class="w-14 h-14 rounded-2xl bg-white/20 flex items-center justify-center mb-6 backdrop-blur-md shadow-inner border border-white/30 group-hover:rotate-6 transition-transform duration-300">
                  <el-icon class="text-white text-3xl"><UserFilled /></el-icon>
                </div>
                <div>
                  <h3 class="text-white text-2xl font-black mb-2 tracking-wide">发起约伴</h3>
                  <p class="text-emerald-100 text-sm font-medium leading-relaxed opacity-90">约饭、运动、学习搭子<br>不再一个人</p>
                </div>
                <div class="mt-4 flex items-center text-white/80 text-sm font-semibold group-hover:translate-x-2 transition-transform duration-300">
                  立即开始 <el-icon class="ml-1"><ArrowRight /></el-icon>
                </div>
              </div>
            </button>

            <button @click="showBeaconDialog" class="group relative overflow-hidden rounded-2xl p-8 text-left transition-all duration-500 hover:-translate-y-2 hover:shadow-2xl bg-gradient-to-br from-orange-400 to-red-500 border border-white/20">
              <div class="absolute top-0 right-0 -mt-8 -mr-8 w-40 h-40 bg-white opacity-10 rounded-full transform group-hover:scale-150 transition-transform duration-700 ease-out blur-xl"></div>
              <div class="absolute bottom-0 left-0 -mb-8 -ml-8 w-32 h-32 bg-orange-400 opacity-20 rounded-full transform group-hover:scale-150 transition-transform duration-700 ease-out blur-xl"></div>
              <div class="relative z-10 flex flex-col h-full justify-between">
                <div class="w-14 h-14 rounded-2xl bg-white/20 flex items-center justify-center mb-6 backdrop-blur-md shadow-inner border border-white/30 group-hover:rotate-6 transition-transform duration-300">
                  <el-icon class="text-white text-3xl"><LocationInformation /></el-icon>
                </div>
                <div>
                  <h3 class="text-white text-2xl font-black mb-2 tracking-wide">发布信标</h3>
                  <p class="text-orange-100 text-sm font-medium leading-relaxed opacity-90">标记位置，等待响应<br>即时互动</p>
                </div>
                <div class="mt-4 flex items-center text-white/80 text-sm font-semibold group-hover:translate-x-2 transition-transform duration-300">
                  立即开始 <el-icon class="ml-1"><ArrowRight /></el-icon>
                </div>
              </div>
            </button>
          </div>
        </div>
      </div>
      
      <!-- 附近事件 -->
      <div class="glass rounded-3xl p-8 shadow-glass backdrop-blur-xl bg-white/20 border-white/30 animate-slide-up" style="animation-delay: 0.3s">
        <div class="flex items-center justify-between mb-8 border-b border-white/10 pb-4">
          <span class="text-2xl font-black text-gray-800 flex items-center tracking-tight">
            <div class="p-2 bg-gradient-to-tr from-blue-400 to-cyan-500 rounded-xl mr-3 shadow-lg shadow-blue-500/30">
              <el-icon class="text-white text-xl"><Place /></el-icon>
            </div>
            附近事件
          </span>
          <div class="flex items-center gap-4">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索感兴趣的活动..."
              clearable
              class="glass-input transition-all duration-300 hover:w-[280px] focus-within:w-[280px]"
              style="width: 220px"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon class="text-gray-500"><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="eventType" @change="handleSearch" size="default" class="glass-select" style="width: 120px">
              <el-option label="全部类型" value="all" />
              <el-option label="拼单" value="group_buy" />
              <el-option label="约伴" value="meetup" />
              <el-option label="信标" value="beacon" />
            </el-select>
            <el-button circle @click="handleSearch" :loading="loading" class="!bg-white/40 !border-white/30 hover:!bg-white/60 !shadow-sm hover:!shadow-md transition-all duration-300">
              <el-icon class="text-gray-700"><Refresh /></el-icon>
            </el-button>
          </div>
        </div>
        
        <div v-if="loading" class="text-center py-20">
          <div class="inline-block p-4 rounded-full bg-white/30 backdrop-blur-md shadow-lg animate-pulse-slow">
            <el-icon class="is-loading text-primary" :size="48"><Loading /></el-icon>
          </div>
          <p class="mt-4 text-gray-500 font-medium">正在寻找有趣的活动...</p>
        </div>
        
        <div v-else-if="nearbyEvents.length === 0" class="text-center py-24 text-gray-500">
          <div class="bg-gray-100/30 w-32 h-32 rounded-full flex items-center justify-center mx-auto mb-6 backdrop-blur-md shadow-inner border border-white/20">
            <el-icon :size="56" class="text-gray-400/70"><DocumentDelete /></el-icon>
          </div>
          <p class="text-xl font-bold text-gray-700">暂无附近事件</p>
          <p class="text-base opacity-70 mt-2">这里有点冷清，快去发起第一个活动吧！</p>
          <div class="mt-6 bg-blue-50/50 text-blue-600 px-6 py-3 rounded-xl inline-block text-sm border border-blue-100/50 backdrop-blur-sm shadow-sm">
             <el-icon class="mr-2 translate-y-0.5"><InfoFilled /></el-icon>
             没看到事件？请检查<router-link to="/profile" class="underline font-bold hover:text-blue-800 ml-1">个人中心</router-link>是否已绑定校园位置
          </div>
        </div>
        
        <div v-else class="events-grid grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          <div
            v-for="(event, index) in nearbyEvents"
            :key="event.eventId"
            class="glass-card group rounded-2xl p-5 hover:shadow-glass-hover transition-all duration-500 cursor-pointer flex flex-col h-full border border-white/40 bg-white/40"
            :style="{ animationDelay: `${index * 0.05}s` }"
            @click="goToEventDetail(event.eventId)"
          >
            <div class="event-header flex justify-between items-start mb-4">
              <div class="flex items-center gap-2">
                <el-tag :type="getEventTypeTag(event.eventType)" effect="dark" class="!rounded-lg !px-3 !h-7 !border-none shadow-md font-bold">
                  {{ getEventTypeName(event.eventType) }}
                </el-tag>
                <el-tag v-if="event.status === 'pending_confirm'" type="warning" size="small" effect="plain" class="!rounded-lg !bg-orange-50 !border-orange-200 !text-orange-500">
                  待确认
                </el-tag>
              </div>
              <span class="distance bg-white/60 px-2.5 py-1 rounded-lg text-xs font-bold text-gray-600 flex items-center shadow-sm backdrop-blur-md border border-white/40">
                <el-icon class="mr-1 text-primary"><Location /></el-icon>
                {{ event.distance }}m
              </span>
            </div>
            
            <!-- 发起者信息 -->
            <div class="flex items-center gap-3 mb-4 p-2 bg-white/30 rounded-xl backdrop-blur-sm border border-white/20 transition-colors group-hover:bg-white/50">
              <el-avatar 
                :size="36" 
                :src="event.ownerAvatar" 
                class="cursor-pointer hover:ring-2 hover:ring-primary hover:ring-offset-2 transition-all shadow-sm"
                @click.stop="goToUserProfile(event.ownerId)"
              >
                {{ event.ownerNickname?.charAt(0) || '?' }}
              </el-avatar>
              <div class="flex flex-col overflow-hidden">
                <span 
                  class="text-sm font-bold text-gray-800 hover:text-primary cursor-pointer transition-colors truncate"
                  @click.stop="goToUserProfile(event.ownerId)"
                >
                  {{ event.ownerNickname || '未知用户' }}
                </span>
                <span class="text-xs text-gray-500 truncate">发布于 {{ formatTime(event.createTime) }}</span>
              </div>
            </div>
            
            <h3 class="event-title text-xl font-black text-gray-800 mb-3 line-clamp-2 leading-tight group-hover:text-primary transition-colors" :title="event.title">{{ event.title }}</h3>
            
            <div class="event-info flex flex-wrap gap-2 mb-4 text-xs font-medium text-gray-600">
              <div class="info-item flex items-center bg-blue-50/50 px-2.5 py-1.5 rounded-lg border border-blue-100/30">
                <el-icon class="mr-1.5 text-primary text-sm"><User /></el-icon>
                <span :class="{'text-primary font-bold': event.currentNum < event.targetNum}">{{ event.currentNum }}</span>
                <span class="mx-0.5 text-gray-400">/</span>
                <span>{{ event.targetNum }}</span>
              </div>
              
              <div 
                class="info-item flex items-center px-2.5 py-1.5 rounded-lg border backdrop-blur-sm shadow-sm"
                :class="getCountdownClass(event)"
              >
                <el-icon class="mr-1.5 text-sm"><Timer /></el-icon>
                <span class="font-bold tracking-tight">{{ getCountdown(event) }}</span>
              </div>
            </div>
            
            <!-- 集合地点 -->
            <div v-if="getEventLocation(event)" class="mb-4 flex items-start bg-gray-50/50 px-3 py-2.5 rounded-xl text-sm border border-gray-100/50">
              <el-icon class="mr-2 mt-0.5 text-gray-500"><LocationInformation /></el-icon>
              <span class="text-gray-700 font-medium line-clamp-2 leading-snug">{{ getEventLocation(event) }}</span>
            </div>

            <p v-if="event.description" class="event-desc text-gray-600 text-sm mb-4 line-clamp-2 h-10 leading-relaxed opacity-80">
              {{ event.description }}
            </p>
            <p v-else class="event-desc text-gray-400 text-sm mb-4 italic h-10 flex items-center opacity-60">
              暂无详细描述...
            </p>

            <div v-if="event.mediaUrls && event.mediaUrls.length" class="media-preview grid grid-cols-3 gap-2 mb-4 mt-auto">
              <template v-for="(url, mediaIndex) in event.mediaUrls.slice(0, 3)" :key="mediaIndex">
                <div class="aspect-square rounded-lg overflow-hidden border border-white/50 relative group/media cursor-pointer shadow-sm" @click="handlePreview({url: getMediaUrl(url)})">
                  <video
                    v-if="isVideo(url)"
                    :src="getMediaUrl(url)"
                    class="w-full h-full object-cover"
                  />
                  <img
                    v-else
                    :src="getMediaUrl(url)"
                    alt="event media"
                    class="w-full h-full object-cover transition-transform duration-500 group-hover/media:scale-110"
                  />
                  <div v-if="isVideo(url)" class="absolute inset-0 flex items-center justify-center bg-black/20 backdrop-blur-[1px] group-hover/media:bg-black/10 transition-colors">
                     <el-icon class="text-white drop-shadow-md" size="20"><VideoPlay /></el-icon>
                  </div>
                </div>
              </template>
            </div>
            
            <div class="mt-auto pt-2">
              <el-button
                type="primary"
                class="w-full !rounded-xl !h-11 !font-bold shadow-lg shadow-blue-200/50 hover:shadow-blue-300/60 transition-all duration-300 transform active:scale-95"
                :class="{'!bg-gray-300 !border-gray-300 !shadow-none cursor-not-allowed': event.currentNum >= event.targetNum}"
                @click.stop="handleJoinEvent(event.eventId)"
                :disabled="event.currentNum >= event.targetNum"
              >
                <div class="flex items-center justify-center gap-2">
                  <span v-if="event.currentNum >= event.targetNum">已满员</span>
                  <template v-else>
                    <el-icon><Plus /></el-icon>
                    立即参与
                  </template>
                </div>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 创建事件对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      :title="dialogTitle"
      :width="locationMode === 'map' ? '650px' : '500px'"
      class="custom-dialog rounded-2xl overflow-hidden"
      destroy-on-close
      top="5vh"
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
        
        <el-form-item label="集合地点">
          <div class="w-full">
            <el-radio-group v-model="locationMode" size="small" class="mb-3">
              <el-radio-button label="point">选择点位</el-radio-button>
              <el-radio-button label="map">地图选点</el-radio-button>
            </el-radio-group>
            
            <!-- 点位选择模式 -->
            <el-select 
              v-if="locationMode === 'point'"
              v-model="eventForm.pointId" 
              placeholder="请选择集合点位" 
              class="w-full custom-select"
            >
              <el-option 
                v-for="point in campusPoints" 
                :key="point.id" 
                :label="point.pointName" 
                :value="point.id" 
              />
            </el-select>
            
            <!-- 地图选点模式 -->
            <MapPicker 
              v-else
              v-model:location="eventForm.location"
              height="280px"
              @change="handleEventLocationChange"
            />
          </div>
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
        
        <el-form-item label="位置选择">
          <div class="w-full">
            <el-radio-group v-model="beaconLocationMode" size="small" class="mb-3">
              <el-radio-button label="campus">选择点位</el-radio-button>
              <el-radio-button label="map">地图选点</el-radio-button>
            </el-radio-group>
            
            <!-- 校园点位选择 -->
            <el-select 
              v-if="beaconLocationMode === 'campus'"
              v-model="beaconForm.pointId" 
              placeholder="请选择大概位置" 
              class="w-full custom-select"
            >
              <el-option 
                v-for="point in campusPoints" 
                :key="point.id" 
                :label="point.pointName" 
                :value="point.id" 
              />
            </el-select>
            
            <!-- 地图选点 -->
            <MapPicker 
              v-else
              v-model:location="beaconForm.mapLocation"
              height="250px"
              @change="handleBeaconLocationChange"
            />
            <div v-if="beaconLocationMode === 'map' && beaconForm.mapLocation" class="mt-2 text-sm text-gray-500">
              <el-icon class="mr-1"><Location /></el-icon>
              {{ beaconForm.mapLocation.address || '已选择位置' }}
            </div>
          </div>
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
  import { ref, reactive, onMounted, onUnmounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import dayjs from 'dayjs'
  import Layout from '@/components/Layout.vue'
  import MapPicker from '@/components/MapPicker.vue'
  import { getNearbyEvents, createEvent, joinEvent } from '@/api/event'
  import { getCampusPoints } from '@/api/user'
  import { publishBeacon } from '@/api/beacon'
  import { uploadFile } from '@/api/file'
  import { ArrowRight, ShoppingCart, UserFilled, LocationInformation, Refresh, Loading, DocumentDelete, User, Clock, Plus, Lightning, Place, Location, VideoPlay, InfoFilled, Timer, Search } from '@element-plus/icons-vue'

  const router = useRouter()

  const now = ref(Date.now())
  let countdownTimer = null

  const loading = ref(false)
  const createLoading = ref(false)
  const beaconLoading = ref(false)
  const eventType = ref('all')
  const searchKeyword = ref('')
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
    extMeta: {},
    location: null
  })

  const beaconForm = reactive({
    locationDesc: '',
    expireMinutes: 120,
    pointId: null,
    description: '',
    mediaUrls: [],
    mapLocation: null
  })

  // 位置选择模式
  const locationMode = ref('point')
  // 信标位置选择模式
  const beaconLocationMode = ref('campus')

  // 处理地图选点位置变化
  const handleEventLocationChange = (location) => {
    if (location) {
      // 将地图选择的位置存储到 extMeta 中
      eventForm.extMeta = {
        ...eventForm.extMeta,
        mapLocation: {
          lng: location.lng,
          lat: location.lat,
          address: location.address
        }
      }
    }
  }

  // 处理信标地图选点位置变化
  const handleBeaconLocationChange = (location) => {
    beaconForm.mapLocation = location
  }

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
    // 启动倒计时定时器，每秒更新一次
    countdownTimer = setInterval(() => {
      now.value = Date.now()
    }, 1000)
  })
  
  onUnmounted(() => {
    // 清理定时器
    if (countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  })

  const loadNearbyEvents = async () => {
    loading.value = true
    try {
      const eventTypeParam = eventType.value === 'all' ? undefined : eventType.value;
      console.log('请求附近事件, eventType:', eventTypeParam)
      const res = await getNearbyEvents(eventTypeParam)
      console.log('附近事件响应:', res)
      if (res.code === 200) {
        let events = res.data || []
        // 前端搜索过滤
        if (searchKeyword.value.trim()) {
          const keyword = searchKeyword.value.trim().toLowerCase()
          events = events.filter(event => 
            event.title?.toLowerCase().includes(keyword) ||
            event.description?.toLowerCase().includes(keyword) ||
            event.ownerNickname?.toLowerCase().includes(keyword)
          )
        }
        nearbyEvents.value = events
        console.log('设置nearbyEvents:', nearbyEvents.value)
      }
    } catch (error) {
      console.error('加载附近事件失败:', error)
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    loadNearbyEvents()
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
    beaconForm.mapLocation = null
    beaconMediaFileList.value = []
    beaconLocationMode.value = 'campus'
    beaconDialogVisible.value = true
  }

  const handleCreateEvent = async () => {
    if (!eventFormRef.value) return
    
    // 地图选点模式下验证是否选择了位置
    if (locationMode.value === 'map') {
      if (!eventForm.location) {
        ElMessage.warning('请在地图上选择集合地点')
        return
      }
    } else {
      // 点位模式下验证是否选择了点位
      if (!eventForm.pointId) {
        ElMessage.warning('请选择校园点位')
        return
      }
    }
    
    // 验证标题
    if (!eventForm.title?.trim()) {
      ElMessage.warning('请输入事件标题')
      return
    }
    
    console.log('提交事件数据:', JSON.parse(JSON.stringify(eventForm)))

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

  const handlePublishBeacon = async () => {
    if (!beaconFormRef.value) return
    
    // 验证位置选择
    if (beaconLocationMode.value === 'campus' && !beaconForm.pointId) {
      ElMessage.warning('请选择校园点位')
      return
    }
    if (beaconLocationMode.value === 'map' && !beaconForm.mapLocation) {
      ElMessage.warning('请在地图上选择位置')
      return
    }
    
    // 验证位置描述
    if (!beaconForm.locationDesc?.trim()) {
      ElMessage.warning('请输入位置描述')
      return
    }
    
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

  const getEventLocation = (event) => {
    // 优先使用地图选点的地址
    if (event.extMeta?.mapLocation?.address) {
      return event.extMeta.mapLocation.address
    }
    // 否则使用点位名称
    if (event.pointName) {
      return event.pointName
    }
    return null
  }

  const isVideo = (url) => {
    return /\.(mp4|mov|webm|avi|mkv)$/i.test(url)
  }

  const goToEventDetail = (eventId) => {
    router.push(`/event/${eventId}`)
  }

  const goToUserProfile = (userId) => {
    if (userId) {
      router.push(`/user/${userId}`)
    }
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

  // 计算倒计时
  const getCountdown = (event) => {
    if (!event.createTime || !event.expireMinutes) return '未知'
    
    const createTime = new Date(event.createTime).getTime()
    const expireTime = createTime + event.expireMinutes * 60 * 1000
    const remaining = expireTime - now.value
    
    if (remaining <= 0) return '已过期'
    
    const hours = Math.floor(remaining / (1000 * 60 * 60))
    const minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((remaining % (1000 * 60)) / 1000)
    
    if (hours > 0) {
      return `${hours}时${minutes}分`
    } else if (minutes > 0) {
      return `${minutes}分${seconds}秒`
    } else {
      return `${seconds}秒`
    }
  }

  // 倒计时样式
  const getCountdownClass = (event) => {
    if (!event.createTime || !event.expireMinutes) return 'bg-gray-100 text-gray-500'
    
    const createTime = new Date(event.createTime).getTime()
    const expireTime = createTime + event.expireMinutes * 60 * 1000
    const remaining = expireTime - now.value
    
    if (remaining <= 0) return 'bg-gray-200 text-gray-500'
    if (remaining <= 5 * 60 * 1000) return 'bg-red-100 text-red-600' // 5分钟内
    if (remaining <= 30 * 60 * 1000) return 'bg-orange-100 text-orange-600' // 30分钟内
    return 'bg-green-100 text-green-600'
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

  /* Glass input customization */
  :deep(.glass-input .el-input__wrapper) {
    background-color: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(5px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    box-shadow: none;
    border-radius: 8px;
  }
  
  :deep(.glass-input .el-input__wrapper.is-focus) {
    background-color: white;
    box-shadow: 0 0 0 1px var(--el-color-primary);
  }
  
  :deep(.glass-input .el-input__wrapper:hover) {
    background-color: rgba(255, 255, 255, 0.8);
  }
</style>
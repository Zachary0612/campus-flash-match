<template>
  <div class="route-map">
    <!-- 路线信息 -->
    <div v-if="routeInfo" class="route-info mb-3 p-4 bg-gradient-to-r from-blue-50 to-green-50 rounded-xl border border-blue-100">
      <div class="flex items-center justify-between flex-wrap gap-3">
        <div class="flex items-center gap-6">
          <div class="text-center">
            <div class="text-2xl font-bold text-blue-600">{{ routeInfo.distance }}</div>
            <div class="text-xs text-gray-500">总距离</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-green-600">{{ routeInfo.duration }}</div>
            <div class="text-xs text-gray-500">预计时间</div>
          </div>
        </div>
        <div class="flex gap-2">
          <el-button 
            size="small" 
            :type="travelMode === 'walking' ? 'primary' : 'default'" 
            @click="changeTravelMode('walking')"
            round
          >
            🚶 步行
          </el-button>
          <el-button 
            size="small" 
            :type="travelMode === 'riding' ? 'primary' : 'default'" 
            @click="changeTravelMode('riding')"
            round
          >
            🚴 骑行
          </el-button>
          <el-button 
            size="small" 
            :type="travelMode === 'driving' ? 'primary' : 'default'" 
            @click="changeTravelMode('driving')"
            round
          >
            🚗 驾车
          </el-button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay absolute inset-0 bg-white/80 flex items-center justify-center z-10 rounded-lg">
      <el-icon class="is-loading text-primary text-3xl"><Loading /></el-icon>
    </div>

    <!-- 地图容器 -->
    <div ref="mapContainer" class="map-container rounded-xl overflow-hidden border border-gray-200 shadow-sm relative"></div>

    <!-- 导航步骤 -->
    <div v-if="routeSteps.length > 0" class="route-steps mt-4">
      <div class="text-sm font-medium text-gray-700 mb-2 flex items-center">
        <span class="mr-2">📍</span>
        导航步骤 ({{ routeSteps.length }}步)
      </div>
      <div class="steps-container max-h-48 overflow-y-auto bg-gray-50 rounded-lg p-2">
        <div 
          v-for="(step, index) in routeSteps" 
          :key="index"
          class="step-item flex items-start gap-3 p-2 hover:bg-white rounded-lg transition-colors"
        >
          <span class="step-number w-6 h-6 rounded-full bg-primary text-white text-xs flex items-center justify-center flex-shrink-0 mt-0.5">
            {{ index + 1 }}
          </span>
          <div class="flex-1">
            <div class="text-sm text-gray-700">{{ step.instruction }}</div>
            <div class="text-xs text-gray-400 mt-1">
              {{ step.distance }} · {{ step.duration }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="actions mt-4 flex gap-3">
      <el-button type="primary" @click="openInAmap" class="flex-1" round>
        🗺️ 在高德地图中打开导航
      </el-button>
      <el-button @click="refreshRoute" :loading="loading" circle>
        <el-icon><Refresh /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { Loading, Refresh } from '@element-plus/icons-vue'
import { loadAMap } from '@/utils/mapLoader'

const props = defineProps({
  // 起点 { lng, lat, address }
  origin: {
    type: Object,
    required: true
  },
  // 终点 { lng, lat, address }
  destination: {
    type: Object,
    required: true
  },
  // 地图高度
  height: {
    type: String,
    default: '350px'
  }
})

const mapContainer = ref(null)
const loading = ref(false)
const travelMode = ref('walking')
const routeInfo = ref(null)
const routeSteps = ref([])

let map = null
let driving = null
let walking = null
let riding = null

// 注：已从 mapLoader.js 导入 loadAMap 统一加载函数

// 初始化地图
const initMap = async () => {
  try {
    loading.value = true
    const AMap = await loadAMap()

    map = new AMap.Map(mapContainer.value, {
      zoom: 15,
      resizeEnable: true
    })

    // 初始化路线规划服务
    walking = new AMap.Walking({ map })
    driving = new AMap.Driving({ map })
    riding = new AMap.Riding({ map })

    // 规划路线
    await planRoute()

  } catch (error) {
    console.error('初始化地图失败:', error)
  } finally {
    loading.value = false
  }
}

// 规划路线
const planRoute = async () => {
  if (!map || !props.origin || !props.destination) return

  loading.value = true
  
  const origin = [props.origin.lng, props.origin.lat]
  const destination = [props.destination.lng, props.destination.lat]

  // 清除之前的路线
  map.clearMap()

  // 添加起点标记
  new window.AMap.Marker({
    position: origin,
    map: map,
    label: {
      content: '<div class="marker-label start">起点</div>',
      direction: 'top'
    }
  })

  // 添加终点标记
  new window.AMap.Marker({
    position: destination,
    map: map,
    label: {
      content: '<div class="marker-label end">集合点</div>',
      direction: 'top'
    }
  })

  // 选择路线规划器
  let planner
  switch (travelMode.value) {
    case 'driving':
      planner = driving
      break
    case 'riding':
      planner = riding
      break
    default:
      planner = walking
  }

  return new Promise((resolve) => {
    planner.search(origin, destination, (status, result) => {
      loading.value = false
      
      if (status === 'complete' && result.routes && result.routes.length > 0) {
        const route = result.routes[0]
        
        // 格式化距离
        const distance = route.distance
        const distanceStr = distance >= 1000 
          ? (distance / 1000).toFixed(1) + ' 公里' 
          : distance + ' 米'
        
        // 格式化时间
        const duration = route.time
        let durationStr
        if (duration >= 3600) {
          const hours = Math.floor(duration / 3600)
          const mins = Math.floor((duration % 3600) / 60)
          durationStr = `${hours}小时${mins}分钟`
        } else {
          durationStr = Math.ceil(duration / 60) + ' 分钟'
        }

        routeInfo.value = {
          distance: distanceStr,
          duration: durationStr
        }

        // 解析步骤
        if (route.steps) {
          routeSteps.value = route.steps.map(step => ({
            instruction: step.instruction || step.action || '继续前行',
            distance: step.distance >= 1000 ? (step.distance / 1000).toFixed(1) + '公里' : step.distance + '米',
            duration: Math.ceil((step.time || 60) / 60) + '分钟'
          }))
        }

        // 调整地图视野
        map.setFitView()
      } else {
        console.error('路线规划失败:', result)
        routeInfo.value = null
        routeSteps.value = []
      }
      
      resolve()
    })
  })
}

// 切换出行方式
const changeTravelMode = async (mode) => {
  travelMode.value = mode
  if (map) {
    await planRoute()
  }
}

// 刷新路线
const refreshRoute = async () => {
  if (map) {
    await planRoute()
  }
}

// 在高德地图中打开导航
const openInAmap = () => {
  const origin = props.origin
  const destination = props.destination
  
  // 构建高德地图导航链接
  const modeMap = {
    walking: 'walk',
    riding: 'ride', 
    driving: 'car'
  }
  
  const url = `https://uri.amap.com/navigation?from=${origin.lng},${origin.lat},我的位置&to=${destination.lng},${destination.lat},${encodeURIComponent(destination.address || '集合点')}&mode=${modeMap[travelMode.value]}&callnative=1`
  
  window.open(url, '_blank')
}

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
  }
})

// 监听起点终点变化
watch([() => props.origin, () => props.destination], () => {
  if (map && props.origin && props.destination) {
    planRoute()
  }
}, { deep: true })
</script>

<style scoped>
.route-map {
  width: 100%;
  position: relative;
}

.map-container {
  width: 100%;
  height: v-bind(height);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
}

.step-number {
  flex-shrink: 0;
}

.steps-container::-webkit-scrollbar {
  width: 4px;
}

.steps-container::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 2px;
}

:deep(.marker-label) {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  white-space: nowrap;
}

:deep(.marker-label.start) {
  background: #409eff;
  color: white;
}

:deep(.marker-label.end) {
  background: #67c23a;
  color: white;
}
</style>

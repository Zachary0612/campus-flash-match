<template>
  <div class="map-picker">
    <!-- 搜索框 -->
    <div class="search-box mb-3">
      <el-input
        v-model="searchText"
        placeholder="输入地址搜索..."
        clearable
        @keyup.enter="handleSearch"
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch" :loading="searching">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 搜索结果列表 -->
    <div v-if="searchResults.length > 0" class="search-results mb-3">
      <div class="text-xs text-gray-500 mb-2">搜索结果：</div>
      <div class="results-list max-h-32 overflow-y-auto">
        <div
          v-for="(result, index) in searchResults"
          :key="index"
          class="result-item p-2 hover:bg-blue-50 cursor-pointer rounded text-sm border-b border-gray-100 last:border-0"
          @click="selectSearchResult(result)"
        >
          <div class="font-medium text-gray-800">{{ result.name }}</div>
          <div class="text-xs text-gray-500">{{ result.address }}</div>
        </div>
      </div>
    </div>

    <!-- 地图容器 -->
    <div ref="mapContainer" class="map-container rounded-lg overflow-hidden border border-gray-200"></div>

    <!-- 当前选择的位置 -->
    <div v-if="selectedLocation" class="selected-location mt-3 p-3 bg-gradient-to-r from-blue-50 to-green-50 rounded-lg border border-blue-100">
      <div class="flex items-center justify-between">
        <div class="flex items-center text-blue-600">
          <el-icon class="mr-1"><Location /></el-icon>
          <span class="font-medium">已选择位置</span>
        </div>
        <el-tag type="success" size="small">✓ 已标记</el-tag>
      </div>
      <div class="text-sm text-gray-700 mt-2 font-medium">{{ selectedLocation.address || '未知地址' }}</div>
      <div class="text-xs text-gray-400 mt-1">
        经纬度：{{ selectedLocation.lng.toFixed(6) }}, {{ selectedLocation.lat.toFixed(6) }}
      </div>
    </div>

    <!-- 提示信息 -->
    <div class="tips mt-2 text-xs text-gray-400">
      <el-icon class="mr-1"><InfoFilled /></el-icon>
      点击地图选择位置，或输入地址搜索
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Aim } from '@element-plus/icons-vue'
import { loadAMap } from '@/utils/mapLoader'

const props = defineProps({
  // 初始位置
  initialLocation: {
    type: Object,
    default: null
  },
  // 地图高度
  height: {
    type: String,
    default: '300px'
  }
})

const emit = defineEmits(['update:location', 'change'])

const mapContainer = ref(null)
const searchText = ref('')
const searching = ref(false)
const searchResults = ref([])
const selectedLocation = ref(null)

let map = null
let marker = null
let geocoder = null
let placeSearch = null
let autoComplete = null

// 获取高德地图 Key
const amapKey = import.meta.env.VITE_AMAP_KEY || ''
console.log('MapPicker: 加载的 API Key:', amapKey ? `${amapKey.substring(0, 8)}...` : '未配置')

// 初始化地图
const initMap = async () => {
  try {
    const AMap = await loadAMap()

    // 默认中心点（北京）
    const defaultCenter = [116.397428, 39.90923]
    const center = props.initialLocation 
      ? [props.initialLocation.lng, props.initialLocation.lat]
      : defaultCenter

    map = new AMap.Map(mapContainer.value, {
      zoom: 16,
      center: center,
      resizeEnable: true
    })

    // 初始化地理编码服务
    geocoder = new AMap.Geocoder({
      city: '全国',
      radius: 1000
    })

    // 初始化地点搜索服务
    placeSearch = new AMap.PlaceSearch({
      pageSize: 10,
      pageIndex: 1,
      extensions: 'all'
    })

    // 初始化输入提示服务
    autoComplete = new AMap.AutoComplete({
      datatype: 'all'
    })

    // 如果有初始位置，添加标记
    if (props.initialLocation) {
      addMarker(props.initialLocation.lng, props.initialLocation.lat)
      selectedLocation.value = props.initialLocation
    }

    // 点击地图选择位置
    map.on('click', (e) => {
      const lng = e.lnglat.getLng()
      const lat = e.lnglat.getLat()
      addMarker(lng, lat)
      reverseGeocode(lng, lat)
    })

  } catch (error) {
    console.error('初始化地图失败:', error)
  }
}

// 添加标记
const addMarker = (lng, lat) => {
  if (!map) return

  if (marker) {
    marker.setPosition([lng, lat])
  } else {
    marker = new window.AMap.Marker({
      position: [lng, lat],
      draggable: true,
      animation: 'AMAP_ANIMATION_DROP'
    })
    marker.setMap(map)

    // 拖拽结束后更新位置
    marker.on('dragend', (e) => {
      const pos = e.target.getPosition()
      reverseGeocode(pos.getLng(), pos.getLat())
    })
  }

  map.setCenter([lng, lat])
}

// 逆地理编码（坐标转地址）
const reverseGeocode = (lng, lat) => {
  if (!geocoder) return

  geocoder.getAddress([lng, lat], (status, result) => {
    if (status === 'complete' && result.regeocode) {
      selectedLocation.value = {
        lng,
        lat,
        address: result.regeocode.formattedAddress,
        addressComponent: result.regeocode.addressComponent
      }
    } else {
      selectedLocation.value = {
        lng,
        lat,
        address: `位置 (${lng.toFixed(6)}, ${lat.toFixed(6)})`
      }
    }
    emitChange()
  })
}

// 搜索地址
const handleSearch = () => {
  if (!searchText.value.trim()) return

  searching.value = true
  searchResults.value = []
  
  // 使用 Promise.race 竞态机制，优先处理返回的结果
  const searchPromise = new Promise((resolve, reject) => {
    let handled = false
    
    // 成功回调
    const onSuccess = (results) => {
      if (handled) return
      handled = true
      resolve(results)
    }

    // 1. 尝试 AutoComplete (通常最快)
    if (autoComplete) {
      autoComplete.search(searchText.value, (status, result) => {
        console.log('AutoComplete结果:', status, result)
        if (status === 'complete' && result.tips && result.tips.length > 0) {
           const validTips = result.tips.filter(tip => tip.location)
           if (validTips.length > 0) {
             onSuccess(validTips.map(tip => ({
               name: tip.name,
               address: (tip.district || '') + (tip.address || ''),
               location: {
                 lng: tip.location.lng,
                 lat: tip.location.lat
               }
             })))
           }
        }
      })
    }

    // 2. 尝试 PlaceSearch
    if (placeSearch) {
      placeSearch.search(searchText.value, (status, result) => {
        console.log('PlaceSearch结果:', status, result)
        if (status === 'complete' && result.poiList && result.poiList.pois && result.poiList.pois.length > 0) {
          onSuccess(result.poiList.pois.map(poi => ({
            name: poi.name,
            address: poi.address || poi.name,
            location: {
              lng: typeof poi.location.getLng === 'function' ? poi.location.getLng() : poi.location.lng,
              lat: typeof poi.location.getLat === 'function' ? poi.location.getLat() : poi.location.lat
            }
          })))
        }
      })
    }

    // 3. 尝试 Geocoder
    if (geocoder) {
      geocoder.getLocation(searchText.value, (status, result) => {
        console.log('Geocoder结果:', status, result)
        if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
          onSuccess(result.geocodes.map(geo => ({
            name: geo.formattedAddress,
            address: geo.formattedAddress,
            location: {
              lng: typeof geo.location.getLng === 'function' ? geo.location.getLng() : geo.location.lng,
              lat: typeof geo.location.getLat === 'function' ? geo.location.getLat() : geo.location.lat
            }
          })))
        }
      })
    }
    
    // 超时保护
    setTimeout(() => {
      if (!handled) {
        handled = true
        reject(new Error('搜索超时或无结果'))
      }
    }, 5000)
  })

  searchPromise.then(results => {
    searching.value = false
    searchResults.value = results
    
    // 自动跳转到第一个结果
    const firstResult = results[0]
    if (firstResult) {
       const { lng, lat } = firstResult.location
       console.log('搜索成功，跳转:', lng, lat)
       const numLng = Number(lng)
       const numLat = Number(lat)
       addMarker(numLng, numLat)
       if (map) {
         map.setZoom(16)
         map.setCenter([numLng, numLat])
       }
       selectedLocation.value = {
         lng: numLng,
         lat: numLat,
         address: firstResult.address || firstResult.name
       }
       emitChange()
    }
  }).catch(err => {
    console.warn('搜索失败:', err)
    searching.value = false
    searchResults.value = []
    
    // 检查是否是配额超限
    if (err.message && err.message.includes('USER_DAILY_QUERY_OVER_LIMIT')) {
      ElMessage.error('今日地图搜索次数已达上限，请明天再试或直接在地图上选点')
    } else {
      ElMessage.warning('未找到相关位置，请尝试更详细的地址或在地图上直接选点')
    }
  })
}

// 选择搜索结果
const selectSearchResult = (result) => {
  const { lng, lat } = result.location
  const numLng = Number(lng)
  const numLat = Number(lat)
  
  // 先添加标记
  addMarker(numLng, numLat)
  
  // 设置地图中心并缩放
  if (map) {
    map.setZoom(16)
    map.setCenter([numLng, numLat])
  }
  
  // 更新选中位置
  selectedLocation.value = {
    lng: numLng,
    lat: numLat,
    address: result.address || result.name
  }
  
  // 清空搜索结果
  searchResults.value = []
  searchText.value = result.name
  
  // 发送事件
  emitChange()
}

// 发送变更事件
const emitChange = () => {
  emit('update:location', selectedLocation.value)
  emit('change', selectedLocation.value)
}

// 获取当前位置
const getCurrentLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lng = position.coords.longitude
        const lat = position.coords.latitude
        addMarker(lng, lat)
        reverseGeocode(lng, lat)
      },
      (error) => {
        console.error('获取当前位置失败:', error)
      }
    )
  }
}

// 暴露方法
defineExpose({
  getCurrentLocation,
  getSelectedLocation: () => selectedLocation.value
})

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
  }
})

// 监听初始位置变化
watch(() => props.initialLocation, (newVal) => {
  if (newVal && map) {
    addMarker(newVal.lng, newVal.lat)
    selectedLocation.value = newVal
  }
})

// 监听选中位置变化，通知父组件
watch(selectedLocation, (newVal) => {
  if (newVal) {
    emit('update:location', newVal)
    emit('change', newVal)
  }
}, { deep: true })
</script>

<style scoped>
.map-picker {
  width: 100%;
}

.map-container {
  width: 100%;
  height: v-bind(height);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
}

.search-results {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.result-item:hover {
  background-color: #ecf5ff;
}
</style>

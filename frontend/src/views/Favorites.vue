<template>
  <Layout>
    <div class="favorites-page relative z-10">
      <!-- 页面标题 -->
      <div class="glass rounded-2xl p-6 mb-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up">
        <div class="flex items-center">
          <el-icon class="text-2xl mr-3 text-yellow-500"><Star /></el-icon>
          <h1 class="text-2xl font-bold text-gray-800">我的收藏</h1>
          <el-tag class="ml-3" type="info">{{ favorites.length }} 个事件</el-tag>
        </div>
      </div>

      <!-- 收藏列表 -->
      <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.1s">
        <div v-if="loading" class="text-center py-12">
          <el-icon class="is-loading text-4xl text-primary"><Loading /></el-icon>
          <p class="mt-4 text-gray-500">加载中...</p>
        </div>

        <div v-else-if="favorites.length === 0" class="text-center py-12">
          <el-icon class="text-6xl text-gray-300"><StarFilled /></el-icon>
          <p class="mt-4 text-gray-500">暂无收藏的事件</p>
          <el-button type="primary" class="mt-4" @click="$router.push('/home')">
            去发现事件
          </el-button>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div
            v-for="item in favorites"
            :key="item.id"
            class="favorite-card bg-white/50 rounded-xl p-5 transition-all hover:shadow-lg cursor-pointer"
            @click="goToEventDetail(item.eventId)"
          >
            <div class="flex justify-between items-start mb-3">
              <div>
                <h3 class="font-bold text-gray-800 text-lg mb-1">{{ item.eventTitle }}</h3>
                <el-tag :type="getEventTypeTag(item.eventType)" size="small">
                  {{ getEventTypeName(item.eventType) }}
                </el-tag>
              </div>
              <el-button
                type="warning"
                :icon="Star"
                circle
                size="small"
                @click.stop="handleRemoveFavorite(item)"
              />
            </div>

            <div class="text-sm text-gray-500 space-y-1">
              <p v-if="item.eventDescription">{{ item.eventDescription }}</p>
              <div class="flex items-center gap-4 mt-3">
                <span class="flex items-center">
                  <el-icon class="mr-1"><User /></el-icon>
                  {{ item.ownerNickname }}
                </span>
                <span class="flex items-center">
                  <el-icon class="mr-1"><Clock /></el-icon>
                  {{ formatTime(item.createTime) }}
                </span>
              </div>
            </div>

            <div class="mt-3 pt-3 border-t border-gray-100 flex justify-between items-center">
              <el-tag :type="getStatusTag(item.eventStatus)" size="small">
                {{ getStatusName(item.eventStatus) }}
              </el-tag>
              <span class="text-xs text-gray-400">收藏于 {{ formatTime(item.favoriteTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="favorites.length > 0" class="mt-6 flex justify-center">
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadFavorites"
          />
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getFavoriteList, removeFavorite } from '@/api/favorite'
import { Star, StarFilled, Loading, User, Clock } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

const loading = ref(false)
const favorites = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载收藏列表
const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getFavoriteList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      favorites.value = res.data || []
      total.value = res.data?.length || 0
    }
  } catch (error) {
    console.error('加载收藏列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 取消收藏
const handleRemoveFavorite = async (item) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏该事件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await removeFavorite(item.eventId)
    favorites.value = favorites.value.filter(f => f.id !== item.id)
    ElMessage.success('已取消收藏')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error)
    }
  }
}

// 跳转到事件详情
const goToEventDetail = (eventId) => {
  router.push(`/event/${eventId}`)
}

// 获取事件类型名称
const getEventTypeName = (type) => {
  const types = {
    'group_buy': '拼单',
    'meetup': '约伴'
  }
  return types[type] || type
}

// 获取事件类型标签
const getEventTypeTag = (type) => {
  return type === 'group_buy' ? 'success' : 'primary'
}

// 获取状态名称
const getStatusName = (status) => {
  const statuses = {
    'active': '进行中',
    'completed': '已完成',
    'cancelled': '已取消',
    'expired': '已过期'
  }
  return statuses[status] || status
}

// 获取状态标签
const getStatusTag = (status) => {
  const tags = {
    'active': 'success',
    'completed': 'info',
    'cancelled': 'danger',
    'expired': 'warning'
  }
  return tags[status] || 'info'
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).fromNow()
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-page {
  max-width: 1000px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.favorite-card:hover {
  transform: translateY(-2px);
}
</style>

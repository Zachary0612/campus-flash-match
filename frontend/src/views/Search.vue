<template>
  <Layout>
    <div class="search-page relative z-10">
      <!-- 搜索头部 -->
      <div class="glass rounded-2xl p-6 mb-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up">
        <div class="flex items-center mb-4">
          <el-icon class="text-2xl mr-3 text-primary"><Search /></el-icon>
          <h1 class="text-2xl font-bold text-gray-800">搜索事件</h1>
        </div>

        <!-- 搜索表单 -->
        <el-form :model="searchForm" :inline="true" class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索事件标题..."
              clearable
              class="!w-64"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-select v-model="searchForm.eventType" placeholder="事件类型" clearable class="!w-32">
              <el-option label="拼单" value="group_buy" />
              <el-option label="约伴" value="meetup" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-select v-model="searchForm.status" placeholder="状态" clearable class="!w-32">
              <el-option label="进行中" value="active" />
              <el-option label="已完成" value="completed" />
              <el-option label="已取消" value="cancelled" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              class="!w-64"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon class="mr-1"><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 搜索结果 -->
      <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.1s">
        <div class="flex items-center justify-between mb-4">
          <span class="text-gray-600">
            共找到 <span class="font-bold text-primary">{{ events.length }}</span> 个事件
          </span>
          <el-select v-model="searchForm.sortBy" placeholder="排序" class="!w-40" @change="handleSearch">
            <el-option label="最新发布" value="create_time" />
            <el-option label="目标人数" value="target_num" />
            <el-option label="当前人数" value="current_num" />
          </el-select>
        </div>

        <div v-if="loading" class="text-center py-12">
          <el-icon class="is-loading text-4xl text-primary"><Loading /></el-icon>
          <p class="mt-4 text-gray-500">搜索中...</p>
        </div>

        <div v-else-if="events.length === 0" class="text-center py-12">
          <el-icon class="text-6xl text-gray-300"><SearchIcon /></el-icon>
          <p class="mt-4 text-gray-500">未找到匹配的事件</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div
            v-for="event in events"
            :key="event.eventId"
            class="event-card bg-white/50 rounded-xl p-5 transition-all hover:shadow-lg cursor-pointer"
            @click="goToEventDetail(event.eventId)"
          >
            <div class="flex justify-between items-start mb-3">
              <el-tag :type="event.eventType === 'group_buy' ? 'success' : 'primary'" size="small">
                {{ event.eventType === 'group_buy' ? '拼单' : '约伴' }}
              </el-tag>
              <el-tag :type="getStatusTag(event.status)" size="small">
                {{ getStatusName(event.status) }}
              </el-tag>
            </div>

            <h3 class="font-bold text-gray-800 text-lg mb-2 line-clamp-2">{{ event.title }}</h3>

            <div class="flex items-center justify-between text-sm text-gray-500">
              <span class="flex items-center">
                <el-icon class="mr-1"><User /></el-icon>
                {{ event.currentNum }}/{{ event.targetNum }}
              </span>
              <span class="flex items-center">
                <el-icon class="mr-1"><Clock /></el-icon>
                {{ formatTime(event.createTime) }}
              </span>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="events.length > 0" class="mt-6 flex justify-center">
          <el-pagination
            v-model:current-page="searchForm.pageNum"
            :page-size="searchForm.pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handleSearch"
          />
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Layout from '@/components/Layout.vue'
import { searchEvents } from '@/api/event'
import { Search, Loading, User, Clock } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const events = ref([])
const total = ref(0)
const dateRange = ref([])

const searchForm = reactive({
  keyword: '',
  eventType: '',
  status: '',
  startTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 12,
  sortBy: 'create_time',
  sortOrder: 'desc'
})

// 搜索事件
const handleSearch = async () => {
  loading.value = true
  
  // 处理日期范围
  if (dateRange.value && dateRange.value.length === 2) {
    searchForm.startTime = dateRange.value[0]
    searchForm.endTime = dateRange.value[1]
  } else {
    searchForm.startTime = ''
    searchForm.endTime = ''
  }
  
  try {
    const res = await searchEvents(searchForm)
    if (res.code === 200) {
      events.value = res.data || []
      total.value = res.data?.length || 0
    }
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.eventType = ''
  searchForm.status = ''
  searchForm.startTime = ''
  searchForm.endTime = ''
  searchForm.pageNum = 1
  dateRange.value = []
  handleSearch()
}

// 跳转到事件详情
const goToEventDetail = (eventId) => {
  router.push(`/event/${eventId}`)
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

// 监听路由参数
watch(() => route.query, (query) => {
  if (query.keyword) {
    searchForm.keyword = query.keyword
    handleSearch()
  }
}, { immediate: true })

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.event-card:hover {
  transform: translateY(-2px);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

:deep(.search-form .el-form-item) {
  margin-bottom: 0;
}
</style>

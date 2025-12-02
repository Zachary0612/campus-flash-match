<template>
  <Layout>
    <div class="credit-page relative z-10">
      <!-- 信用分概览 -->
      <div class="glass rounded-2xl p-8 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 mb-8 animate-slide-up" style="animation-delay: 0.1s">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="col-span-1 flex justify-center md:justify-start">
            <div class="credit-score-card relative w-full max-w-[300px] aspect-square rounded-full flex flex-col items-center justify-center text-white shadow-2xl overflow-hidden group">
               <div class="absolute inset-0 bg-gradient-to-br from-primary to-purple-600 transition-transform duration-700 group-hover:scale-110"></div>
               <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>
               <div class="relative z-10 text-center">
                  <div class="text-lg opacity-80 font-medium mb-2 tracking-wider">当前信用分</div>
                  <div class="text-7xl font-black mb-2 drop-shadow-lg tracking-tighter">
                    {{ creditInfo.currentCredit || 0 }}
                  </div>
                  <div class="text-xl font-bold bg-white/20 px-4 py-1 rounded-full backdrop-blur-md border border-white/10 shadow-sm">
                    {{ getScoreDesc(creditInfo.currentCredit) }}
                  </div>
               </div>
            </div>
          </div>
          
          <div class="col-span-1 md:col-span-2 flex items-center">
            <div class="bg-white/60 backdrop-blur-sm rounded-xl p-6 border border-white/50 w-full h-full shadow-sm">
              <div class="flex items-center mb-4">
                <el-icon class="text-xl text-primary mr-2"><InfoFilled /></el-icon>
                <h3 class="text-lg font-bold text-gray-800">信用分规则说明</h3>
              </div>
              <ul class="space-y-3 text-gray-600">
                <li class="flex items-center bg-white/50 p-3 rounded-lg">
                   <div class="w-8 h-8 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center mr-3 text-sm font-bold">80</div>
                   <span>初始信用分</span>
                </li>
                <li class="flex items-center bg-white/50 p-3 rounded-lg">
                   <div class="w-8 h-8 rounded-full bg-green-100 text-green-600 flex items-center justify-center mr-3 font-bold text-lg">+2</div>
                   <span>成功参与事件</span>
                </li>
                <li class="flex items-center bg-white/50 p-3 rounded-lg">
                   <div class="w-8 h-8 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center mr-3 font-bold text-lg">-5</div>
                   <span>恶意退出事件</span>
                </li>
                <li class="flex items-center bg-white/50 p-3 rounded-lg">
                   <div class="w-8 h-8 rounded-full bg-red-100 text-red-600 flex items-center justify-center mr-3 font-bold text-lg">-10</div>
                   <span>发布虚假信标</span>
                </li>
                <li class="flex items-center text-red-500 text-sm mt-2 ml-2 font-medium">
                   <el-icon class="mr-1"><Warning /></el-icon>
                   信用分低于60分将限制发起事件
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 信用分变更记录 -->
      <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.2s">
        <div class="flex items-center justify-between mb-8 border-b border-white/20 pb-4">
          <span class="text-xl font-bold text-gray-800 flex items-center">
            <el-icon class="mr-2 text-primary"><List /></el-icon>
            信用分变更记录
          </span>
          <el-button circle @click="loadRecords" :loading="loading" class="!bg-white/50 !border-white/50 hover:!bg-white/80">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
        
        <div v-if="loading" class="text-center py-16">
          <el-icon class="is-loading text-primary" :size="40"><Loading /></el-icon>
        </div>
        
        <div v-else-if="records.length === 0" class="text-center py-16 text-gray-500">
          <div class="bg-gray-100/50 w-24 h-24 rounded-full flex items-center justify-center mx-auto mb-4">
            <el-icon :size="40" class="text-gray-400"><DocumentDelete /></el-icon>
          </div>
          <p class="text-lg font-medium">暂无变更记录</p>
        </div>
        
        <el-timeline v-else class="pl-4">
          <el-timeline-item
            v-for="record in records"
            :key="record.recordId"
            :timestamp="formatTime(record.createTime)"
            placement="top"
            :color="record.changeScore > 0 ? '#67C23A' : '#F56C6C'"
            :hollow="true"
            size="large"
          >
            <div class="bg-white/60 backdrop-blur-sm rounded-xl p-4 border border-white/50 shadow-sm hover:shadow-md transition-all flex items-center justify-between">
              <div class="flex-1">
                <div class="text-base font-bold text-gray-800 mb-1">{{ record.reason }}</div>
                <div class="text-sm text-gray-500 font-mono" v-if="record.eventId">
                  事件ID: {{ record.eventId }}
                </div>
              </div>
              <div
                class="text-2xl font-black tracking-tighter"
                :class="record.changeScore > 0 ? 'text-green-500' : 'text-red-500'"
              >
                {{ record.changeScore > 0 ? '+' : '' }}{{ record.changeScore }}
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        
        <div class="mt-6 flex justify-center">
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="loadRecords"
          />
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import Layout from '@/components/Layout.vue'
import { getCreditRecords } from '@/api/credit'
import { getCreditInfo } from '@/api/user'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import { Refresh, Loading, DocumentDelete, InfoFilled, Warning, List } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const records = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const creditInfo = reactive({
  userId: userStore.userId,
  currentCredit: userStore.creditScore, // Initialize with store value
  recentRecords: []
})

// Watch for store changes to update local state
watch(() => userStore.creditScore, (newVal) => {
  if (newVal) {
    creditInfo.currentCredit = newVal
  }
})

onMounted(() => {
  loadCreditInfo()
  loadRecords()
})

const loadCreditInfo = async () => {
  try {
    const res = await getCreditInfo()
    if (res.code === 200 && res.data) {
      // Update local state
      Object.assign(creditInfo, res.data)
      // Also update store to keep sync
      if (res.data.currentCredit) {
        userStore.userInfo.creditScore = res.data.currentCredit
        // Update localStorage
        localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
      }
    }
  } catch (error) {
    console.error('加载信用分信息失败:', error)
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getCreditRecords(pageNum.value, pageSize.value)
    if (res.code === 200) {
      records.value = res.data || []
      total.value = records.value.length
    }
  } catch (error) {
    console.error('加载信用分记录失败:', error)
  } finally {
    loading.value = false
  }
}

const getScoreDesc = (score) => {
  if (score >= 90) return '信用优秀'
  if (score >= 80) return '信用良好'
  if (score >= 60) return '信用一般'
  return '信用较低'
}

const formatTime = (timestamp) => {
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped>
.credit-page {
  max-width: 1000px;
  margin: 0 auto;
  padding-bottom: 40px;
}
</style>

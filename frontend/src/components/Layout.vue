<template>
  <div class="layout-container min-h-screen flex flex-col">
    <!-- 顶部导航栏 -->
    <header class="header glass fixed w-full top-0 z-50 px-6 h-16 flex items-center justify-between backdrop-blur-md bg-white/70 border-b border-white/40 shadow-sm transition-all duration-300">
      <div class="header-content w-full max-w-[1440px] mx-auto flex justify-between items-center">
        <div class="logo flex items-center text-primary cursor-pointer" @click="router.push('/home')">
          <div class="w-8 h-8 bg-gradient-to-tr from-primary to-blue-300 rounded-lg flex items-center justify-center shadow-lg mr-3">
             <el-icon :size="20" class="text-white"><Location /></el-icon>
          </div>
          <span class="text-xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-primary to-purple-600">校园闪配</span>
        </div>
        
        <div class="user-info flex items-center gap-4">
          <!-- 通知图标 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <el-button circle class="!bg-white/50 !border-white/60 hover:!bg-white/80" @click="router.push('/notifications')">
              <el-icon :size="18" class="text-gray-600"><Bell /></el-icon>
            </el-button>
          </el-badge>
          
          <el-tooltip content="当前信用分" placement="bottom">
            <div class="flex items-center bg-white/50 px-3 py-1.5 rounded-full border border-white/60 shadow-sm hover:shadow-md transition-all cursor-help">
               <el-icon :size="18" class="text-warning mr-1"><Medal /></el-icon>
               <span class="font-bold text-gray-700">{{ creditScore }}</span>
            </div>
          </el-tooltip>
          
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-dropdown flex items-center cursor-pointer hover:bg-white/50 px-2 py-1 rounded-lg transition-colors">
              <el-avatar :size="36" :src="avatar" class="mr-2 border-2 border-white shadow-sm bg-gradient-to-br from-blue-100 to-purple-100 text-primary font-bold">{{ nickname.charAt(0) }}</el-avatar>
              <span class="font-medium text-gray-700">{{ nickname }}</span>
              <el-icon class="ml-1 text-gray-400"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="!rounded-xl !border-none !shadow-xl !p-2">
                <el-dropdown-item command="profile" class="!rounded-lg !mb-1">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="credit" class="!rounded-lg !mb-1">
                  <el-icon><Medal /></el-icon>
                  信用分记录
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="!rounded-lg !text-red-500 hover:!bg-red-50">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    
    <!-- 主体内容 -->
    <div class="main-container flex flex-1 pt-16 max-w-[1440px] mx-auto w-full">
      <!-- 侧边栏 -->
      <aside class="sidebar w-64 fixed h-[calc(100vh-64px)] hidden md:block z-40">
        <div class="h-full p-4">
          <div class="glass h-full rounded-2xl bg-white/40 border-white/30 shadow-lg backdrop-blur-md overflow-hidden flex flex-col">
             <el-menu
              :default-active="activeMenu"
              router
              class="sidebar-menu !bg-transparent !border-none flex-1 p-2"
            >
              <el-menu-item index="/home" class="!rounded-xl !mb-2 !h-12 !font-medium !text-gray-600 hover:!text-primary hover:!bg-white/60">
                <template #title>
                  <el-icon class="text-lg"><HomeFilled /></el-icon>
                  <span class="text-base">首页</span>
                </template>
              </el-menu-item>
              <el-menu-item index="/events" class="!rounded-xl !mb-2 !h-12 !font-medium !text-gray-600 hover:!text-primary hover:!bg-white/60">
                <template #title>
                  <el-icon class="text-lg"><List /></el-icon>
                  <span class="text-base">我的事件</span>
                </template>
              </el-menu-item>
              <el-menu-item index="/beacon" class="!rounded-xl !mb-2 !h-12 !font-medium !text-gray-600 hover:!text-primary hover:!bg-white/60">
                 <template #title>
                   <el-icon class="text-lg"><LocationInformation /></el-icon>
                   <span class="text-base">占位信标</span>
                 </template>
              </el-menu-item>
              <el-menu-item index="/credit" class="!rounded-xl !mb-2 !h-12 !font-medium !text-gray-600 hover:!text-primary hover:!bg-white/60">
                <template #title>
                  <el-icon class="text-lg"><TrophyBase /></el-icon>
                  <span class="text-base">信用记录</span>
                </template>
              </el-menu-item>
              <el-menu-item index="/favorites" class="!rounded-xl !mb-2 !h-12 !font-medium !text-gray-600 hover:!text-primary hover:!bg-white/60">
                <template #title>
                  <el-icon class="text-lg"><Star /></el-icon>
                  <span class="text-base">我的收藏</span>
                </template>
              </el-menu-item>
              <el-menu-item index="/notifications" class="!rounded-xl !mb-2 !h-12 !font-medium !text-gray-600 hover:!text-primary hover:!bg-white/60">
                <template #title>
                  <el-icon class="text-lg"><Bell /></el-icon>
                  <span class="text-base">消息通知</span>
                </template>
              </el-menu-item>
            </el-menu>
            
            <div class="p-4 text-center text-xs text-gray-400/80 font-medium">
              © 2025 校园闪配
            </div>
          </div>
        </div>
      </aside>
      
      <!-- 内容区域 -->
      <main class="content flex-1 md:ml-64 p-6 overflow-x-hidden">
        <slot></slot>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWebSocketStore } from '@/stores/websocket'
import { getUnreadCount } from '@/api/notification'
import {
  Location,
  Medal,
  ArrowDown,
  User,
  SwitchButton,
  HomeFilled,
  List,
  LocationInformation,
  TrophyBase,
  Star,
  Bell
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const wsStore = useWebSocketStore()

const activeMenu = computed(() => route.path)
const nickname = computed(() => userStore.nickname)
const creditScore = computed(() => userStore.creditScore)
const avatar = computed(() => userStore.avatar)
const unreadCount = ref(0)

// 加载未读消息数
const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('获取未读消息数失败:', error)
  }
}

onMounted(() => {
  loadUnreadCount()
  // 每分钟刷新一次未读数
  setInterval(loadUnreadCount, 60000)
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'credit':
      router.push('/credit')
      break
    case 'logout':
      wsStore.disconnect()
      userStore.logout()
      break
  }
}
</script>

<style scoped>
:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.2) 100%) !important;
  color: var(--el-color-primary) !important;
  font-weight: 600 !important;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.1);
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.6) !important;
}

:deep(.el-menu-item .el-icon) {
  transition: transform 0.3s;
}

:deep(.el-menu-item:hover .el-icon) {
  transform: scale(1.1);
}
</style>

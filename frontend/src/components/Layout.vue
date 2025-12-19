<template>
  <div class="layout-container min-h-screen flex flex-col relative font-sans">
    <!-- Background image -->
    <div
        class="fixed inset-0 -z-10 bg-cover bg-center"
        style="background-image: url('https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=2000&q=80')"
    >
      <div class="absolute inset-0 bg-white/10 backdrop-blur-[1px]"></div>
    </div>
    <!-- 顶部导航栏 -->
    <header class="header glass fixed w-full top-0 z-50 px-6 h-20 flex items-center justify-between backdrop-blur-xl bg-white/60 border-b border-white/30 shadow-glass-sm transition-all duration-300">
      <div class="header-content w-full max-w-[1440px] mx-auto flex justify-between items-center">
        <div class="logo flex items-center group cursor-pointer" @click="router.push('/home')">
          <div class="w-10 h-10 bg-gradient-to-tr from-primary to-blue-400 rounded-xl flex items-center justify-center shadow-lg shadow-blue-500/30 mr-3 transition-transform duration-300 group-hover:scale-110 group-hover:rotate-3">
             <el-icon :size="22" class="text-white"><Location /></el-icon>
          </div>
          <span class="text-2xl font-black tracking-tighter bg-clip-text text-transparent bg-gradient-to-r from-gray-800 to-gray-600 group-hover:from-primary group-hover:to-blue-600 transition-all duration-300">校园闪配</span>
        </div>
        
        <div class="user-info flex items-center gap-5">
          <!-- 通知图标 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="transition-transform hover:scale-110">
            <el-button circle class="!bg-white/40 !border-white/40 hover:!bg-white/80 hover:!shadow-md transition-all duration-300 !w-11 !h-11" @click="router.push('/notifications')">
              <el-icon :size="20" class="text-gray-700"><Bell /></el-icon>
            </el-button>
          </el-badge>
          
          <el-tooltip content="当前信用分" placement="bottom" effect="light">
            <div class="flex items-center bg-gradient-to-r from-orange-50/80 to-yellow-50/80 px-4 py-2 rounded-full border border-orange-100/50 shadow-sm hover:shadow-md transition-all cursor-help group">
               <el-icon :size="20" class="text-orange-400 mr-1.5 group-hover:scale-110 transition-transform"><Medal /></el-icon>
               <span class="font-bold text-gray-700 font-mono text-lg">{{ creditScore }}</span>
            </div>
          </el-tooltip>
          
          <el-dropdown @command="handleCommand" trigger="click" popper-class="custom-dropdown">
            <span class="user-dropdown flex items-center cursor-pointer hover:bg-white/40 px-3 py-2 rounded-xl transition-all duration-300 border border-transparent hover:border-white/30 group">
              <el-avatar :size="40" :src="avatar" class="mr-3 border-2 border-white shadow-md bg-gradient-to-br from-blue-100 to-purple-100 text-primary font-bold transition-transform group-hover:scale-105">{{ nickname.charAt(0) }}</el-avatar>
              <div class="flex flex-col mr-2">
                <span class="font-bold text-gray-800 text-sm leading-tight">{{ nickname }}</span>
                <span class="text-[10px] text-gray-500 font-medium flex items-center"><span class="w-1.5 h-1.5 bg-green-500 rounded-full mr-1 animate-pulse"></span>在线</span>
              </div>
              <el-icon class="text-gray-400 transition-transform duration-300 group-hover:rotate-180"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="!rounded-2xl !border-none !shadow-glass !p-2 !bg-white/90 !backdrop-blur-xl">
                <el-dropdown-item command="profile" class="!rounded-xl !mb-1 !h-10 !font-medium">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="credit" class="!rounded-xl !mb-1 !h-10 !font-medium">
                  <el-icon><Medal /></el-icon>
                  信用分记录
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="!rounded-xl !text-red-500 hover:!bg-red-50 !h-10 !font-medium">
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
    <div class="main-container flex flex-1 pt-24 max-w-[1440px] mx-auto w-full px-6 pb-8 gap-8">
      <!-- 侧边栏 -->
      <aside class="sidebar w-72 fixed h-[calc(100vh-120px)] hidden md:block z-40 top-24 rounded-3xl overflow-hidden shadow-glass-sm transition-all duration-300 hover:shadow-glass">
        <div class="h-full">
          <div class="glass h-full bg-white/50 border-white/40 backdrop-blur-xl flex flex-col p-5">
             <el-menu
              :default-active="activeMenu"
              router
              class="sidebar-menu !bg-transparent !border-none flex-1"
            >
              <div class="mb-3 px-4 text-xs font-black text-gray-400 uppercase tracking-wider">菜单</div>
              
              <el-menu-item index="/home" class="menu-item">
                <template #title>
                  <el-icon><HomeFilled /></el-icon>
                  <span>首页</span>
                </template>
              </el-menu-item>
              
              <el-menu-item index="/events" class="menu-item">
                <template #title>
                  <el-icon><List /></el-icon>
                  <span>我的事件</span>
                </template>
              </el-menu-item>
              
              <div class="mt-8 mb-3 px-4 text-xs font-black text-gray-400 uppercase tracking-wider">个人</div>
              
              <el-menu-item index="/credit" class="menu-item">
                <template #title>
                  <el-icon><TrophyBase /></el-icon>
                  <span>信用记录</span>
                </template>
              </el-menu-item>
              
              <el-menu-item index="/favorites" class="menu-item">
                <template #title>
                  <el-icon><Star /></el-icon>
                  <span>我的收藏</span>
                </template>
              </el-menu-item>
              
              <el-menu-item index="/notifications" class="menu-item">
                <template #title>
                  <el-icon><Bell /></el-icon>
                  <span>消息通知</span>
                </template>
              </el-menu-item>
            </el-menu>
            
            <div class="p-2 mt-auto">
              <div class="rounded-2xl bg-gradient-to-br from-white/40 to-blue-50/30 p-5 border border-white/50 shadow-sm backdrop-blur-sm">
                <div class="flex items-center gap-3 mb-2">
                  <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-blue-400 flex items-center justify-center shadow-md">
                    <el-icon class="text-white text-sm"><Location /></el-icon>
                  </div>
                  <div class="text-sm font-bold text-gray-800">校园闪配 App</div>
                </div>
                <div class="text-xs text-gray-500 mb-2 pl-1">随时随地，想约就约</div>
                <div class="text-[10px] text-gray-400/80 font-medium pl-1"> 2025 v1.0.0</div>
              </div>
            </div>
          </div>
        </div>
      </aside>
      
      <!-- 内容区域 -->
      <main class="content flex-1 md:ml-[310px] min-w-0">
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
.menu-item {
  @apply !rounded-2xl !mb-2 !h-14 !mx-0 !font-bold !text-gray-600 !border-none transition-all duration-300 !flex !items-center;
}

.menu-item:hover {
  @apply !bg-white/70 !text-primary !shadow-sm translate-x-1;
}

.menu-item.is-active {
  @apply !bg-gradient-to-r !from-blue-50/90 !to-blue-100/50 !text-primary shadow-sm;
}

:deep(.el-menu-item .el-icon) {
  @apply !text-xl mr-3 transition-transform duration-300;
}

.menu-item:hover :deep(.el-icon) {
  @apply scale-110 rotate-3;
}

.menu-item.is-active :deep(.el-icon) {
  @apply text-primary scale-110;
}
</style>

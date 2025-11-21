<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <el-icon :size="24" class="mr-2"><Location /></el-icon>
          <span class="text-xl font-bold">校园闪配</span>
        </div>
        
        <div class="user-info">
          <el-badge :value="creditScore" :max="99" class="mr-4">
            <el-icon :size="20"><Medal /></el-icon>
          </el-badge>
          
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" class="mr-2">{{ nickname.charAt(0) }}</el-avatar>
              <span>{{ nickname }}</span>
              <el-icon class="ml-1"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="credit">
                  <el-icon><Medal /></el-icon>
                  信用分记录
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    
    <!-- 主体内容 -->
    <el-container class="main-container">
      <!-- 侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/events">
            <el-icon><List /></el-icon>
            <span>我的事件</span>
          </el-menu-item>
          <el-menu-item index="/beacon">
            <el-icon><LocationInformation /></el-icon>
            <span>占位信标</span>
          </el-menu-item>
          <el-menu-item index="/credit">
            <el-icon><TrophyBase /></el-icon>
            <span>信用记录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 内容区域 -->
      <el-main class="content">
        <slot></slot>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWebSocketStore } from '@/stores/websocket'
import {
  Location,
  Medal,
  ArrowDown,
  User,
  SwitchButton,
  HomeFilled,
  List,
  LocationInformation,
  TrophyBase
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const wsStore = useWebSocketStore()

const activeMenu = computed(() => route.path)
const nickname = computed(() => userStore.nickname)
const creditScore = computed(() => userStore.creditScore)

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
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 24px;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  color: #409EFF;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.sidebar {
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.content {
  background: #f5f7fa;
  overflow-y: auto;
  padding: 24px;
}
</style>

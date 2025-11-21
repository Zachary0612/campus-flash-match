<template>
  <router-view />
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useWebSocketStore } from '@/stores/websocket'

const userStore = useUserStore()
const wsStore = useWebSocketStore()

onMounted(() => {
  // 检查token有效性
  if (userStore.isLoggedIn) {
    if (userStore.checkTokenValidity()) {
      // token有效，连接WebSocket
      wsStore.connect()
    }
    // 如果token无效，checkTokenValidity会自动登出
  }
})

onUnmounted(() => {
  // 组件卸载时断开WebSocket
  wsStore.disconnect()
})
</script>

<style scoped>
</style>

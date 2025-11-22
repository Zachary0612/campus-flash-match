import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useUserStore } from './user'
import { ElNotification, ElMessage } from 'element-plus'

export const useWebSocketStore = defineStore('websocket', () => {
  const ws = ref(null)
  const connected = ref(false)
  const reconnectTimer = ref(null)
  const heartbeatTimer = ref(null)
  
  // 消息处理器
  const messageHandlers = ref({})
  
  // 连接WebSocket
  function connect() {
    const userStore = useUserStore()
    
    if (!userStore.userId) {
      console.warn('用户未登录，无法建立WebSocket连接')
      return
    }
    
    // 如果已经连接，先断开
    if (ws.value) {
      disconnect()
    }
    
    try {
      // 构建WebSocket URL，将userId放在路径中，并携带token
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const host = window.location.hostname
      const port = import.meta.env.DEV ? '8080' : window.location.port
      const token = localStorage.getItem('token')
      
      // 确保路径格式正确：/ws/{userId}?token={token}
      const wsUrl = `${protocol}//${host}:${port}/ws/${userStore.userId}${token ? `?token=${encodeURIComponent(token)}` : ''}`
      
      console.log('尝试连接WebSocket:', wsUrl)
      ws.value = new WebSocket(wsUrl)
      
      ws.value.onopen = () => {
        console.log('WebSocket连接成功')
        connected.value = true
        
        // 启动心跳
        startHeartbeat()
        
        // 清除重连定时器
        if (reconnectTimer.value) {
          clearTimeout(reconnectTimer.value)
          reconnectTimer.value = null
        }
      }
      
      ws.value.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          handleMessage(message)
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }
      
      ws.value.onerror = (error) => {
        console.error('WebSocket连接错误:', error)
        ElNotification({
          title: '连接失败',
          message: 'WebSocket连接失败，请检查网络或重新登录',
          type: 'error',
          duration: 5000
        })
      }
      
      ws.value.onclose = () => {
        console.log('WebSocket连接关闭')
        connected.value = false
        stopHeartbeat()
        
        // 尝试重连
        if (userStore.isLoggedIn) {
          reconnectTimer.value = setTimeout(() => {
            console.log('尝试重新连接WebSocket...')
            connect()
          }, 5000)
        }
      }
    } catch (error) {
      console.error('创建WebSocket连接失败:', error)
    }
  }
  
  // 断开连接
  function disconnect() {
    if (ws.value) {
      ws.value.close()
      ws.value = null
    }
    connected.value = false
    stopHeartbeat()
    
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value)
      reconnectTimer.value = null
    }
  }
  
  // 发送消息
  function send(message) {
    if (ws.value && connected.value) {
      ws.value.send(JSON.stringify(message))
    } else {
      console.warn('WebSocket未连接，无法发送消息')
    }
  }
  
  // 订阅事件
  function subscribeEvent(eventId) {
    send({
      type: 'subscribe_event',
      eventId: eventId
    })
  }
  
  // 处理接收到的消息
  function handleMessage(message) {
    console.log('收到WebSocket消息:', message)
    
    const { type, data } = message
    
    switch (type) {
      case 'connected':
        console.log('WebSocket连接确认')
        break
        
      case 'pong':
        // 心跳响应
        break
        
      case 'event_full':
        // 事件满员通知
        ElNotification({
          title: '事件满员',
          message: data?.message || '您参与的事件已满员',
          type: 'success',
          duration: 5000
        })
        break
        
      case 'event_settled':
        // 事件结算通知
        ElNotification({
          title: '事件结算',
          message: data?.message || '事件已结算',
          type: 'info',
          duration: 5000
        })
        // 更新信用分
        const userStore = useUserStore()
        userStore.updateCreditScore()
        break
        
      case 'new_participant':
        // 新参与者加入
        ElNotification({
          title: '新成员加入',
          message: data?.message || '有新成员加入事件',
          type: 'info',
          duration: 3000
        })
        break
        
      default:
        // 调用自定义处理器
        if (messageHandlers.value[type]) {
          messageHandlers.value[type](data)
        }
    }
  }
  
  // 注册消息处理器
  function onMessage(type, handler) {
    messageHandlers.value[type] = handler
  }
  
  // 移除消息处理器
  function offMessage(type) {
    delete messageHandlers.value[type]
  }
  
  // 启动心跳
  function startHeartbeat() {
    heartbeatTimer.value = setInterval(() => {
      send({ type: 'ping' })
    }, 30000) // 每30秒发送一次心跳
  }
  
  // 停止心跳
  function stopHeartbeat() {
    if (heartbeatTimer.value) {
      clearInterval(heartbeatTimer.value)
      heartbeatTimer.value = null
    }
  }
  
  return {
    connected,
    connect,
    disconnect,
    send,
    subscribeEvent,
    onMessage,
    offMessage
  }
})
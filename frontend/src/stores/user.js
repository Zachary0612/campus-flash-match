import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getCreditInfo as getCreditInfoApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => userInfo.value?.userId || null)
  const nickname = computed(() => userInfo.value?.nickname || '')
  const creditScore = computed(() => userInfo.value?.creditScore || 0)
  const avatar = computed(() => userInfo.value?.avatar || '')
  
  // 登录
  async function login(loginForm) {
    try {
      const res = await loginApi(loginForm)
      
      if (res.code === 200 && res.data) {
        token.value = res.data.token
        userInfo.value = {
          userId: res.data.userId,
          nickname: res.data.nickname,
          creditScore: res.data.creditScore,
          avatar: res.data.avatar || ''
        }
        
        // 保存到localStorage
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        
        ElMessage.success('登录成功')
        return true
      }
      return false
    } catch (error) {
      console.error('登录失败:', error)
      return false
    }
  }
  
  // 注册
  async function register(registerForm) {
    try {
      const res = await registerApi(registerForm)
      
      if (res.code === 200) {
        ElMessage.success('注册成功，请登录')
        return true
      }
      return false
    } catch (error) {
      console.error('注册失败:', error)
      return false
    }
  }
  
  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出登录')
    router.push('/login')
  }
  
  // 更新信用分
  async function updateCreditScore() {
    try {
      const res = await getCreditInfoApi()
      if (res.code === 200 && res.data) {
        if (userInfo.value) {
          userInfo.value.creditScore = res.data.currentCredit
          localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        }
      }
    } catch (error) {
      console.error('更新信用分失败:', error)
      // 如果是401错误，说明token过期，自动登出
      if (error.response && error.response.status === 401) {
        logout()
      }
    }
  }
  
  // 设置头像
  function setAvatar(avatarUrl) {
    if (userInfo.value) {
      userInfo.value.avatar = avatarUrl
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }
  
  // 检查token是否有效
  function checkTokenValidity() {
    if (!token.value) {
      return false
    }
    
    try {
      // 简单的token格式检查
      const parts = token.value.split('.')
      if (parts.length !== 3) {
        return false
      }
      
      // 解析token payload检查过期时间
      const payload = JSON.parse(atob(parts[1]))
      const currentTime = Math.floor(Date.now() / 1000)
      
      if (payload.exp && payload.exp < currentTime) {
        console.log('Token已过期，自动登出')
        logout()
        return false
      }
      
      return true
    } catch (error) {
      console.error('Token验证失败:', error)
      logout()
      return false
    }
  }
  
  return {
    token,
    userInfo,
    isLoggedIn,
    userId,
    nickname,
    creditScore,
    avatar,
    login,
    register,
    logout,
    updateCreditScore,
    setAvatar,
    checkTokenValidity
  }
})

<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden">
    <!-- Background image -->
    <div class="absolute top-0 left-0 w-full h-full -z-10">
      <img src="/photo1.jpg" alt="background" class="w-full h-full object-cover" />
    </div>

    <div class="glass p-10 rounded-[2rem] shadow-glass w-full max-w-md animate-fade-in backdrop-blur-xl bg-white/30 border-white/40">
      <div class="text-center mb-10 animate-slide-up" style="animation-delay: 0.1s">
        <h1 class="text-5xl font-black text-gray-800 mb-3 tracking-tighter bg-clip-text text-transparent bg-gradient-to-r from-primary to-purple-600 drop-shadow-sm">校园闪配</h1>
        <p class="text-gray-600 font-bold text-lg tracking-wide opacity-80">校园拼单约伴平台</p>
      </div>
      
      <div class="flex justify-center mb-8 animate-slide-up" style="animation-delay: 0.2s">
        <el-radio-group v-model="loginMode" size="large" class="!bg-white/20 !p-1 !rounded-full shadow-inner">
          <el-radio-button label="studentId" class="!rounded-full overflow-hidden">
            <span class="flex items-center gap-2 px-2"><el-icon><User /></el-icon> 学号登录</span>
          </el-radio-button>
          <el-radio-button label="email" class="!rounded-full overflow-hidden">
             <span class="flex items-center gap-2 px-2"><el-icon><Message /></el-icon> 邮箱登录</span>
          </el-radio-button>
        </el-radio-group>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0" class="animate-slide-up space-y-6" style="animation-delay: 0.3s">
        <el-form-item prop="account">
          <el-input
            v-model="loginForm.account"
            :placeholder="accountPlaceholder"
            size="large"
            :prefix-icon="User"
            class="glass-input h-12"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
            class="glass-input h-12"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="w-full !rounded-xl !h-12 !text-lg !font-bold shadow-lg shadow-blue-500/30 hover:shadow-blue-500/50 transition-all duration-300 hover:-translate-y-1"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="text-center mt-8 animate-slide-up flex items-center justify-center gap-2" style="animation-delay: 0.4s">
        <span class="text-gray-500 font-medium">还没有账号？</span>
        <router-link to="/register" class="text-primary font-extrabold hover:text-purple-600 transition-colors flex items-center group">
          立即注册 <el-icon class="ml-0.5 group-hover:translate-x-1 transition-transform"><ArrowRight /></el-icon>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useWebSocketStore } from '@/stores/websocket'
import { User, Lock, Message, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const wsStore = useWebSocketStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginMode = ref('studentId')

const loginForm = reactive({
  account: '',
  password: ''
})

const accountPlaceholder = computed(() => 
  loginMode.value === 'studentId' ? '请输入学号' : '请输入邮箱'
)

const validateAccount = (rule, value, callback) => {
  if (!value) {
    callback(new Error(loginMode.value === 'studentId' ? '请输入学号' : '请输入邮箱'))
    return
  }
  if (loginMode.value === 'email') {
    const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
    if (!emailRegex.test(value)) {
      callback(new Error('邮箱格式不正确'))
      return
    }
  }
  callback()
}

watch(loginMode, () => {
  loginForm.account = ''
})

const rules = {
  account: [
    { required: true, validator: validateAccount, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.login(loginForm)
        if (success) {
          // 登录成功后连接WebSocket
          wsStore.connect()
          router.push('/home')
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* Radio Button Styles */
:deep(.el-radio-button__inner) {
  border: none !important;
  background: transparent !important;
  border-radius: 9999px !important;
  color: #666;
  padding: 8px 24px;
  transition: all 0.3s ease;
  font-weight: 600;
  box-shadow: none !important;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: white !important;
  color: var(--el-color-primary) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
}
</style>

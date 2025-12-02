<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden">
    <!-- Background decorations -->
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden -z-10">
      <div class="absolute top-[-10%] left-[-10%] w-96 h-96 bg-primary/30 rounded-full mix-blend-multiply filter blur-3xl opacity-70 animate-blob"></div>
      <div class="absolute top-[-10%] right-[-10%] w-96 h-96 bg-purple-300 rounded-full mix-blend-multiply filter blur-3xl opacity-70 animate-blob animation-delay-2000"></div>
      <div class="absolute bottom-[-20%] left-[20%] w-96 h-96 bg-pink-300 rounded-full mix-blend-multiply filter blur-3xl opacity-70 animate-blob animation-delay-4000"></div>
    </div>

    <div class="glass p-8 rounded-3xl shadow-2xl w-full max-w-md animate-fade-in backdrop-blur-xl bg-white/40 border-white/20">
      <div class="text-center mb-8 animate-slide-up" style="animation-delay: 0.1s">
        <h1 class="text-4xl font-extrabold text-gray-800 mb-2 tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-primary to-purple-600">校园闪配</h1>
        <p class="text-gray-600 font-medium">校园拼单约伴平台</p>
      </div>
      
      <div class="flex justify-center mb-8 animate-slide-up" style="animation-delay: 0.2s">
        <el-radio-group v-model="loginMode" size="large" class="shadow-sm">
          <el-radio-button label="studentId">学号登录</el-radio-button>
          <el-radio-button label="email">邮箱登录</el-radio-button>
        </el-radio-group>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0" class="animate-slide-up" style="animation-delay: 0.3s">
        <el-form-item prop="account">
          <el-input
            v-model="loginForm.account"
            :placeholder="accountPlaceholder"
            size="large"
            :prefix-icon="User"
            class="custom-input"
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
            class="custom-input"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="w-full !rounded-xl !h-12 !text-lg !font-semibold shadow-lg hover:shadow-primary/50 transition-all duration-300 hover:-translate-y-0.5"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="text-center mt-6 animate-slide-up" style="animation-delay: 0.4s">
        <span class="text-gray-600">还没有账号？</span>
        <router-link to="/register" class="text-primary font-bold hover:text-purple-600 ml-2 transition-colors">
          立即注册
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
import { User, Lock } from '@element-plus/icons-vue'

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
.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 8px 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.5);
  background-color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.2) !important;
  border-color: var(--el-color-primary);
  background-color: #fff;
}

.custom-input :deep(.el-input__inner) {
  height: 32px;
}

.animate-blob {
  animation: blob 7s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.animation-delay-4000 {
  animation-delay: 4s;
}

@keyframes blob {
  0% {
    transform: translate(0px, 0px) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
  100% {
    transform: translate(0px, 0px) scale(1);
  }
}
</style>

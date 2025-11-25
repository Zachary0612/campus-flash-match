<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
    <div class="bg-white p-8 rounded-2xl shadow-2xl w-full max-w-md">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-800 mb-2">校园闪配</h1>
        <p class="text-gray-500">校园拼单约伴平台</p>
      </div>
      
      <div class="flex justify-center mb-4">
        <el-radio-group v-model="loginMode" size="large">
          <el-radio-button label="studentId">学号登录</el-radio-button>
          <el-radio-button label="email">邮箱登录</el-radio-button>
        </el-radio-group>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0">
        <el-form-item prop="account">
          <el-input
            v-model="loginForm.account"
            :placeholder="accountPlaceholder"
            size="large"
            :prefix-icon="User"
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
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="w-full"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="text-center mt-4">
        <span class="text-gray-600">还没有账号？</span>
        <router-link to="/register" class="text-blue-500 hover:text-blue-600 ml-2">
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
</style>

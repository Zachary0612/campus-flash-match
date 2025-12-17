<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden">
    <!-- Background image -->
    <div class="absolute top-0 left-0 w-full h-full -z-10">
      <img src="/photo1.jpg" alt="background" class="w-full h-full object-cover" />
    </div>

    <div class="glass p-8 rounded-[2rem] shadow-glass w-full max-w-md animate-fade-in backdrop-blur-xl bg-white/30 border-white/40 my-8">
      <div class="text-center mb-8 animate-slide-up" style="animation-delay: 0.1s">
        <h1 class="text-3xl font-black text-gray-800 mb-2 tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-success to-teal-600 drop-shadow-sm">用户注册</h1>
        <p class="text-gray-600 font-bold text-sm tracking-wide opacity-80">请使用校园网络注册</p>
      </div>
      
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="0" class="animate-slide-up space-y-5" style="animation-delay: 0.2s">
        <el-form-item prop="studentId">
          <el-input
            v-model="registerForm.studentId"
            placeholder="请输入学号"
            size="large"
            :prefix-icon="User"
            class="glass-input h-11"
          />
        </el-form-item>
        
        <el-form-item prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="请输入昵称"
            size="large"
            :prefix-icon="Avatar"
            class="glass-input h-11"
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            size="large"
            :prefix-icon="Message"
            class="glass-input h-11"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（8-20位，含字母、数字、特殊符号）"
            size="large"
            :prefix-icon="Lock"
            show-password
            class="glass-input h-11"
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
            class="glass-input h-11"
          />
        </el-form-item>

        <el-form-item prop="verifyCode">
          <div class="flex gap-3 w-full">
            <el-input
              v-model="registerForm.verifyCode"
              placeholder="请输入邮箱验证码"
              size="large"
              maxlength="6"
              class="glass-input flex-1 h-11"
            />
            <el-button
              type="primary"
              size="large"
              class="shrink-0 !rounded-xl !h-11 !font-bold shadow-md hover:shadow-primary/40 transition-all duration-300"
              :loading="sendingCode"
              :disabled="codeCountdown > 0"
              @click="handleSendCode"
            >
              <span v-if="codeCountdown === 0">发送验证码</span>
              <span v-else>{{ codeCountdown }}s后重发</span>
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="w-full !rounded-xl !h-12 !text-lg !font-bold shadow-lg hover:shadow-success/50 transition-all duration-300 hover:-translate-y-1 !bg-gradient-to-r !from-success !to-teal-500 !border-none"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="text-center mt-6 animate-slide-up flex items-center justify-center gap-2" style="animation-delay: 0.3s">
        <span class="text-gray-600 font-medium">已有账号？</span>
        <router-link to="/login" class="text-success font-extrabold hover:text-teal-600 transition-colors flex items-center group">
          立即登录 <el-icon class="ml-0.5 group-hover:translate-x-1 transition-transform"><ArrowRight /></el-icon>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock, Avatar, Message, ArrowRight } from '@element-plus/icons-vue'
import { sendEmailCode } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

const registerForm = reactive({
  studentId: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
  verifyCode: ''
})

// 密码强度验证
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
    return
  }
  if (value.length < 8 || value.length > 20) {
    callback(new Error('密码长度必须在8-20位之间'))
    return
  }
  if (!/[a-zA-Z]/.test(value)) {
    callback(new Error('密码必须包含至少一个字母'))
    return
  }
  if (!/[0-9]/.test(value)) {
    callback(new Error('密码必须包含至少一个数字'))
    return
  }
  if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?~`]/.test(value)) {
    callback(new Error('密码必须包含至少一个特殊符号'))
    return
  }
  callback()
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在2-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const startCountdown = () => {
  codeCountdown.value = 60
  countdownTimer = setInterval(() => {
    codeCountdown.value -= 1
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const handleSendCode = async () => {
  if (codeCountdown.value > 0 || sendingCode.value) return
  if (!registerForm.email) {
    ElMessage.error('请先输入邮箱')
    return
  }

  sendingCode.value = true
  try {
    const res = await sendEmailCode(registerForm.email)
    if (res.code === 200) {
      ElMessage.success('验证码已发送，请检查邮箱')
      startCountdown()
    } else {
      ElMessage.error(res.message || '验证码发送失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error?.response?.data?.message || '验证码发送失败，请稍后再试')
  } finally {
    sendingCode.value = false
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.register({
          studentId: registerForm.studentId,
          nickname: registerForm.nickname,
          password: registerForm.password,
          email: registerForm.email,
          verifyCode: registerForm.verifyCode
        })
        if (success) {
          router.push('/login')
        }
      } finally {
        loading.value = false
      }
    }
  })
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
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
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.2) !important;
  border-color: var(--el-color-success);
  background-color: #fff;
}

.custom-input :deep(.el-input__inner) {
  height: 32px;
}

</style>

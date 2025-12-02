<template>
  <Layout>
    <div class="user-profile-page relative z-10">
      <!-- 用户信息头部 -->
      <div class="glass rounded-2xl p-8 mb-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up">
        <div class="flex items-start gap-8">
          <!-- 头像 -->
          <div class="relative">
            <div class="absolute inset-0 bg-gradient-to-br from-primary to-purple-500 rounded-full blur opacity-50"></div>
            <el-avatar 
              :size="120" 
              :src="profile.avatar"
              class="relative border-4 border-white shadow-md text-4xl font-bold bg-gradient-to-br from-primary to-blue-400"
            >
              {{ profile.nickname?.charAt(0) || '?' }}
            </el-avatar>
            <el-upload
              v-if="isOwner"
              class="avatar-uploader absolute bottom-0 right-0"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
            >
              <el-button type="primary" circle size="small" class="shadow-md">
                <el-icon><Camera /></el-icon>
              </el-button>
            </el-upload>
          </div>

          <!-- 用户信息 -->
          <div class="flex-1">
            <div class="flex items-center gap-4 mb-2">
              <h1 class="text-2xl font-bold text-gray-800">{{ profile.nickname }}</h1>
              <el-tag v-if="profile.gender === 1" type="primary" size="small">男</el-tag>
              <el-tag v-else-if="profile.gender === 2" type="danger" size="small">女</el-tag>
            </div>
            
            <div class="text-sm text-gray-500 mb-4 space-y-1">
              <p v-if="profile.major">{{ profile.major }} · {{ profile.grade }}</p>
              <p v-if="profile.bio" class="text-gray-600">{{ profile.bio }}</p>
            </div>

            <!-- 兴趣标签 -->
            <div v-if="profile.interests" class="flex flex-wrap gap-2 mb-4">
              <el-tag 
                v-for="tag in profile.interests.split(',')" 
                :key="tag" 
                type="info" 
                effect="plain"
                size="small"
              >
                {{ tag.trim() }}
              </el-tag>
            </div>

            <!-- 操作按钮 -->
            <div class="flex gap-3">
              <template v-if="isOwner">
                <el-button type="primary" @click="showEditDialog = true">
                  <el-icon class="mr-1"><Edit /></el-icon>编辑资料
                </el-button>
              </template>
              <template v-else>
                <el-button 
                  :type="profile.isFollowed ? 'default' : 'primary'"
                  @click="handleFollow"
                  :loading="followLoading"
                >
                  <el-icon class="mr-1"><component :is="profile.isFollowed ? 'Check' : 'Plus'" /></el-icon>
                  {{ profile.isFollowed ? '已关注' : '关注' }}
                </el-button>
              </template>
            </div>
          </div>

          <!-- 统计数据 -->
          <div class="flex gap-8 text-center">
            <div class="stat-item cursor-pointer hover:opacity-80" @click="showFollowDialog('following')">
              <div class="text-2xl font-bold text-gray-800">{{ profile.followingCount || 0 }}</div>
              <div class="text-sm text-gray-500">关注</div>
            </div>
            <div class="stat-item cursor-pointer hover:opacity-80" @click="showFollowDialog('followers')">
              <div class="text-2xl font-bold text-gray-800">{{ profile.followerCount || 0 }}</div>
              <div class="text-sm text-gray-500">粉丝</div>
            </div>
            <div class="stat-item">
              <div class="text-2xl font-bold" :class="getScoreClass(profile.creditScore)">{{ profile.creditScore || 0 }}</div>
              <div class="text-sm text-gray-500">信用分</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="mb-6">
        <el-col :span="6">
          <div class="glass rounded-xl p-6 text-center shadow-lg backdrop-blur-lg bg-white/30 animate-slide-up" style="animation-delay: 0.1s">
            <div class="text-3xl font-bold text-blue-500">{{ statistics.createdEventCount || 0 }}</div>
            <div class="text-sm text-gray-500 mt-2">发起事件</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="glass rounded-xl p-6 text-center shadow-lg backdrop-blur-lg bg-white/30 animate-slide-up" style="animation-delay: 0.15s">
            <div class="text-3xl font-bold text-green-500">{{ statistics.joinedEventCount || 0 }}</div>
            <div class="text-sm text-gray-500 mt-2">参与事件</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="glass rounded-xl p-6 text-center shadow-lg backdrop-blur-lg bg-white/30 animate-slide-up" style="animation-delay: 0.2s">
            <div class="text-3xl font-bold text-orange-500">{{ statistics.ratingCount || 0 }}</div>
            <div class="text-sm text-gray-500 mt-2">收到评价</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="glass rounded-xl p-6 text-center shadow-lg backdrop-blur-lg bg-white/30 animate-slide-up" style="animation-delay: 0.25s">
            <div class="text-3xl font-bold text-yellow-500">{{ statistics.averageRating?.toFixed(1) || '-' }}</div>
            <div class="text-sm text-gray-500 mt-2">平均评分</div>
          </div>
        </el-col>
      </el-row>

      <!-- 收到的评价 -->
      <div class="glass rounded-2xl p-6 shadow-lg backdrop-blur-lg bg-white/30 border-white/40 animate-slide-up" style="animation-delay: 0.3s">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-bold text-gray-800 flex items-center">
            <el-icon class="mr-2 text-yellow-500"><Star /></el-icon>
            收到的评价
          </h2>
        </div>

        <div v-if="ratings.length === 0" class="text-center py-8 text-gray-400">
          暂无评价
        </div>

        <div v-else class="space-y-4">
          <div v-for="rating in ratings" :key="rating.id" class="bg-white/50 rounded-xl p-4">
            <div class="flex items-start gap-4">
              <el-avatar :size="40">{{ rating.raterNickname?.charAt(0) }}</el-avatar>
              <div class="flex-1">
                <div class="flex items-center justify-between mb-2">
                  <span class="font-medium text-gray-800">{{ rating.raterNickname }}</span>
                  <el-rate v-model="rating.score" disabled size="small" />
                </div>
                <p class="text-sm text-gray-600">{{ rating.comment }}</p>
                <div v-if="rating.tags" class="mt-2 flex flex-wrap gap-1">
                  <el-tag v-for="tag in rating.tags.split(',')" :key="tag" size="small" type="info">
                    {{ tag.trim() }}
                  </el-tag>
                </div>
                <div class="text-xs text-gray-400 mt-2">{{ formatTime(rating.createTime) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 编辑资料对话框 -->
      <el-dialog v-model="showEditDialog" title="编辑个人资料" width="500px">
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="昵称">
            <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="editForm.gender">
              <el-radio :label="0">保密</el-radio>
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="专业">
            <el-input v-model="editForm.major" placeholder="请输入专业" />
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="editForm.grade" placeholder="请选择年级">
              <el-option label="大一" value="大一" />
              <el-option label="大二" value="大二" />
              <el-option label="大三" value="大三" />
              <el-option label="大四" value="大四" />
              <el-option label="研一" value="研一" />
              <el-option label="研二" value="研二" />
              <el-option label="研三" value="研三" />
              <el-option label="博士" value="博士" />
            </el-select>
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input v-model="editForm.bio" type="textarea" :rows="3" placeholder="介绍一下自己吧" />
          </el-form-item>
          <el-form-item label="兴趣标签">
            <el-input v-model="editForm.interests" placeholder="多个标签用逗号分隔，如：编程,篮球,音乐" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="handleUpdateProfile" :loading="updateLoading">保存</el-button>
        </template>
      </el-dialog>

      <!-- 关注/粉丝列表对话框 -->
      <el-dialog v-model="showFollowListDialog" :title="followListType === 'following' ? '关注列表' : '粉丝列表'" width="400px">
        <div v-if="followList.length === 0" class="text-center py-8 text-gray-400">
          暂无数据
        </div>
        <div v-else class="space-y-3 max-h-96 overflow-y-auto">
          <div v-for="user in followList" :key="user.userId" class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <div class="flex items-center gap-3">
              <el-avatar :size="40">{{ user.nickname?.charAt(0) }}</el-avatar>
              <div>
                <div class="font-medium">{{ user.nickname }}</div>
                <div class="text-xs text-gray-400">{{ user.isMutual ? '互相关注' : '' }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { useUserStore } from '@/stores/user'
import { getMyProfile, getUserProfile, updateProfile, uploadAvatar, getUserStatistics } from '@/api/profile'
import { followUser, unfollowUser, getFollowingList, getFollowerList, getUserFollowingList, getUserFollowerList } from '@/api/follow'
import { getUserReceivedRatings } from '@/api/rating'
import { Camera, Edit, Star, Plus, Check } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const userStore = useUserStore()

const profile = ref({})
const statistics = ref({})
const ratings = ref([])
const loading = ref(false)
const followLoading = ref(false)
const updateLoading = ref(false)
const showEditDialog = ref(false)
const showFollowListDialog = ref(false)
const followListType = ref('following')
const followList = ref([])

const editForm = reactive({
  nickname: '',
  bio: '',
  gender: 0,
  major: '',
  grade: '',
  interests: ''
})

// 判断是否是自己的主页
const isOwner = computed(() => {
  const userId = route.params.userId
  return !userId || userId == userStore.userId
})

// 获取用户ID
const getUserId = () => {
  return route.params.userId || userStore.userId
}

// 加载用户资料
const loadProfile = async () => {
  loading.value = true
  try {
    const userId = getUserId()
    const res = isOwner.value ? await getMyProfile() : await getUserProfile(userId)
    if (res.code === 200) {
      profile.value = res.data
      // 同步编辑表单
      Object.assign(editForm, {
        nickname: res.data.nickname,
        bio: res.data.bio,
        gender: res.data.gender || 0,
        major: res.data.major,
        grade: res.data.grade,
        interests: res.data.interests
      })
    }
  } catch (error) {
    console.error('加载用户资料失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const userId = isOwner.value ? null : getUserId()
    const res = await getUserStatistics(userId)
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载评价
const loadRatings = async () => {
  try {
    const userId = getUserId()
    const res = await getUserReceivedRatings(userId, { pageNum: 1, pageSize: 10 })
    if (res.code === 200) {
      ratings.value = res.data || []
    }
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

// 关注/取消关注
const handleFollow = async () => {
  followLoading.value = true
  try {
    const userId = getUserId()
    if (profile.value.isFollowed) {
      await unfollowUser(userId)
      profile.value.isFollowed = false
      profile.value.followerCount--
      ElMessage.success('已取消关注')
    } else {
      await followUser(userId)
      profile.value.isFollowed = true
      profile.value.followerCount++
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    followLoading.value = false
  }
}

// 更新资料
const handleUpdateProfile = async () => {
  updateLoading.value = true
  try {
    await updateProfile(editForm)
    ElMessage.success('资料更新成功')
    showEditDialog.value = false
    loadProfile()
    // 更新store中的昵称
    if (editForm.nickname) {
      userStore.nickname = editForm.nickname
    }
  } catch (error) {
    console.error('更新资料失败:', error)
  } finally {
    updateLoading.value = false
  }
}

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传头像
const handleAvatarUpload = async ({ file }) => {
  try {
    const res = await uploadAvatar(file)
    if (res.code === 200) {
      profile.value.avatar = res.data
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
  }
}

// 显示关注/粉丝列表
const showFollowDialog = async (type) => {
  followListType.value = type
  showFollowListDialog.value = true
  
  try {
    const userId = getUserId()
    let res
    if (type === 'following') {
      res = isOwner.value ? await getFollowingList() : await getUserFollowingList(userId)
    } else {
      res = isOwner.value ? await getFollowerList() : await getUserFollowerList(userId)
    }
    if (res.code === 200) {
      followList.value = res.data || []
    }
  } catch (error) {
    console.error('加载列表失败:', error)
  }
}

// 信用分颜色
const getScoreClass = (score) => {
  if (score >= 90) return 'text-emerald-500'
  if (score >= 80) return 'text-blue-500'
  if (score >= 60) return 'text-yellow-500'
  return 'text-red-500'
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).fromNow()
}

// 监听路由变化
watch(() => route.params.userId, () => {
  loadProfile()
  loadStatistics()
  loadRatings()
})

onMounted(() => {
  loadProfile()
  loadStatistics()
  loadRatings()
})
</script>

<style scoped>
.user-profile-page {
  max-width: 1000px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.avatar-uploader {
  z-index: 10;
}
</style>

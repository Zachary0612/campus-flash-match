<template>
  <el-dialog
    v-model="visible"
    title="评价参与者"
    width="500px"
    :close-on-click-modal="false"
  >
    <div v-if="participant" class="rating-content">
      <!-- 被评价者信息 -->
      <div class="flex items-center gap-4 mb-6 p-4 bg-gray-50 rounded-xl">
        <el-avatar :size="50">{{ participant.nickname?.charAt(0) }}</el-avatar>
        <div>
          <div class="font-bold text-gray-800">{{ participant.nickname }}</div>
          <div class="text-sm text-gray-500">{{ eventTitle }}</div>
        </div>
      </div>

      <!-- 评分 -->
      <div class="mb-6">
        <div class="text-sm font-medium text-gray-700 mb-2">评分</div>
        <el-rate
          v-model="ratingForm.score"
          :texts="['很差', '较差', '一般', '较好', '很好']"
          show-text
          size="large"
        />
      </div>

      <!-- 评价标签 -->
      <div class="mb-6">
        <div class="text-sm font-medium text-gray-700 mb-2">评价标签</div>
        <div class="flex flex-wrap gap-2">
          <el-tag
            v-for="tag in availableTags"
            :key="tag"
            :type="selectedTags.includes(tag) ? 'primary' : 'info'"
            :effect="selectedTags.includes(tag) ? 'dark' : 'plain'"
            class="cursor-pointer"
            @click="toggleTag(tag)"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>

      <!-- 评价内容 -->
      <div class="mb-4">
        <div class="text-sm font-medium text-gray-700 mb-2">评价内容（可选）</div>
        <el-input
          v-model="ratingForm.comment"
          type="textarea"
          :rows="3"
          placeholder="说说你对TA的评价..."
          maxlength="200"
          show-word-limit
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading" :disabled="!ratingForm.score">
        提交评价
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { submitRating } from '@/api/rating'

const props = defineProps({
  modelValue: Boolean,
  eventId: String,
  eventTitle: String,
  participant: Object
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const selectedTags = ref([])

const ratingForm = reactive({
  score: 0,
  comment: '',
  tags: ''
})

const availableTags = [
  '准时', '友好', '靠谱', '有趣', '热心',
  '守信', '礼貌', '专业', '耐心', '高效'
]

// 切换标签
const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    if (selectedTags.value.length < 5) {
      selectedTags.value.push(tag)
    } else {
      ElMessage.warning('最多选择5个标签')
    }
  }
}

// 提交评价
const handleSubmit = async () => {
  if (!ratingForm.score) {
    ElMessage.warning('请选择评分')
    return
  }

  loading.value = true
  try {
    await submitRating({
      eventId: props.eventId,
      ratedUserId: props.participant.userId,
      score: ratingForm.score,
      comment: ratingForm.comment,
      tags: selectedTags.value.join(',')
    })
    ElMessage.success('评价成功')
    emit('success')
    handleClose()
  } catch (error) {
    console.error('评价失败:', error)
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  // 重置表单
  ratingForm.score = 0
  ratingForm.comment = ''
  selectedTags.value = []
}

// 监听对话框打开，重置表单
watch(() => props.modelValue, (val) => {
  if (val) {
    ratingForm.score = 0
    ratingForm.comment = ''
    selectedTags.value = []
  }
})
</script>

<style scoped>
.rating-content {
  padding: 0 10px;
}
</style>

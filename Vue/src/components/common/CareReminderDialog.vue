<template>
  <el-dialog
    v-model="dialogVisible"
    title="🌱 养护提醒"
    width="500px"
    :before-close="handleClose"
    class="care-reminder-dialog"
  >
    <div class="reminder-content">
      <div class="reminder-icon">
        <el-icon :size="60" color="#52c41a">
          <Bell />
        </el-icon>
      </div>
      
      <h3 class="reminder-title">{{ reminderData?.brief }}</h3>
      
      <div class="reminder-detail">
        <pre>{{ reminderData?.content }}</pre>
      </div>
      
      <div class="reminder-time">
        ⏰ 提醒时间：{{ formatTime(reminderData?.remindTime) }}
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">稍后处理</el-button>
        <el-button type="success" @click="handleConfirm">
          <el-icon><Check /></el-icon>
          立即养护
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Check } from '@element-plus/icons-vue'

const router = useRouter()

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  reminderData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'close'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const handleClose = () => {
  emit('close')
}

const handleConfirm = () => {
  router.push('/user/plant-care')
  emit('close')
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.care-reminder-dialog :deep(.el-dialog) {
  border-radius: 16px;
}

.reminder-content {
  text-align: center;
  padding: 20px 0;
}

.reminder-icon {
  margin-bottom: 20px;
  animation: shake 1s infinite;
}

@keyframes shake {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-10deg); }
  75% { transform: rotate(10deg); }
}

.reminder-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.reminder-detail {
  background: #f5f5f5;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  text-align: left;
}

.reminder-detail pre {
  margin: 0;
  font-family: inherit;
  white-space: pre-wrap;
  color: #666;
  line-height: 1.6;
}

.reminder-time {
  font-size: 14px;
  color: #999;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
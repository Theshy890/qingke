<template>
  <transition name="slide-down">
    <div v-if="visible" class="notification-bar">
      <div class="notification-content">
        <el-icon class="notification-icon" :size="20" color="#ff9800">
          <BellFilled />
        </el-icon>
        
        <div class="notification-text">
          <span
            v-for="(item, index) in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ 'is-clickable': item.type === '新手引导' }"
            @click="handleItemClick(item)"
          >
            <el-tag :type="getTagType(item.type)" size="small">{{ item.type }}</el-tag>
            {{ item.title }}
            <span v-if="index < notifications.length - 1" class="divider">|</span>
          </span>
        </div>
        
        <el-icon class="close-icon" @click="handleClose">
          <Close />
        </el-icon>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  notifications: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'click'])

const visible = ref(true)

const handleClose = () => {
  visible.value = false
  setTimeout(() => {
    emit('close')
  }, 300)
}

const getTagType = (type) => {
  const typeMap = {
    '系统通知': 'success',
    '系统活动': 'danger',
    '新手引导': 'warning'
  }
  return typeMap[type] || 'info'
}

const handleItemClick = (item) => {
  if (item.type === '新手引导') {
    emit('click', item)
  }
}
</script>

<style scoped>
.notification-bar {
  position: fixed;
  top: 70px;
  left: 0;
  right: 0;
  z-index: 999;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 12px 24px;
}

.notification-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-icon {
  flex-shrink: 0;
}

.notification-text {
  flex: 1;
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-item.is-clickable {
  cursor: pointer;
  transition: all 0.3s;
}

.notification-item.is-clickable:hover {
  color: #52c41a;
}

.divider {
  margin: 0 8px;
  color: #ccc;
}

.close-icon {
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.close-icon:hover {
  color: #f44336;
  transform: scale(1.2);
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s;
}

.slide-down-enter-from {
  transform: translateY(-100%);
  opacity: 0;
}

.slide-down-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}
</style>
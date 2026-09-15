<template>
  <!-- 浮窗主按钮 -->
  <div class="cs-fab-wrap">
    <!-- 展开后的选项面板 -->
    <transition name="cs-panel">
      <div v-if="panelVisible" class="cs-panel">
        <div class="cs-panel-header">
          <span> 青稞客服</span>
          <el-icon class="cs-close-btn" @click="panelVisible = false"><Close /></el-icon>
        </div>
        <div class="cs-options">
          <div class="cs-option" @click="openAiChat">
            <div class="cs-option-icon ai">
              <el-icon :size="28"><ChatDotRound /></el-icon>
            </div>
            <div class="cs-option-info">
              <div class="cs-option-title">AI 客服</div>
              <div class="cs-option-desc">智能问答，秒速响应</div>
            </div>
            <el-icon class="cs-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </transition>

    <!-- FAB 按钮 -->
    <div class="cs-fab" :class="{ active: aiChatVisible }" @click="openAiChat">
      <el-icon :size="26" class="cs-fab-icon"><ChatDotRound /></el-icon>
      <span class="cs-fab-label">AI客服</span>
      <!-- 未读红点 -->
      <span v-if="!panelVisible" class="cs-badge"></span>
    </div>
  </div>

  <!-- AI 客服对话弹窗 -->
  <el-dialog
    v-model="aiChatVisible"
    width="820px"
    :close-on-click-modal="false"
    draggable
    class="cs-dialog"
    @open="onChatOpen"
  >
    <template #header>
      <div class="cs-dialog-header">
        <span>🤖 AI 智能客服 · 稞儿</span>
        <el-button
          size="small"
          text
          type="info"
          @click="clearMessages"
          style="font-size: 12px; color: #bbb;"
        >清空记录</el-button>
      </div>
    </template>
    <!-- 单一 wrapper：隔离全局 .el-dialog__body>div 规则，内部布局自控 -->
    <div class="cs-dialog-inner">
      <div class="cs-chat-body" ref="chatBodyRef">
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="cs-msg-row"
          :class="msg.role"
        >
          <div v-if="msg.role === 'bot'" class="cs-avatar bot-avatar">🤖</div>
          <div class="cs-bubble" :class="msg.role">
            <!-- 用户消息中的图片 -->
            <img v-if="msg.role === 'user' && msg.image" :src="msg.image" class="cs-bubble-img" @click="previewImage(msg.image)" />
            <!-- 文字内容 -->
            <div v-if="msg.content">{{ msg.content }}</div>
          </div>
          <div v-if="msg.role === 'user'" class="cs-avatar user-avatar">
            <el-icon><User /></el-icon>
          </div>
        </div>
        <div v-if="aiLoading" class="cs-msg-row bot">
          <div class="cs-avatar bot-avatar">🤖</div>
          <div class="cs-bubble bot cs-typing">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <!-- 快捷问题 -->
      <div class="cs-quick-wrap">
        <el-tag
          v-for="q in quickQuestions"
          :key="q"
          class="cs-quick-tag"
          @click="sendQuick(q)"
        >{{ q }}</el-tag>
      </div>

      <!-- 图片预览区 -->
      <div v-if="pendingImage" class="cs-image-preview">
        <img :src="pendingImage" class="cs-preview-img" />
        <el-icon class="cs-preview-remove" @click="removePendingImage"><Close /></el-icon>
      </div>

      <!-- 输入区 -->
      <div class="cs-input-wrap">
        <div class="cs-input-area">
          <el-input
            v-model="inputText"
            placeholder="请输入您的问题...（支持 Ctrl+V 粘贴图片）"
            :rows="4"
            type="textarea"
            resize="none"
            @keyup.enter.exact="sendMessage"
            @paste="handlePaste"
          />
          <!-- 上传图片按钮 -->
          <div class="cs-upload-btn" @click="triggerFileInput" title="上传图片">
            <el-icon :size="20"><Picture /></el-icon>
          </div>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleFileSelect"
          />
        </div>
        <el-button
          type="primary"
          class="cs-send-btn"
          :loading="aiLoading"
          @click="sendMessage"
        >
          <el-icon v-if="!aiLoading"><Promotion /></el-icon>
          发送
        </el-button>
      </div>

      <div class="cs-dialog-tip">内容由AI生成，仅供参考，遇复杂问题请联系人工客服</div>
    </div>
  </el-dialog>

  <!-- 图片预览弹窗 -->
  <el-image-viewer
    v-if="previewVisible"
    :url-list="[previewUrl]"
    @close="previewVisible = false"
  />
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Close, ChatDotRound, User, Promotion, Picture
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const aiChatVisible = ref(false)
const inputText = ref('')
const aiLoading = ref(false)
const chatBodyRef = ref(null)
const messages = ref([])
const pendingImage = ref('')
const fileInputRef = ref(null)
const previewVisible = ref(false)
const previewUrl = ref('')

const quickQuestions = [
  '如何养护多肉植物？',
  'AI识别次数用完了怎么办？',
  '如何设置养护提醒？',
  '怎么发帖到社区？'
]

// 获取 token
const getToken = () => {
  return localStorage.getItem('user_token') || localStorage.getItem('token')
}

// 加载历史聊天记录
const loadHistory = async () => {
  if (!getToken()) return
  try {
    const res = await request({ url: '/api/ai/chat/history', method: 'get' })
    if (res.code === 200 && Array.isArray(res.data) && res.data.length > 0) {
      messages.value = res.data.map(item => ({
        role: 'user',
        content: item.ask,
        image: item.imageUrl || null,
        _hasReply: true
      })).flatMap((msg, i) => {
        const reply = res.data[i].reply
        return [
          msg,
          { role: 'bot', content: reply || '抱歉，暂时无法回答，请联系人工客服 😊' }
        ]
      })
    }
  } catch {
    // 加载失败不阻塞
  }
}

// 显示欢迎语（无历史记录时）
const botGreet = () => {
  messages.value = [
    {
      role: 'bot',
      content: '您好！我是青稞绿植养护 AI 客服稞儿 🤖，请问有什么可以帮您？'
    }
  ]
}

const onChatOpen = async () => {
  await loadHistory()
  if (messages.value.length === 0) botGreet()
  scrollToBottom()
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

// 触发文件选择
const triggerFileInput = () => {
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileSelect = (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (ev) => {
    pendingImage.value = ev.target.result
  }
  reader.readAsDataURL(file)
  // 清空 input 以便重复选择同一文件
  e.target.value = ''
}

// 处理粘贴事件
const handlePaste = (e) => {
  const items = e.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      e.preventDefault()
      const file = item.getAsFile()
      if (!file) return
      if (file.size > 5 * 1024 * 1024) {
        ElMessage.warning('粘贴的图片大小不能超过 5MB')
        return
      }
      const reader = new FileReader()
      reader.onload = (ev) => {
        pendingImage.value = ev.target.result
      }
      reader.readAsDataURL(file)
      break
    }
  }
}

// 移除待发送图片
const removePendingImage = () => {
  pendingImage.value = ''
}

// 预览大图
const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  const image = pendingImage.value

  // 纯文字模式：文字不能为空
  // 多模态模式：有图片时文字可以为空
  if (!image && !text) return

  // 构建用户消息
  const userMsg = { role: 'user', content: text, image: image || null }
  messages.value.push(userMsg)

  // 清空输入
  inputText.value = ''
  pendingImage.value = ''
  aiLoading.value = true
  scrollToBottom()

  try {
    const token = getToken()
    const headers = { 'Content-Type': 'application/json' }
    if (token) headers['Authorization'] = `Bearer ${token}`

    const body = { question: text }
    if (image) {
      body.imageBase64 = image
    }

    const res = await fetch('/api/ai/chat', {
      method: 'POST',
      headers,
      body: JSON.stringify(body)
    })
    const result = await res.json()
    messages.value.push({
      role: 'bot',
      content: result.code === 200 && result.data
        ? result.data
        : '抱歉，暂时无法回答，请联系人工客服 😊'
    })
  } catch {
    messages.value.push({
      role: 'bot',
      content: '网络异常，请稍后重试，或联系人工客服。'
    })
  } finally {
    aiLoading.value = false
    scrollToBottom()
  }
}

const sendQuick = (q) => {
  inputText.value = q
  sendMessage()
}

// 清空聊天记录（调用后端删除）
const clearMessages = () => {
  ElMessageBox.confirm(
    '确定要清空所有对话记录吗？清空后将无法恢复。',
    '清空记录',
    {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      const token = getToken()
      const headers = {}
      if (token) headers['Authorization'] = `Bearer ${token}`

      await fetch('/api/ai/chat/history', {
        method: 'DELETE',
        headers
      })
      messages.value = []
      botGreet()
      ElMessage.success('对话记录已清空')
    } catch {
      ElMessage.error('清空失败，请重试')
    }
  }).catch(() => {})
}

const openAiChat = () => {
  aiChatVisible.value = true
}

</script>

<style scoped>
/* ===== FAB 浮窗 ===== */
.cs-fab-wrap {
  position: fixed;
  right: 24px;
  bottom: 80px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.cs-fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  box-shadow: 0 4px 16px rgba(82, 196, 26, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: white;
  position: relative;
  user-select: none;
}

.cs-fab:hover,
.cs-fab.active {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(82, 196, 26, 0.55);
}

.cs-fab-icon {
  margin-bottom: 2px;
}

.cs-fab-label {
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
}

.cs-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 10px;
  height: 10px;
  background: #ff4d4f;
  border-radius: 50%;
  border: 2px solid white;
  animation: cs-pulse 2s infinite;
}

@keyframes cs-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.3); opacity: 0.7; }
}

/* ===== AI 对话弹窗 ===== */
.cs-chat-body {
  height: 560px;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f7f9f7;
  border-radius: 8px;
  margin-bottom: 12px;
}

/* ===== 单一 wrapper 隔离全局 .el-dialog__body>div{flex:1;display:flex;column} 规则 ===== */
.cs-dialog-inner {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.cs-dialog-inner > .cs-chat-body {
  flex: 1 1 auto;
  height: auto;
  min-height: 0;
}

.cs-dialog-inner > .cs-quick-wrap,
.cs-dialog-inner > .cs-image-preview,
.cs-dialog-inner > .cs-input-wrap,
.cs-dialog-inner > .cs-dialog-tip {
  flex: 0 0 auto;
}

.cs-msg-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.cs-msg-row.user {
  flex-direction: row-reverse;
}

.cs-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.bot-avatar {
  background: #e8f4fd;
  border: 1px solid #91d5ff;
  font-size: 20px;
}

.user-avatar {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.cs-bubble {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.cs-bubble.bot {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.cs-bubble.user {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

/* 气泡中的图片 */
.cs-bubble-img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
  display: block;
  margin-bottom: 6px;
  object-fit: cover;
}

/* 打字动画 */
.cs-typing {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 12px 16px;
}

.cs-typing span {
  width: 7px;
  height: 7px;
  background: #52c41a;
  border-radius: 50%;
  animation: cs-bounce 1.2s infinite;
}

.cs-typing span:nth-child(2) { animation-delay: 0.2s; }
.cs-typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes cs-bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-6px); }
}

/* 快捷提问 */
.cs-quick-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.cs-quick-tag {
  cursor: pointer;
  font-size: 12px;
  color: #52c41a;
  border-color: #b7eb8f;
  background: #f6ffed;
  transition: all 0.2s;
}

.cs-quick-tag:hover {
  background: #52c41a;
  color: white;
  border-color: #52c41a;
}

/* 图片预览区 */
.cs-image-preview {
  position: relative;
  display: inline-block;
  margin-bottom: 8px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e0e0e0;
  max-width: 120px;
}

.cs-preview-img {
  width: 100%;
  height: auto;
  display: block;
  max-height: 120px;
  object-fit: cover;
}

.cs-preview-remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
}

.cs-preview-remove:hover {
  background: rgba(0, 0, 0, 0.7);
}

/* 输入区 */
.cs-input-wrap {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.cs-input-area {
  flex: 1;
  position: relative;
}

.cs-input-wrap :deep(.el-textarea__inner) {
  border-radius: 8px;
  resize: none;
  padding-bottom: 32px;
}

/* 上传图片按钮（输入框右下角） */
.cs-upload-btn {
  position: absolute;
  bottom: 8px;
  right: 10px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #999;
  border-radius: 4px;
  transition: all 0.2s;
  z-index: 1;
}

.cs-upload-btn:hover {
  color: #52c41a;
  background: #f0f9eb;
}

.cs-send-btn {
  height: 96px;
  padding: 0 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border: none;
  flex-shrink: 0;
}

.cs-dialog-tip {
  text-align: center;
  font-size: 11px;
  color: #bbb;
  margin-top: 12px;
  width: 100%;
}

/* 弹窗深度覆盖 */
:deep(.cs-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #f6ffed 0%, #e8f5e9 100%);
  border-radius: 8px 8px 0 0;
  padding: 16px 20px;
  cursor: move;
}

:deep(.cs-dialog .el-dialog__title) {
  font-weight: 600;
  color: #333;
}

:deep(.cs-dialog .el-dialog__body) {
  padding: 16px 20px;
  height: 660px;
  max-height: 80vh;
}

:deep(.cs-dialog .el-dialog) {
  min-width: 400px;
}

.cs-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #333;
  width: 100%;
}
</style>

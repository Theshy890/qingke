<template>
  <div class="messages-page">
    <!-- 左侧会话列表 -->
    <aside class="conversation-list">
      <div class="list-header">
        <span class="list-title">消息</span>
        <el-button link @click="loadConversations">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
      <div class="list-body">
        <div v-if="conversations.length === 0" class="empty-tip">
          <el-empty description="暂无会话" :image-size="80" />
        </div>
        <div
          v-for="item in conversations"
          :key="item.userId"
          class="conversation-item"
          :class="{ active: currentTarget && currentTarget.userId === item.userId }"
          @click="openConversation(item)"
        >
          <el-badge :value="item.unreadCount" :hidden="!item.unreadCount" :max="99">
            <el-avatar :size="46" :src="item.avatarUrl || defaultAvatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </el-badge>
          <div class="conversation-info">
            <div class="conversation-top">
              <span class="conversation-name">{{ item.nickname || '用户' }}</span>
              <span class="conversation-time">{{ formatTime(item.lastMessageTime) }}</span>
            </div>
            <p class="conversation-preview">{{ item.lastMessage || '' }}</p>
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧聊天窗 -->
    <section class="chat-panel">
      <template v-if="currentTarget">
        <div class="chat-header">
          <el-avatar :size="36" :src="currentTarget.avatarUrl || defaultAvatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="chat-target-name">{{ currentTarget.nickname || '用户' }}</span>
        </div>

        <div class="chat-body" ref="chatBodyRef">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-row"
            :class="{ mine: msg.senderId === currentUserId }"
          >
            <el-avatar
              v-if="msg.senderId !== currentUserId"
              :size="34"
              :src="currentTarget.avatarUrl || defaultAvatar"
              class="message-avatar"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="message-content">
              <div v-if="msg.isRecalled === 1" class="recalled-tip">
                {{ msg.senderId === currentUserId ? '你撤回了一条消息' : '对方撤回了一条消息' }}
              </div>
              <template v-else>
                <div class="message-bubble">
                  <div v-if="msg.quoteContent" class="quote-block">{{ msg.quoteContent }}</div>
                  {{ msg.content }}
                </div>
                <div class="message-time">{{ formatFullTime(msg.createTime) }}</div>
              </template>
            </div>
            <div v-if="msg.isRecalled !== 1" class="message-actions">
              <span class="action-item" @click.stop="handleQuote(msg)">引用</span>
              <span
                v-if="msg.senderId === currentUserId && canRevoke(msg)"
                class="action-item"
                @click.stop="handleRevoke(msg)"
              >撤回</span>
              <span class="action-item danger" @click.stop="handleDelete(msg)">删除</span>
            </div>
          </div>
          <div v-if="messages.length === 0" class="chat-empty">还没有消息，发一条开始聊天吧~</div>
        </div>

        <div class="chat-input">
          <div v-if="quoteTarget" class="quote-bar">
            <div class="quote-bar-info">
              <span class="quote-bar-name">回复 {{ quoteTarget.nickname }}</span>
              <span class="quote-bar-content">{{ quoteTarget.content }}</span>
            </div>
            <el-icon class="quote-bar-close" @click="quoteTarget = null"><Close /></el-icon>
          </div>
          <div class="chat-input-row">
            <el-input
              v-model="draft"
              type="textarea"
              :rows="3"
              resize="none"
              maxlength="500"
              show-word-limit
              placeholder="输入消息，Enter 发送"
              @keydown.enter.exact.prevent="handleSend"
            />
            <el-button type="success" round :loading="sending" @click="handleSend">发送</el-button>
          </div>
        </div>
      </template>

      <div v-else class="chat-placeholder">
        <el-icon :size="60" color="#c8e6c9"><ChatDotRound /></el-icon>
        <p>选择左侧会话开始聊天</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Refresh, ChatDotRound, Close } from '@element-plus/icons-vue'
import { userMessageApi, userProfileApi } from '@/api'
import defaultAvatar from '@/assets/img/touxiang.png'

const route = useRoute()
const currentUserId = JSON.parse(localStorage.getItem('userInfo') || '{}').id
const currentNickname = JSON.parse(localStorage.getItem('userInfo') || '{}').name || '我'

const conversations = ref([])
const currentTarget = ref(null)
const messages = ref([])
const draft = ref('')
const sending = ref(false)
const chatBodyRef = ref(null)
const quoteTarget = ref(null)

// 加载会话列表
const loadConversations = async () => {
  try {
    const res = await userMessageApi.getConversations(currentUserId)
    if (res.code === 200) {
      conversations.value = res.data || []
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatBodyRef.value) {
      chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
    }
  })
}

// 加载与目标用户的聊天记录
const loadMessages = async (targetUserId) => {
  try {
    const res = await userMessageApi.getChatHistory(currentUserId, targetUserId, 1, 100)
    if (res.code === 200) {
      // 后端按时间倒序分页，这里转正序展示
      messages.value = (res.data.list || []).slice().reverse()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error)
  }
}

// 打开会话
const openConversation = async (item) => {
  currentTarget.value = item
  quoteTarget.value = null
  await loadMessages(item.userId)
  if (item.unreadCount > 0) {
    try {
      await userMessageApi.markAsRead(currentUserId, item.userId)
      item.unreadCount = 0
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

// 发送消息
const handleSend = async () => {
  const content = draft.value.trim()
  if (!content || !currentTarget.value || sending.value) return
  sending.value = true
  try {
    const quoteContent = quoteTarget.value
      ? `${quoteTarget.value.nickname}：${quoteTarget.value.content}`
      : null
    const res = await userMessageApi.sendMessage(
      currentUserId, currentTarget.value.userId, content, quoteContent || undefined
    )
    if (res.code === 200) {
      // 后端返回插入后的完整消息（含 id 与服务器时间），用于撤回
      const saved = res.data || {}
      messages.value.push({
        id: saved.id,
        senderId: currentUserId,
        content,
        quoteContent: saved.quoteContent || quoteContent,
        isRecalled: 0,
        createTime: saved.createTime || new Date().toISOString().replace('T', ' ').slice(0, 19)
      })
      draft.value = ''
      quoteTarget.value = null
      scrollToBottom()
      loadConversations()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    console.error('发送失败:', error)
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

// 引用消息：填入引用条
const handleQuote = (msg) => {
  quoteTarget.value = {
    nickname: msg.senderId === currentUserId ? currentNickname : (currentTarget.value?.nickname || '用户'),
    content: msg.content
  }
}

// 撤回时限：2 分钟内
const canRevoke = (msg) => {
  if (!msg.createTime) return false
  const sent = new Date(msg.createTime.replace(/-/g, '/'))
  return Date.now() - sent.getTime() <= 2 * 60 * 1000
}

// 撤回消息
const handleRevoke = async (msg) => {
  try {
    const res = await userMessageApi.revokeMessage(msg.id, currentUserId)
    if (res.code === 200) {
      msg.isRecalled = 1
      loadConversations()
    } else {
      ElMessage.error(res.message || '撤回失败')
    }
  } catch (error) {
    console.error('撤回失败:', error)
    ElMessage.error('撤回失败')
  }
}

// 删除消息（仅自己不可见）
const handleDelete = async (msg) => {
  try {
    await ElMessageBox.confirm('删除后该消息仅对你不可见，对方仍可查看。确定删除吗？', '删除消息', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    const res = await userMessageApi.deleteMessage(msg.id, currentUserId)
    if (res.code === 200) {
      messages.value = messages.value.filter(m => m.id !== msg.id)
      loadConversations()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 从主页"私信"入口进入：自动打开与 to 用户的会话
const openConversationWith = async (targetUserId) => {
  let target = conversations.value.find(c => String(c.userId) === String(targetUserId))
  if (!target) {
    // 无历史会话，拉取对方资料发起新会话
    try {
      const res = await userProfileApi.getUserProfile(targetUserId)
      if (res.code === 200 && res.data) {
        target = {
          userId: res.data.id,
          nickname: res.data.name,
          avatarUrl: res.data.avatarUrl,
          unreadCount: 0
        }
      }
    } catch (error) {
      console.error('加载用户信息失败:', error)
    }
  }
  if (target) {
    await openConversation(target)
  }
}

// 时间格式化
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const today = new Date().toISOString().slice(0, 10)
  const date = timeStr.slice(0, 10)
  return date === today ? timeStr.slice(11, 16) : date.slice(5)
}

const formatFullTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.slice(5, 16)
}

onMounted(async () => {
  await loadConversations()
  const to = route.query.to
  if (to) {
    await openConversationWith(to)
  } else if (conversations.value.length > 0) {
    await openConversation(conversations.value[0])
  }
})
</script>

<style scoped>
.messages-page {
  display: flex;
  height: calc(100vh - 120px);
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 左侧会话列表 */
.conversation-list {
  width: 300px;
  flex-shrink: 0;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.list-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.list-body {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.conversation-item:hover {
  background: #f5f7fa;
}

.conversation-item.active {
  background: #e8f5e9;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-name {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.conversation-preview {
  margin: 4px 0 0;
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-tip {
  padding: 40px 20px;
  text-align: center;
}

.empty-sub {
  font-size: 13px;
  color: #999;
  margin-top: 8px;
}

/* 右侧聊天窗 */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.chat-target-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
}

.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
}

.message-row.mine {
  flex-direction: row-reverse;
}

.message-actions {
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  flex-shrink: 0;
}

.message-row:hover .message-actions {
  display: flex;
}

.action-item {
  font-size: 12px;
  color: #999;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  white-space: nowrap;
}

.action-item:hover {
  color: #4caf50;
}

.action-item.danger:hover {
  color: #f56c6c;
}

.recalled-tip {
  align-self: center;
  font-size: 12px;
  color: #bbb;
  padding: 4px 0;
}

.quote-block {
  border-left: 3px solid rgba(76, 175, 80, 0.5);
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
  padding: 4px 8px;
  margin-bottom: 6px;
  font-size: 12px;
  color: #888;
  max-height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-row.mine .quote-block {
  border-left-color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.85);
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 60%;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  background: white;
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-row.mine .message-bubble {
  background: #4caf50;
  color: white;
}

.message-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
}

.message-row.mine .message-time {
  text-align: right;
}

.chat-empty {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 40px 0;
}

.chat-input {
  padding: 14px 20px;
  border-top: 1px solid #f0f0f0;
}

.chat-input-row {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.chat-input-row .el-input {
  flex: 1;
}

.quote-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  background: #f5f7fa;
  border-left: 3px solid #4caf50;
  border-radius: 6px;
  padding: 8px 12px;
  margin-bottom: 10px;
}

.quote-bar-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.quote-bar-name {
  font-size: 12px;
  color: #4caf50;
}

.quote-bar-content {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quote-bar-close {
  cursor: pointer;
  color: #999;
  flex-shrink: 0;
}

.quote-bar-close:hover {
  color: #f56c6c;
}

.chat-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #999;
}

@media (max-width: 768px) {
  .conversation-list {
    width: 220px;
  }

  .message-content {
    max-width: 75%;
  }
}
</style>

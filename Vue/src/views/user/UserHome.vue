<template>
  <div class="user-home">
    <!-- 顶部导航栏 -->
    <header class="user-header">
      <div class="header-content">
        <div class="logo-area">
          <img :src="logoImg" class="logo-icon" alt="logo" />
          <h1 class="logo-title">青稞绿植养护</h1>
        </div>
        
        <nav class="nav-menu">
          <router-link to="/user/index" class="nav-item">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link to="/user/plant-care" class="nav-item">
            <el-icon><Management /></el-icon>
            <span>我的养护</span>
          </router-link>
          <router-link to="/user/knowledge" class="nav-item">
            <el-icon><Reading /></el-icon>
            <span>养护知识</span>
          </router-link>
          <router-link to="/user/community" class="nav-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>社区</span>
          </router-link>
          <router-link to="/user/ai-diagnosis" class="nav-item">
            <el-icon><Camera /></el-icon>
            <span>AI识别</span>
          </router-link>
          <router-link to="/user/reminders" class="nav-item">
            <el-icon><Bell /></el-icon>
            <span>提醒</span>
          </router-link>
        </nav>

        <div class="user-info">
          <el-dropdown @command="handleUserCommand">
            <div class="user-avatar-box">
              <el-avatar :src="userInfo.avatarUrl || defaultAvatar" :size="40">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userInfo.name || '用户' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人主页
                </el-dropdown-item>
                <el-dropdown-item command="messages">
                  <el-icon><ChatLineRound /></el-icon>私信
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主体内容区 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 底部 -->
    <footer class="user-footer">
      <div class="footer-content">
        <p>🌱 青稞绿植养护系统 · 让每一株植物都健康成长</p>
        <p class="copyright">© 2025 Qingke Green Plant Care System. All rights reserved.</p>
      </div>
    </footer>

    <!-- 养护提醒弹窗 -->
    <CareReminderDialog 
      v-model:visible="reminderVisible" 
      :reminder-data="currentReminder"
      @close="handleReminderClose"
    />

    <!-- 公告通知栏 -->
    <NotificationBar
      v-if="showNotificationBar"
      :notifications="notifications"
      @close="showNotificationBar = false"
      @click="handleNotificationClick"
    />

    <!-- 客服浮窗 -->
    <CustomerService />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sysNoticeApi, messageCenterApi } from '@/api'
import CareReminderDialog from '@/components/common/CareReminderDialog.vue'
import NotificationBar from '@/components/common/NotificationBar.vue'
import store from '@/store'
import { Bell, ChatLineRound, User } from '@element-plus/icons-vue'
import logoImg from '@/assets/img/logo.png'
import CustomerService from '@/components/common/CustomerService.vue'
const router = useRouter()

// 用户信息（响应式跟随 store，资料更新后顶栏即时刷新）
const userInfo = computed(() => ({
  name: store.user.userInfo?.name || '',
  avatarUrl: store.user.userInfo?.avatarUrl || ''
}))

const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDgiIGhlaWdodD0iNDgiIHZpZXdCb3g9IjAgMCA0OCA0OCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48Y2lyY2xlIGN4PSIyNCIgY3k9IjI0IiByPSIyNCIgZmlsbD0iIzUyYzQxYSIvPjxwYXRoIGQ9Ik0yNCAyNkMyOC40MTgzIDI2IDMyIDIyLjQxODMgMzIgMThDMzIgMTMuNTgxNyAyOC40MTgzIDEwIDI0IDEwQzE5LjU4MTcgMTAgMTYgMTMuNTgxNyAxNiAxOEMxNiAyMi40MTgzIDE5LjU4MTcgMjYgMjQgMjZaIiBmaWxsPSJ3aGl0ZSIvPjxwYXRoIGQ9Ik0xMiAzOEMxMiAzMi40NzcyIDE2LjQ3NzIgMjggMjIgMjhIMjZDMzEuNTIyOCAyOCAzNiAzMi40NzcyIDM2IDM4VjQwSDEyVjM4WiIgZmlsbD0id2hpdGUiLz48L3N2Zz4='

// 消息中心相关
const unreadTotal = ref(0)
const messageCenterVisible = ref(false)

// 提醒相关
const reminderVisible = ref(false)
const currentReminder = ref(null)
const reminderTimer = ref(null)
const preciseTimer = ref(null)
// 使用内存 Set 记录当前会话已弹出的提醒ID，防止同一次页面会话内重复弹窗
const sessionShownIds = ref(new Set())

// 通知栏相关
const showNotificationBar = ref(false)
const notifications = ref([])


// 获取未读消息总数
const fetchUnreadCount = async () => {
  try {
    const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!currentUserInfo.id) return

    const res = await messageCenterApi.getUnreadTotal(currentUserInfo.id)
    if (res.code === 200) {
      unreadTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取未读消息数失败:', error)
  }
}

// 用户下拉菜单操作
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile': {
      const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      if (currentUserInfo.id) {
        const routeUrl = router.resolve(`/user/profile/${currentUserInfo.id}`)
        window.open(routeUrl.href, '_blank')
      }
      break
    }
    case 'messages':
      router.push('/user/messages')
      break
    case 'settings':
      router.push('/user/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = () => {
  store.logout()
  ElMessage.success('退出成功')
  router.push('/login')
}

// 检查养护提醒（拉取数据 + 精确定时 + 已到期立即弹）
const checkCareReminders = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) return

    const res = await sysNoticeApi.getPage({
      pageNum: 1,
      pageSize: 10,
      userId: userInfo.id,
      type: ['个人提醒'],
      isRead: 0
    })
    
    if (res.code === 200 && res.data && res.data.records && res.data.records.length > 0) {
      const now = new Date()
      let closestUpcoming = null
      let minDelay = Infinity
      
      for (const r of res.data.records) {
        if (!r.remindTime || sessionShownIds.value.has(r.id)) continue
        
        const remindTime = new Date(r.remindTime)
        const diff = remindTime - now
        
        // 已到期且在24小时内 → 立即弹出
        if (diff <= 0 && diff > -24 * 60 * 60 * 1000) {
          if (!reminderVisible.value) {
            currentReminder.value = r
            reminderVisible.value = true
            sessionShownIds.value.add(r.id)
          }
          return // 已有弹窗，不再调度
        }
        // 未来24小时内 → 记录最早到期的
        if (diff > 0 && diff <= 24 * 60 * 60 * 1000 && diff < minDelay) {
          minDelay = diff
          closestUpcoming = r
        }
      }
      
      // 为最近一条未来提醒设置精确定时器
      if (closestUpcoming && !reminderVisible.value) {
        schedulePreciseReminder(closestUpcoming, minDelay)
      }
    }
  } catch (error) {
    console.error('获取提醒失败:', error)
  }
}

// 精确定时器：到提醒时间那一刻准时弹出
const schedulePreciseReminder = (reminder, delay) => {
  if (preciseTimer.value) clearTimeout(preciseTimer.value)
  preciseTimer.value = setTimeout(() => {
    if (!reminderVisible.value && !sessionShownIds.value.has(reminder.id)) {
      currentReminder.value = reminder
      reminderVisible.value = true
      sessionShownIds.value.add(reminder.id)
    }
    preciseTimer.value = null
  }, delay)
}

// 检查公告通知
const checkNotifications = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) return
    
    const res = await sysNoticeApi.getPage({
      pageNum: 1,
      pageSize: 5,
      userId: userInfo.id,
      isRead: 0
    })
    
    if (res.code === 200 && res.data && res.data.records) {
      const careNotifications = res.data.records.filter(r => {
        const systemNoticeTypes = ['系统公告', '功能更新', '活动通知', '系统通知']
        return !systemNoticeTypes.includes(r.type)
      })
      
      notifications.value = careNotifications.map(item => ({
        id: item.id,
        type: item.type,
        title: item.title,
        content: item.brief || item.content
      }))
      
      if (notifications.value.length > 0) {
        showNotificationBar.value = true
      }
    }
  } catch (error) {
    console.error('获取通知失败:', error)
  }
}

// 关闭提醒弹窗（标记已读 + 立即调度下一条）
const handleReminderClose = async () => {
  if (currentReminder.value && currentReminder.value.id) {
    try {
      await sysNoticeApi.markAsRead(currentReminder.value.id)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  reminderVisible.value = false
  currentReminder.value = null
  // 关闭后立即检查是否有下一条到期提醒
  checkCareReminders()
}

// 点击通知栏
const handleNotificationClick = (item) => {
  if (item.type === '新手引导') {
    router.push('/user/newbie-guide')
    showNotificationBar.value = false
  }
}

// 组件挂载
onMounted(() => {
  checkCareReminders()
  checkNotifications()
  fetchUnreadCount()

  // 轮询兜底（每60秒），防止 setTimeout 因浏览器节流失效
  reminderTimer.value = setInterval(() => {
    checkCareReminders()
    checkNotifications()
    fetchUnreadCount()
  }, 60 * 1000)
})

// 组件卸载
onUnmounted(() => {
  if (reminderTimer.value) clearInterval(reminderTimer.value)
  if (preciseTimer.value) clearTimeout(preciseTimer.value)
})
</script>

<style scoped>
.user-home {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 50%, #fff9c4 100%);
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.user-header {
  background: linear-gradient(135deg, #66bb6a 0%, #81c784 100%);
  box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  object-fit: contain;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.logo-title {
  font-size: 24px;
  font-weight: 700;
  color: white;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-menu {
  display: flex;
  gap: 8px;
  flex: 1;
  justify-content: center;
  max-width: 600px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 20px;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  border-radius: 12px;
  transition: all 0.3s;
  font-size: 14px;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  transform: translateY(-2px);
}

.nav-item.router-link-active {
  background: rgba(255, 255, 255, 0.25);
  color: white;
  font-weight: 600;
}

.message-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
  color: white;
  margin-right: 15px;
}

.message-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.05);
}

.user-info {
  cursor: pointer;
}

.user-avatar-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 25px;
  transition: all 0.3s;
}

.user-avatar-box:hover {
  background: rgba(255, 255, 255, 0.25);
}

.username {
  color: white;
  font-weight: 500;
  font-size: 14px;
}

/* 主体内容 */
.main-content {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

/* 底部 */
.user-footer {
  background: linear-gradient(135deg, #66bb6a 0%, #81c784 100%);
  color: white;
  padding: 30px 24px;
  margin-top: 60px;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  text-align: center;
}

.footer-content p {
  margin: 8px 0;
  font-size: 14px;
}

.copyright {
  opacity: 0.8;
  font-size: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .nav-menu {
    gap: 4px;
  }
  
  .nav-item {
    padding: 8px 12px;
    font-size: 12px;
  }
  
  .logo-title {
    font-size: 18px;
  }
}
</style>
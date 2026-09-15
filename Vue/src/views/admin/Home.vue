<template>
  <el-container class="home-container admin-dark">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
<div class="header-logo"><img src="@/assets/img/logo.png" style="height:36px;width:auto;display:block;object-fit:contain;"></div>
        <div class="header-title">青稞绿植养护管理系统</div>
      </div>
      <div class="header-right">
        <div class="header-clock">
          <span class="clock-time">{{ currentTime }}</span>
          <span class="clock-date">{{ currentDate }}</span>
        </div>
        <el-dropdown class="user-dropdown" popper-class="admin-dark">
          <div class="user-info">
            <el-avatar :size="32" :src="userInfo.avatarUrl || defaultAvatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <span class="username">{{ userInfo.name || '管理员' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleGoProfile">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 左侧菜单栏 -->
      <el-aside class="sidebar" :width="sidebarExpanded ? '180px' : '64px'" @mouseenter="sidebarExpanded = true" @mouseleave="sidebarExpanded = false">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          :collapse="!sidebarExpanded"
          background-color="transparent"
          text-color="rgba(0,191,165,0.6)"
          active-text-color="#00bfa5"
          @select="handleMenuSelect"
        >
          <el-menu-item index="dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-menu-item index="users">
            <el-icon><Grid /></el-icon>
            <span>用户</span>
          </el-menu-item>

          <el-menu-item index="categories">
            <el-icon><Menu /></el-icon>
            <span>绿植种类</span>
          </el-menu-item>

          <el-menu-item index="care-tips">
            <el-icon><Reading /></el-icon>
            <span>养护知识</span>
          </el-menu-item>

          <el-menu-item index="care-records">
            <el-icon><Grid /></el-icon>
            <span>养护记录</span>
          </el-menu-item>

          <el-menu-item index="plant-exchange">
            <el-icon><Grid /></el-icon>
            <span>绿植识别</span>
          </el-menu-item>

          <el-menu-item index="community">
            <el-icon><Grid /></el-icon>
            <span>社区互动</span>
          </el-menu-item>

          <el-menu-item index="system">
            <el-icon><User /></el-icon>
            <span>系统管理</span>
          </el-menu-item>

          <el-menu-item index="profile">
            <el-icon><Notebook /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区域 -->
      <el-main class="main-content">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import store from '../../store'
import { ElMessage } from 'element-plus'
import { 
  HomeFilled, 
  Grid, 
  Menu, 
  Reading, 
  User, 
  Notebook,
  ArrowDown 
} from '@element-plus/icons-vue'
import '@/assets/css/admin-dark-theme.css'
import touxiangImg from '@/assets/img/touxiang.png'

const router = useRouter()
const route = useRoute()

// 实时时钟
const currentTime = ref('')
const currentDate = ref('')
let clockTimer = null

const updateClock = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 路径到菜单的映射
const pathToMenuMap = {
  '/admin/dashboard': 'dashboard',
  '/admin/users': 'users',
  '/admin/plant-category': 'categories',
  '/admin/plant-knowledge': 'care-tips',
  '/admin/plant-maintain-record': 'care-records',
  '/admin/plant-recognize': 'plant-exchange',
  '/admin/community-post': 'community',
  '/admin/profile': 'profile',
  '/admin/system': 'system'
}

const activeMenu = ref(pathToMenuMap[route.path] || 'dashboard')
const sidebarExpanded = ref(false)
const defaultAvatar = touxiangImg

const userInfo = reactive({
  name: '',
  touxiang: '',
  role: 'admin'
})

const loadUserInfo = () => {
  const storedUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (storedUserInfo) {
    userInfo.name = storedUserInfo.name || '管理员'
    userInfo.avatarUrl = storedUserInfo.avatarUrl || ''
    userInfo.role = storedUserInfo.role || 'admin'
  }
}

const handleMenuSelect = (index) => {
  activeMenu.value = index
  const routeMap = {
    'dashboard': '/admin/dashboard',
    'users': '/admin/users',
    'categories': '/admin/plant-category',
    'care-tips': '/admin/plant-knowledge',
    'care-records': '/admin/plant-maintain-record',
    'plant-exchange': '/admin/plant-recognize',
    'community': '/admin/community-post',
    'profile': '/admin/profile',
    'system': '/admin/system'
  }
  if (routeMap[index]) {
    router.push(routeMap[index])
  }
}

const handleGoProfile = () => {
  router.push('/admin/profile')
}

const handleLogout = () => {
  store.clearUser()
  ElMessage.success('退出登录成功')
  router.push('/admin/login')
}

watch(() => route.path, (newPath) => {
  activeMenu.value = pathToMenuMap[newPath] || 'dashboard'
})

onMounted(() => {
  loadUserInfo()
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
})
</script>

<style scoped>
.home-container {
  height: 100vh;
  width: 100%;
  background: var(--admin-bg);
}

/* ===== 顶部导航栏 ===== */
.header {
  background: linear-gradient(90deg, #06192e 0%, #0a2540 50%, #06192e 100%);
  color: var(--admin-text);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 56px !important;
  border-bottom: 2px solid var(--admin-accent);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 10;
}

.header::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--admin-accent), transparent);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}


.header-title {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--admin-text);
  text-shadow: 0 0 10px rgba(0,191,165, 0.3);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-clock {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  padding: 4px 16px;
  background: rgba(0,191,165, 0.05);
  border: 1px solid var(--admin-border);
  border-radius: 6px;
}

.clock-time {
  font-family: var(--admin-font-number);
  font-size: 18px;
  color: var(--admin-data);
  text-shadow: 0 0 8px var(--admin-data-glow);
  letter-spacing: 2px;
}

.clock-date {
  font-size: 11px;
  color: var(--admin-text-secondary);
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--admin-text);
}

.username {
  font-size: 13px;
  color: var(--admin-text-secondary);
}

/* ===== 主体容器 ===== */
.main-container {
  height: calc(100vh - 56px);
}

/* ===== 左侧菜单（透明科技风 + 自动收缩） ===== */
.sidebar {
  background: rgba(6, 25, 55, 0.4);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  overflow: hidden;
  border-right: 1px solid rgba(0,191,165, 0.1);
  position: relative;
  transition: width 0.3s ease;
}

/* 网格纹理叠加 */
.sidebar::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    repeating-linear-gradient(0deg, rgba(0,191,165,0.03) 0px, rgba(0,191,165,0.03) 1px, transparent 1px, transparent 30px),
    repeating-linear-gradient(90deg, rgba(0,191,165,0.03) 0px, rgba(0,191,165,0.03) 1px, transparent 1px, transparent 30px);
  z-index: 0;
}

.sidebar-menu {
  border-right: none !important;
  padding-top: 12px;
  position: relative;
  z-index: 1;
}

/* 收缩态：el-menu 内置 collapse 处理 */
.sidebar-menu:not(.el-menu--collapse) {
  width: 180px;
}

.sidebar-menu .el-menu-item {
  border: none;
  border-radius: 0;
  margin: 0;
  height: 48px;
  line-height: 48px;
  transition: all 0.3s;
  position: relative;
  color: rgba(0,191,165, 0.6) !important;
  letter-spacing: 1px;
  overflow: hidden;
  white-space: nowrap;
}

.sidebar-menu .el-menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: transparent;
  transition: background 0.3s;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(0,191,165, 0.06) !important;
  color: rgba(0,191,165, 0.9) !important;
  text-shadow: 0 0 8px rgba(0,191,165, 0.4);
}

.sidebar-menu .el-menu-item.is-active {
  background: rgba(0,191,165, 0.1) !important;
  color: #00bfa5 !important;
  text-shadow: 0 0 12px rgba(0,191,165, 0.6), 0 0 24px rgba(0,191,165, 0.2);
  border-top: 2px solid var(--admin-accent);
}

.sidebar-menu .el-menu-item.is-active::before {
  background: var(--admin-accent);
  box-shadow: 0 0 8px var(--admin-accent);
}

.sidebar-menu .el-menu-item .el-icon {
  color: inherit;
  margin-right: 10px;
  font-size: 18px;
}

/* 菜单项分隔线 */
.sidebar-menu .el-menu-item + .el-menu-item {
  border-top: 1px solid rgba(0,191,165, 0.05);
}

/* ===== 内容区域 ===== */
.main-content {
  background: var(--admin-bg) !important;
  padding: 20px;
  overflow-y: auto;
  background-image: 
    radial-gradient(ellipse at 50% 0%, rgba(0,191,165, 0.03) 0%, transparent 60%),
    linear-gradient(rgba(0,191,165, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,191,165, 0.02) 1px, transparent 1px);
  background-size: 100% 100%, 40px 40px, 40px 40px;
}

:deep(.el-dropdown-menu__item) {
  padding: 10px 20px;
}
</style>

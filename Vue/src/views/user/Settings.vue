<template>
  <div class="settings-page" :class="[`theme-${currentTheme}`, `font-${currentFontSize}`, `density-${currentDensity}`]">
    <div class="settings-header">
      <h2>⚙️ 设置</h2>
      <p>管理您的账号和偏好设置</p>
    </div>

    <div class="settings-container">
      <!-- 左侧菜单 -->
      <div class="settings-menu">
        <div
          v-for="item in menuList"
          :key="item.id"
          class="menu-item"
          :class="{ active: activeMenu === item.id }"
          @click="activeMenu = item.id"
        >
          <el-icon :size="20">
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.label }}</span>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="settings-content">
        <!-- 个人信息 -->
        <div v-show="activeMenu === 'profile'" class="content-section">
          <h3 class="section-title">个人信息</h3>
          
          <el-form :model="profileForm" label-width="100px" class="settings-form">
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-avatar :size="100" :src="profileForm.avatarUrl || defaultAvatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <el-upload
                  :show-file-list="false"
                  :before-upload="handleAvatarUpload"
                  accept="image/*"
                >
                  <el-button type="primary" plain size="small">更换头像</el-button>
                </el-upload>
              </div>
            </el-form-item>

            <el-form-item label="昵称">
              <el-input v-model="profileForm.name" placeholder="请输入昵称" style="width: 300px" />
            </el-form-item>

            <el-form-item label="性别">
              <el-radio-group v-model="profileForm.xingbie">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
                <el-radio label="保密">保密</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="手机号">
              <el-input v-model="profileForm.shouji" placeholder="请输入手机号" style="width: 300px" />
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input v-model="profileForm.youxiang" placeholder="请输入邮箱" style="width: 300px" />
            </el-form-item>

            <el-form-item label="个性签名">
              <el-input
                v-model="profileForm.signature"
                type="textarea"
                :rows="3"
                placeholder="写一句话介绍自己..."
                maxlength="100"
                show-word-limit
                style="width: 400px"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSaveProfile" :loading="saving">
                保存修改
              </el-button>
              <el-button @click="resetProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 账号安全 -->
        <div v-show="activeMenu === 'security'" class="content-section">
          <h3 class="section-title">账号安全</h3>
          
          <div class="security-items">
            <div class="security-item">
              <div class="security-info">
                <el-icon color="#66bb6a" :size="24"><Lock /></el-icon>
                <div>
                  <h4>登录密码</h4>
                  <p>定期更换密码可以保护账号安全</p>
                </div>
              </div>
              <el-button type="primary" plain @click="passwordDialogVisible = true">
                修改密码
              </el-button>
            </div>

            <el-divider />

            <div class="security-item">
              <div class="security-info">
                <el-icon color="#66bb6a" :size="24"><Iphone /></el-icon>
                <div>
                  <h4>手机绑定</h4>
                  <p>已绑定手机：{{ maskPhone(profileForm.shouji) }}</p>
                </div>
              </div>
              <el-button type="primary" plain @click="ElMessage.info('功能开发中')">
                更换手机
              </el-button>
            </div>

            <el-divider />

            <div class="security-item">
              <div class="security-info">
                <el-icon color="#66bb6a" :size="24"><Message /></el-icon>
                <div>
                  <h4>邮箱绑定</h4>
                  <p>{{ profileForm.youxiang ? `已绑定：${maskEmail(profileForm.youxiang)}` : '未绑定' }}</p>
                </div>
              </div>
              <el-button type="primary" plain @click="ElMessage.info('功能开发中')">
                {{ profileForm.youxiang ? '更换邮箱' : '绑定邮箱' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 消息通知 -->
        <div v-show="activeMenu === 'notification'" class="content-section">
          <h3 class="section-title">消息通知</h3>
          
          <div class="notification-settings">
            <div class="notification-item">
              <div class="notification-info">
                <h4>养护提醒通知</h4>
                <p>到达养护时间时发送提醒</p>
              </div>
              <el-switch v-model="notificationSettings.careReminder" />
            </div>

            <el-divider />

            <div class="notification-item">
              <div class="notification-info">
                <h4>社区互动通知</h4>
                <p>有人评论或点赞您的帖子时通知</p>
              </div>
              <el-switch v-model="notificationSettings.community" />
            </div>

            <el-divider />

            <div class="notification-item">
              <div class="notification-info">
                <h4>系统消息通知</h4>
                <p>系统公告和重要更新提醒</p>
              </div>
              <el-switch v-model="notificationSettings.system" />
            </div>

            <el-divider />

            <div class="notification-item">
              <div class="notification-info">
                <h4>声音提示</h4>
                <p>收到通知时播放提示音</p>
              </div>
              <el-switch v-model="notificationSettings.sound" />
            </div>

            <div class="save-btn-group">
              <el-button type="primary" @click="handleSaveNotification">
                保存设置
              </el-button>
            </div>
          </div>
        </div>

        <!-- 隐私设置 -->
        <div v-show="activeMenu === 'privacy'" class="content-section">
          <h3 class="section-title">隐私设置</h3>
          
          <div class="privacy-settings">
            <div class="privacy-item">
              <div class="privacy-info">
                <h4>个人资料可见性</h4>
                <p>控制谁可以查看您的个人资料</p>
              </div>
              <el-select v-model="privacySettings.profileVisibility" style="width: 150px">
                <el-option label="所有人" value="public" />
                <el-option label="仅好友" value="friends" />
                <el-option label="仅自己" value="private" />
              </el-select>
            </div>

            <el-divider />

            <div class="privacy-item">
              <div class="privacy-info">
                <h4>养护记录公开</h4>
                <p>是否允许其他用户查看您的养护记录</p>
              </div>
              <el-switch v-model="privacySettings.careRecordsPublic" />
            </div>

            <el-divider />

            <div class="privacy-item">
              <div class="privacy-info">
                <h4>收藏内容公开</h4>
                <p>是否允许其他用户查看您的收藏</p>
              </div>
              <el-switch v-model="privacySettings.favoritesPublic" />
            </div>

            <div class="save-btn-group">
              <el-button type="primary" @click="handleSavePrivacy">
                保存设置
              </el-button>
            </div>
          </div>
        </div>

        <!-- 其他设置 -->
        <div v-show="activeMenu === 'other'" class="content-section">
          <h3 class="section-title">其他设置</h3>
          
          <div class="other-settings">
            <div class="other-item">
              <div class="other-info">
                <el-icon color="#66bb6a" :size="24"><Delete /></el-icon>
                <div>
                  <h4>清除缓存</h4>
                  <p>清除本地缓存数据，释放存储空间</p>
                </div>
              </div>
              <el-button type="warning" plain @click="handleClearCache">
                清除缓存
              </el-button>
            </div>

            <el-divider />

            <div class="other-item">
              <div class="other-info">
                <el-icon color="#66bb6a" :size="24"><InfoFilled /></el-icon>
                <div>
                  <h4>关于我们</h4>
                  <p>青稞绿植养护系统 v1.0.0</p>
                </div>
              </div>
              <el-button type="primary" plain @click="aboutDialogVisible = true">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="500px"
      :close-on-click-modal="false"
      draggable
      class="resizable-dialog user-cartoon-dialog"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changing">
          确认修改
        </el-button>
      </template>
    </el-dialog>

    <!-- 关于我们对话框 -->
    <el-dialog v-model="aboutDialogVisible" title="关于我们" width="600px" draggable class="user-cartoon-dialog">
      <div class="about-content">
        <div class="about-logo">
          <img :src="logoImg" class="about-logo-img" alt="logo" />
          <h2>青稞绿植养护系统</h2>
          <p class="version">版本 v1.0.0</p>
        </div>

        <el-divider />

        <div class="about-info">
          <h3>📱 功能介绍</h3>
          <ul>
            <li>🌿 智能养护记录管理</li>
            <li>🤖 AI病害诊断识别</li>
            <li>📚 专业养护知识库</li>
            <li>🔔 定时养护提醒</li>
            <li>💬 社区交流互动</li>
          </ul>

          <h3>👥 团队信息</h3>
          <p>开发团队：无名氏</p>
          <p>联系邮箱：2368137782@qq.com</p>
          
          <h3>📄 版权声明</h3>
          <p>© 2025 青稞绿植养护系统. All rights reserved.</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, Setting, Bell, Lock, More,
  Iphone, Message, Delete, InfoFilled
} from '@element-plus/icons-vue'
import { userApi } from '../../api'
import md5 from 'md5'
import store from '../../store'
import touxiangImg from '@/assets/img/touxiang.png'
import logoImg from '@/assets/img/logo.png'

const defaultAvatar = touxiangImg

// 左侧菜单
const activeMenu = ref('profile')
const menuList = [
  { id: 'profile', label: '个人信息', icon: 'User' },
  { id: 'security', label: '账号安全', icon: 'Lock' },
  { id: 'notification', label: '消息通知', icon: 'Bell' },
  { id: 'privacy', label: '隐私设置', icon: 'Lock' },
  { id: 'other', label: '其他设置', icon: 'More' }
]

// 个人信息表单
const profileForm = reactive({
  id: null,
  name: '',
  touxiang: '',
  xingbie: '',
  shouji: '',
  youxiang: '',
  signature: ''
})

const saving = ref(false)

// 密码修改
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const changing = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 消息通知设置
const notificationSettings = reactive({
  careReminder: true,
  community: true,
  system: true,
  sound: false
})

// 隐私设置
const privacySettings = reactive({
  profileVisibility: 'public',
  careRecordsPublic: true,
  favoritesPublic: true
})

// 关于我们
const aboutDialogVisible = ref(false)

// 加载用户信息
const loadUserInfo = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo) {
    profileForm.id = userInfo.id
    profileForm.name = userInfo.name || ''
    profileForm.avatarUrl = userInfo.avatarUrl || ''
    profileForm.xingbie = userInfo.xingbie || '保密'
    profileForm.shouji = userInfo.shouji || ''
    profileForm.youxiang = userInfo.youxiang || ''
    profileForm.signature = userInfo.signature || ''
  }

  // 加载通知设置
  const notifSettings = localStorage.getItem('notificationSettings')
  if (notifSettings) {
    Object.assign(notificationSettings, JSON.parse(notifSettings))
  }

  // 加载隐私设置
  const privSettings = localStorage.getItem('privacySettings')
  if (privSettings) {
    Object.assign(privacySettings, JSON.parse(privSettings))
  }
}

// 头像上传
const handleAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    profileForm.avatarUrl = e.target.result
  }
  reader.readAsDataURL(file)

  return false
}

// 保存个人信息
const handleSaveProfile = async () => {
  saving.value = true
  try {
    const res = await userApi.updateUser(profileForm)
    if (res.code === 200) {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const updatedUserInfo = { ...userInfo, ...profileForm }
      localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
      // 同步响应式 store，保证顶栏等读取 store 的组件即时刷新头像/昵称
      store.setUser(updatedUserInfo)

      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 重置个人信息
const resetProfile = () => {
  loadUserInfo()
  ElMessage.info('已重置')
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    changing.value = true
    try {
      const res = await userApi.changePassword({
        id: profileForm.id,
        oldPassword: md5(passwordForm.oldPassword),
        password: md5(passwordForm.newPassword)
      })

      if (res.code === 200) {
        ElMessage.success('密码修改成功，请重新登录')
        passwordDialogVisible.value = false
        
        // 清空表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''

        // 延迟退出登录
        setTimeout(() => {
          store.logout()
          window.location.href = '/login'
        }, 1500)
      } else {
        ElMessage.error(res.message || '密码修改失败')
      }
    } catch (error) {
      console.error('密码修改失败:', error)
      ElMessage.error(error.message || '密码修改失败')
    } finally {
      changing.value = false
    }
  })
}

// 保存通知设置
const handleSaveNotification = () => {
  localStorage.setItem('notificationSettings', JSON.stringify(notificationSettings))
  ElMessage.success('通知设置已保存')
}

// 保存隐私设置
const handleSavePrivacy = async () => {
  try {
    const res = await userApi.updatePrivacySettings({
      id: profileForm.id,
      profileVisibility: privacySettings.profileVisibility,
      careRecordsPublic: privacySettings.careRecordsPublic ? 1 : 0,
      favoritesPublic: privacySettings.favoritesPublic ? 1 : 0
    })

    if (res.code === 200) {
      // 同时保存到本地存储
      localStorage.setItem('privacySettings', JSON.stringify(privacySettings))
      ElMessage.success('隐私设置已保存')
    } else {
      ElMessage.error(res.message || '隐私设置保存失败')
    }
  } catch (error) {
    console.error('隐私设置保存失败:', error)
    ElMessage.error('隐私设置保存失败')
  }
}



// 清除缓存
const handleClearCache = () => {
  ElMessageBox.confirm('确定要清除所有缓存数据吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 保留登录信息
    const preserveKeys = ['userInfo', 'token', 'user_token', 'admin_token', 'isLoggedIn', 'loginTime']
    const preserved = {}
    for (const key of preserveKeys) {
      const val = localStorage.getItem(key)
      if (val) preserved[key] = val
    }

    localStorage.clear()

    for (const [key, val] of Object.entries(preserved)) {
      localStorage.setItem(key, val)
    }

    ElMessage.success('缓存清除成功')

    // 重新加载设置
    loadUserInfo()
  }).catch(() => {
    ElMessage.info('已取消')
  })
}

// 手机号脱敏
const maskPhone = (phone) => {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 邮箱脱敏
const maskEmail = (email) => {
  if (!email) return ''
  const [name, domain] = email.split('@')
  if (name.length <= 2) return email
  return name.substring(0, 2) + '***@' + domain
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
/* 主题色变量 */
.theme-green {
  --theme-color: #66bb6a;
  --theme-color-light: #81c784;
  --theme-color-dark: #4caf50;
  --theme-gradient: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
}

.theme-blue {
  --theme-color: #42a5f5;
  --theme-color-light: #64b5f6;
  --theme-color-dark: #1e88e5;
  --theme-gradient: linear-gradient(135deg, #42a5f5 0%, #1e88e5 100%);
}

.theme-purple {
  --theme-color: #ab47bc;
  --theme-color-light: #ba68c8;
  --theme-color-dark: #8e24aa;
  --theme-gradient: linear-gradient(135deg, #ab47bc 0%, #8e24aa 100%);
}

.theme-orange {
  --theme-color: #ffa726;
  --theme-color-light: #ffb74d;
  --theme-color-dark: #fb8c00;
  --theme-gradient: linear-gradient(135deg, #ffa726 0%, #fb8c00 100%);
}

.theme-red {
  --theme-color: #ef5350;
  --theme-color-light: #e57373;
  --theme-color-dark: #e53935;
  --theme-gradient: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
}

/* 字体大小 */
.font-small {
  --base-font-size: 13px;
  --title-font-size: 18px;
  --section-title-size: 16px;
}

.font-medium {
  --base-font-size: 14px;
  --title-font-size: 20px;
  --section-title-size: 18px;
}

.font-large {
  --base-font-size: 16px;
  --title-font-size: 24px;
  --section-title-size: 20px;
}

/* 列表密度 */
.density-compact {
  --item-padding: 12px;
  --item-gap: 8px;
}

.density-default {
  --item-padding: 16px;
  --item-gap: 12px;
}

.density-comfortable {
  --item-padding: 20px;
  --item-gap: 16px;
}

.settings-page {
  min-height: calc(100vh - 120px);
  padding: 30px 20px;
  font-size: var(--base-font-size, 14px);
}

.settings-header h2 {
  font-size: var(--title-font-size, 20px);
  color: #2e7d32;
  margin: 0 0 10px 0;
}

.settings-header p {
  font-size: 16px;
  color: #666;
}

.settings-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 24px;
}

/* 左侧菜单 */
.settings-menu {
  background: white;
  border-radius: 16px;
  padding: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  height: fit-content;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  font-size: 15px;
}

.menu-item:hover {
  background: rgba(var(--theme-color), 0.1);
  color: var(--theme-color);
}

.menu-item.active {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  color: white !important;
}

.menu-item.active span {
  color: white !important;
}

/* 右侧内容区 */
.settings-content {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  min-height: 600px;
}

.content-section {
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-title {
  font-size: var(--section-title-size, 18px);
  color: #333;
  margin: 0 0 24px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

/* 个人信息 */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.settings-form {
  max-width: 600px;
}

/* 账号安全 */
.security-items,
.notification-settings,
.privacy-settings,
.appearance-settings,
.other-settings {
  max-width: 700px;
}

.security-item,
.other-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.security-info,
.other-info {
  display: flex;
  gap: 16px;
  align-items: center;
  flex: 1;
}

.security-info h4,
.other-info h4,
.notification-info h4,
.privacy-info h4,
.appearance-info h4 {
  font-size: 16px;
  margin: 0 0 4px 0;
  color: #333;
}

.security-info p,
.other-info p,
.notification-info p,
.privacy-info p,
.appearance-info p {
  font-size: 13px;
  color: #999;
  margin: 0;
}

/* 消息通知 */
.notification-item,
.privacy-item,
.appearance-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}

.save-btn-group {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px dashed #e0e0e0;
}

/* 主题颜色选择 */
.color-picker-group {
  display: flex;
  gap: 16px;
}

.color-item {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  border: 3px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.color-item:hover {
  transform: scale(1.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

.color-item.active {
  border-color: #fff;
  box-shadow: 0 0 0 3px var(--theme-color), 0 4px 12px rgba(0, 0, 0, 0.25);
  transform: scale(1.1);
}

/* 预览区域 */
.preview-section {
  margin-top: 32px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px dashed #e0e0e0;
}

.preview-section h4 {
  font-size: 16px;
  color: #333;
  margin: 0 0 16px 0;
}

.preview-container {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.preview-list {
  flex: 1;
  min-width: 200px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.preview-item {
  padding: var(--item-padding, 16px);
  border-bottom: 1px solid #f0f0f0;
  font-size: var(--base-font-size, 14px);
  transition: all 0.3s;
}

.preview-item:last-child {
  border-bottom: none;
}

.preview-item:hover {
  background: #f5f5f5;
  color: var(--theme-color);
}

/* 响应式字体大小 */
.settings-form :deep(.el-form-item__label),
.security-info h4,
.notification-info h4,
.privacy-info h4,
.appearance-info h4,
.other-info h4 {
  font-size: var(--base-font-size, 14px);
}

.security-info p,
.notification-info p,
.privacy-info p,
.appearance-info p,
.other-info p {
  font-size: calc(var(--base-font-size, 14px) - 1px);
}

/* Element Plus 组件主题色覆盖 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%) !important;
  border: none !important;
  color: white !important;
}

:deep(.el-button--primary.is-plain) {
  background: white !important;
  border: 1px solid #66bb6a !important;
  color: #66bb6a !important;
}

:deep(.el-button--primary.is-plain:hover) {
  background: #f0f9f0 !important;
  color: #66bb6a !important;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #66bb6a !important;
}

/* 列表密度应用 */
.security-item,
.notification-item,
.privacy-item,
.appearance-item,
.other-item {
  padding: var(--item-padding, 16px) 0;
}

.notification-settings,
.privacy-settings,
.appearance-settings,
.other-settings {
  gap: var(--item-gap, 12px);
}

@media (max-width: 768px) {
  .settings-container {
    grid-template-columns: 1fr;
  }

  .settings-menu {
    display: flex;
    overflow-x: auto;
  }

  .menu-item span {
    white-space: nowrap;
  }

  .color-picker-group {
    gap: 12px;
  }

  .color-item {
    width: 50px;
    height: 50px;
  }

  .preview-container {
    flex-direction: column;
    align-items: stretch;
  }
}

.about-logo {
  text-align: center;
  padding: 20px 0;
}

.about-logo-img {
  width: 80px;
  height: 80px;
  object-fit: contain;
  margin-bottom: 12px;
}
</style>
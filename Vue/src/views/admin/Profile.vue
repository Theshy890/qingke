<template>
  <div class="admin-profile-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="•" class="breadcrumb">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>个人中心</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="profile-layout">
      <!-- ===== 左侧：头像 & 概览卡 ===== -->
      <div class="side-card hud-panel">
        <div class="panel-hud-tl"></div>
        <div class="panel-hud-br"></div>

        <!-- 头像 -->
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          :disabled="!isEditing"
          :before-upload="beforeAvatarUpload"
          action="#"
          :auto-upload="false"
          @change="handleAvatarChange"
        >
          <div class="avatar-ring" :class="{ editable: isEditing }">
            <el-avatar :size="120" :src="formData.avatarUrl || defaultAvatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <div v-if="isEditing" class="avatar-overlay">
              <el-icon><Camera /></el-icon>
              <span>更换</span>
            </div>
          </div>
        </el-upload>

        <div class="side-name">{{ formData.name || formData.zh || '管理员' }}</div>
        <div class="side-role">
          <el-icon><Star /></el-icon> 系统管理员
        </div>

        <!-- 概览统计 -->
        <div class="side-stats">
          <div class="stat-cell">
            <span class="stat-dot" :class="userInfo.status === 0 ? 'ok' : 'bad'"></span>
            <div class="stat-meta">
              <div class="stat-label">账户状态</div>
              <div class="stat-value">{{ userInfo.status === 0 ? '正常' : '已禁用' }}</div>
            </div>
          </div>
          <div class="stat-cell">
            <el-icon class="stat-ico"><Clock /></el-icon>
            <div class="stat-meta">
              <div class="stat-label">在管天数</div>
              <div class="stat-value">{{ joinDays }} 天</div>
            </div>
          </div>
          <div class="stat-cell">
            <el-icon class="stat-ico"><Calendar /></el-icon>
            <div class="stat-meta">
              <div class="stat-label">上次登录</div>
              <div class="stat-value small">{{ formatDateTime(userInfo.lastLoginTime || userInfo.registerTime) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== 右侧：信息 Tab ===== -->
      <div class="main-card hud-panel">
        <div class="panel-hud-tl"></div>
        <div class="panel-hud-br"></div>

        <el-tabs v-model="activeTab" class="profile-tabs">
          <!-- Tab 1：基本资料 -->
          <el-tab-pane name="basic">
            <template #label>
              <span class="tab-label"><el-icon><Postcard /></el-icon> 基本资料</span>
            </template>

            <div class="tab-toolbar">
              <span class="section-hint">维护你的个人身份信息</span>
              <el-button v-if="!isEditing" type="primary" :icon="Edit" @click="handleEdit">编辑资料</el-button>
              <div v-else class="edit-actions">
                <el-button class="transparent-btn" @click="handleCancelEdit">取消</el-button>
                <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
              </div>
            </div>

            <el-form
              ref="formRef"
              :model="formData"
              :rules="formRules"
              label-width="100px"
              class="profile-form"
            >
              <el-row :gutter="20">
                <el-col :xs="24" :sm="12">
                  <el-form-item label="管理员账号">
                    <el-input v-model="formData.zh" disabled>
                      <template #prefix><el-icon><User /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :sm="12">
                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="formData.email" :disabled="!isEditing" placeholder="请输入邮箱">
                      <template #prefix><el-icon><Message /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <!-- Tab 2：修改密码 -->
          <el-tab-pane name="password">
            <template #label>
              <span class="tab-label"><el-icon><Lock /></el-icon> 修改密码</span>
            </template>

            <el-alert
              title="修改密码后需要重新登录"
              type="warning"
              :closable="false"
              show-icon
              class="pwd-alert"
            />

            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              class="profile-form pwd-form"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password>
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" placeholder="6-20位，建议含字母和数字" show-password>
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password>
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="passwordChanging" @click="handleChangePassword">
                  <el-icon><CircleCheck /></el-icon> 修改密码
                </el-button>
                <el-button class="transparent-btn" @click="resetPasswordForm">
                  <el-icon><RefreshLeft /></el-icon> 重置
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, UserFilled, Edit, Camera, Message, Calendar,
  Lock, Star, CircleCheck, RefreshLeft,
  Clock, Postcard
} from '@element-plus/icons-vue'
import { userApi } from '../../api'
import store from '../../store'
import md5 from 'md5'

const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDgiIGhlaWdodD0iNDgiIHZpZXdCb3g9IjAgMCA0OCA0OCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48Y2lyY2xlIGN4PSIyNCIgY3k9IjI0IiByPSIyNCIgZmlsbD0iIzAwYmZhNSIvPjxwYXRoIGQ9Ik0yNCAyNkMyOC40MTgzIDI2IDMyIDIyLjQxODMgMzIgMThDMzIgMTMuNTgxNyAyOC40MTgzIDEwIDI0IDEwQzE5LjU4MTcgMTAgMTYgMTMuNTgxNyAxNiAxOEMxNiAyMi40MTgzIDE5LjU4MTcgMjYgMjQgMjZaIiBmaWxsPSJ3aGl0ZSIvPjxwYXRoIGQ9Ik0xMiAzOEMxMiAzMi40NzcyIDE2LjQ3NzIgMjggMjIgMjhIMjZDMzEuNTIyOCAyOCAzNiAzMi40NzcyIDM2IDM4VjQwSDEyVjM4WiIgZmlsbD0id2hpdGUiLz48L3N2Zz4='

const activeTab = ref('basic')
const isEditing = ref(false)
const saving = ref(false)
const passwordChanging = ref(false)
const formRef = ref(null)
const passwordFormRef = ref(null)

const userInfo = reactive({
  id: null, zh: '', email: '',
  avatarUrl: '', registerTime: null, lastLoginTime: null, status: 0,
  role: 'admin'
})

const formData = reactive({
  id: null, zh: '', email: '',
  avatarUrl: ''
})

const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const formRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

// 在管天数
const joinDays = computed(() => {
  if (!userInfo.registerTime) return 0
  const diff = Date.now() - new Date(userInfo.registerTime).getTime()
  return Math.max(0, Math.floor(diff / 86400000))
})

const syncFormFromUser = () => {
  Object.assign(formData, {
    id: userInfo.id,
    zh: userInfo.zh,
    email: userInfo.email,
    avatarUrl: userInfo.avatarUrl || userInfo.touxiang || '' // 兼容touxiang字段
  })
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const storedUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!storedUserInfo.id) {
      ElMessage.warning('请先登录')
      return
    }
    const res = await userApi.getUserInfo(storedUserInfo.id)
    if (res.code === 200 && res.data) {
      Object.assign(userInfo, res.data)
      syncFormFromUser()
    } else {
      ElMessage.error(res.msg || '获取用户信息失败')
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

const handleEdit = () => { isEditing.value = true }

const handleCancelEdit = () => {
  isEditing.value = false
  syncFormFromUser()
}

const handleSave = async () => {
  try {
    if (formRef.value) await formRef.value.validate()
    saving.value = true
    const res = await userApi.updateUser(formData)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      isEditing.value = false
      Object.assign(userInfo, formData)
      const storedUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      storedUserInfo.email = formData.email
      storedUserInfo.avatarUrl = formData.avatarUrl
      localStorage.setItem('userInfo', JSON.stringify(storedUserInfo))
      store.setUser(storedUserInfo)
      await loadUserInfo()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('保存失败:', error)
      ElMessage.error('保存失败，请检查输入信息')
    }
  } finally {
    saving.value = false
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.error('只能上传图片文件!'); return false }
  if (!isLt2M) { ElMessage.error('图片大小不能超过 2MB!'); return false }
  return true
}

const handleAvatarChange = (uploadFile) => {
  const reader = new FileReader()
  reader.onload = (e) => { formData.avatarUrl = e.target.result }
  reader.readAsDataURL(uploadFile.raw)
}

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    await ElMessageBox.confirm('修改密码后需要重新登录，确定要继续吗？', '安全提示', {
      confirmButtonText: '确定修改', cancelButtonText: '取消', type: 'warning'
    })
    passwordChanging.value = true
    const res = await userApi.changePassword({
      id: userInfo.id,
      oldPassword: md5(passwordForm.oldPassword),
      password: md5(passwordForm.newPassword)
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，3秒后将自动退出登录')
      resetPasswordForm()
      setTimeout(() => {
        store.logout()
        window.location.href = '/admin/login'
      }, 3000)
    } else {
      ElMessage.error(res.message || '密码修改失败，请检查当前密码是否正确')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== false) {
      console.error('修改密码失败:', error)
      ElMessage.error(error.message || '修改密码失败')
    }
  } finally {
    passwordChanging.value = false
  }
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const formatDateTime = (date) => {
  if (!date) return '未知'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => { loadUserInfo() })
</script>

<style scoped>
.admin-profile-page { width: 100%; }

/* 面包屑 —— 沿用主题绿渐变 */
.breadcrumb {
  background: linear-gradient(135deg, #5a8f7b 0%, #4a7765 100%);
  padding: 15px 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}
:deep(.breadcrumb .el-breadcrumb__inner),
:deep(.breadcrumb .el-breadcrumb__separator) { color: #fff; font-weight: normal; }

/* 布局 */
.profile-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* HUD 面板通用 */
.hud-panel {
  position: relative;
  background: linear-gradient(160deg, rgba(6,25,55,0.55) 0%, rgba(10,30,65,0.45) 100%);
  border: 1px solid rgba(0,191,165,0.18);
  border-radius: 8px;
  padding: 24px;
}
.panel-hud-tl, .panel-hud-br { position: absolute; width: 18px; height: 18px; pointer-events: none; }
.panel-hud-tl { top: -1px; left: -1px; border-top: 2px solid #61ada3; border-left: 2px solid #00bfa5; border-top-left-radius: 8px; }
.panel-hud-br { bottom: -1px; right: -1px; border-bottom: 2px solid #00bfa5; border-right: 2px solid #00bfa5; border-bottom-right-radius: 8px; }

/* ===== 左侧概览卡 ===== */
.side-card {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-uploader { cursor: pointer; }
.avatar-ring {
  position: relative;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(0,191,165,0.3), rgba(255,106,0,0.25));
  box-shadow: 0 0 18px rgba(0,191,165,0.35);
  transition: all 0.3s;
}
.avatar-ring.editable:hover { box-shadow: 0 0 26px rgba(0,191,165,0.6); }
.avatar-ring :deep(.el-avatar) {
  border: 3px solid rgba(10,25,41,0.9);
  background: #0d2436;
}
.avatar-overlay {
  position: absolute;
  inset: 5px;
  border-radius: 50%;
  background: rgba(0,0,0,0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-ring:hover .avatar-overlay { opacity: 1; }
.avatar-overlay .el-icon { font-size: 26px; }

.side-name {
  margin-top: 16px;
  font-size: 18px;
  font-weight: 700;
  color: var(--admin-text, #e0e6ed);
  letter-spacing: 1px;
}
.side-role {
  margin-top: 6px;
  font-size: 12px;
  color: var(--admin-accent, #ff6a00);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 12px;
  border: 1px solid rgba(255,106,0,0.4);
  border-radius: 12px;
  background: rgba(255,106,0,0.08);
}

.side-stats {
  width: 100%;
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.stat-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(0,191,165,0.05);
  border: 1px solid rgba(0,191,165,0.12);
  border-radius: 6px;
}
.stat-ico { font-size: 20px; color: var(--admin-data, #00bfa5); }
.stat-dot { width: 10px; height: 10px; border-radius: 50%; margin: 0 5px; }
.stat-dot.ok { background: #00e676; box-shadow: 0 0 8px #00e676; }
.stat-dot.bad { background: #ff5252; box-shadow: 0 0 8px #ff5252; }
.stat-meta { flex: 1; min-width: 0; }
.stat-label { font-size: 11px; color: var(--admin-text-secondary, #7b9a8f); }
.stat-value { font-size: 14px; font-weight: 600; color: var(--admin-text, #e0e6ed); }
.stat-value.small { font-size: 12px; font-weight: 500; }

/* ===== 右侧主卡 ===== */
.main-card { flex: 1; min-width: 0; }

.profile-tabs :deep(.el-tabs__item) { color: var(--admin-text-secondary, #7b9a8f); }
.profile-tabs :deep(.el-tabs__item.is-active) { color: var(--admin-data, #00bfa5); }
.profile-tabs :deep(.el-tabs__active-bar) { background-color: var(--admin-data, #00bfa5); }
.profile-tabs :deep(.el-tabs__nav-wrap::after) { background-color: rgba(0,191,165,0.12); }
.tab-label { display: inline-flex; align-items: center; gap: 6px; }

.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 4px 0 20px;
}
.section-hint { font-size: 12px; color: var(--admin-text-secondary, #7b9a8f); }
.edit-actions { display: flex; gap: 10px; }

.profile-form { max-width: 720px; }
.profile-form :deep(.el-form-item) { margin-bottom: 20px; }

.pwd-alert { margin-bottom: 20px; max-width: 720px; }
.pwd-form { max-width: 520px; }

/* 响应式 */
@media (max-width: 900px) {
  .profile-layout { flex-direction: column; }
  .side-card { width: 100%; flex-direction: row; flex-wrap: wrap; justify-content: flex-start; gap: 16px; }
  .side-stats { margin-top: 0; flex: 1; min-width: 220px; }
}
</style>

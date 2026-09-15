<template>
  <div class="admin-login-container">
    <div class="admin-login-card">
      <div class="admin-header">
        <div class="admin-logo">🔐</div>
        <h1>管理后台</h1>
        <p>Administration Console</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="admin-form"
      >
        <el-form-item prop="zh">
          <el-input
            v-model="loginForm.zh"
            placeholder="管理员账号"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="管理员密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登录管理后台
        </el-button>
      </el-form>

      <div class="admin-footer">
        <span>青稞绿植养护系统 · 管理端</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import store from '../../store'

const router = useRouter()

const loginForm = reactive({
  zh: '',
  password: ''
})

const loading = ref(false)
const loginFormRef = ref(null)

const loginRules = {
  zh: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true

    const response = await fetch('/api/sys-user/admin-login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        zh: loginForm.zh,
        password: loginForm.password
      })
    })

    const result = await response.json()

    if (result.code === 200) {
      const { token, user } = result.data

      // 后端已做role校验，前端再次确认
      if (user.role !== 'admin') {
        ElMessage.error('该账号无管理员权限，请使用管理员账号登录')
        loading.value = false
        return
      }

      // 保存登录信息（使用 admin token key）
      localStorage.setItem('admin_token', token)
      store.setUser(user)

      ElMessage.success('登录成功')
      router.push('/admin/dashboard')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('管理员登录异常:', error)
    ElMessage.error('登录失败：' + error.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const route = router.currentRoute.value
  if (route.query.expired === 'true') {
    ElMessage.warning('登录已过期，请重新登录')
  }
})
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a237e 0%, #283593 50%, #1565c0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.admin-login-container::before {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 50%;
  top: -100px;
  right: -100px;
}

.admin-login-container::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 50%;
  bottom: -80px;
  left: -80px;
}

.admin-login-card {
  width: 420px;
  max-width: 90%;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.admin-header {
  text-align: center;
  margin-bottom: 36px;
}

.admin-logo {
  font-size: 48px;
  margin-bottom: 12px;
}

.admin-header h1 {
  font-size: 26px;
  color: #1a237e;
  margin: 0 0 6px 0;
  font-weight: 700;
}

.admin-header p {
  font-size: 13px;
  color: #999;
  margin: 0;
  letter-spacing: 1px;
}

.admin-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d0d5dd;
}

.admin-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #1a237e;
}

.admin-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1a237e;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #1a237e 0%, #1565c0 100%);
  border: none;
  margin-top: 8px;
}

.login-btn:hover {
  opacity: 0.9;
}

.admin-footer {
  text-align: center;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.admin-footer span {
  font-size: 12px;
  color: #bbb;
}
</style>

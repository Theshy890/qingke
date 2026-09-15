<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="leaf leaf-1">🍃</div>
      <div class="leaf leaf-2">🌿</div>
      <div class="leaf leaf-3">🌱</div>
      <div class="leaf leaf-4">🍀</div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 左侧 - 欢迎区域 -->
      <div class="login-welcome">
        <div class="welcome-content">
          <div class="logo-area">
            <img :src="logoImg" class="logo-icon" alt="logo" />
            <h1 class="logo-title">青稞绿植养护系统</h1>
          </div>
          <p class="welcome-text">让每一株植物都健康成长</p>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon color="#52c41a"><Check /></el-icon>
              <span>AI智能识别植物病害</span>
            </div>
            <div class="feature-item">
              <el-icon color="#52c41a"><Check /></el-icon>
              <span>科学养护记录管理</span>
            </div>
            <div class="feature-item">
              <el-icon color="#52c41a"><Check /></el-icon>
              <span>定时提醒贴心服务</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧 - 登录表单 -->
      <div class="login-form-area">
        <div class="form-header">
          <h2>欢迎登录</h2>
          <p>Welcome Back</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
        >
          <el-form-item prop="zh">
            <el-input
              v-model="loginForm.zh"
              placeholder="请输入账号"
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
              placeholder="请输入密码"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <div class="login-options">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false" @click="showForgetPassword = true">
              忘记密码？
            </el-link>
          </div>

          <el-button 
            type="success" 
            size="large"
            class="login-btn" 
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>

          <div class="register-link">
            还没有账号？
            <router-link to="/register">立即注册</router-link>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog
      v-model="showForgetPassword"
      title="找回密码"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="forgetFormRef"
        :model="forgetForm"
        :rules="forgetRules"
        label-width="100px"
      >
        <el-form-item label="账号" prop="zh">
          <el-input v-model="forgetForm.zh" placeholder="请输入账号" />
        </el-form-item>

        <el-form-item label="密保问题" prop="securityQuestion">
          <el-select v-model="forgetForm.securityQuestion" placeholder="请选择密保问题" style="width: 100%">
            <el-option label="您的出生地是？" value="您的出生地是？" />
            <el-option label="您的母亲姓名是？" value="您的母亲姓名是？" />
            <el-option label="您的小学名称是？" value="您的小学名称是？" />
            <el-option label="您最喜欢的颜色是？" value="您最喜欢的颜色是？" />
          </el-select>
        </el-form-item>

        <el-form-item label="密保答案" prop="securityAnswer">
          <el-input v-model="forgetForm.securityAnswer" placeholder="请输入密保答案" />
        </el-form-item>

        <el-form-item label="新密码" prop="password">
          <el-input 
            v-model="forgetForm.password" 
            type="password" 
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="forgetForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showForgetPassword = false">取消</el-button>
        <el-button @click="resetForgetForm">重置</el-button>
        <el-button type="primary" :loading="forgetLoading" @click="handleForgetPassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import store from '../store'
import logoImg from '../assets/img/logo.png'

const router = useRouter()

// 登录表单
const loginForm = reactive({
  zh: '',
  password: '',
  rememberMe: false
})

const loading = ref(false)
const loginFormRef = ref(null)

const loginRules = {
  zh: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

// 忘记密码
const showForgetPassword = ref(false)
const forgetLoading = ref(false)
const forgetFormRef = ref(null)

const forgetForm = reactive({
  zh: '',
  securityQuestion: '',
  securityAnswer: '',
  password: '',
  confirmPassword: ''
})

const forgetRules = {
  zh: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  securityQuestion: [{ required: true, message: '请选择密保问题', trigger: 'change' }],
  securityAnswer: [{ required: true, message: '请输入密保答案', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgetForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const resetForgetForm = () => {
  forgetFormRef.value?.resetFields()
}

// 处理登录
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true

    // 调用后端APP用户登录API（强制role=user）
    const response = await fetch('/api/sys-user/app-login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        zh: loginForm.zh,
        password: loginForm.password
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const result = await response.json()

    if (result.code === 200) {
      // 保存token和用户信息
      const { token, user } = result.data

      // 后端已做role校验，前端再次确认
      if (user.role === 'admin') {
        ElMessage.error('管理员请使用专用管理入口登录')
        loading.value = false
        return
      }

      // 保存登录信息
      localStorage.setItem('user_token', token)
      store.setUser(user)

      if (loginForm.rememberMe) {
        localStorage.setItem('rememberedAccount', loginForm.zh)
      }

      ElMessage.success('登录成功')
      router.push('/user/index')
    } else {
      console.error('登录失败：', result.message)
      ElMessage.error(result.message || '登录失败，请检查账号密码')
    }

  } catch (error) {
    console.error('登录异常:', error)
    ElMessage.error('登录失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 处理忘记密码
const handleForgetPassword = async () => {
  try {
    await forgetFormRef.value.validate()
    
    // 验证两次密码是否一致
    if (forgetForm.password !== forgetForm.confirmPassword) {
      ElMessage.error('两次输入的密码不一致')
      return
    }
    
    forgetLoading.value = true

    const response = await fetch('/api/sys-user/reset-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        zh: forgetForm.zh,
        securityQuestion: forgetForm.securityQuestion,
        securityAnswer: forgetForm.securityAnswer,
        password: forgetForm.password
      })
    })

    const result = await response.json()

    if (result.code === 200) {
      ElMessage.success('密码重置成功，请重新登录')
      showForgetPassword.value = false
      
      // 清空表单
      Object.keys(forgetForm).forEach(key => {
        forgetForm[key] = ''
      })
    } else {
      ElMessage.error(result.message || '密码重置失败')
    }

  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('操作失败：' + error.message)
  } finally {
    forgetLoading.value = false
  }
}

// 初始化
onMounted(() => {
  // 检查是否是 Token 过期跳转过来的
  const route = router.currentRoute.value
  if (route.query.expired === 'true') {
    ElMessage.warning('登录已过期，请重新登录')
  }
  
  const rememberedAccount = localStorage.getItem('rememberedAccount')
  if (rememberedAccount) {
    loginForm.zh = rememberedAccount
    loginForm.rememberMe = true
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.leaf {
  position: absolute;
  font-size: 40px;
  opacity: 0.3;
  animation: float 6s infinite;
}

.leaf-1 {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.leaf-2 {
  top: 60%;
  left: 80%;
  animation-delay: 2.5s;
}

.leaf-3 {
  top: 30%;
  right: 15%;
  animation-delay: 5s;
}

.leaf-4 {
  bottom: 20%;
  left: 70%;
  animation-delay: 7.5s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(10deg);
  }
}

/* 登录卡片 */
.login-card {
  width: 900px;
  height: 550px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: flex;
  overflow: hidden;
  position: relative;
  z-index: 1;
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左侧欢迎区 */
.login-welcome {
  flex: 1;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  padding: 60px 40px;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo-area {
  text-align: center;
  margin-bottom: 30px;
}

.logo-icon {
  width: 72px;
  height: 72px;
  object-fit: contain;
  margin-bottom: 16px;
  animation: bounce 2s infinite;
}

.logo-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.welcome-text {
  text-align: center;
  font-size: 16px;
  margin-bottom: 40px;
  opacity: 0.95;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
}

/* 右侧表单区 */
.login-form-area {
  flex: 1;
  padding: 50px 50px;
  display: flex;
  flex-direction: column;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-header h2 {
  font-size: 28px;
  color: #333;
  margin: 0 0 8px 0;
}

.form-header p {
  color: #999;
  font-size: 14px;
  margin: 0;
}

/* 表单 */
.login-form {
  flex: 1;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  margin-bottom: 16px;
}

.register-link {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.register-link a {
  color: #52c41a;
  text-decoration: none;
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
}

/* 响应式 */
@media (max-width: 960px) {
  .login-card {
    width: 90%;
    flex-direction: column;
    height: auto;
  }

  .login-welcome {
    padding: 40px 30px;
  }

  .login-form-area {
    padding: 40px 30px;
  }
}
</style>
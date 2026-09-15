<template>
  <div class="register-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="plant plant-1">🌱</div>
      <div class="plant plant-2">🌿</div>
      <div class="plant plant-3">🍀</div>
    </div>

    <!-- 注册卡片 -->
    <div class="register-card">
      <div class="card-header">
        <router-link to="/login" class="back-link">
          <el-icon><ArrowLeft /></el-icon>
          返回登录
        </router-link>
        <h2>🌿 欢迎加入青稞绿植养护</h2>
        <p>注册账号，开启您的植物养护之旅</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-position="top"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="账号" prop="zh">
              <el-input
                v-model="registerForm.zh"
                placeholder="请输入账号"
                size="large"
                clearable
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input
                v-model="registerForm.name"
                placeholder="请输入真实姓名"
                size="large"
                clearable
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="6-20位字符"
                size="large"
                show-password
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                size="large"
                show-password
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                size="large"
                clearable
              >
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="registerForm.gender" placeholder="请选择性别" size="large" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密保问题" prop="securityQuestion">
          <el-select v-model="registerForm.securityQuestion" placeholder="用于找回密码" size="large" style="width: 100%">
            <el-option label="您的出生地是？" value="您的出生地是？" />
            <el-option label="您的母亲姓名是？" value="您的母亲姓名是？" />
            <el-option label="您的小学名称是？" value="您的小学名称是？" />
            <el-option label="您最喜欢的颜色是？" value="您最喜欢的颜色是？" />
          </el-select>
        </el-form-item>

        <el-form-item label="密保答案" prop="securityAnswer">
          <el-input
            v-model="registerForm.securityAnswer"
            placeholder="请输入密保答案"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意
            <el-link type="primary" :underline="false">《用户协议》</el-link>
            和
            <el-link type="primary" :underline="false">《隐私政策》</el-link>
          </el-checkbox>
        </el-form-item>

        <el-button 
          type="success" 
          size="large"
          class="register-btn" 
          :loading="loading"
          @click="handleRegister"
        >
          立即注册
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

const registerForm = reactive({
  zh: '',
  name: '',
  password: '',
  confirmPassword: '',
  phone: '',
  gender: '',
  email: '',
  securityQuestion: '',
  securityAnswer: '',
  agreement: false
})

const loading = ref(false)
const registerFormRef = ref(null)

const registerRules = {
  zh: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度3-20位', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  securityQuestion: [
    { required: true, message: '请选择密保问题', trigger: 'change' }
  ],
  securityAnswer: [
    { required: true, message: '请输入密保答案', trigger: 'blur' }
  ]
}

// 处理注册
const handleRegister = async () => {
  if (!registerForm.agreement) {
    ElMessage.warning('请先阅读并同意用户协议和隐私政策')
    return
  }

  try {
    await registerFormRef.value.validate()
    loading.value = true

    // 调用后端注册API
    const response = await fetch('/api/sys-user/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(registerForm)
    })

    const result = await response.json()

    if (result.code === 200) {
      ElMessage.success('注册成功，即将跳转到登录页')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(result.msg || '注册失败')
    }

    loading.value = false

  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error('注册失败，请检查网络连接')
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.plant {
  position: absolute;
  font-size: 50px;
  opacity: 0.2;
  animation: sway 15s infinite;
}

.plant-1 {
  top: 15%;
  left: 10%;
  animation-delay: 0s;
}

.plant-2 {
  top: 70%;
  right: 15%;
  animation-delay: 5s;
}

.plant-3 {
  bottom: 10%;
  left: 60%;
  animation-delay: 10s;
}

@keyframes sway {
  0%, 100% {
    transform: rotate(-5deg);
  }
  50% {
    transform: rotate(5deg);
  }
}

.register-card {
  width: 700px;
  max-width: 100%;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  padding: 40px 50px;
  position: relative;
  z-index: 1;
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
}

.back-link {
  position: absolute;
  left: 0;
  top: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #52c41a;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.back-link:hover {
  color: #389e0d;
}

.card-header h2 {
  font-size: 26px;
  color: #333;
  margin: 0 0 8px 0;
}

.card-header p {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.code-input-group {
  display: flex;
  gap: 12px;
}

.code-input-group .el-input {
  flex: 1;
}

.code-input-group .el-button {
  min-width: 120px;
}

.register-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .register-card {
    padding: 30px 25px;
  }

  .register-form :deep(.el-col) {
    width: 100% !important;
  }
}
</style>
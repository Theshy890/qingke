import axios from 'axios'
import { ElMessage } from 'element-plus'
import qs from 'qs'

// 创建 axios 实例
const service = axios.create({
  baseURL: '',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  },
  // 修复数组参数序列化：type=['a','b'] → type=a&type=b（Spring Boot 兼容）
  paramsSerializer: params => qs.stringify(params, { arrayFormat: 'repeat' })
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 发送token认证信息（支持用户端和管理端）
    const token = localStorage.getItem('user_token') || localStorage.getItem('admin_token') || localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 处理响应数据
    const res = response.data

    // 根据后端返回的状态码处理
    if (res.code !== 200) {
      ElMessage.error({
        message: res.message || '请求失败',
        duration: 3 * 1000
      })
      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      return res
    }
  },
  error => {
    console.error('响应错误:', error)

    // 401 未授权 - 跳转登录页
    if (error.response && error.response.status === 401) {
      ElMessage.error({
        message: '登录已过期，请重新登录',
        duration: 2000
      })
      // 清除token
      localStorage.removeItem('user_token')
      localStorage.removeItem('admin_token')
      localStorage.removeItem('token')
      // 跳转登录页
      setTimeout(() => {
        const currentPath = window.location.pathname
        if (currentPath.startsWith('/admin')) {
          window.location.href = '/admin/login'
        } else {
          window.location.href = '/login'
        }
      }, 500)
      return Promise.reject(new Error('未授权'))
    }

    // 处理其他网络错误
    ElMessage.error({
      message: error.message || '网络错误',
      duration: 3 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
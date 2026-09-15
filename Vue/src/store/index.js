import { reactive } from 'vue'

// Token 有效期：24小时（单位：毫秒）
const TOKEN_EXPIRE_TIME = 24 * 60 * 60 * 1000

const store = reactive({
  // 用户信息
  user: {
    isLoggedIn: false,
    userInfo: null
  },
  
  // 设置用户信息
  setUser(userInfo) {
    this.user = {
      isLoggedIn: true,
      userInfo: userInfo
    }
    // 保存到localStorage，并记录登录时间
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
    localStorage.setItem('isLoggedIn', 'true')
    localStorage.setItem('loginTime', Date.now().toString())
  },
  
  // 退出登录
  logout() {
    this.user = {
      isLoggedIn: false,
      userInfo: null
    }
    localStorage.removeItem('userInfo')
    localStorage.removeItem('isLoggedIn')
    localStorage.removeItem('loginTime')
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    localStorage.removeItem('user_token')
    localStorage.removeItem('admin_token')
  },
  
  // 清除用户信息（别名，兼容旧代码）
  clearUser() {
    this.logout()
  },
  
  // 检查 Token 是否过期
  isTokenExpired() {
    const loginTime = localStorage.getItem('loginTime')
    if (!loginTime) {
      return true
    }
    
    const elapsed = Date.now() - parseInt(loginTime)
    return elapsed > TOKEN_EXPIRE_TIME
  },
  
  // 从localStorage恢复用户信息
  restoreUser() {
    const isLoggedIn = localStorage.getItem('isLoggedIn')
    const userInfoStr = localStorage.getItem('userInfo')
    
    // 检查是否有登录信息
    if (isLoggedIn === 'true' && userInfoStr) {
      // 检查 Token 是否过期
      if (this.isTokenExpired()) {
        this.logout()
        return
      }
      
      try {
        const userInfo = JSON.parse(userInfoStr)
        this.user = {
          isLoggedIn: true,
          userInfo: userInfo
        }
        
        // 计算剩余时间
        const loginTime = parseInt(localStorage.getItem('loginTime'))
        const elapsed = Date.now() - loginTime
        
        // 设置自动过期定时器
        setTimeout(() => {
          const role = userInfo.role
          this.logout()
          // 根据角色跳转到对应登录页
          window.location.href = role === 'admin' ? '/admin/login' : '/login'
        }, TOKEN_EXPIRE_TIME - elapsed)
        
      } catch (e) {
        console.error('恢复用户信息失败:', e)
        this.logout()
      }
    } else {
      this.logout()
    }
  }
})

export default store
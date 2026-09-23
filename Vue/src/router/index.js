import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import store from '../store'

const router = createRouter({
    history: createWebHistory(),
    routes: [
      // 管理员路由
      {
        path: '/admin',
        name: 'AdminHome',
        component: () => import('../views/admin/Home.vue'),
        meta: {
          requiresAuth: true,
          title: '管理员首页',
          role: 'admin'
        },
        redirect: '/admin/dashboard',
        children: [
          {
            path: 'dashboard',
            name: 'AdminDashboard',
            component: () => import('../views/admin/Dashboard.vue'),
            meta: {
              requiresAuth: true,
              title: '系统首页',
              role: 'admin'
            }
          },
          {
            path: 'users',
            name: 'AdminUsers',
            component: () => import('../views/admin/Users.vue'),
            meta: {
              requiresAuth: true,
              title: '用户管理',
              role: 'admin'
            }
          },
          {
            path: 'plant-category',
            name: 'AdminPlantCategory',
            component: () => import('../views/admin/PlantCategory.vue'),
            meta: {
              requiresAuth: true,
              title: '绿植种类',
              role: 'admin'
            }
          },
          {
            path: 'plant-knowledge',
            name: 'AdminPlantKnowledge',
            component: () => import('../views/admin/PlantKnowledge.vue'),
            meta: {
              requiresAuth: true,
              title: '养护知识',
              role: 'admin'
            }
          },
          {
            path: 'plant-maintain-record',
            name: 'AdminPlantMaintainRecord',
            component: () => import('../views/admin/PlantMaintainRecord.vue'),
            meta: {
              requiresAuth: true,
              title: '养护记录',
              role: 'admin'
            }
          },
          {
            path: 'plant-recognize',
            name: 'AdminPlantRecognize',
            component: () => import('../views/admin/PlantRecognize.vue'),
            meta: {
              requiresAuth: true,
              title: '绿植识别',
              role: 'admin'
            }
          },
          {
            path: 'community-post',
            name: 'AdminCommunityPost',
            component: () => import('../views/admin/CommunityPost.vue'),
            meta: {
              requiresAuth: true,
              title: '社区互动',
              role: 'admin'
            }
          },
          {
            path: 'profile',
            name: 'AdminProfile',
            component: () => import('../views/admin/Profile.vue'),
            meta: {
              requiresAuth: true,
              title: '个人中心',
              role: 'admin'
            }
          },
          {
            path: 'system',
            name: 'AdminSystem',
            component: () => import('../views/admin/SystemManage.vue'),
            meta: {
              requiresAuth: true,
              title: '系统管理',
              role: 'admin'
            }
          }
        ]
      },
      // 用户路由
      {
        path: '/user',
        name: 'UserHome',
        component: () => import('../views/user/UserHome.vue'),
        meta: {
          requiresAuth: true,
          title: '用户中心',
          role: 'user'
        },
        redirect: '/user/index',
        children: [
          {
            path: 'index',
            name: 'UserIndex',
            component: () => import('../views/user/UserIndex.vue'),
            meta: {
              requiresAuth: true,
              title: '首页',
              role: 'user'
            }
          },
          {
            path: 'plant-care',
            name: 'UserPlantCare',
            component: () => import('../views/user/PlantCare.vue'),
            meta: {
              requiresAuth: true,
              title: '我的养护',
              role: 'user'
            }
          },
          {
            path: 'knowledge',
            name: 'UserKnowledge',
            component: () => import('../views/user/Knowledge.vue'),
            meta: {
              requiresAuth: true,
              title: '养护知识',
              role: 'user'
            }
          },
          {
            path: 'community',
            name: 'UserCommunity',
            component: () => import('../views/user/Community.vue'),
            meta: {
              requiresAuth: true,
              title: '社区',
              role: 'user'
            }
          },
          {
            path: 'ai-diagnosis',
            name: 'UserAiDiagnosis',
            component: () => import('../views/user/AiDiagnosis.vue'),
            meta: {
              requiresAuth: true,
              title: 'AI识别',
              role: 'user'
            }
          },
          {
            path: 'reminders',
            name: 'UserReminders',
            component: () => import('../views/user/Reminders.vue'),
            meta: {
              requiresAuth: true,
              title: '养护提醒',
              role: 'user'
            }
          },
          {
            path: 'favorites',
            name: 'UserFavorites',
            component: () => import('../views/user/Favorites.vue'),
            meta: {
              requiresAuth: true,
              title: '我的收藏',
              role: 'user'
            }
          },
          {
            path: 'messages',
            name: 'UserMessages',
            component: () => import('../views/user/Messages.vue'),
            meta: {
              requiresAuth: true,
              title: '私信',
              role: 'user'
            }
          },
          {
            path: 'settings',
            name: 'UserSettings',
            component: () => import('../views/user/Settings.vue'),
            meta: {
              requiresAuth: true,
              title: '设置',
              role: 'user'
            }
          },
          {
            path: 'newbie-guide',
            name: 'UserNewbieGuide',
            component: () => import('../views/user/NewbieGuide.vue'),
            meta: {
              requiresAuth: true,
              title: '新手引导',
              role: 'user'
            }
          }
        ]
      },
      // 用户主页（顶级路由，独立页面，不带导航栏和底部）
      {
        path: '/user/profile/:userId',
        name: 'UserProfile',
        component: () => import('../views/user/UserProfile.vue'),
        meta: {
          requiresAuth: true,
          title: '用户主页',
          role: 'user'
        }
      },
      // 根路径重定向
      {
        path: '/',
        redirect: '/login'
      },
      // 管理员登录页（独立入口）
      {
        path: '/admin/login',
        name: 'AdminLogin',
        component: () => import('../views/admin/AdminLogin.vue'),
        meta: {
          requiresAuth: false,
          title: '管理后台登录'
        }
      },
      // 用户登录页
      {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: {
          requiresAuth: false,
          title: '登录'
        }
      },
      {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: {
          requiresAuth: false,
          title: '注册'
        }
      },
      {
        path: '/notFound',
        component: () => import('../views/404.vue'),
        meta: {
          requiresAuth: false,
          title: '页面不存在'
        }
      },
      {
        path: '/:pathMatch(.*)*',
        redirect: '/notFound'
      }
    ]
})

router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 青稞绿植养护系统` : '青稞绿植养护系统'
    
    // 检查需要登录的页面
    if (to.meta.requiresAuth) {
      // 检查是否登录
      if (!store.user.isLoggedIn) {
        // 管理员路由跳转到管理员登录页，用户路由跳转到用户登录页
        const loginPath = to.path.startsWith('/admin') ? '/admin/login' : '/login'
        next({
          path: loginPath,
          query: { redirect: to.fullPath }
        })
        return
      }
      
      // 检查 Token 是否过期
      if (store.isTokenExpired()) {
        store.logout()
        const loginPath = to.path.startsWith('/admin') ? '/admin/login' : '/login'
        next({
          path: loginPath,
          query: { redirect: to.fullPath, expired: 'true' }
        })
        return
      }
      
      // 检查角色权限
      if (to.meta.role && store.user.userInfo?.role) {
        const userRole = store.user.userInfo.role
        const requiredRole = to.meta.role
        
        if (requiredRole === 'admin' && userRole !== 'admin') {
          next({ path: '/user/index' })
          return
        }
        
        if (requiredRole === 'user' && userRole === 'admin') {
          next({ path: '/admin/dashboard' })
          return
        }
      }
    }
    
    // 已登录用户访问登录页，重定向到对应首页
    if (to.path === '/login' && store.user.isLoggedIn) {
      const userRole = store.user.userInfo?.role
      if (userRole === 'admin') {
        next({ path: '/admin/dashboard' })
      } else {
        next({ path: '/user/index' })
      }
      return
    }
    
    // 已登录管理员访问管理员登录页
    if (to.path === '/admin/login' && store.user.isLoggedIn) {
      const userRole = store.user.userInfo?.role
      if (userRole === 'admin') {
        next({ path: '/admin/dashboard' })
      } else {
        next({ path: '/user/index' })
      }
      return
    }
    
    next()
})

export default router;
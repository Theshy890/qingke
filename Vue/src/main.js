import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/css/global.css'
import './assets/css/cartoon-dialog.css'
import Particles from '@tsparticles/vue3'
import { loadSlim } from '@tsparticles/slim'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import store from './store'

// 创建应用实例
const app = createApp(App)

// 注册全局状态管理
app.config.globalProperties.$store = store

// 注册路由
app.use(router)

app.use(ElementPlus, {
  locale: zhCn,
})

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 初始化时恢复用户状态
store.restoreUser()

// 注册粒子效果
app.use(Particles, {
  init: async (engine) => {
    await loadSlim(engine)
  }
})

// 挂载应用
app.mount('#app')

// ========== 全局弹窗四角四边拖拽缩放 ==========
const RESIZE_HANDLES = ['n', 's', 'e', 'w', 'ne', 'nw', 'se', 'sw']
const MIN_WIDTH = 360
const MIN_HEIGHT = 200

function addResizeHandles(dialogEl) {
  if (dialogEl.dataset.resizeReady) return
  dialogEl.dataset.resizeReady = 'true'
  
  RESIZE_HANDLES.forEach(dir => {
    const handle = document.createElement('div')
    handle.className = `dialog-resize-handle ${dir}`
    handle.addEventListener('mousedown', (e) => startResize(e, dialogEl, dir))
    dialogEl.appendChild(handle)
  })
}

function startResize(e, dialogEl, direction) {
  e.preventDefault()
  e.stopPropagation()
  
  const startX = e.clientX
  const startY = e.clientY
  const startWidth = dialogEl.offsetWidth
  const startHeight = dialogEl.offsetHeight
  const startLeft = dialogEl.offsetLeft
  const startTop = dialogEl.offsetTop
  
  const onMouseMove = (ev) => {
    const dx = ev.clientX - startX
    const dy = ev.clientY - startY
    
    let newWidth = startWidth
    let newHeight = startHeight
    let newLeft = startLeft
    let newTop = startTop
    
    if (direction.includes('e')) newWidth = Math.max(MIN_WIDTH, startWidth + dx)
    if (direction.includes('s')) newHeight = Math.max(MIN_HEIGHT, startHeight + dy)
    if (direction.includes('w')) {
      newWidth = Math.max(MIN_WIDTH, startWidth - dx)
      if (newWidth > MIN_WIDTH) newLeft = startLeft + dx
    }
    if (direction.includes('n')) {
      newHeight = Math.max(MIN_HEIGHT, startHeight - dy)
      if (newHeight > MIN_HEIGHT) newTop = startTop + dy
    }
    
    dialogEl.style.width = newWidth + 'px'
    dialogEl.style.height = newHeight + 'px'
    dialogEl.style.left = newLeft + 'px'
    dialogEl.style.top = newTop + 'px'
    dialogEl.style.margin = '0'
  }
  
  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// 监听 DOM 变化，自动为新弹窗添加拖拽手柄
const observer = new MutationObserver(() => {
  document.querySelectorAll('.el-overlay-dialog .el-dialog:not([data-resize-ready])').forEach(addResizeHandles)
})

observer.observe(document.body, { childList: true, subtree: true })

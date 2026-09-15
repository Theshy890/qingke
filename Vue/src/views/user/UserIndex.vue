<template>
  <div class="user-index">
    <!-- 轮播图区域 -->
    <section class="carousel-section">
      <el-carousel height="400px" indicator-position="outside" :interval="5000">
        <el-carousel-item v-for="(item, index) in carouselList" :key="index">
          <div class="carousel-item" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="carousel-content">
              <h2 class="carousel-title">{{ item.title }}</h2>
              <p class="carousel-desc">{{ item.description }}</p>
              <el-button type="success" size="large" round @click="handleCarouselClick(item.action)">
                {{ item.buttonText }}
              </el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 功能导航区 -->
    <section class="function-section">
      <h2 class="section-title">
        <el-icon color="#52c41a"><Sunny /></el-icon>
        快捷功能
      </h2>
      <div class="function-grid">
        <div 
          v-for="func in functionList" 
          :key="func.id"
          class="function-card"
          @click="navigateTo(func.path)"
        >
          <div class="function-icon" :style="{ background: func.color }">
            <el-icon :size="36">
              <component :is="func.icon" />
            </el-icon>
          </div>
          <h3 class="function-title">{{ func.title }}</h3>
          <p class="function-desc">{{ func.description }}</p>
        </div>
      </div>
    </section>

    <!-- 公告活动区 -->
    <section class="announcement-section">
      <div class="announcement-left">
        <div class="section-title-row">
          <h2 class="section-title">
            <el-icon color="#ff9800"><Bell /></el-icon>
            最新公告
          </h2>
          <span class="view-more-link" @click="openAllAnnouncements">
            查看更多
            <el-icon><ArrowRight /></el-icon>
          </span>
        </div>
        <div class="announcement-list">
          <div 
            v-for="item in announcements" 
            :key="item.id"
            class="announcement-item"
            @click="viewAnnouncement(item)"
          >
            <el-tag :type="getTagType(item.type)" size="small">{{ item.type }}</el-tag>
            <span class="announcement-title">{{ item.title }}</span>
            <span class="announcement-time">{{ formatTime(item.time) }}</span>
          </div>
        </div>
      </div>

      <div class="announcement-right">
        <h2 class="section-title">
          <el-icon color="#f44336"><Trophy /></el-icon>
          热门活动
        </h2>
        <div class="activity-cards">
          <div 
            v-for="activity in activities" 
            :key="activity.id"
            class="activity-card"
            @click="handleActivityClick(activity)"
          >
            <img :src="activity.image" :alt="activity.title" class="activity-image">
            <div class="activity-info">
              <h3>{{ activity.title }}</h3>
              <p>{{ activity.description }}</p>
              <el-button type="danger" size="small" round>立即参与</el-button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 养护知识推荐 -->
     <section class="knowledge-section">
      <div class="knowledge-section-header">
        <h2 class="section-title">
          <el-icon color="#2196f3"><Reading /></el-icon>
          养护知识精选
        </h2>
        <div class="knowledge-header-right">
          <el-radio-group v-model="activeKnowledgeTab" size="small" @change="activeKnowledgeTab = $event">
            <el-radio-button value="hot">🔥 热门榜单</el-radio-button>
            <el-radio-button value="new">✨ 最新上架</el-radio-button>
          </el-radio-group>
          <el-button text type="primary" @click="navigateTo('/user/knowledge')">
            查看全部 →
          </el-button>
        </div>
      </div>

      <div class="knowledge-grid">
        <div
          v-for="(item, index) in (activeKnowledgeTab === 'hot' ? knowledgeList : personalList)"
          :key="item.id"
          class="knowledge-card"
          @click="viewKnowledge(item)"
        >
          <div class="knowledge-img-wrap">
            <img :src="item.image" :alt="item.title" class="knowledge-image">
            <!-- 热门榜排名角标 -->
            <div v-if="activeKnowledgeTab === 'hot'" class="rank-badge" :class="'rank-' + (index + 1)">
              {{ index < 3 ? ['🥇','🥈','🥉'][index] : index + 1 }}
            </div>
            <!-- 最新上架 NEW 标签 -->
            <div v-else class="new-badge">NEW</div>
            <!-- 悬停时显示核心养护要点 -->
            <div class="knowledge-tip-overlay">
              <el-icon><Sunny /></el-icon>
              <span>{{ item.tip }}</span>
            </div>
          </div>
          <div class="knowledge-info">
            <div class="knowledge-top">
              <h3 class="knowledge-title">{{ item.title }}</h3>
              <el-tag :type="getCategoryTag(item.category).type" size="small" round>
                {{ getCategoryTag(item.category).label }}
              </el-tag>
            </div>
            <p class="knowledge-desc">{{ item.description }}</p>
            <div class="knowledge-meta">
              <span><el-icon><View /></el-icon> {{ item.views }} 次浏览</span>
              <span><el-icon><Star /></el-icon> {{ item.likes }} 人收藏</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 公告详情弹窗 -->
    <el-dialog
      v-model="announcementDialogVisible"
      :title="currentAnnouncement?.title || '公告详情'"
      width="600px"
      draggable
      class="resizable-dialog announcement-detail-dialog user-cartoon-dialog"
    >
      <div v-if="currentAnnouncement" class="announcement-detail">
        <div class="detail-header">
          <el-tag :type="getTagType(currentAnnouncement.type)" size="large">{{ currentAnnouncement.type }}</el-tag>
          <span class="detail-time">
            <el-icon><Clock /></el-icon>
            {{ formatTime(currentAnnouncement.time) }}
          </span>
        </div>
        <div class="detail-brief" v-if="currentAnnouncement.brief">
          {{ currentAnnouncement.brief }}
        </div>
        <div class="detail-content">
          {{ currentAnnouncement.content }}
        </div>
      </div>
      <template #footer>
        <el-button type="primary" size="large" @click="announcementDialogVisible = false">
          我知道了
        </el-button>
      </template>
    </el-dialog>

    <!-- 全部公告弹窗 -->
    <el-dialog
      v-model="allAnnouncementsVisible"
      title="全部公告"
      width="700px"
      draggable
      class="resizable-dialog all-announcements-dialog user-cartoon-dialog"
    >
      <div v-loading="allAnnouncementsLoading" class="all-announcements-list">
        <el-empty v-if="!allAnnouncementsLoading && allAnnouncements.length === 0" description="暂无公告" />
        <div
          v-for="item in allAnnouncements"
          :key="item.id"
          class="all-announcement-item"
          @click="viewAnnouncement(item); allAnnouncementsVisible = false"
        >
          <el-tag :type="getTagType(item.type)" size="small">{{ item.type }}</el-tag>
          <span class="all-announcement-title">{{ item.title }}</span>
          <span class="all-announcement-brief" v-if="item.brief">{{ item.brief }}</span>
          <span class="all-announcement-time">{{ formatTime(item.time) }}</span>
        </div>
      </div>
      <div class="all-announcements-pagination" v-if="allAnnouncementsTotal > allAnnouncementsPageSize">
        <el-pagination
          v-model:current-page="allAnnouncementsPage"
          :page-size="allAnnouncementsPageSize"
          :total="allAnnouncementsTotal"
          layout="prev, pager, next"
          small
          @current-change="loadAllAnnouncements"
        />
      </div>
    </el-dialog>

    <!-- 新人专享弹窗 -->
    <el-dialog
      v-model="newUserDialogVisible"
      title="🎁 新人专享大礼包"
      width="500px"
      :close-on-click-modal="false"
      draggable
      class="resizable-dialog user-cartoon-dialog"
    >
      <div class="gift-dialog-content">
        <div class="gift-banner">
          <img src="https://images.unsplash.com/photo-1513885535751-8b9238bd345a?w=600&h=200&fit=crop" alt="礼包">
        </div>
        
        <div class="gift-items">
          <h3>🎉 恭喜您获得以下奖励：</h3>
          <div class="gift-list">
            <div class="gift-item">
              <el-icon color="#52c41a" :size="24"><Present /></el-icon>
              <span>养护积分 x 100</span>
            </div>
            <div class="gift-item">
              <el-icon color="#1890ff" :size="24"><Tickets /></el-icon>
              <span>AI识别次数 x 10</span>
            </div>
            <div class="gift-item">
              <el-icon color="#faad14" :size="24"><Trophy /></el-icon>
              <span>新手养护指南</span>
            </div>
            <div class="gift-item">
              <el-icon color="#f5222d" :size="24"><StarFilled /></el-icon>
              <span>专属会员徽章</span>
            </div>
          </div>
        </div>

        <div class="gift-tips">
          <el-alert type="success" :closable="false" show-icon>
            <template #title>
              温馨提示：奖励已自动发放到您的账户，快去查看吧！
            </template>
          </el-alert>
        </div>
      </div>
      
      <template #footer>
        <el-button type="primary" size="large" @click="handleClaimGift">
          <el-icon><CircleCheck /></el-icon>
          领取礼包
        </el-button>
      </template>
    </el-dialog>

    <!-- 每日签到弹窗 -->
    <el-dialog
      v-model="signInDialogVisible"
      title="🌟 每日签到"
      width="600px"
      :close-on-click-modal="false"
      draggable
      class="user-cartoon-dialog"
    >
      <div class="signin-dialog-content">
        <div class="signin-header">
          <div class="signin-stats">
            <div class="stat-item">
              <div class="stat-value">{{ signInData.continuousDays }}</div>
              <div class="stat-label">连续签到</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ signInData.totalDays }}</div>
              <div class="stat-label">累计签到</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ signInData.points }}</div>
              <div class="stat-label">当前积分</div>
            </div>
          </div>
        </div>

        <div class="signin-calendar">
          <h3>本周签到记录</h3>
          <div class="calendar-grid">
            <div 
              v-for="(day, index) in weekDays" 
              :key="index"
              class="calendar-day"
              :class="{ 
                'signed': day.signed, 
                'today': day.isToday,
                'future': day.isFuture 
              }"
            >
              <div class="day-name">{{ day.name }}</div>
              <div class="day-icon">
                <el-icon v-if="day.signed" color="#52c41a" :size="32"><CircleCheckFilled /></el-icon>
                <el-icon v-else-if="day.isToday" color="#1890ff" :size="32"><Calendar /></el-icon>
                <el-icon v-else color="#d9d9d9" :size="32"><Calendar /></el-icon>
              </div>
              <div class="day-reward">+{{ day.reward }}积分</div>
            </div>
          </div>
        </div>

        <div class="signin-rewards">
          <h3>签到奖励规则</h3>
          <el-timeline>
            <el-timeline-item color="#52c41a">
              每日签到可获得 <strong>10积分</strong>
            </el-timeline-item>
            <el-timeline-item color="#1890ff">
              连续签到3天额外奖励 <strong>20积分</strong>
            </el-timeline-item>
            <el-timeline-item color="#faad14">
              连续签到7天额外奖励 <strong>50积分</strong>
            </el-timeline-item>
            <el-timeline-item color="#f5222d">
              连续签到30天可获得 <strong>神秘大礼包</strong>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      
      <template #footer>
        <el-button 
          type="primary" 
          size="large" 
          :disabled="signInData.todaySigned"
          @click="handleSignIn"
        >
          <el-icon><Calendar /></el-icon>
          {{ signInData.todaySigned ? '今日已签到' : '立即签到' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { plantKnowledgeApi, sysNoticeApi } from '../../api'

const router = useRouter()

// 轮播图数据
const carouselList = ref([
  {
    image: 'https://images.unsplash.com/photo-1466781783364-36c955e42a7f?w=1200&h=400&fit=crop',
    title: '🌿 让绿植成为生活的一部分',
    description: '科学养护，健康成长',
    buttonText: '开始养护',
    action: 'plant-care'
  },
  {
    image: 'https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=1200&h=400&fit=crop',
    title: '🤖 AI智能识别',
    description: '一键诊断植物病害',
    buttonText: '立即识别',
    action: 'ai-diagnosis'
  },
  {
    image: 'https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=1200&h=400&fit=crop',
    title: '📚 专业养护知识',
    description: '从入门到精通',
    buttonText: '学习知识',
    action: 'knowledge'
  }
])

// 功能导航数据
const functionList = ref([
  {
    id: 1,
    icon: 'Management',
    title: '我的养护',
    description: '查看和管理养护记录',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    path: '/user/plant-care'
  },
  {
    id: 2,
    icon: 'Camera',
    title: 'AI识别',
    description: '智能诊断植物病害',
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    path: '/user/ai-diagnosis'
  },
  {
    id: 3,
    icon: 'Reading',
    title: '养护知识',
    description: '学习专业养护技巧',
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    path: '/user/knowledge'
  },
  {
    id: 4,
    icon: 'ChatDotRound',
    title: '社区交流',
    description: '分享养护心得',
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    path: '/user/community'
  },
  {
    id: 5,
    icon: 'Bell',
    title: '养护提醒',
    description: '定时提醒浇水施肥',
    color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    path: '/user/reminders'
  },
  {
    id: 6,
    icon: 'Star',
    title: '我的收藏',
    description: '收藏喜欢的内容',
    color: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
    path: '/user/favorites'
  }
])

// 公告数据
const announcements = ref([])

// 活动数据
const activities = ref([
  {
    id: 1,
    title: '🎁 新人专享',
    description: '注册即送养护大礼包',
    image: 'https://images.unsplash.com/photo-1463936575829-25148e1db1b8?w=400&h=300&fit=crop'
  },
  {
    id: 2,
    title: '🌟 每日签到',
    description: '连续签到获取积分奖励',
    image: 'https://images.unsplash.com/photo-1501004318641-b39e6451bec6?w=400&h=300&fit=crop'
  }
])

// 养护知识数据
const knowledgeList = ref([])
const personalList = ref([])
const activeKnowledgeTab = ref('hot')
// 加载养护知识推荐
const loadKnowledgeRecommend = async () => {
  try {
    const res = await plantKnowledgeApi.getKnowledgeList({
      pageNum: 1,
      pageSize: 6,
      orderBy: 'clicknum'
    })
    if (res.code === 200 && res.data && res.data.list) {
      knowledgeList.value = res.data.list.map(item => ({
        id: item.id,
        title: item.plantName,
        description: item.maintainTutorial ? item.maintainTutorial.substring(0, 50) + '...' : '暂无介绍',
        image: item.imgUrl || 'https://images.unsplash.com/photo-1463936575829-25148e1db1b8?w=400&h=300&fit=crop',
        views: item.clickNum || 0,
        likes: item.collectNum || 0,
        category: item.categoryName || '',
        tip: extractFirstTip(item.maintainTutorial)
      }))
    }
  } catch (error) {
    console.error('加载养护知识推荐失败:', error)
  }
}
// 加载个性化推荐（按最新发布）
const loadPersonalRecommend = async () => {
  try {
    const res = await plantKnowledgeApi.getKnowledgeList({
      pageNum: 1,
      pageSize: 6,
      orderBy: 'fabushijian'
    })
    if (res.code === 200 && res.data && res.data.list) {
      personalList.value = res.data.list.map(item => ({
        id: item.id,
        title: item.plantName,
        description: item.maintainTutorial ? item.maintainTutorial.substring(0, 50) + '...' : '暂无介绍',
        image: item.imgUrl || 'https://images.unsplash.com/photo-1463936575829-25148e1db1b8?w=400&h=300&fit=crop',
        views: item.clickNum || 0,
        likes: item.collectNum || 0,
        category: item.categoryName || '',
        tip: extractFirstTip(item.maintainTutorial)
      }))
    }
  } catch (error) {
    console.error('加载个性化推荐失败:', error)
  }
}

// 提取第一条养护要点
const extractFirstTip = (content) => {
  if (!content) return '点击查看养护要点'
  const match = content.match(/1[.．、]\s*([^\n。]{6,30})/)
  return match ? match[1].replace(/：.*/, '') : content.substring(0, 20)
}

// 获取种类对应的场景标签和颜色
const getCategoryTag = (category) => {
  const map = {
    '多肉植物': { label: '🌵 懒人首选', type: 'success' },
    '观叶植物': { label: '🍃 净化空气', type: '' },
    '观花植物': { label: '🌸 赏花养眼', type: 'warning' },
    '盆景类': { label: '🎋 禅意盆景', type: 'info' },
    '水生植物': { label: '💧 水培植物', type: 'primary' },
    '藤蔓植物': { label: '🌿 垂挂绿化', type: 'success' }
  }
  return map[category] || { label: '🌱 新手推荐', type: 'success' }
}
// 加载系统公告（展示管理员发布的通知）
const loadAnnouncements = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) return
    const res = await sysNoticeApi.getPage({
      pageNum: 1,
      pageSize: 5,
      userId: userInfo.id,
      type: ['系统公告', '维护通知', '活动通知']
    })
    
    if (res.code === 200 && res.data && res.data.records) {
      announcements.value = res.data.records.map(item => ({
        id: item.id,
        type: item.type,
        title: item.title,
        brief: item.brief || '',
        content: item.content || '',
        time: item.remindTime
      }))
    }
  } catch (error) {
    console.error('加载系统公告失败:', error)
  }
}

// 导航到指定页面
const navigateTo = (path) => {
  router.push(path)
}

// 查看公告详情
const currentAnnouncement = ref(null)
const announcementDialogVisible = ref(false)
const viewAnnouncement = (item) => {
  currentAnnouncement.value = item
  announcementDialogVisible.value = true
}

// 全部公告
const allAnnouncementsVisible = ref(false)
const allAnnouncements = ref([])
const allAnnouncementsLoading = ref(false)
const allAnnouncementsPage = ref(1)
const allAnnouncementsPageSize = ref(10)
const allAnnouncementsTotal = ref(0)

const loadAllAnnouncements = async () => {
  allAnnouncementsLoading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) return
    const res = await sysNoticeApi.getPage({
      pageNum: allAnnouncementsPage.value,
      pageSize: allAnnouncementsPageSize.value,
      userId: userInfo.id,
      type: ['系统公告', '维护通知', '活动通知']
    })
    if (res.code === 200 && res.data && res.data.records) {
      allAnnouncements.value = res.data.records.map(item => ({
        id: item.id,
        type: item.type,
        title: item.title,
        brief: item.brief || '',
        content: item.content || '',
        time: item.remindTime
      }))
      allAnnouncementsTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载全部公告失败:', error)
  } finally {
    allAnnouncementsLoading.value = false
  }
}

const openAllAnnouncements = () => {
  allAnnouncementsPage.value = 1
  allAnnouncementsVisible.value = true
  loadAllAnnouncements()
}

// 查看知识详情
const viewKnowledge = (item) => {
  // 跳转到养护知识页面，并通过路由参数传递知识ID
  router.push({
    path: '/user/knowledge',
    query: {
      id: item.id
    }
  })
}



// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取标签类型
const getTagType = (type) => {
  switch (type) {
    case '系统公告':
      return 'primary'
    case '维护通知':
      return 'warning'
    case '活动通知':
      return 'success'
    default:
      return 'info'
  }
}

// 轮播图按钮点击事件
const handleCarouselClick = (action) => {
  const routeMap = {
    'plant-care': '/user/plant-care',
    'ai-diagnosis': '/user/ai-diagnosis',
    'knowledge': '/user/knowledge'
  }
  
  if (routeMap[action]) {
    router.push(routeMap[action])
  }
}

// 新人专享弹窗
const newUserDialogVisible = ref(false)

// 每日签到弹窗
const signInDialogVisible = ref(false)

// 签到数据
const signInData = ref({
  continuousDays: 0,
  totalDays: 0,
  points: 0,
  todaySigned: false
})

// 本周签到数据
const weekDays = ref([
  { name: '周一', signed: true, isToday: false, isFuture: false, reward: 10 },
  { name: '周二', signed: true, isToday: false, isFuture: false, reward: 10 },
  { name: '周三', signed: true, isToday: false, isFuture: false, reward: 10 },
  { name: '周四', signed: false, isToday: true, isFuture: false, reward: 10 },
  { name: '周五', signed: false, isToday: false, isFuture: true, reward: 10 },
  { name: '周六', signed: false, isToday: false, isFuture: true, reward: 10 },
  { name: '周日', signed: false, isToday: false, isFuture: true, reward: 10 }
])

// 活动点击事件
const handleActivityClick = async (activity) => {
  if (activity.id === 1) {
    // 新人专享 - 检查是否已领取
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) {
      ElMessage.warning('请先登录')
      return
    }

    // 检查数据库是否已领取
    try {
      const res = await fetch(`/api/sys-user/${userInfo.id}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      })
      const result = await res.json()

      if (result.code === 200 && result.data) {
        if (result.data.newbieRewardReceived === 1) {
          ElMessage.warning('您已经领取过新人礼包啦！')
          return
        }
      }
    } catch (error) {
      console.error('检查领取状态失败:', error)
    }

    newUserDialogVisible.value = true
  } else if (activity.id === 2) {
    // 每日签到
    await loadSignInData()
    signInDialogVisible.value = true
  }
}

// 加载签到数据
const loadSignInData = async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const signInKey = `signIn_${userInfo.id || 'guest'}`
  const storedData = localStorage.getItem(signInKey)

  const today = getCurrentDate() // 获取今天日期（不含时间）

  // 从后端获取真实积分
  let dbPoints = 0
  try {
    const res = await fetch(`/api/sys-user/${userInfo.id}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    const result = await res.json()
    if (result.code === 200 && result.data) {
      dbPoints = result.data.points || 0
    }
  } catch (error) {
    console.error('获取积分数据失败:', error)
  }

  if (storedData) {
    const data = JSON.parse(storedData)
    signInData.value = {
      continuousDays: data.continuousDays || 0,
      totalDays: data.totalDays || 0,
      points: dbPoints,
      todaySigned: data.lastSignInDate === today // 只检查日期，不检查具体时间
    }

    // 更新本周签到记录
    updateWeekDays(data.signedDates || [])
  } else {
    signInData.value = {
      continuousDays: 0,
      totalDays: 0,
      points: dbPoints,
      todaySigned: false
    }
  }
}

// 更新本周签到状态
const updateWeekDays = (signedDates) => {
  const today = new Date()
  const dayOfWeek = today.getDay() || 7 // 周日为0，转换为7
  
  weekDays.value.forEach((day, index) => {
    const dayIndex = index + 1
    day.isToday = dayIndex === dayOfWeek
    day.isFuture = dayIndex > dayOfWeek
    
    // 检查是否已签到
    const date = new Date(today)
    date.setDate(date.getDate() - (dayOfWeek - dayIndex))
    const dateStr = formatDate(date)
    day.signed = signedDates.includes(dateStr)
  })
}

// 签到操作
const handleSignIn = async () => {
  if (signInData.value.todaySigned) {
    ElMessage.warning('今日已签到，明天再来吧！')
    return
  }
  
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const signInKey = `signIn_${userInfo.id || 'guest'}`
  const storedData = JSON.parse(localStorage.getItem(signInKey) || '{}')
  
  const today = getCurrentDate()
  const yesterday = getYesterdayDate()
  
  // 计算连续签到天数
  let continuousDays = 1
  if (storedData.lastSignInDate === yesterday) {
    continuousDays = (storedData.continuousDays || 0) + 1
  }
  
  // 基础积分
  let earnedPoints = 10
  
  // 连续签到奖励
  if (continuousDays === 3) {
    earnedPoints += 20
    ElMessage.success('🎉 连续签到3天，额外获得20积分！')
  } else if (continuousDays === 7) {
    earnedPoints += 50
    ElMessage.success('🎉 连续签到7天，额外获得50积分！')
  } else if (continuousDays === 30) {
    earnedPoints += 100
    ElMessage.success('🎊 连续签到30天，获得神秘大礼包！')
  }
  
  // 调用后端 API 更新积分
  try {
    const res = await fetch(`/api/sys-user/${userInfo.id}/signIn?points=${earnedPoints}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    const result = await res.json()
    if (result.code !== 200) {
      ElMessage.error('签到失败：' + result.message)
      return
    }
  } catch (error) {
    console.error('签到请求失败:', error)
    ElMessage.error('签到请求失败')
    return
  }
  
  // 更新本地数据
  const newData = {
    continuousDays: continuousDays,
    totalDays: (storedData.totalDays || 0) + 1,
    points: (storedData.points || 0) + earnedPoints,
    lastSignInDate: today,
    signedDates: [...(storedData.signedDates || []), today]
  }
  
  localStorage.setItem(signInKey, JSON.stringify(newData))
  
  signInData.value = {
    continuousDays: newData.continuousDays,
    totalDays: newData.totalDays,
    points: signInData.value.points + earnedPoints,
    todaySigned: true
  }
  
  updateWeekDays(newData.signedDates)
  
  ElMessage.success(`✨ 签到成功！获得 ${earnedPoints} 积分`)
}

// 领取新人礼包
const handleClaimGift = async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

  if (!userInfo.id) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    // 更新数据库标记为已领取
    const res = await fetch('/api/sys-user/update', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        id: userInfo.id,
        newbieRewardReceived: 1
      })
    })

    const result = await res.json()

    if (result.code === 200) {
      // 发放积分奖励
      const signInKey = `signIn_${userInfo.id}`
      const signInDataStored = JSON.parse(localStorage.getItem(signInKey) || '{}')
      signInDataStored.points = (signInDataStored.points || 0) + 100
      localStorage.setItem(signInKey, JSON.stringify(signInDataStored))

      ElMessage.success('🎉 恭喜您！新人礼包已领取成功！获得100积分')
      newUserDialogVisible.value = false
    } else {
      ElMessage.error(result.message || '领取失败')
    }
  } catch (error) {
    console.error('领取新人礼包失败:', error)
    ElMessage.error('领取失败，请稍后重试')
  }
}

// 获取当前日期字符串
const getCurrentDate = () => {
  return formatDate(new Date())
}

// 获取昨天日期字符串
const getYesterdayDate = () => {
  const date = new Date()
  date.setDate(date.getDate() - 1)
  return formatDate(date)
}

// 格式化日期
const formatDate = (date) => {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadKnowledgeRecommend()
  loadAnnouncements()
})
</script>

<style scoped>
.user-index {
  padding-bottom: 40px;
}

/* 轮播图区域 */
.carousel-section {
  margin-bottom: 40px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.carousel-item {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
}

.carousel-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.carousel-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);
}

.carousel-desc {
  font-size: 20px;
  margin-bottom: 32px;
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.3);
}

/* 功能导航区 */
.function-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-title-row .section-title {
  margin-bottom: 0;
}

.view-more-link {
  font-size: 13px;
  color: #4caf50;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 2px;
  transition: color 0.2s;
}

.view-more-link:hover {
  color: #388e3c;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.function-card {
  background: white;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.function-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
}

.function-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: white;
}

.function-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.function-desc {
  font-size: 14px;
  color: #666;
}

/* 公告活动区 */
.announcement-section {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 24px;
  margin-bottom: 40px;
}

.announcement-left,
.announcement-right {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.announcement-item:hover {
  background: #f5f5f5;
}

.announcement-title {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.announcement-time {
  font-size: 12px;
  color: #999;
}

.activity-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%);
  cursor: pointer;
  transition: all 0.3s;
}

.activity-card:hover {
  transform: scale(1.02);
}

.activity-image {
  width: 100px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.activity-info h3 {
  font-size: 16px;
  margin-bottom: 8px;
}

.activity-info p {
  font-size: 12px;
  color: #666;
  margin-bottom: 12px;
}

/* 公告详情弹窗 */
.announcement-detail {
  padding: 8px 0;
}

.announcement-detail .detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8f5e9;
  margin-bottom: 16px;
}

.announcement-detail .detail-time {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.announcement-detail .detail-brief {
  font-size: 15px;
  color: #555;
  background: #f1f8e9;
  border-left: 3px solid #4caf50;
  padding: 12px 16px;
  border-radius: 0 8px 8px 0;
  margin-bottom: 16px;
  line-height: 1.6;
}

.announcement-detail .detail-content {
  font-size: 14px;
  color: #444;
  line-height: 1.8;
  white-space: pre-wrap;
  padding: 0 4px;
}

/* 全部公告弹窗 */
.all-announcements-list {
  max-height: none;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.all-announcement-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.all-announcement-item:last-child {
  border-bottom: none;
}

.all-announcement-item:hover {
  background: #f1f8e9;
}

.all-announcement-title {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.all-announcement-brief {
  flex: 1;
  font-size: 13px;
  color: #888;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.all-announcement-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
  white-space: nowrap;
}

.all-announcements-pagination {
  display: flex;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
}

/* 养护知识区 */
.knowledge-section {
  margin-bottom: 40px;
}

.knowledge-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.knowledge-section-header .section-title {
  margin-bottom: 0;
}

.knowledge-header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.knowledge-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.knowledge-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
}

.knowledge-img-wrap {
  position: relative;
  overflow: hidden;
}

.knowledge-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.knowledge-card:hover .knowledge-image {
  transform: scale(1.06);
}

/* 热门排名角标 */
.rank-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  font-size: 22px;
  line-height: 1;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));
}

/* NEW 标签 */
.new-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 10px;
  letter-spacing: 1px;
}

/* 悬停养护要点蒙层 */
.knowledge-tip-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(46, 125, 50, 0.88));
  color: white;
  padding: 24px 12px 10px;
  font-size: 13px;
  display: flex;
  align-items: flex-end;
  gap: 5px;
  opacity: 0;
  transform: translateY(6px);
  transition: all 0.3s ease;
}

.knowledge-card:hover .knowledge-tip-overlay {
  opacity: 1;
  transform: translateY(0);
}

.knowledge-info {
  padding: 16px 18px 18px;
}

.knowledge-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.knowledge-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.knowledge-desc {
  font-size: 13px;
  color: #888;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
}

.knowledge-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #aaa;
}

.knowledge-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 新人礼包弹窗样式 */
.gift-dialog-content {
  padding: 10px 0;
}

.gift-banner {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
}

.gift-banner img {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.gift-items h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
  text-align: center;
}

.gift-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.gift-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  border-radius: 12px;
  font-size: 15px;
  color: #333;
  transition: all 0.3s;
}

.gift-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.gift-tips {
  margin-top: 16px;
}

/* 签到弹窗样式 */
.signin-dialog-content {
  padding: 10px 0;
}

.signin-header {
  margin-bottom: 24px;
}

.signin-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.signin-calendar {
  margin-bottom: 24px;
}

.signin-calendar h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 16px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-day {
  text-align: center;
  padding: 16px 8px;
  background: #f5f5f5;
  border-radius: 12px;
  transition: all 0.3s;
}

.calendar-day.signed {
  background: linear-gradient(135deg, #d4f4dd 0%, #c1f0cf 100%);
}

.calendar-day.today {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 2px solid #1890ff;
}

.calendar-day.future {
  opacity: 0.5;
}

.day-name {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.day-icon {
  margin-bottom: 8px;
}

.day-reward {
  font-size: 12px;
  color: #52c41a;
  font-weight: 600;
}

.signin-rewards h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 16px;
}

:deep(.el-timeline-item__content) {
  font-size: 14px;
}

@media (max-width: 768px) {
  .announcement-section {
    grid-template-columns: 1fr;
  }
  
  .function-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .knowledge-grid {
    grid-template-columns: 1fr;
  }
  
  .gift-list {
    grid-template-columns: 1fr;
  }
  
  .calendar-grid {
    gap: 4px;
  }
  
  .calendar-day {
    padding: 12px 4px;
  }
}
</style>
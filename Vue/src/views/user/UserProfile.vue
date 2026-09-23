<template>
  <div class="user-profile-page">
    <!-- 顶部用户信息区 -->
    <div class="profile-header">
      <div class="user-info-section">
        <el-avatar :size="80" :src="userProfile.avatarUrl || defaultAvatar" class="user-avatar">
          <el-icon><User /></el-icon>
        </el-avatar>

        <div class="user-details">
          <h2 class="user-name">{{ userProfile.name || '未设置昵称' }}</h2>
          <p class="user-signature">{{ userProfile.signature || '这个人很懒，什么都没留下~' }}</p>
        </div>

        <!-- 关注/私信按钮（仅访问他人主页时显示） -->
        <div v-if="!isMyProfile" class="follow-button-section">
          <el-button
            :type="followStats.isFollowing ? 'default' : 'success'"
            @click="handleFollow"
            round
            size="large"
          >
            {{ followStats.isFollowing ? '已关注' : '+ 关注' }}
          </el-button>
          <el-button class="message-button" round size="large" @click="goToChat">
            私信
          </el-button>
        </div>
      </div>

      <!-- 数据统计栏 -->
      <div class="stats-bar">
        <div class="stat-item clickable" @click="showFollowingList">
          <span class="stat-number">{{ followStats.followingCount || 0 }}</span>
          <span class="stat-label">关注</span>
        </div>
        <div class="stat-item clickable" @click="showFollowersList">
          <span class="stat-number">{{ followStats.followersCount || 0 }}</span>
          <span class="stat-label">粉丝</span>
        </div>
        <div class="stat-item">
          <span class="stat-number">{{ followStats.likesCount || 0 }}</span>
          <span class="stat-label">获赞</span>
        </div>
        <div v-if="isMyProfile" class="stat-item clickable" @click="showVisitorsList">
          <el-badge :value="visitorUnread" :hidden="visitorUnread === 0" :max="99">
            <span class="stat-number">{{ visitorTotal || 0 }}</span>
          </el-badge>
          <span class="stat-label">访客</span>
        </div>
      </div>
    </div>

    <!-- Tab 切换区域 -->
    <div class="tabs-section">
      <div class="tabs-header">
        <div
          class="tab-item"
          :class="{ active: activeTab === 'posts' }"
          @click="activeTab = 'posts'"
        >
          帖子
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'maintain' }"
          @click="activeTab = 'maintain'"
        >
          养护记录
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'collect' }"
          @click="activeTab = 'collect'"
        >
          收藏内容
        </div>
      </div>

      <!-- Tab 内容区 -->
      <div class="tab-content">
        <!-- 作品 Tab -->
        <div v-if="activeTab === 'posts'" class="posts-grid">
          <div v-if="userPosts.length === 0" class="empty-notice">
            <el-empty description="暂无帖子" />
          </div>
          <div v-else class="grid-container">
            <div
              v-for="post in userPosts"
              :key="post.id"
              class="post-card"
              @click="goToPostDetail(post.id)"
            >
              <img :src="post.imgUrl || defaultPlantImg" class="post-img" />
              <div class="post-info">
                <p class="post-title">{{ post.title }}</p>
                <div class="post-stats">
                  <span class="stat">
                    <el-icon><Star /></el-icon>
                    {{ post.likeCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 养护记录 Tab -->
        <div v-if="activeTab === 'maintain'" class="maintain-content">
          <div v-if="!userProfile.careRecordsVisible" class="privacy-notice">
            <el-icon :size="60" color="#ccc"><Lock /></el-icon>
            <p>该用户未公开养护记录</p>
          </div>

          <div v-else-if="maintainRecords.length === 0" class="empty-notice">
            <el-empty description="暂无养护记录" />
          </div>

          <div v-else class="record-list">
            <div v-for="record in maintainRecords" :key="record.id" class="record-card">
              <img :src="record.imgUrl || defaultPlantImg" class="record-img" />
              <div class="record-info">
                <h3>{{ record.plantName }}</h3>
                <p class="category">{{ record.categoryName }}</p>
                <p class="content">{{ record.maintainContent }}</p>
                <div class="record-meta">
                  <span class="date">{{ record.maintainDate }}</span>
                  <span class="status">{{ record.growStatus }}</span>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <el-pagination
              v-if="maintainTotal > maintainPageSize"
              background
              layout="prev, pager, next"
              :total="maintainTotal"
              :page-size="maintainPageSize"
              :current-page="maintainPageNum"
              @current-change="handleMaintainPageChange"
              class="pagination"
            />
          </div>
        </div>

        <!-- 收藏内容 Tab -->
        <div v-if="activeTab === 'collect'" class="collect-content">
          <div v-if="!userProfile.favoritesVisible" class="privacy-notice">
            <el-icon :size="60" color="#ccc"><Lock /></el-icon>
            <p>该用户未公开收藏内容</p>
          </div>

          <div v-else-if="collections.length === 0" class="empty-notice">
            <el-empty description="暂无收藏内容" />
          </div>

          <div v-else class="collect-list">
            <div v-for="collect in collections" :key="collect.id" class="collect-card">
              <img :src="collect.imgUrl || defaultPlantImg" class="collect-img" />
              <div class="collect-info">
                <h3>{{ collect.targetName }}</h3>
                <p class="type">{{ collect.targetType || collect.recType }}</p>
                <p class="remark">{{ collect.remark }}</p>
                <span class="date">{{ formatDate(collect.createTime) }}</span>
              </div>
            </div>

            <!-- 分页 -->
            <el-pagination
              v-if="collectTotal > collectPageSize"
              background
              layout="prev, pager, next"
              :total="collectTotal"
              :page-size="collectPageSize"
              :current-page="collectPageNum"
              @current-change="handleCollectPageChange"
              class="pagination"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 关注/粉丝合并抽屉 -->
    <el-drawer
      v-model="followDrawerVisible"
      direction="ltr"
      size="420px"
      :show-close="true"
    >
      <template #header>
        <el-tabs v-model="followActiveTab" class="follow-tabs">
          <el-tab-pane :label="`关注 (${followStats.followingCount || 0})`" name="following" />
          <el-tab-pane :label="`粉丝 (${followStats.followersCount || 0})`" name="followers" />
        </el-tabs>
      </template>

      <!-- 搜索框 -->
      <div class="search-bar">
        <el-input
          v-model="followSearchQuery"
          placeholder="搜索账号/用户名"
          clearable
          :prefix-icon="Search"
        />
      </div>

      <!-- 关注列表 -->
      <div v-if="followActiveTab === 'following'" class="follow-list-content">
        <div v-if="filteredFollowingList.length === 0" class="empty-notice">
          <el-empty description="暂无关注" />
        </div>
        <div v-else class="user-list">
          <div v-for="item in filteredFollowingList" :key="item.id" class="user-item">
            <el-avatar
              :size="50"
              :src="item.avatarUrl || defaultAvatar"
              class="clickable-avatar"
              @click="goToUserProfile(item.userId)"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="user-info clickable-user" @click="goToUserProfile(item.userId)">
              <h4>{{ item.name || '用户' }}</h4>
              <p>{{ item.signature || '这个人很懒~' }}</p>
            </div>
            <el-button size="small" round disabled>已关注</el-button>
          </div>
        </div>
      </div>

      <!-- 粉丝列表 -->
      <div v-if="followActiveTab === 'followers'" class="follow-list-content">
        <div v-if="filteredFollowersList.length === 0" class="empty-notice">
          <el-empty description="暂无粉丝" />
        </div>
        <div v-else class="user-list">
          <div v-for="item in filteredFollowersList" :key="item.id" class="user-item">
            <el-avatar
              :size="50"
              :src="item.avatarUrl || defaultAvatar"
              class="clickable-avatar"
              @click="goToUserProfile(item.userId)"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="user-info clickable-user" @click="goToUserProfile(item.userId)">
              <h4>{{ item.name || '用户' }}</h4>
              <p>{{ item.signature || '这个人很懒~' }}</p>
            </div>
            <div class="user-actions">
              <el-button
                v-if="!item.isFollowing"
                size="small"
                round
                type="danger"
                @click.stop="handleFollowBack(item.userId)"
              >
                回关
              </el-button>
              <el-button
                v-if="isMyProfile"
                size="small"
                round
                @click.stop="handleRemoveFollower(item)"
              >
                移除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 访客列表抽屉 -->
    <el-drawer v-model="visitorDrawerVisible" title="访客记录" direction="rtl" size="420px">
      <div class="visitor-list-content">
        <div v-if="visitorList.length === 0" class="empty-notice">
          <el-empty description="暂无访客记录" />
        </div>
        <div v-else class="user-list">
          <div v-for="item in visitorList" :key="item.id" class="user-item">
            <el-avatar
              :size="50"
              :src="item.visitorAvatar || defaultAvatar"
              class="clickable-avatar"
              @click="goToUserProfile(item.visitorId)"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="user-info clickable-user" @click="goToUserProfile(item.visitorId)">
              <h4>{{ item.visitorNickname || '用户' }}</h4>
              <p class="visit-time">{{ item.visitTime }}</p>
            </div>
            <el-tag v-if="item.isRead === 0" type="danger" size="small" effect="light">未读</el-tag>
          </div>

          <el-pagination
            v-if="visitorTotal > visitorPageSize"
            background
            layout="prev, pager, next"
            :total="visitorTotal"
            :page-size="visitorPageSize"
            :current-page="visitorPageNum"
            @current-change="handleVisitorPageChange"
            class="pagination"
          />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Star, Search } from '@element-plus/icons-vue'
import { userProfileApi, userFollowApi, userVisitorApi, communityPostApi } from '@/api'
import defaultAvatar from '@/assets/img/touxiang.png'
import defaultPlantImg from '@/assets/img/logo.png'

const route = useRoute()
const router = useRouter()

const activeTab = ref('posts')

// 当前登录用户信息
const currentUser = JSON.parse(localStorage.getItem('userInfo') || '{}')

// 是否是访问自己的主页
const isMyProfile = computed(() => {
  return currentUser.id && currentUser.id == route.params.userId
})

// 用户主页信息
const userProfile = reactive({
  id: null,
  name: '',
  avatarUrl: '',
  signature: '',
  gender: '',
  registerTime: null,
  careRecordsVisible: false,
  favoritesVisible: false,
  maintainRecordCount: 0,
  collectCount: 0
})

// 关注/粉丝统计
const followStats = reactive({
  followingCount: 0,
  followersCount: 0,
  likesCount: 0,
  isFollowing: false
})

// 用户帖子列表（作品Tab）
const userPosts = ref([])

// 养护记录
const maintainRecords = ref([])
const maintainPageNum = ref(1)
const maintainPageSize = ref(10)
const maintainTotal = ref(0)

// 收藏内容
const collections = ref([])
const collectPageNum = ref(1)
const collectPageSize = ref(10)
const collectTotal = ref(0)

// 关注/粉丝合并抽屉
const followDrawerVisible = ref(false)
const followActiveTab = ref('following')
const followSearchQuery = ref('')
const followingList = ref([])
const followersList = ref([])

// 访客列表抽屉
const visitorDrawerVisible = ref(false)
const visitorList = ref([])
const visitorPageNum = ref(1)
const visitorPageSize = ref(20)
const visitorTotal = ref(0)
const visitorUnread = ref(0)

// 加载用户主页信息
const loadUserProfile = async () => {
  try {
    const userId = route.params.userId
    const res = await userProfileApi.getUserProfile(userId)

    if (res.code === 200) {
      Object.assign(userProfile, res.data)

      // 加载关注/粉丝统计
      loadFollowStats()

      // 加载用户帖子
      loadUserPosts()

      // 记录访客（仅访问他人主页时）
      if (!isMyProfile.value && currentUser.id) {
        userVisitorApi.recordVisit(userId, currentUser.id).catch(err => {
          console.error('记录访客失败:', err)
        })
      } else if (isMyProfile.value) {
        // 本人主页：加载访客统计
        loadVisitorStats()
      }
    } else {
      ElMessage.error(res.message || '加载用户信息失败')
      if (res.message && res.message.includes('隐私保护')) {
        setTimeout(() => router.back(), 1500)
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}

// 加载关注/粉丝统计
const loadFollowStats = async () => {
  try {
    const userId = route.params.userId
    const res = await userFollowApi.getStats(userId, currentUser.id)

    if (res.code === 200) {
      Object.assign(followStats, res.data)
    }
  } catch (error) {
    console.error('加载关注统计失败:', error)
  }
}

// 加载用户帖子
const loadUserPosts = async () => {
  try {
    const userId = route.params.userId
    const res = await communityPostApi.getUserPosts(userId)

    if (res.code === 200) {
      userPosts.value = res.data || []
    }
  } catch (error) {
    console.error('加载用户帖子失败:', error)
  }
}

// 加载访客统计（仅本人主页：总数 + 未读数）
const loadVisitorStats = async () => {
  try {
    const [listRes, unreadRes] = await Promise.all([
      userVisitorApi.getVisitorList(currentUser.id, 1, 1),
      userVisitorApi.getUnreadCount(currentUser.id)
    ])
    if (listRes.code === 200) {
      visitorTotal.value = listRes.data.total || 0
    }
    if (unreadRes.code === 200) {
      visitorUnread.value = unreadRes.data || 0
    }
  } catch (error) {
    console.error('加载访客统计失败:', error)
  }
}

// 加载访客列表
const loadVisitorList = async () => {
  try {
    const res = await userVisitorApi.getVisitorList(currentUser.id, visitorPageNum.value, visitorPageSize.value)
    if (res.code === 200) {
      visitorList.value = res.data.list || []
      visitorTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载访客列表失败:', error)
  }
}

// 打开访客抽屉：加载列表并标记全部已读
const showVisitorsList = async () => {
  visitorDrawerVisible.value = true
  visitorPageNum.value = 1
  await loadVisitorList()
  if (visitorUnread.value > 0) {
    try {
      await userVisitorApi.markAsRead(currentUser.id)
      visitorUnread.value = 0
      visitorList.value = visitorList.value.map(item => ({ ...item, isRead: 1 }))
    } catch (error) {
      console.error('标记访客已读失败:', error)
    }
  }
}

// 访客列表分页
const handleVisitorPageChange = (page) => {
  visitorPageNum.value = page
  loadVisitorList()
}

// 加载养护记录
const loadMaintainRecords = async () => {
  try {
    const userId = route.params.userId
    const res = await userProfileApi.getMaintainRecords(userId, maintainPageNum.value, maintainPageSize.value)

    if (res.code === 200) {
      maintainRecords.value = res.data.list || []
      maintainTotal.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '加载养护记录失败')
    }
  } catch (error) {
    console.error('加载养护记录失败:', error)
    ElMessage.error('加载养护记录失败')
  }
}

// 加载收藏内容
const loadCollections = async () => {
  try {
    const userId = route.params.userId
    const res = await userProfileApi.getCollections(userId, collectPageNum.value, collectPageSize.value)

    if (res.code === 200) {
      collections.value = res.data.list || []
      collectTotal.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '加载收藏内容失败')
    }
  } catch (error) {
    console.error('加载收藏内容失败:', error)
    ElMessage.error('加载收藏内容失败')
  }
}

// 监听路由参数变化，重新加载用户数据
watch(() => route.params.userId, (newUserId) => {
  if (newUserId) {
    loadUserProfile()
  }
})

// Tab 切换
watch(activeTab, (newTab) => {
  if (newTab === 'maintain' && userProfile.careRecordsVisible && maintainRecords.value.length === 0) {
    loadMaintainRecords()
  } else if (newTab === 'collect' && userProfile.favoritesVisible && collections.value.length === 0) {
    loadCollections()
  }
})

// 养护记录分页
const handleMaintainPageChange = (page) => {
  maintainPageNum.value = page
  loadMaintainRecords()
}

// 收藏内容分页
const handleCollectPageChange = (page) => {
  collectPageNum.value = page
  loadCollections()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

// 关注/取消关注
const handleFollow = async () => {
  if (!currentUser.id) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    const userId = route.params.userId

    if (followStats.isFollowing) {
      await userFollowApi.unfollow(currentUser.id, userId)
      followStats.isFollowing = false
      followStats.followersCount--
      ElMessage.success('已取消关注')
    } else {
      await userFollowApi.follow(currentUser.id, userId)
      followStats.isFollowing = true
      followStats.followersCount++
      ElMessage.success('关注成功')
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 显示关注/粉丝合并抽屉
const showFollowingList = async () => {
  followActiveTab.value = 'following'
  followDrawerVisible.value = true
  followSearchQuery.value = ''
  await loadFollowingList()
}

const showFollowersList = async () => {
  followActiveTab.value = 'followers'
  followDrawerVisible.value = true
  followSearchQuery.value = ''
  await loadFollowersList()
}

// 加载关注列表
const loadFollowingList = async () => {
  try {
    const userId = route.params.userId
    const res = await userFollowApi.getFollowingList(userId, 1, 50)
    if (res.code === 200) {
      followingList.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载关注列表失败:', error)
  }
}

// 加载粉丝列表
const loadFollowersList = async () => {
  try {
    const userId = route.params.userId
    const res = await userFollowApi.getFollowersList(userId, 1, 50)
    if (res.code === 200) {
      followersList.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载粉丝列表失败:', error)
  }
}

// 搜索过滤 - 关注列表（支持账号/用户名）
const filteredFollowingList = computed(() => {
  if (!followSearchQuery.value) return followingList.value
  const query = followSearchQuery.value.toLowerCase()
  return followingList.value.filter(item =>
    (item.name || '').toLowerCase().includes(query) ||
    (item.zh || '').toLowerCase().includes(query)
  )
})

// 搜索过滤 - 粉丝列表（支持账号/用户名）
const filteredFollowersList = computed(() => {
  if (!followSearchQuery.value) return followersList.value
  const query = followSearchQuery.value.toLowerCase()
  return followersList.value.filter(item =>
    (item.name || '').toLowerCase().includes(query) ||
    (item.zh || '').toLowerCase().includes(query)
  )
})

// 回关粉丝
const handleFollowBack = async (userId) => {
  if (!currentUser.id) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await userFollowApi.follow(currentUser.id, userId)
    ElMessage.success('关注成功')
    // 更新粉丝列表中的关注状态
    const item = followersList.value.find(f => f.userId === userId)
    if (item) item.isFollowing = true
    // 更新关注统计
    loadFollowStats()
  } catch (error) {
    console.error('关注失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 移除粉丝
const handleRemoveFollower = (item) => {
  if (!currentUser.id) {
    ElMessage.warning('请先登录')
    return
  }
  // 移除粉丝 = 当前用户取消关注该粉丝
  // 即 followerId = item.userId, followingId = currentUser.id
  userFollowApi.unfollow(item.userId, currentUser.id).then(() => {
    followersList.value = followersList.value.filter(f => f.userId !== item.userId)
    ElMessage.success('已移除粉丝')
    loadFollowStats()
  }).catch(err => {
    console.error('移除粉丝失败:', err)
    ElMessage.error(err.response?.data?.message || '操作失败')
  })
}

// 跳转到用户主页（新标签页打开）
const goToUserProfile = (userId) => {
  const routeUrl = router.resolve(`/user/profile/${userId}`)
  window.open(routeUrl.href, '_blank')
}

// 发起私信（跳转私信页并自动打开与对方的会话）
const goToChat = () => {
  if (!currentUser.id) {
    ElMessage.warning('请先登录')
    return
  }
  router.push(`/user/messages?to=${route.params.userId}`)
}

// 跳转到帖子详情
const goToPostDetail = (postId) => {
  router.push(`/community/detail/${postId}`)
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.user-profile-page {
  min-height: calc(100vh - 120px);
  background: #f5f7fa;
  padding-bottom: 30px;
}

.profile-header {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  padding: 30px 20px;
  color: white;
}

.user-info-section {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  border: 3px solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 24px;
  margin: 0 0 8px 0;
  font-weight: bold;
}

.user-signature {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.follow-button-section {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.follow-button-section .el-button {
  padding: 12px 30px;
  font-size: 15px;
  font-weight: 500;
  margin-left: 0;
}

.stats-bar {
  max-width: 1200px;
  margin: 20px auto 0;
  display: flex;
  gap: 40px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-item.clickable {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-item.clickable:hover {
  transform: translateY(-2px);
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
}

.tabs-section {
  max-width: 1200px;
  margin: 20px auto 0;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid #e8e8e8;
}

.tab-item {
  flex: 1;
  padding: 16px 0;
  text-align: center;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.tab-item.active {
  color: #4caf50;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background: #4caf50;
  border-radius: 2px;
}

.tab-content {
  padding: 20px;
  min-height: 400px;
}

.posts-grid {
  padding: 10px;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.post-card {
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.post-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.post-info {
  padding: 12px;
}

.post-title {
  font-size: 14px;
  margin: 0 0 8px 0;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
}

.post-stats .stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-notice {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.privacy-notice {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.privacy-notice p,
.empty-notice p {
  margin-top: 20px;
  font-size: 16px;
}

.record-list,
.collect-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.record-card,
.collect-card {
  background: #f8f9fa;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
}

.record-card:hover,
.collect-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.record-img,
.collect-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.record-info,
.collect-info {
  padding: 16px;
}

.record-info h3,
.collect-info h3 {
  font-size: 18px;
  margin: 0 0 8px 0;
  color: #333;
}

.category,
.type {
  font-size: 13px;
  color: #66bb6a;
  margin: 0 0 8px 0;
}

.content,
.remark {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.record-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.date {
  color: #999;
}

.status {
  background: #e8f5e9;
  color: #4caf50;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.pagination {
  margin-top: 30px;
  justify-content: center;
}

/* 关注/粉丝列表样式 */
.follow-list {
  padding: 10px;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.user-item:hover {
  background: #f0f0f0;
}

.user-info {
  flex: 1;
}

.user-info h4 {
  margin: 0 0 4px 0;
  font-size: 15px;
  color: #333;
}

.user-info p {
  margin: 0;
  font-size: 13px;
  color: #999;
}

.visit-time {
  font-size: 12px;
}

.visitor-list-content {
  padding: 16px;
  max-height: calc(100vh - 140px);
  overflow-y: auto;
}

/* 关注/粉丝合并抽屉样式 */
.follow-tabs {
  width: 100%;
}

.follow-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.follow-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.search-bar {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.search-bar .el-input {
  width: 100%;
}

.follow-list-content {
  padding: 16px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.user-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.clickable-avatar {
  cursor: pointer;
  transition: transform 0.2s;
}

.clickable-avatar:hover {
  transform: scale(1.08);
}

.clickable-user {
  cursor: pointer;
}

.clickable-user:hover h4 {
  color: #4caf50;
}

@media (max-width: 768px) {
  .user-info-section {
    flex-direction: column;
    text-align: center;
  }

  .stats-bar {
    justify-content: center;
    gap: 30px;
  }

  .grid-container {
    grid-template-columns: repeat(2, 1fr);
  }

  .record-list,
  .collect-list {
    grid-template-columns: 1fr;
  }
}
</style>

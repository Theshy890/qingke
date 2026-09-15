<template>
  <div class="favorites-page">
    <div class="page-header">
      <h2>我的收藏</h2>
      <p>收藏喜欢的内容</p>
    </div>

    <!-- 分类标签 -->
    <div class="category-tabs">
      <el-radio-group v-model="activeCategory" @change="handleCategoryChange">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="yanghuzhishi">养护知识</el-radio-button>
        <el-radio-button label="shequhudong">社区</el-radio-button>
        <el-radio-button label="lvzhizhonglei">绿植种类</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 收藏列表 -->
    <div v-loading="loading" class="favorites-list">
      <el-empty v-if="!loading && favoritesList.length === 0" description="暂无收藏内容" />
      
      <div class="favorites-grid">
        <div
          v-for="item in favoritesList"
          :key="item.id"
          class="favorite-card"
          @click="handleGoToOriginal(item)"
        >
          <div class="card-image">
            <el-image
              :src="item.picture || defaultImage"
              fit="cover"
              class="favorite-img"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="card-badge">
              <el-tag :type="getTableTypeColor(item.tablename)" size="small">
                {{ getTableTypeName(item.tablename) }}
              </el-tag>
            </div>
          </div>
          
          <div class="card-content">
            <h3 class="card-title">{{ item.name }}</h3>
            <div class="card-footer">
              <div class="card-time">
                {{ formatDate(item.createTime) }}
              </div>
              <el-button 
                type="danger" 
                size="small" 
                plain
                @click.stop="handleCancelStoreup(item)"
              >
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'
import { userCollectApi, plantKnowledgeApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const defaultImage = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const activeCategory = ref('all')
const loading = ref(false)
const favoritesList = ref([])

const getFavoritesList = async () => {
  loading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) {
      ElMessage.warning('请先登录')
      loading.value = false
      return
    }

    let res
    if (activeCategory.value === 'all') {
      res = await userCollectApi.getStoreupList(userInfo.id)
    } else if (activeCategory.value === 'shequhudong') {
      // 社区类型需同时包含帖子收藏和点赞记录，客户端过滤
      res = await userCollectApi.getStoreupList(userInfo.id)
      if (res.code === 200 && res.data) {
        res.data = res.data.filter(item => 
          item.tablename === 'shequhudong' || item.tablename === 'shequhudong_thumbsup'
        )
      }
    } else {
      res = await userCollectApi.getStoreupByTable(userInfo.id, activeCategory.value)
    }
    
    if (res.code === 200) {
      favoritesList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取收藏列表失败')
      favoritesList.value = []
    }
  } catch (error) {
    console.error('获取收藏列表错误:', error)
    ElMessage.error('网络错误，请稍后重试')
    favoritesList.value = []
  } finally {
    loading.value = false
  }
}

const handleCategoryChange = () => {
  getFavoritesList()
}

const handleGoToOriginal = (item) => {
  const tableRouteMap = {
    'yanghuzhishi': '/user/knowledge',
    'shequhudong': '/user/community',
    'shequhudong_thumbsup': '/user/community',
  }
  const path = tableRouteMap[item.tablename]
  if (path) {
    router.push({ path, query: { id: item.spid } })
  } else {
    ElMessage.info('该内容暂无详情页')
  }
}

const handleCancelStoreup = (item) => {
  ElMessageBox.confirm(
    `确定要取消收藏"${item.name}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      await userCollectApi.cancelStoreup(userInfo.id, item.spid, item.tablename)
      
      if (item.tablename === 'yanghuzhishi') {
        await plantKnowledgeApi.decreaseStoreupnum(item.spid)
      }
      
      ElMessage.success('取消收藏成功')
      getFavoritesList()
    } catch (error) {
      console.error('取消收藏失败:', error)
      ElMessage.error('操作失败')
    }
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

const getTableTypeName = (tablename) => {
  const typeMap = {
    'yanghuzhishi': '养护知识',
    'lvzhizhonglei': '绿植种类',
    'yanghujilu': '养护记录',
    'shequhudong': '社区',
    'shequhudong_thumbsup': '社区'
  }
  return typeMap[tablename] || '其他'
}

const getTableTypeColor = (tablename) => {
  const colorMap = {
    'yanghuzhishi': 'success',
    'lvzhizhonglei': 'primary',
    'yanghujilu': 'warning',
    'shequhudong': '',
    'shequhudong_thumbsup': ''
  }
  return colorMap[tablename] || 'info'
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  getFavoritesList()
})
</script>

<style scoped>
.favorites-page {
  min-height: calc(100vh - 120px);
  padding: 30px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 32px;
  color: #2e7d32;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.page-header p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.category-tabs {
  max-width: 800px;
  margin: 0 auto 30px;
  text-align: center;
  background: white;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-radio-button__inner) {
  padding: 12px 30px;
  border-radius: 8px;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  border-color: #4caf50;
}

.favorites-list {
  max-width: 1200px;
  margin: 0 auto;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

.favorite-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.favorite-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.card-image {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.favorite-img {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  font-size: 48px;
  color: #81c784;
}

.card-badge {
  position: absolute;
  top: 12px;
  right: 12px;
}

.card-content {
  padding: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2e7d32;
  margin: 0 0 15px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.card-time {
  font-size: 13px;
  color: #999;
}
</style>
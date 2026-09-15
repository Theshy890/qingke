<template>
  <div class="knowledge-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.plantName"
        placeholder="搜索植物名称..."
        prefix-icon="Search"
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="searchForm.categoryName"
        placeholder="选择绿植种类"
        clearable
        class="category-select"
        @change="handleSearch"
      >
        <el-option label="全部" value="" />
        <el-option label="观叶植物" value="观叶植物" />
        <el-option label="多肉植物" value="多肉植物" />
        <el-option label="观花植物" value="观花植物" />
        <el-option label="水生植物" value="水生植物" />
      </el-select>
      <el-button type="primary" class="search-btn" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
    </div>

    <!-- 养护知识列表 -->
    <div v-loading="loading" class="knowledge-list">
      <el-empty v-if="!loading && knowledgeList.length === 0" description="暂无养护知识" />
      
      <div class="knowledge-grid">
        <div
          v-for="item in knowledgeList"
          :key="item.id"
          class="knowledge-card"
          @click="handleViewDetail(item)"
        >
          <div class="card-image">
            <el-image
              :src="item.imgUrl || defaultImage"
              fit="cover"
              class="knowledge-img"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="card-badge">
              <el-tag type="success" size="small">{{ item.categoryName || '未分类' }}</el-tag>
            </div>
          </div>
          
          <div class="card-content">
            <h3 class="card-title">{{ item.plantName }}</h3>
            <div class="card-desc">
              {{ truncateText(item.maintainTutorial, 80) }}
            </div>
            
            <div class="card-footer">
              <div class="card-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ item.clickNum || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><StarFilled /></el-icon>
                  {{ item.collectNum || 0 }}
                </span>
              </div>
              <div class="card-time">
                {{ formatDate(item.publishDate) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        class="pagination"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentKnowledge.plantName"
      width="800px"
      draggable
      class="detail-dialog resizable-dialog user-cartoon-dialog"
    >
      <div v-if="currentKnowledge.id" class="knowledge-detail">
        <div class="detail-header">
          <el-image
            :src="currentKnowledge.imgUrl || defaultImage"
            fit="cover"
            class="detail-image"
          />
          <div class="detail-info">
            <el-tag type="success" size="large">{{ currentKnowledge.categoryName }}</el-tag>
            <div class="detail-stats">
              <span><el-icon><View /></el-icon> {{ currentKnowledge.clickNum || 0 }} 次浏览</span>
              <span><el-icon><StarFilled /></el-icon> {{ currentKnowledge.collectNum || 0 }} 人收藏</span>
            </div>
            <div class="detail-time">
              发布时间：{{ formatDateTime(currentKnowledge.publishDate) }}
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="detail-content">
          <h3>养护教程</h3>
          <div class="content-text" v-html="formatContent(currentKnowledge.maintainTutorial)"></div>
        </div>
        
        <div v-if="currentKnowledge.videoUrl" class="detail-video">
          <h3>养护视频</h3>
          <video :src="currentKnowledge.videoUrl" controls class="video-player"></video>
        </div>

        <div class="ai-disclaimer">
          <el-icon><Warning /></el-icon>
          内容由AI生成，仅供参考
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button :type="isStored ? 'warning' : 'primary'" @click="handleStoreup">
            <el-icon><Star /></el-icon>
            {{ isStored ? '取消收藏' : '收藏' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search, View, StarFilled, Picture, Star, Warning } from '@element-plus/icons-vue'
import { plantKnowledgeApi, userCollectApi } from '../../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const defaultImage = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const searchForm = reactive({
  plantName: '',
  categoryName: ''
})

const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(false)
const knowledgeList = ref([])

const detailDialogVisible = ref(false)
const currentKnowledge = ref({})
const isStored = ref(false)

const getKnowledgeList = async () => {
  loading.value = true
  try {
    const res = await plantKnowledgeApi.getKnowledgeList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      plantName: searchForm.plantName || undefined,
      categoryName: searchForm.categoryName || undefined
    })
    
    if (res.code === 200 && res.data) {
      const pageInfo = res.data
      knowledgeList.value = pageInfo.list || []
      total.value = pageInfo.total || 0
    } else {
      ElMessage.error(res.message || '获取养护知识失败')
      knowledgeList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取养护知识错误:', error)
    ElMessage.error('网络错误，请稍后重试')
    knowledgeList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  getKnowledgeList()
}

const handleSizeChange = () => {
  getKnowledgeList()
}

const handleCurrentChange = () => {
  getKnowledgeList()
}

const handleViewDetail = async (item) => {
  try {
    const res = await plantKnowledgeApi.getKnowledgeById(item.id)
    if (res.code === 200 && res.data) {
      currentKnowledge.value = res.data
      detailDialogVisible.value = true
      
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      if (userInfo.id) {
        const checkRes = await userCollectApi.checkStoreup(userInfo.id, item.id, 'plant_knowledge')
        isStored.value = checkRes.data || false
      }
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 根据ID加载详情（从路由参数）
const loadDetailById = async (id) => {
  if (!id) return
  
  try {
    const res = await plantKnowledgeApi.getKnowledgeById(id)
    if (res.code === 200 && res.data) {
      currentKnowledge.value = res.data
      detailDialogVisible.value = true
      
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      if (userInfo.id) {
        const checkRes = await userCollectApi.checkStoreup(userInfo.id, id, 'plant_knowledge')
        isStored.value = checkRes.data || false
      }
    } else {
      ElMessage.error('未找到该养护知识')
    }
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载详情失败')
  }
}

const handleStoreup = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) {
      ElMessage.warning('请先登录')
      return
    }
    
    if (isStored.value) {
      await userCollectApi.cancelStoreup(userInfo.id, currentKnowledge.value.id, 'plant_knowledge')
      await plantKnowledgeApi.decreaseStoreupnum(currentKnowledge.value.id)
      ElMessage.success('取消收藏成功')
      isStored.value = false
      currentKnowledge.value.collectNum = (currentKnowledge.value.collectNum || 1) - 1
    } else {
      const storeupData = {
        userId: userInfo.id,
        targetId: currentKnowledge.value.id,
        targetTable: 'plant_knowledge',
        targetName: currentKnowledge.value.plantName,
        imgUrl: currentKnowledge.value.imgUrl,
        targetType: '1'
      }
      await userCollectApi.addStoreup(storeupData)
      await plantKnowledgeApi.increaseStoreupnum(currentKnowledge.value.id)
      ElMessage.success('收藏成功')
      isStored.value = true
      currentKnowledge.value.collectNum = (currentKnowledge.value.collectNum || 0) + 1
    }
    
    getKnowledgeList()
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const truncateText = (text, maxLength) => {
  if (!text) return '暂无介绍'
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const formatContent = (content) => {
  if (!content) return '暂无内容'
  return content.replace(/\n/g, '<br>')
}

onMounted(async () => {
  await getKnowledgeList()
  
  // 检查是否有路由参数传递的ID
  const knowledgeId = route.query.id
  if (knowledgeId) {
    setTimeout(() => {
      loadDetailById(Number(knowledgeId))
    }, 300)
  }
})

// 监听路由参数变化（组件已挂载时再次导航）
watch(() => route.query.id, (newId) => {
  if (newId) {
    loadDetailById(Number(newId))
  }
})
</script>

<style scoped>
.knowledge-page {
  min-height: calc(100vh - 120px);
  padding: 30px 20px;
}

.search-bar {
  max-width: 900px;
  margin: 0 auto 30px;
  display: flex;
  gap: 15px;
  background: white;
  padding: 20px;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.search-input {
  flex: 1;
}

.category-select {
  width: 180px;
}

.search-btn {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  border: none;
  color: white;
  padding: 0 30px;
}

.knowledge-list {
  max-width: 1400px;
  margin: 0 auto;
}

.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 30px;
}

.knowledge-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
}

.knowledge-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.card-image {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
}

.knowledge-img {
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
  padding: 14px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2e7d32;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
  min-height: 58px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.card-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #888;
}

.stat-item .el-icon {
  font-size: 14px;
}

.card-time {
  font-size: 12px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
}

.detail-dialog {
  border-radius: 16px;
}

.knowledge-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.detail-image {
  width: 40%;
  min-width: 200px;
  max-width: 300px;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.detail-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.detail-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.detail-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.detail-time {
  font-size: 13px;
  color: #999;
}

.detail-content {
  margin-top: 20px;
}

.detail-content h3 {
  font-size: 18px;
  color: #2e7d32;
  margin-bottom: 15px;
}

.content-text {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}

.detail-video {
  margin-top: 30px;
}

.detail-video h3 {
  font-size: 18px;
  color: #2e7d32;
  margin-bottom: 15px;
}

.video-player {
  width: 100%;
  border-radius: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.ai-disclaimer {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 24px;
  padding: 12px 16px;
  background: #fff3e0;
  border-left: 3px solid #ff9800;
  border-radius: 4px;
  font-size: 13px;
  color: #e65100;
}

.ai-disclaimer .el-icon {
  font-size: 16px;
}
</style>
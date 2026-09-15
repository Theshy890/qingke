<template>
  <div class="community-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="•" class="breadcrumb">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>社区互动管理</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="发布账号">
          <el-input v-model="searchForm.userAccount" placeholder="请输入账号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="请选择" clearable style="width: 150px">
            <el-option label="未审核" value="0" />
            <el-option-group label="已审核">
              <el-option label="审核通过" value="1" />
              <el-option label="审核不通过" value="2" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" class="search-btn" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" class="transparent-btn" @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="danger" plain class="transparent-btn" @click="handleBatchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total-icon">
              <el-icon :size="32"><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ total }}</div>
              <div class="stat-label">总帖子数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending-icon">
              <el-icon :size="32"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ pendingCount }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon approved-icon">
              <el-icon :size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ approvedCount }}</div>
              <div class="stat-label">已审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon hot-icon">
              <el-icon :size="32"><TrophyBase /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ hotCount }}</div>
              <div class="stat-label">热门帖子</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table
        :data="tableData"
        :loading="loading"
        style="width: 100%"
        stripe
        header-cell-class-name="table-header"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tupian" label="封面图片" width="100">
          <template #default="{ row }">
            <el-image
              style="width: 60px; height: 60px; border-radius: 4px"
              :src="row.imgUrl || defaultImage"
              fit="cover"
              preview-teleported
              :preview-src-list="[row.imgUrl || defaultImage]"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="biaoti" label="标题" min-width="200" show-overflow-tooltip sortable />
        <el-table-column prop="zhanghao" label="发布账号" width="120" sortable />
        <el-table-column prop="fabushijian" label="发布时间" width="160" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="thumbsupnum" label="点赞" width="80" sortable>
          <template #default="{ row }">
            <el-tag type="success" size="small">
              <el-icon><CaretTop /></el-icon> {{ row.likeCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discussnum" label="评论" width="80" sortable>
          <template #default="{ row }">
            <el-tag type="info" size="small">
              <el-icon><ChatDotRound /></el-icon> {{ row.commentCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sfsh" label="审核状态" width="140" sortable>
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 1" type="success" size="small">已审核（通过）</el-tag>
            <el-tag v-else-if="row.auditStatus === 2" type="danger" size="small">已审核（不通过）</el-tag>
            <el-tag v-else type="warning" size="small">待审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" plain size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" plain size="small" @click="handleAudit(row)">审核</el-button>
            <el-button type="danger" plain size="small" class="transparent-btn" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="帖子详情"
      width="900px"
      :close-on-click-modal="false"
      draggable
    >
      <div class="detail-content">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-descriptions :column="2" border size="large">
              <el-descriptions-item label="帖子ID">
                {{ currentRecord.id }}
              </el-descriptions-item>
              <el-descriptions-item label="发布账号">
                {{ currentRecord.userAccount }}
              </el-descriptions-item>
              <el-descriptions-item label="标题" :span="2">
                <el-tag type="primary">{{ currentRecord.title }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="发布时间">
                {{ formatDateTime(currentRecord.publishTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="审核状态">
                <el-tag v-if="currentRecord.auditStatus === 1" type="success">已审核（通过）</el-tag>
                <el-tag v-else-if="currentRecord.auditStatus === 2" type="danger">已审核（不通过）</el-tag>
                <el-tag v-else type="warning">待审核</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="点赞数">
                <el-tag type="success">{{ currentRecord.likeCount || 0 }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="点踩数">
                <el-tag type="danger">{{ currentRecord.dislikeCount || 0 }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="评论数" :span="2">
                <el-tag type="info">{{ currentRecord.commentCount || 0 }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>

        <el-divider content-position="left">封面图片</el-divider>
        <div class="detail-image-box">
          <el-image
            :src="currentRecord.imgUrl || defaultImage"
            fit="contain"
            class="detail-image"
            preview-teleported
            :preview-src-list="[currentRecord.imgUrl || defaultImage]"
          >
            <template #error>
              <div class="image-error-large">
                <el-icon :size="64"><Picture /></el-icon>
                <p>图片加载失败</p>
              </div>
            </template>
          </el-image>
        </div>

        <el-divider content-position="left">帖子内容</el-divider>
        <div class="detail-text" v-html="currentRecord.content || '暂无内容'"></div>

        <el-divider v-if="currentRecord.auditReply" content-position="left">审核回复</el-divider>
        <div v-if="currentRecord.auditReply" class="detail-text audit-reply">
          {{ currentRecord.auditReply }}
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button class="transparent-btn" @click="dialogVisible = false">关闭</el-button>
          <el-button type="success" @click="handleAuditFromDialog">审核</el-button>
          <el-button type="danger" plain class="transparent-btn" @click="handleDeleteFromDialog">删除</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="审核帖子"
      width="500px"
      :close-on-click-modal="false"
      draggable
    >
      <el-form :model="auditForm" :rules="auditRules" ref="auditFormRef" label-width="100px">
        <el-form-item label="审核结果" prop="sfsh">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio label="1">通过</el-radio>
            <el-radio label="2">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核回复" prop="shhf">
          <el-input
            v-model="auditForm.auditReply"
            type="textarea"
            :rows="4"
            placeholder="请输入审核回复（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button class="transparent-btn" @click="auditDialogVisible = false">取消</el-button>
          <el-button class="transparent-btn" @click="resetAuditForm">重置</el-button>
          <el-button type="primary" @click="submitAudit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Download, Picture, ChatDotRound, Clock, CircleCheck, TrophyBase, CaretTop } from '@element-plus/icons-vue'
import { communityPostApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const defaultImage = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const searchForm = ref({
  title: '',
  userAccount: '',
  auditStatus: ''
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const allRecords = ref([])
const loading = ref(false)
const selectedIds = ref([])

const dialogVisible = ref(false)
const currentRecord = reactive({
  id: null,
  title: '',
  imgUrl: '',
  userAccount: '',
  content: '',
  publishTime: null,
  auditStatus: '',
  auditReply: '',
  likeCount: 0,
  dislikeCount: 0,
  commentCount: 0
})

const auditDialogVisible = ref(false)
const auditFormRef = ref(null)
const auditForm = reactive({
  id: null,
  auditStatus: '1',
  auditReply: ''
})

const auditRules = {
  auditStatus: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
}

const resetAuditForm = () => {
  auditForm.auditStatus = '1'
  auditForm.auditReply = ''
  auditFormRef.value?.clearValidate()
}

// 统计数据
const pendingCount = computed(() => {
  return allRecords.value.filter(r => {
    const sfsh = r.auditStatus === null || r.auditStatus === undefined ? '' : String(r.auditStatus)
    return sfsh === '' || sfsh === '0' || !sfsh
  }).length
})

const approvedCount = computed(() => {
  return allRecords.value.filter(r => {
    const sfsh = r.auditStatus === null || r.auditStatus === undefined ? '' : String(r.auditStatus)
    return sfsh === '1' || sfsh === '2' || sfsh === '已审核'
  }).length
})

const hotCount = computed(() => {
  // 热门帖子：点赞数 >= 10 或 评论数 >= 5
  return allRecords.value.filter(r => 
    (r.likeCount && r.likeCount >= 10) || (r.commentCount && r.commentCount >= 5)
  ).length
})

// 获取社区互动列表
const getCommunityList = async () => {
  loading.value = true
  try {
    const res = await communityPostApi.getAllCommunity()
    
    if (res && res.code === 200) {
      let records = res.data || []
      allRecords.value = records
      
      // 前端过滤搜索条件
      if (searchForm.value.title) {
        records = records.filter(item => 
          item.title && item.title.includes(searchForm.value.title)
        )
      }
      if (searchForm.value.userAccount) {
        records = records.filter(item => 
          item.userAccount && item.userAccount.includes(searchForm.value.userAccount)
        )
      }
      if (searchForm.value.auditStatus !== '' && searchForm.value.auditStatus !== null && searchForm.value.auditStatus !== undefined) {
        records = records.filter(item => {
          // 转换为字符串进行比较
          const itemSfsh = item.auditStatus === null || item.auditStatus === undefined ? '' : String(item.auditStatus)
          const searchSfsh = String(searchForm.value.auditStatus)
          
          // 未审核：sfsh 为空、'0'、null 或 undefined
          if (searchSfsh === '0') {
            return itemSfsh === '' || itemSfsh === '0' || !itemSfsh
          }
          // 审核通过：'1'
          if (searchSfsh === '1') {
            return itemSfsh === '1'
          }
          // 审核不通过：'2'
          if (searchSfsh === '2') {
            return itemSfsh === '2'
          }
          // 其他情况（兼容旧数据）
          return itemSfsh === searchSfsh
        })
      }
      
      // 前端分页
      total.value = records.length
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      tableData.value = records.slice(start, end)
      
    } else {
      ElMessage.error(res.message || '获取数据失败')
      tableData.value = []
      allRecords.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取社区互动列表失败:', error)
    ElMessage.error('加载失败: ' + (error.message || '未知错误'))
    tableData.value = []
    allRecords.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  getCommunityList()
}

// 重置
const handleReset = () => {
  searchForm.value.title = ''
  searchForm.value.userAccount = ''
  searchForm.value.auditStatus = ''
  currentPage.value = 1
  getCommunityList()
}

// 分页大小变更
const handleSizeChange = () => {
  currentPage.value = 1
  getCommunityList()
}

// 页码变更
const handleCurrentChange = () => {
  getCommunityList()
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 查看详情
const handleView = (row) => {
  Object.assign(currentRecord, {
    id: row.id,
    title: row.title,
    imgUrl: row.imgUrl,
    userAccount: row.userAccount,
    content: row.content,
    publishTime: row.publishTime,
    auditStatus: row.auditStatus,
    auditReply: row.auditReply,
    likeCount: row.likeCount,
    dislikeCount: row.dislikeCount,
    commentCount: row.commentCount
  })
  dialogVisible.value = true
}

// 审核
const handleAudit = (row) => {
  auditForm.id = row.id
  auditForm.auditStatus = '1'
  auditForm.auditReply = ''
  auditDialogVisible.value = true
}

// 从详情对话框审核
const handleAuditFromDialog = () => {
  dialogVisible.value = false
  handleAudit(currentRecord)
}

// 提交审核
const submitAudit = async () => {
  if (!auditFormRef.value) return
  
  await auditFormRef.value.validate()
  
  try {
    const res = await communityPostApi.auditCommunity(
      auditForm.id,
      auditForm.auditStatus,
      auditForm.auditReply
    )
    
    if (res.code === 200) {
      ElMessage.success('审核成功')
      auditDialogVisible.value = false
      getCommunityList()
    } else {
      ElMessage.error(res.msg || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除"${row.title || '该'}"帖子吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await communityPostApi.deleteCommunity(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getCommunityList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 从详情对话框删除
const handleDeleteFromDialog = () => {
  handleDelete(currentRecord)
  dialogVisible.value = false
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的数据')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedIds.value.length} 条帖子吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await communityPostApi.deleteCommunityBatch(selectedIds.value)
      if (res.code === 200) {
        ElMessage.success('批量删除成功')
        selectedIds.value = []
        getCommunityList()
      } else {
        ElMessage.error(res.msg || '批量删除失败')
      }
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  getCommunityList()
})
</script>

<style scoped>
.community-page {
  width: 100%;
}

.breadcrumb {
  background: linear-gradient(135deg, #5a8f7b 0%, #4a7765 100%);
  padding: 15px 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

:deep(.breadcrumb .el-breadcrumb__item) {
  color: white;
}

:deep(.breadcrumb .el-breadcrumb__inner) {
  color: white;
  font-weight: normal;
}

:deep(.breadcrumb .el-breadcrumb__separator) {
  color: white;
}

.search-card {
  margin-bottom: 20px;
}

:deep(.search-card .el-card__body) {
  padding: 15px 20px;
}

.search-btn {
  background: linear-gradient(135deg, #5a8f7b 0%, #4a7765 100%);
  border: none;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.total-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.pending-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.approved-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.hot-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}


:deep(.table-header) {
  background: linear-gradient(135deg, #5a8f7b 0%, #4a7765 100%) !important;
  color: white !important;
  font-weight: 500;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table tbody tr:hover > td) {
  background-color: rgba(0, 191, 165, 0.05) !important;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: rgba(0, 191, 165, 0.04);
  color: #909399;
  font-size: 24px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.detail-content {
  padding: 10px;
}

.detail-image-box {
  max-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 191, 165, 0.04);
  border-radius: 8px;
  overflow: hidden;
}

.detail-image {
  width: 100%;
  max-height: 400px;
}

.image-error-large {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  padding: 60px;
}

.image-error-large p {
  margin-top: 10px;
  font-size: 14px;
}

.detail-text {
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
  line-height: 1.8;
  color: #333;
  min-height: 100px;
  white-space: pre-wrap;
}

.audit-reply {
  background: #fff3e0;
  border-left: 4px solid #ff9800;
}
</style>
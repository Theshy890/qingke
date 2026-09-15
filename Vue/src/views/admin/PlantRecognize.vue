<template>
  <div class="plant-recognition-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="•" class="breadcrumb">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>绿植识别记录</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="绿植名称">
          <el-input v-model="searchForm.plantName" placeholder="绿植名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="病害名称">
          <el-input v-model="searchForm.diseaseName" placeholder="病害名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="用户ID" clearable style="width: 150px" type="number" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" class="search-btn" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" class="transparent-btn" @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="danger" plain class="transparent-btn" @click="handleBatchDelete" :disabled="selectedIds.length === 0">删除</el-button>
          <el-button type="success" plain :icon="Download" @click="handleExport" :loading="exporting">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total-icon">
              <el-icon :size="32"><Histogram /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ total }}</div>
              <div class="stat-label">总识别次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon healthy-icon">
              <el-icon :size="32"><SuccessFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ healthyCount }}</div>
              <div class="stat-label">健康植物</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon disease-icon">
              <el-icon :size="32"><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ diseaseCount }}</div>
              <div class="stat-label">检测到病害</div>
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
        <el-table-column prop="imgUrl" label="识别图片" width="100">
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
        <el-table-column prop="plantName" label="绿植名称" min-width="120" sortable />
        <el-table-column prop="diseaseName" label="病害名称" min-width="120" sortable>
          <template #default="{ row }">
            <el-tag v-if="row.diseaseName === '健康' || (row.diseaseName && row.diseaseName.includes('无明显病害'))" type="success" size="small">
              {{ row.diseaseName.includes('无明显病害') ? '无明显病害' : '健康' }}
            </el-tag>
            <el-tag v-else-if="row.diseaseName" type="danger" size="small">
              {{ row.diseaseName }}
            </el-tag>
            <el-tag v-else type="info" size="small">
              未识别
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diseaseDesc" label="病害描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="100" sortable />
        <el-table-column prop="createTime" label="识别时间" min-width="150" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" plain size="small" @click="handleView(row)">查看</el-button>
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
      title="识别详情"
      width="900px"
      :close-on-click-modal="false"
      draggable
    >
      <div class="detail-content scrollable-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-image-box">
              <el-image
                :src="currentRecord.imgUrl || defaultImage"
                fit="contain"
                class="detail-image"
              >
                <template #error>
                  <div class="image-error-large">
                    <el-icon :size="64"><Picture /></el-icon>
                    <p>图片加载失败</p>
                  </div>
                </template>
              </el-image>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-info-box">
              <el-descriptions :column="1" border size="large">
                <el-descriptions-item label="识别ID">
                  {{ currentRecord.id }}
                </el-descriptions-item>
                <el-descriptions-item label="用户ID">
                  {{ currentRecord.userId }}
                </el-descriptions-item>
                <el-descriptions-item label="绿植名称">
                  <el-tag type="success">{{ currentRecord.plantName || '未知' }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="病害名称">
                  <el-tag v-if="currentRecord.diseaseName === '健康' || (currentRecord.diseaseName && currentRecord.diseaseName.includes('无明显病害'))" type="success">
                    {{ currentRecord.diseaseName.includes('无明显病害') ? '无明显病害' : '健康' }}
                  </el-tag>
                  <el-tag v-else-if="currentRecord.diseaseName" type="danger">
                    {{ currentRecord.diseaseName }}
                  </el-tag>
                  <el-tag v-else type="info">
                    未识别
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="识别时间">
                  {{ formatDateTime(currentRecord.createTime) }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
        </el-row>

        <el-divider content-position="left">植物介绍</el-divider>
        <div class="detail-text">
          {{ currentRecord.plantIntro || '暂无植物介绍' }}
        </div>

        <el-divider content-position="left">病害描述</el-divider>
        <div class="detail-text">
          {{ currentRecord.diseaseDesc || '暂无病害描述' }}
        </div>

        <el-divider content-position="left">治疗建议</el-divider>
        <div class="detail-text">
          {{ currentRecord.treatSuggest || '暂无治疗建议' }}
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button class="transparent-btn" @click="dialogVisible = false">关闭</el-button>
          <el-button type="danger" plain class="transparent-btn" @click="handleDeleteFromDialog">删除此记录</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Download, Picture, Histogram, SuccessFilled, WarningFilled } from '@element-plus/icons-vue'
import { aiApi } from '../../api'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { exportToExcel, formatDateTime as formatDateTimeUtil } from '../../utils/exportExcel'

const defaultImage = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const searchForm = ref({
  plantName: '',
  diseaseName: '',
  userId: ''
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const allRecords = ref([])
const loading = ref(false)
const selectedIds = ref([])
const selectedRows = ref([])
const exporting = ref(false)

const dialogVisible = ref(false)
const currentRecord = reactive({
  id: null,
  plantName: '',
  imgUrl: '',
  diseaseName: '',
  diseaseDesc: '',
  plantIntro: '',
  treatSuggest: '',
  userId: null,
  createTime: null
})

// 统计数据
const healthyCount = computed(() => {
  return allRecords.value.filter(r => {
    const disease = r.diseaseName || ''
    // 健康植物：病害名称为"健康"或包含"无明显病害"
    return disease === '健康' || disease.includes('无明显病害')
  }).length
})

const diseaseCount = computed(() => {
  return allRecords.value.filter(r => {
    const disease = r.diseaseName || ''
    // 检测到病害：病害名称存在且不是健康状态
    return disease && disease !== '健康' && !disease.includes('无明显病害')
  }).length
})

// 获取识别记录列表（管理员查看所有记录）
const getRecordList = async () => {
  loading.value = true
  try {
    const res = await aiApi.getAllRecords()
    
    if (res && res.code === 200) {
      let records = res.data || []
      allRecords.value = records
      
      // 前端过滤搜索条件
      if (searchForm.value.plantName) {
        records = records.filter(item => 
          item.plantName && item.plantName.includes(searchForm.value.plantName)
        )
      }
      if (searchForm.value.diseaseName) {
        records = records.filter(item => 
          item.diseaseName && item.diseaseName.includes(searchForm.value.diseaseName)
        )
      }
      if (searchForm.value.userId) {
        records = records.filter(item => 
          item.userId && item.userId.toString() === searchForm.value.userId
        )
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
    console.error('获取识别记录列表失败:', error)
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
  getRecordList()
}

// 重置
const handleReset = () => {
  searchForm.value.plantName = ''
  searchForm.value.diseaseName = ''
  searchForm.value.userId = ''
  currentPage.value = 1
  getRecordList()
}

// 分页大小变更
const handleSizeChange = () => {
  currentPage.value = 1
  getRecordList()
}

// 页码变更
const handleCurrentChange = () => {
  getRecordList()
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
  selectedRows.value = selection
}

// 查看详情
const handleView = (row) => {
  Object.assign(currentRecord, {
    id: row.id,
    plantName: row.plantName,
    imgUrl: row.imgUrl,
    diseaseName: row.diseaseName,
    diseaseDesc: row.diseaseDesc,
    plantIntro: row.plantIntro,
    treatSuggest: row.treatSuggest,
    userId: row.userId,
    createTime: row.createTime
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除“${row.plantName || '该'}”的识别记录吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await aiApi.deleteRecord(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getRecordList()
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
    `确定要删除选中的 ${selectedIds.value.length} 条识别记录吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await aiApi.deleteBatch(selectedIds.value)
      if (res.code === 200) {
        ElMessage.success('批量删除成功')
        selectedIds.value = []
        getRecordList()
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

// 导出Excel
const handleExport = async () => {
  // 检查是否选中数据
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要导出的数据')
    return
  }
  
  try {
    exporting.value = true
    
    // 显示加载提示
    const loadingInstance = ElLoading.service({
      lock: true,
      text: '正在导出数据，请稍候...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    // 使用选中的数据进行导出
    const exportData = selectedRows.value
    
    // 格式化导出数据
    const formattedData = exportData.map(item => {
      // 判断病害状态
      let diseaseStatus = '未识别'
      if (item.binghaiming) {
        if (item.binghaiming === '健康' || item.binghaiming.includes('无明显病害')) {
          diseaseStatus = item.binghaiming.includes('无明显病害') ? '无明显病害' : '健康'
        } else {
          diseaseStatus = item.binghaiming
        }
      }
      
      return {
        识别ID: item.id || '-',
        用户ID: item.userId || '-',
        绿植名称: item.plantName || '-',
        病害名称: diseaseStatus,
        病害描述: item.diseaseDesc || '-',
        植物介绍: item.plantIntro || '-',
        治疗建议: item.treatSuggest || '-',
        识别时间: formatDateTimeUtil(item.createTime),
        图片链接: item.imgUrl || '-'
      }
    })
    
    // 定义列配置
    const columns = [
      { header: '识别ID', key: '识别ID', width: 10 },
      { header: '用户ID', key: '用户ID', width: 12 },
      { header: '绿植名称', key: '绿植名称', width: 20 },
      { header: '病害名称', key: '病害名称', width: 20 },
      { header: '病害描述', key: '病害描述', width: 35 },
      { header: '植物介绍', key: '植物介绍', width: 40 },
      { header: '治疗建议', key: '治疗建议', width: 45 },
      { header: '识别时间', key: '识别时间', width: 20 },
      { header: '图片链接', key: '图片链接', width: 50 }
    ]
    
    // 调用导出函数
    await exportToExcel(formattedData, '绿植识别记录', {
      columns,
      sheetName: '识别记录'
    })
    
    ElMessage.success(`成功导出 ${exportData.length} 条识别记录！`)
    
    loadingInstance.close()
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败: ' + (error.message || '未知错误'))
  } finally {
    exporting.value = false
  }
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  getRecordList()
})
</script>

<style scoped>
.plant-recognition-page {
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

.healthy-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.disease-icon {
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

/* 弹窗内容区域：全局 global.css 强制 dialog body 及其首个子 div 为
   flex 列布局(flex:1; min-height:0)，会压缩子元素导致重叠。
   这里让内容区可滚动，并阻止子元素被压缩(flex-shrink:0)。 */
.detail-content {
  padding: 10px;
  overflow-y: auto;
}

/* 关键修复：阻止 flex 列布局压缩子元素，保持各块自然高度 */
.detail-content > * {
  flex-shrink: 0;
}

/* 滚动条美化 */
.scrollable-content::-webkit-scrollbar {
  width: 8px;
}

.scrollable-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.scrollable-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  transition: all 0.3s;
}

.scrollable-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

.detail-image-box {
  width: 100%;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 191, 165, 0.04);
  border-radius: 8px;
  overflow: hidden;
}

.detail-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.image-error-large {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.image-error-large p {
  margin-top: 10px;
  font-size: 14px;
}

.detail-info-box {
  width: 100%;
}

.detail-text {
  padding: 15px;
  background: rgba(0, 191, 165, 0.06);
  border-radius: 4px;
  line-height: 1.8;
  min-height: 60px;
  word-wrap: break-word;
  word-break: break-word;
  white-space: pre-wrap;
}
</style>
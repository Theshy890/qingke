<template>
  <div class="care-records-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="•" class="breadcrumb">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>养护记录</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="绿植名称">
          <el-input v-model="searchForm.plantName" placeholder="绿植名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="searchForm.userAccount" placeholder="用户账号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="绿植种类">
          <el-select v-model="searchForm.categoryName" placeholder="请选择绿植种类" clearable style="width: 150px">
            <el-option label="观叶植物" value="观叶植物" />
            <el-option label="多肉植物" value="多肉植物" />
            <el-option label="观花植物" value="观花植物" />
            <el-option label="水生植物" value="水生植物" />
          </el-select>
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
        <el-table-column prop="id" label="序号" width="80" />
        <el-table-column prop="plantName" label="绿植名称" min-width="120" sortable />
        <el-table-column prop="categoryName" label="绿植种类" min-width="100" sortable />
        <el-table-column prop="imgUrl" label="绿植图片" width="100">
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
        <el-table-column prop="userAccount" label="账号" min-width="100" sortable />
        <el-table-column prop="growStatus" label="生长状态" min-width="100" />
        <el-table-column prop="maintainDate" label="养护日期" min-width="120" sortable />
        <el-table-column prop="maintainCycle" label="养护周期" min-width="100" />
        <el-table-column prop="nextMaintainTime" label="下次养护时间" min-width="150" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.nextMaintainTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" plain size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" plain size="small" @click="handleEdit(row)">修改</el-button>
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

    <!-- 查看/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
      draggable
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="绿植名称" prop="plantName">
              <el-input
                v-model="formData.plantName"
                placeholder="请输入绿植名称"
                :disabled="dialogMode === 'view'"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="绿植种类" prop="categoryName">
              <el-select
                v-model="formData.categoryName"
                placeholder="请选择绿植种类"
                :disabled="dialogMode === 'view'"
                style="width: 100%"
              >
                <el-option label="观叶植物" value="观叶植物" />
                <el-option label="多肉植物" value="多肉植物" />
                <el-option label="观花植物" value="观花植物" />
                <el-option label="水生植物" value="水生植物" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="账号" prop="userAccount">
              <el-input
                v-model="formData.userAccount"
                placeholder="请输入用户账号"
                :disabled="true"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生长状态" prop="growStatus">
              <el-input
                v-model="formData.growStatus"
                placeholder="如：健康生长、叶片发黄等"
                :disabled="dialogMode === 'view'"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="绿植图片URL" prop="imgUrl">
          <el-input
            v-model="formData.imgUrl"
            placeholder="请输入绿植图片URL"
            :disabled="dialogMode === 'view'"
            clearable
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="养护日期" prop="maintainDate">
              <el-date-picker
                v-model="formData.maintainDate"
                type="date"
                placeholder="选择养护日期"
                :disabled="dialogMode === 'view'"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="养护周期" prop="maintainCycle">
              <el-input
                v-model="formData.maintainCycle"
                placeholder="如：每7天、每月"
                :disabled="dialogMode === 'view'"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="下次养护时间" prop="nextMaintainTime">
          <el-date-picker
            v-model="formData.nextMaintainTime"
            type="datetime"
            placeholder="选择下次养护时间"
            :disabled="dialogMode === 'view'"
            :disabled-date="disabledNextDate"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="养护记录" prop="maintainContent">
          <el-input
            v-model="formData.maintainContent"
            type="textarea"
            :rows="4"
            placeholder="请输入养护记录详情"
            :disabled="dialogMode === 'view'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button class="transparent-btn" @click="dialogVisible = false">取消</el-button>
          <el-button class="transparent-btn" @click="handleResetForm" v-if="dialogMode !== 'view'">重置</el-button>
          <el-button type="primary" @click="handleSubmit" v-if="dialogMode !== 'view'" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Download, Picture } from '@element-plus/icons-vue'
import { plantMaintainRecordApi } from '../../api'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { exportToExcel, formatDateTime as formatDateTimeUtil, formatDate } from '../../utils/exportExcel'

const defaultImage = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const searchForm = ref({
  plantName: '',
  userAccount: '',
  categoryName: ''
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const selectedIds = ref([])
const selectedRows = ref([])
const exporting = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref('view') // view, edit
const submitting = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  plantName: '',
  categoryName: '',
  imgUrl: '',
  userAccount: '',
  growStatus: '',
  maintainContent: '',
  maintainDate: '',
  maintainCycle: '',
  nextMaintainTime: ''
})

// 限制下次养护时间：只能选当前时间 ~ 2100年
const disabledNextDate = (time) => {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  const maxDate = new Date('2100-12-31')
  return time.getTime() < now.getTime() || time.getTime() > maxDate.getTime()
}

const formRules = {
  plantName: [
    { required: true, message: '请输入绿植名称', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '请选择绿植种类', trigger: 'change' }
  ],
  growStatus: [
    { required: true, message: '请输入生长状态', trigger: 'blur' }
  ],
  maintainContent: [
    { required: true, message: '请输入养护记录', trigger: 'blur' }
  ],
  maintainDate: [
    { required: true, message: '请选择养护日期', trigger: 'change' }
  ]
}

const handleResetForm = () => {
  formRef.value?.resetFields()
}

// 获取养护记录列表（管理员查看所有记录）
const getRecordList = async () => {
  loading.value = true
  try {
    // 管理员获取所有养护记录
    const res = await plantMaintainRecordApi.getAll()
    
    if (res && res.code === 200) {
      let records = res.data || []
      
      // 前端过滤搜索条件
      if (searchForm.value.plantName) {
        records = records.filter(item => 
          item.plantName && item.plantName.includes(searchForm.value.plantName)
        )
      }
      if (searchForm.value.userAccount) {
        records = records.filter(item => 
          item.userAccount && item.userAccount.includes(searchForm.value.userAccount)
        )
      }
      if (searchForm.value.categoryName) {
        records = records.filter(item => 
          item.categoryName === searchForm.value.categoryName
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
      total.value = 0
    }
  } catch (error) {
    console.error('获取养护记录列表失败:', error)
    ElMessage.error('加载失败: ' + (error.message || '未知错误'))
    tableData.value = []
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
  searchForm.value.userAccount = ''
  searchForm.value.categoryName = ''
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

// 查看
const handleView = (row) => {
  Object.assign(formData, {
    id: row.id,
    plantName: row.plantName,
    categoryName: row.categoryName,
    imgUrl: row.imgUrl,
    userAccount: row.userAccount,
    growStatus: row.growStatus,
    maintainContent: row.maintainContent,
    maintainDate: row.maintainDate,
    maintainCycle: row.maintainCycle,
    nextMaintainTime: row.nextMaintainTime
  })
  dialogMode.value = 'view'
  dialogTitle.value = '查看养护记录'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  Object.assign(formData, {
    id: row.id,
    plantName: row.plantName,
    categoryName: row.categoryName,
    imgUrl: row.imgUrl,
    userAccount: row.userAccount,
    growStatus: row.growStatus,
    maintainContent: row.maintainContent,
    maintainDate: row.maintainDate,
    maintainCycle: row.maintainCycle,
    nextMaintainTime: row.nextMaintainTime
  })
  dialogMode.value = 'edit'
  dialogTitle.value = '修改养护记录'
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const res = await plantMaintainRecordApi.update(formData)
    if (res.code === 200) {
      ElMessage.success('修改成功')
      dialogVisible.value = false
      getRecordList()
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    submitting.value = false
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除"${row.plantName}"的养护记录吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await plantMaintainRecordApi.deleteById(row.id)
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

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的数据')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedIds.value.length} 条养护记录吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await plantMaintainRecordApi.deleteBatch(selectedIds.value)
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
    const formattedData = exportData.map(item => ({
      序号: item.id || '-',
      绿植名称: item.plantName || '-',
      绿植种类: item.categoryName || '-',
      用户账号: item.userAccount || '-',
      生长状态: item.growStatus || '-',
      养护记录: item.maintainContent || '-',
      养护日期: formatDate(item.maintainDate),
      养护周期: item.maintainCycle || '-',
      下次养护时间: formatDateTimeUtil(item.nextMaintainTime),
      绿植图片: item.imgUrl || '-'
    }))
    
    // 定义列配置
    const columns = [
      { header: '序号', key: '序号', width: 10 },
      { header: '绿植名称', key: '绿植名称', width: 20 },
      { header: '绿植种类', key: '绿植种类', width: 15 },
      { header: '用户账号', key: '用户账号', width: 15 },
      { header: '生长状态', key: '生长状态', width: 20 },
      { header: '养护记录', key: '养护记录', width: 40 },
      { header: '养护日期', key: '养护日期', width: 15 },
      { header: '养护周期', key: '养护周期', width: 15 },
      { header: '下次养护时间', key: '下次养护时间', width: 20 },
      { header: '绿植图片', key: '绿植图片', width: 50 }
    ]
    
    // 调用导出函数
    await exportToExcel(formattedData, '养护记录数据', {
      columns,
      sheetName: '养护记录'
    })
    
    ElMessage.success(`成功导出 ${exportData.length} 条养护记录！`)
    
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
.care-records-page {
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
</style>
<template>
  <div class="care-tips-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="•" class="breadcrumb">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>养护知识</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="植物名称">
          <el-input v-model="searchForm.plantName" placeholder="植物名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="绿植种类">
          <el-select v-model="searchForm.categoryName" placeholder="请选择绿植种类" clearable style="width: 200px">
            <el-option label="观叶植物" value="观叶植物" />
            <el-option label="多肉植物" value="多肉植物" />
            <el-option label="观花植物" value="观花植物" />
            <el-option label="水生植物" value="水生植物" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" class="search-btn" @click="handleSearch">查询</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" plain @click="handleAdd">添加</el-button>
          <el-button type="danger" plain class="transparent-btn" @click="handleBatchDelete" :disabled="selectedIds.length === 0">删除</el-button>
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
        <el-table-column prop="plantName" label="植物名称" min-width="120" sortable />
        <el-table-column prop="categoryName" label="绿植种类" min-width="120" sortable />
        <el-table-column prop="imgUrl" label="图片" width="120">
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
        <el-table-column prop="maintainTutorial" label="养护教程" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fabushijian" label="发布时间" min-width="150" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.publishDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="clickNum" label="点击次数" width="100" sortable />
        <el-table-column prop="collectNum" label="收藏数量" width="100" sortable />
        <el-table-column label="操作" width="280" fixed="right">
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

    <!-- 添加/编辑/查看对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
      draggable
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="植物名称" prop="plantName">
              <el-input
                v-model="formData.plantName"
                placeholder="请输入植物名称"
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

        <el-form-item label="图片URL" prop="imgUrl">
          <el-input
            v-model="formData.imgUrl"
            placeholder="请输入图片URL"
            :disabled="dialogMode === 'view'"
            clearable
          />
        </el-form-item>

        <el-form-item label="视频URL" prop="videoUrl">
          <el-input
            v-model="formData.videoUrl"
            placeholder="请输入视频URL（可选）"
            :disabled="dialogMode === 'view'"
            clearable
          />
        </el-form-item>

        <el-form-item label="养护教程" prop="maintainTutorial">
          <el-input
            v-model="formData.maintainTutorial"
            type="textarea"
            :rows="6"
            placeholder="请输入养护教程"
            :disabled="dialogMode === 'view'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button class="transparent-btn" @click="dialogVisible = false">取消</el-button>
          <el-button class="transparent-btn" @click="resetForm" v-if="dialogMode !== 'view'">重置</el-button>
          <el-button type="primary" @click="handleSubmit" v-if="dialogMode !== 'view'" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Picture } from '@element-plus/icons-vue'
import { plantKnowledgeApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const defaultImage = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const searchForm = ref({
  plantName: '',
  categoryName: ''
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const selectedIds = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref('add') // add, edit, view
const submitting = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  plantName: '',
  categoryName: '',
  imgUrl: '',
  videoUrl: '',
  maintainTutorial: ''
})

const formRules = {
  plantName: [
    { required: true, message: '请输入植物名称', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '请选择绿植种类', trigger: 'change' }
  ],
  maintainTutorial: [
    { required: true, message: '请输入养护教程', trigger: 'blur' }
  ]
}

// 获取养护知识列表
const getKnowledgeList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    if (searchForm.value.plantName) {
      params.plantName = searchForm.value.plantName
    }
    if (searchForm.value.categoryName) {
      params.categoryName = searchForm.value.categoryName
    }
    
    const res = await plantKnowledgeApi.getKnowledgeList(params)
    
    if (res && res.code === 200) {
      if (res.data && typeof res.data === 'object') {
        tableData.value = res.data.list || []
        total.value = res.data.total || 0
      } else if (Array.isArray(res.data)) {
        tableData.value = res.data
        total.value = res.data.length
      }
    } else {
      ElMessage.error(res.message || '获取数据失败')
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取养护知识列表失败:', error)
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
  getKnowledgeList()
}

// 分页大小变更
const handleSizeChange = () => {
  getKnowledgeList()
}

// 页码变更
const handleCurrentChange = () => {
  getKnowledgeList()
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.plantName = ''
  formData.categoryName = ''
  formData.imgUrl = ''
  formData.videoUrl = ''
  formData.maintainTutorial = ''
  formRef.value?.clearValidate()
}

// 添加
const handleAdd = () => {
  resetForm()
  dialogMode.value = 'add'
  dialogTitle.value = '添加养护知识'
  dialogVisible.value = true
}

// 查看
const handleView = (row) => {
  Object.assign(formData, {
    id: row.id,
    plantName: row.plantName,
    categoryName: row.categoryName,
    imgUrl: row.imgUrl,
    videoUrl: row.videoUrl,
    maintainTutorial: row.maintainTutorial
  })
  dialogMode.value = 'view'
  dialogTitle.value = '查看养护知识'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  Object.assign(formData, {
    id: row.id,
    plantName: row.plantName,
    categoryName: row.categoryName,
    imgUrl: row.imgUrl,
    videoUrl: row.videoUrl,
    maintainTutorial: row.maintainTutorial
  })
  dialogMode.value = 'edit'
  dialogTitle.value = '修改养护知识'
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    if (dialogMode.value === 'add') {
      const res = await plantKnowledgeApi.addKnowledge(formData)
      if (res.code === 200) {
        ElMessage.success('添加成功')
        dialogVisible.value = false
        getKnowledgeList()
      } else {
        ElMessage.error(res.msg || '添加失败')
      }
    } else if (dialogMode.value === 'edit') {
      const res = await plantKnowledgeApi.updateKnowledge(formData)
      if (res.code === 200) {
        ElMessage.success('修改成功')
        dialogVisible.value = false
        getKnowledgeList()
      } else {
        ElMessage.error(res.msg || '修改失败')
      }
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
    `确定要删除养护知识"${row.plantName}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await plantKnowledgeApi.deleteKnowledge(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getKnowledgeList()
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
    `确定要删除选中的 ${selectedIds.value.length} 条养护知识吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await plantKnowledgeApi.deleteKnowledgeBatch(selectedIds.value)
      if (res.code === 200) {
        ElMessage.success('批量删除成功')
        selectedIds.value = []
        getKnowledgeList()
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
  getKnowledgeList()
})
</script>

<style scoped>
.care-tips-page {
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
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
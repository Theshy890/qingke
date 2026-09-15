<template>
  <div class="category-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="•" class="breadcrumb">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>绿植种类</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item>
          <el-input v-model="searchForm.categoryName" placeholder="绿植种类" clearable style="width: 200px" />
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
        <el-table-column prop="categoryName" label="绿植种类" min-width="150" sortable />
        <el-table-column label="操作" width="300" fixed="right">
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      draggable
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="绿植种类" prop="categoryName">
          <el-input
            v-model="formData.categoryName"
            placeholder="请输入绿植种类名称"
            :disabled="dialogMode === 'view'"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button class="transparent-btn" @click="dialogVisible = false">取消</el-button>
          <el-button class="transparent-btn" @click="resetForm" v-if="dialogMode !== 'view'">重置</el-button>
          <el-button type="primary" @click="handleSubmit" v-if="dialogMode !== 'view'">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { categoryApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = ref({
  categoryName: '',
 
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const selectedIds = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMode = ref('add')
const formRef = ref(null)
const formData = reactive({
  id: null,
  categoryName: ''
})

const formRules = {
  categoryName: [
    { required: true, message: '请输入绿植种类名称', trigger: 'blur' },
    { min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur' }
  ]
}

// 获取绿植种类列表 - 仅从数据库获取真实数据
const getCategoryList = async () => {
  loading.value = true
  try {
    // 构建请求参数
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    }
    // 只在有值时添加搜索参数
    if (searchForm.value.categoryName && searchForm.value.categoryName.trim()) {
      params.categoryName = searchForm.value.categoryName.trim()
    }
    
    // 调用API获取数据
    const res = await categoryApi.getCategoryList(params)
    
    if (res && res.code === 200) {
      // 判断返回的data是数组还是PageInfo对象
      if (Array.isArray(res.data)) {
        // 如果是数组，说明调用的是/list接口（不应该发生）
        console.warn('⚠️ 返回的是数组，不是PageInfo对象')
        tableData.value = res.data
        total.value = res.data.length
      } else if (res.data && typeof res.data === 'object') {
        // 如果是对象，应该包含list和total
        tableData.value = res.data.list || []
        total.value = res.data.total || 0
      } else {
        // 异常情况
        console.error('❌ 未知的数据格式:', res.data)
        tableData.value = []
        total.value = 0
      }
    } else {
      ElMessage.error(res.message || '获取数据失败')
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取列表失败:', error)
    ElMessage.error('加载失败: ' + (error.message || '未知错误'))
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  getCategoryList()
}

// 处理分页大小变更
const handleSizeChange = (size) => {
  pageSize.value = size
  getCategoryList()
}

// 处理页码变更
const handleCurrentChange = (current) => {
  currentPage.value = current
  getCategoryList()
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.categoryName = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 添加
const handleAdd = () => {
  resetForm()
  dialogMode.value = 'add'
  dialogTitle.value = '添加绿植种类'
  dialogVisible.value = true
}

// 查看
const handleView = (row) => {
  formData.id = row.id
  formData.categoryName = row.categoryName
  dialogMode.value = 'view'
  dialogTitle.value = '查看绿植种类'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  formData.id = row.id
  formData.categoryName = row.categoryName
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑绿植种类'
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogMode.value === 'add') {
          await categoryApi.addCategory({
            categoryName: formData.categoryName
          })
          ElMessage.success('添加成功')
        } else if (dialogMode.value === 'edit') {
          await categoryApi.updateCategory({
            id: formData.id,
            categoryName: formData.categoryName
          })
          ElMessage.success('修改成功')
        }
        dialogVisible.value = false
        getCategoryList()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除绿植种类“${row.categoryName}”吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await categoryApi.deleteCategory(row.id)
      ElMessage.success('删除成功')
      getCategoryList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
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
    `确定要删除选中的 ${selectedIds.value.length} 条数据吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await categoryApi.deleteCategoryBatch(selectedIds.value)
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      getCategoryList()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error(error.message || '批量删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 组件挂载时获取数据
onMounted(() => {
  getCategoryList()
})
</script>

<style scoped>
.category-page {
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
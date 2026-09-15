<template>
  <div class="user-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="•" class="breadcrumb">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>用户</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="账号">
          <el-input v-model="searchForm.zh" placeholder="账号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="姓名" clearable style="width: 150px" />
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
        <el-table-column prop="zh" label="账号" min-width="100" sortable />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="gender" label="性别" width="80" sortable />
        <el-table-column prop="age" label="年龄" width="80" sortable />
        <el-table-column prop="phone" label="联系方式" min-width="130" sortable />
        <el-table-column prop="touxiang" label="头像" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.avatarUrl"
              style="width: 50px; height: 50px; border-radius: 4px"
              :src="row.avatarUrl"
              fit="cover"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="zhuceshibijian" label="注册时间" min-width="120" sortable />
        <el-table-column prop="email" label="邮箱" min-width="150" sortable />
        <el-table-column prop="status" label="状态" width="100" sortable>
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              :active-value="1"
              :inactive-value="0"
              style="--el-switch-on-color: #5a8f7b; --el-switch-off-color: #c0c4cc"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
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
        layout="total, sizes, prev, pager, next"
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
      width="600px"
      :close-on-click-modal="false"
      draggable
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="账号" prop="zh">
              <el-input
                v-model="formData.zh"
                placeholder="请输入账号"
                :disabled="dialogMode === 'view' || dialogMode === 'edit'"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input
                v-model="formData.name"
                placeholder="请输入姓名"
                :disabled="dialogMode === 'view'"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" v-if="dialogMode === 'add'">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="formData.password"
                type="password"
                placeholder="请输入密码"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select
                v-model="formData.gender"
                placeholder="请选择性别"
                :disabled="dialogMode === 'view'"
                style="width: 100%"
              >
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number
                v-model="formData.age"
                :min="1"
                :max="150"
                :disabled="dialogMode === 'view'"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="formData.phone"
                placeholder="请输入手机号"
                :disabled="dialogMode === 'view'"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="formData.email"
                placeholder="请输入邮箱"
                :disabled="dialogMode === 'view'"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="头像URL" prop="touxiang">
          <el-input
            v-model="formData.avatarUrl"
            placeholder="请输入头像URL"
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
import { userApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = ref({
  zh: '',
  name: ''
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
  zh: '',
  password: '',
  name: '',
  gender: '',
  age: null,
  phone: '',
  email: '',
  touxiang: '',
  status: 1
})

const formRules = {
  zh: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 获取用户列表
const getUserList = async () => {
  loading.value = true
  try {
    const res = await userApi.getUserList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      zh: searchForm.value.zh || undefined,
      name: searchForm.value.name || undefined
    })
    
    if (res.code === 200 && res.data) {
      const pageInfo = res.data
      tableData.value = pageInfo.list || []
      total.value = pageInfo.total || 0
    } else {
      ElMessage.error(res.message || '获取用户列表失败')
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取用户列表错误:', error)
    ElMessage.error('网络错误，请稍后重试')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  getUserList()
}

// 处理分页大小变更
const handleSizeChange = (size) => {
  pageSize.value = size
  getUserList()
}

// 处理页码变更
const handleCurrentChange = (current) => {
  currentPage.value = current
  getUserList()
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.zh = ''
  formData.password = ''
  formData.name = ''
  formData.gender = ''
  formData.age = null
  formData.phone = ''
  formData.email = ''
  formData.avatarUrl = ''
  formData.status = 1
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 添加
const handleAdd = () => {
  resetForm()
  dialogMode.value = 'add'
  dialogTitle.value = '添加用户'
  dialogVisible.value = true
}

// 查看
const handleView = (row) => {
  Object.assign(formData, row)
  dialogMode.value = 'view'
  dialogTitle.value = '查看用户'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  Object.assign(formData, row)
  formData.password = ''
  dialogMode.value = 'edit'
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogMode.value === 'add') {
          await userApi.addUser(formData)
          ElMessage.success('添加成功')
        } else if (dialogMode.value === 'edit') {
          await userApi.updateUser(formData)
          ElMessage.success('修改成功')
        }
        dialogVisible.value = false
        getUserList()
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
    `确定要删除用户"${row.name}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await userApi.deleteUser(row.id)
      ElMessage.success('删除成功')
      getUserList()
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
      await userApi.deleteUserBatch(selectedIds.value)
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      getUserList()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error(error.message || '批量删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 处理状态变更
const handleStatusChange = async (row) => {
  try {
    await userApi.updateUserStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error(error.message || '状态更新失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getUserList()
})
</script>

<style scoped>
.user-page {
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
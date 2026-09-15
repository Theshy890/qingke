<template>
  <div class="reminders-page">
    <div class="page-header">
      <h1>📬 我的提醒</h1>
      <div class="header-actions">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge-item">
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加提醒
          </el-button>
        </el-badge>
        <el-button type="success" @click="handleMarkAllAsRead" :disabled="unreadCount === 0">
          <el-icon><Check /></el-icon>
          全部已读
        </el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterRead" @change="loadReminders">
        <el-radio-button :label="null">全部状态</el-radio-button>
        <el-radio-button :label="0">未读</el-radio-button>
        <el-radio-button :label="1">已读</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 提醒列表 -->
    <div class="reminders-list" v-loading="loading">
      <el-empty v-if="!loading && reminderList.length === 0" description="暂无提醒">
        <el-button type="primary" @click="showAddDialog = true">添加第一个提醒</el-button>
      </el-empty>

      <div class="reminder-item" 
           v-for="item in reminderList" 
           :key="item.id"
           :class="{ 'unread': item.isRead === 0 }"
           @click="handleViewDetail(item)"
      >
        <div class="reminder-icon">
          <el-icon color="#52c41a" :size="24"><Bell /></el-icon>
        </div>

        <div class="reminder-content">
          <div class="reminder-header">
            <h3 class="reminder-title">
              {{ item.title }}
              <el-tag v-if="item.isRead === 0 && isFutureReminder(item)" type="warning" size="small">即将到来</el-tag>
              <el-tag v-else-if="item.isRead === 0" type="danger" size="small">未读</el-tag>
            </h3>
            <el-tag :type="getTypeColor(item.type)" size="small">{{ item.type }}</el-tag>
          </div>
          
          <p class="reminder-brief">{{ item.brief }}</p>
          
          <div class="reminder-footer">
            <span class="reminder-time">
              <el-icon><Clock /></el-icon>
              提醒时间：{{ formatDateTime(item.remindTime) }}
            </span>
            <div class="reminder-actions">
              <el-button v-if="item.isRead === 0" type="primary" size="small" @click.stop="handleMarkAsRead(item.id)">
                标记已读
              </el-button>
              <el-button type="danger" size="small" plain @click.stop="handleDelete(item.id)">
                删除
              </el-button>
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
      :page-sizes="[10, 20, 50]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="loadReminders"
      @current-change="loadReminders"
    />

    <!-- 添加提醒对话框 -->
    <el-dialog v-model="showAddDialog" title="添加提醒" width="600px" :close-on-click-modal="false" draggable class="resizable-dialog user-cartoon-dialog">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入提醒标题" />
        </el-form-item>
        
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="个人提醒" value="个人提醒" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="简介" prop="brief">
          <el-input v-model="formData.brief" placeholder="请输入简短描述" />
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="formData.content" 
            type="textarea" 
            :rows="5"
            placeholder="请输入详细内容"
          />
        </el-form-item>
        
        <el-form-item label="提醒时间" prop="remindTime">
          <el-date-picker
            v-model="formData.remindTime"
            type="datetime"
            placeholder="选择提醒时间"
            :disabled-date="disabledPastDate"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button @click="handleResetForm">重置</el-button>
        <el-button type="primary" @click="handleAdd" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="提醒详情" width="700px" draggable class="user-cartoon-dialog">
      <div v-if="currentReminder" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="标题">
            {{ currentReminder.title }}
            <el-tag v-if="currentReminder.isRead === 0" type="danger" size="small" style="margin-left: 10px">未读</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeColor(currentReminder.type)">{{ currentReminder.type }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="简介">{{ currentReminder.brief }}</el-descriptions-item>
          <el-descriptions-item label="内容">
            <div style="white-space: pre-wrap; line-height: 1.8;">{{ currentReminder.content }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="提醒时间">{{ formatDateTime(currentReminder.remindTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentReminder.createTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button v-if="currentReminder && currentReminder.isRead === 0" type="primary" @click="handleMarkAsRead(currentReminder.id)">
          标记已读
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus, Check, Bell, Clock } from '@element-plus/icons-vue'
import { sysNoticeApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const loading = ref(false)
const reminderList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const unreadCount = ref(0)

const filterRead = ref(null)

const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const currentReminder = ref(null)
const submitting = ref(false)

// 限制提醒时间：不能选今天及之前的日期，只能选明天及之后
const disabledPastDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000)
  return time.getTime() < tomorrow.getTime()
}

const formData = reactive({
  title: '',
  type: '个人提醒',
  brief: '',
  content: '',
  remindTime: ''
})

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  brief: [{ required: true, message: '请输入简介', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  remindTime: [{ required: true, message: '请选择提醒时间', trigger: 'change' }]
}

const handleResetForm = () => {
  formRef.value?.resetFields()
}

const formRef = ref(null)

// 加载提醒列表
const loadReminders = async () => {
  if (!userInfo.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      userId: userInfo.id,
      type: ['个人提醒'] // 只展示用户个人设置的提醒，系统公告在首页展示
    }
    
    if (filterRead.value !== null) {
      params.isRead = filterRead.value
    }
    
    const res = await sysNoticeApi.getPage(params)
    
    if (res.code === 200 && res.data) {
      reminderList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取提醒列表失败')
    }
  } catch (error) {
    console.error('获取提醒列表错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
  
  // 同时更新未读数量
  loadUnreadCount()
}

// 获取未读数量
const loadUnreadCount = async () => {
  if (!userInfo.id) return
  
  try {
    const res = await sysNoticeApi.getUnreadCount(userInfo.id)
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('获取未读数量错误:', error)
  }
}

// 查看详情
const handleViewDetail = async (item) => {
  currentReminder.value = item
  showDetailDialog.value = true
  
  // 如果未读，自动标记为已读
  if (item.isRead === 0) {
    await handleMarkAsRead(item.id)
  }
}

// 添加提醒
const handleAdd = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const data = {
        ...formData,
        userId: userInfo.id
      }
      
      const res = await sysNoticeApi.add(data)
      
      if (res.code === 200) {
        ElMessage.success('添加成功')
        showAddDialog.value = false
        formRef.value.resetFields()
        loadReminders()
      } else {
        ElMessage.error(res.message || '添加失败')
      }
    } catch (error) {
      console.error('添加提醒错误:', error)
      ElMessage.error('网络错误，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// 标记为已读
const handleMarkAsRead = async (id) => {
  try {
    const res = await sysNoticeApi.markAsRead(id)
    
    if (res.code === 200) {
      ElMessage.success('已标记为已读')
      loadReminders()
      
      // 更新当前详情对话框中的状态
      if (currentReminder.value && currentReminder.value.id === id) {
        currentReminder.value.isRead = 1
      }
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('标记已读错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 全部标记为已读
const handleMarkAllAsRead = async () => {
  if (unreadCount.value === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定将所有未读提醒（${unreadCount.value}条）标记为已读吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await sysNoticeApi.markAllAsRead(userInfo.id)
    
    if (res.code === 200) {
      ElMessage.success('已全部标记为已读')
      loadReminders()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('全部标记已读错误:', error)
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 删除提醒
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这条提醒吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await sysNoticeApi.deleteById(id)
    
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadReminders()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除提醒错误:', error)
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 获取类型颜色
const getTypeColor = (type) => {
  const colorMap = {
    '个人提醒': 'success',
    '系统通知': 'primary',
    '新手引导': 'warning',
    '系统活动': 'danger'
  }
  return colorMap[type] || 'info'
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

// 判断是否为未到时间的未来提醒
const isFutureReminder = (item) => {
  if (!item.remindTime) return false
  return new Date(item.remindTime) > new Date()
}

onMounted(() => {
  loadReminders()
})
</script>

<style scoped>
.reminders-page {
  min-height: calc(100vh - 120px);
  padding: 30px 20px;
}

.page-header {
  max-width: 1200px;
  margin: 0 auto 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h1 {
  font-size: 28px;
  color: #2e7d32;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.badge-item {
  margin-right: 10px;
}

.filter-bar {
  max-width: 1200px;
  margin: 0 auto 20px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.reminders-list {
  max-width: 1200px;
  margin: 0 auto;
  min-height: 400px;
}

.reminder-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  gap: 20px;
  transition: all 0.3s ease;
  cursor: pointer;
  border-left: 4px solid transparent;
}

.reminder-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateX(4px);
}

.reminder-item.unread {
  border-left-color: #52c41a;
  background: linear-gradient(to right, #f6ffed 0%, white 100%);
}

.reminder-icon {
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 50%;
}

.reminder-content {
  flex: 1;
}

.reminder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.reminder-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.reminder-brief {
  font-size: 14px;
  color: #666;
  margin: 0 0 15px 0;
  line-height: 1.6;
}

.reminder-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.reminder-time {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 5px;
}

.reminder-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
}

.detail-content {
  padding: 10px;
}
</style>
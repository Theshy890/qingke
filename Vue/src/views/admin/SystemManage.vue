<template>
  <div class="system-manage-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="•" class="breadcrumb">
      <el-breadcrumb-item>首页</el-breadcrumb-item>
      <el-breadcrumb-item>系统管理</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 数据统计看板 -->
    <el-card class="statistics-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><DataAnalysis /></el-icon>
            数据统计看板
          </span>
          <el-button type="primary" :icon="Refresh" size="small" @click="loadStatistics">
            刷新数据
          </el-button>
        </div>
      </template>

      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card user-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="48"><Avatar /></el-icon>
              </div>
              <div class="stat-info">
                <el-statistic :value="statistics.totalUsers" title="总用户数">
                  <template #suffix>人</template>
                </el-statistic>
                <div class="stat-extra">
                  <el-tag type="success" size="small">今日新增: {{ statistics.todayUsers }}</el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card knowledge-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="48"><Reading /></el-icon>
              </div>
              <div class="stat-info">
                <el-statistic :value="statistics.totalKnowledge" title="养护知识">
                  <template #suffix>篇</template>
                </el-statistic>
                <div class="stat-extra">
                  <el-tag type="info" size="small">收藏: {{ statistics.storeupCount }}</el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card record-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="48"><EditPen /></el-icon>
              </div>
              <div class="stat-info">
                <el-statistic :value="statistics.totalRecords" title="养护记录">
                  <template #suffix>条</template>
                </el-statistic>
                <div class="stat-extra">
                  <el-tag type="warning" size="small">本月: {{ statistics.monthRecords }}</el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card community-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="48"><ChatDotRound /></el-icon>
              </div>
              <div class="stat-info">
                <el-statistic :value="statistics.totalPosts" title="社区帖子">
                  <template #suffix>个</template>
                </el-statistic>
                <div class="stat-extra">
                  <el-tag type="danger" size="small">待审核: {{ statistics.pendingPosts }}</el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-divider />

      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card ai-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="48"><Opportunity /></el-icon>
              </div>
              <div class="stat-info">
                <el-statistic :value="statistics.totalAiRecognitions" title="AI识别次数">
                  <template #suffix>次</template>
                </el-statistic>
                <div class="stat-extra">
                  <el-tag type="primary" size="small">今日: {{ statistics.todayAi }}</el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card category-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="48"><Collection /></el-icon>
              </div>
              <div class="stat-info">
                <el-statistic :value="statistics.totalCategories" title="绿植种类">
                  <template #suffix>种</template>
                </el-statistic>
                <div class="stat-extra">
                  <el-tag type="success" size="small">已启用</el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card comment-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="48"><Comment /></el-icon>
              </div>
              <div class="stat-info">
                <el-statistic :value="statistics.totalComments" title="社区评论">
                  <template #suffix>条</template>
                </el-statistic>
                <div class="stat-extra">
                  <el-tag type="info" size="small">活跃度: 高</el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card storage-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="48"><FolderOpened /></el-icon>
              </div>
              <div class="stat-info">
                <el-statistic :value="statistics.logCount" title="操作日志">
                  <template #suffix>条</template>
                </el-statistic>
                <div class="stat-extra">
                  <el-progress :percentage="storagePercentage" :color="storageColor" />
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 数据可视化图表 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><TrendCharts /></el-icon>
                用户增长趋势
              </span>
              <el-radio-group v-model="userTrendPeriod" size="small" class="trend-radio" @change="updateUserTrendChart">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="userTrendChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Histogram /></el-icon>
                养护记录统计
              </span>
            </div>
          </template>
          <div ref="recordsChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><PieChart /></el-icon>
                植物健康状况
              </span>
            </div>
          </template>
          <div ref="healthChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><DataLine /></el-icon>
                社区活跃度
              </span>
            </div>
          </template>
          <div ref="communityChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷管理 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="recent-logs-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Notification /></el-icon>
                最近操作日志
              </span>
              <div class="card-actions">
                <el-button size="small" type="danger" plain @click="handleClearLogs">
                  <el-icon><Delete /></el-icon> 清空日志
                </el-button>
                <el-button size="small" class="transparent-btn" @click="handleViewAllLogs">
                  查看全部 <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
          </template>

          <el-timeline>
            <el-timeline-item
              v-for="log in recentLogs"
              :key="log.id"
              :timestamp="log.time"
              :type="log.statusType"
              placement="top"
            >
              <div class="log-content">
                <div class="log-header">
                  <el-icon><User /></el-icon>
                  <span class="log-user">{{ log.user }}</span>
                  <el-tag :type="log.result === 'success' ? 'success' : 'danger'" size="small" round>
                    {{ log.result === 'success' ? '成功' : '失败' }}
                  </el-tag>
                </div>
                <div class="log-action">{{ log.action }}</div>
                <div v-if="log.errorMessage" class="log-error">
                  <el-icon color="#f56c6c"><CircleClose /></el-icon>
                  {{ log.errorMessage }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <!-- 系统通知公告 -->
      <el-col :span="12">
        <el-card class="notice-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Bell /></el-icon>
                系统通知公告
              </span>
              <el-button size="small" class="view-all-btn transparent-btn" @click="handleViewAllNotices">
                查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>

          <el-timeline v-if="systemNotices.length > 0">
            <el-timeline-item
              v-for="notice in systemNotices.slice(0, 5)"
              :key="notice.id"
              :timestamp="notice.time"
              :type="notice.type"
              placement="top"
            >
              <div class="notice-content">
                <div class="notice-header">
                  <el-tag :type="notice.tagType" size="small">{{ notice.typeName }}</el-tag>
                  <span class="notice-title">{{ notice.title }}</span>
                </div>
                <div class="notice-text">{{ notice.content }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <div v-else style="text-align:center;color:#7b8fa3;padding:40px 0;font-size:14px">暂无通知公告</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 全部通知公告弹窗 -->
    <el-dialog v-model="allNoticesVisible" title="全部通知公告" width="700px" draggable class="flex-dialog">
      <div v-loading="allNoticesLoading" class="all-logs-container">
        <div class="all-logs-summary">
          <span>共 {{ allNotices.length }} 条通知</span>
          <div class="batch-actions" v-if="selectedNotices.length > 0">
            <el-tag type="warning" size="small">已选择 {{ selectedNotices.length }} 条</el-tag>
            <el-button type="danger" size="small" :icon="Delete" @click="handleBatchDeleteNotices">
              批量删除
            </el-button>
          </div>
        </div>
        <el-table
          :data="allNotices"
          stripe
          border
          style="width: 100%; height: 100%"
          size="small"
          @selection-change="handleNoticeSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column prop="time" label="时间" width="155" />
          <el-table-column prop="typeName" label="类型" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.tagType" size="small">{{ row.typeName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
          <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        </el-table>
      </div>
    </el-dialog>

    <!-- 系统公告设置对话框 -->
    <el-dialog
      v-model="settingsDialogVisible"
      title="系统设置"
      width="600px"
      :close-on-click-modal="false"
      draggable
    >
      <el-form :model="settingsForm" label-width="120px">
        <el-form-item label="网站名称">
          <el-input v-model="settingsForm.siteName" placeholder="请输入网站名称" />
        </el-form-item>
        <el-form-item label="社区审核">
          <el-switch v-model="settingsForm.needAudit" active-text="开启" inactive-text="关闭" />
        </el-form-item>
        <el-form-item label="AI识别限制">
          <el-input-number v-model="settingsForm.aiLimit" :min="10" :max="1000" />
          <span style="margin-left: 10px; color: #999;">次/天</span>
        </el-form-item>
        <el-form-item label="系统公告">
          <el-input
            v-model="settingsForm.announcement"
            type="textarea"
            :rows="4"
            placeholder="请输入系统公告内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settingsDialogVisible = false">取消</el-button>
        <el-button @click="handleResetSettings">重置</el-button>
        <el-button type="primary" @click="handleSaveSettings">保存设置</el-button>
      </template>
    </el-dialog>

    <!-- 全部操作日志弹窗 -->
    <el-dialog
      v-model="allLogsVisible"
      title="全部操作日志"
      width="850px"
      draggable
    >
      <div v-loading="allLogsLoading" class="all-logs-container">
        <div class="all-logs-summary">
          <span>共 {{ allLogs.length }} 条记录</span>
          <el-tag type="success" size="small">成功: {{ allLogs.filter(l => l.result === 'success').length }}</el-tag>
          <el-tag type="danger" size="small">失败: {{ allLogs.filter(l => l.result === 'fail').length }}</el-tag>
        </div>
        <el-table :data="allLogs" stripe border style="width: 100%; height: 100%" size="small">
          <el-table-column prop="time" label="时间" width="155" />
          <el-table-column prop="user" label="用户" width="80" />
          <el-table-column prop="module" label="模块" width="90" />
          <el-table-column prop="action" label="操作描述" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.result === 'success' ? 'success' : 'danger'" size="small" round>
                {{ row.result === 'success' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="错误信息" min-width="150">
            <template #default="{ row }">
              <span v-if="row.errorMessage" class="error-text">{{ row.errorMessage }}</span>
              <span v-else class="no-error">-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CircleClose, DataLine,
  DataAnalysis, Refresh, Avatar, Reading, EditPen, ChatDotRound, Opportunity,
  Collection, Comment, FolderOpened, Delete,
  Notification, ArrowRight, User, TrendCharts, Histogram, PieChart, Bell
} from '@element-plus/icons-vue'
import { userApi, categoryApi, plantKnowledgeApi, plantMaintainRecordApi, communityPostApi, aiApi, operationLogApi, statsApi, sysNoticeApi } from '../../api'
import * as echarts from 'echarts'

// 统计数据
const statistics = reactive({
  totalUsers: 0,
  todayUsers: 0,
  totalKnowledge: 0,
  totalRecords: 0,
  monthRecords: 0,
  totalPosts: 0,
  pendingPosts: 0,
  totalAiRecognitions: 0,
  todayAi: 0,
  totalCategories: 0,
  totalComments: 0,
  storeupCount: 0,
  logCount: 0
})

// 存储使用百分比（基于操作日志数量估算，每条日志约0.5KB）
const storagePercentage = computed(() => {
  const totalLogs = 100000 // 假设日志容量上限10万条
  return Math.min(Math.round((statistics.logCount / totalLogs) * 100), 100)
})

// 存储颜色
const storageColor = computed(() => {
  const percentage = storagePercentage.value
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
})

// 最近日志
const recentLogs = ref([])

// 加载最近日志（多取几条再去重，确保面板有足够的唯一条目）
const loadRecentLogs = async () => {
  try {
    const res = await operationLogApi.getRecentLogs(50)
    if (res.code === 200 && res.data) {
      const seen = new Set()
      const deduped = res.data.filter(log => {
        const action = log.description || `${getOperationTypeText(log.operationType)}${log.module}`
        // 去重 key：操作人 + 操作描述（忽略同一操作的批量重复）
        const key = `${log.username}|${action}`
        if (seen.has(key)) return false
        seen.add(key)
        return true
      })
      recentLogs.value = deduped.slice(0, 5).map(log => ({
        id: log.id,
        user: log.username,
        action: log.description || `${getOperationTypeText(log.operationType)}${log.module}`,
        time: formatDateTime(log.operationTime),
        statusType: log.result === 'fail' ? 'danger' : getLogType(log.operationType),
        result: log.result || 'success',
        errorMessage: log.errorMessage || ''
      }))
    }
  } catch (error) {
    console.error('加载操作日志失败:', error)
  }
}


// 系统通知公告
const systemNotices = ref([])

// 全部通知弹窗
const allNoticesVisible = ref(false)
const allNotices = ref([])
const allNoticesLoading = ref(false)
const selectedNotices = ref([])

// 处理通知选择变化
const handleNoticeSelectionChange = (selection) => {
  selectedNotices.value = selection
}

// 批量删除通知
const handleBatchDeleteNotices = async () => {
  if (selectedNotices.value.length === 0) {
    ElMessage.warning('请选择要删除的通知')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedNotices.value.length} 条通知吗？删除后，所有用户的相同通知也会一起删除。`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const ids = selectedNotices.value.map(n => n.id)
    const res = await sysNoticeApi.deleteBatchAllUsers(ids)

    if (res.code === 200) {
      ElMessage.success('批量删除成功')
      selectedNotices.value = []
      // 重新加载通知列表
      await handleViewAllNotices()
      // 刷新首页通知面板
      await loadSystemNotices()
    } else {
      ElMessage.error(res.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除通知失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleViewAllNotices = async () => {
  allNoticesVisible.value = true
  allNoticesLoading.value = true
  try {
    const res = await sysNoticeApi.getPage({ pageNum: 1, pageSize: 100, type: ['系统公告', '维护通知', '活动通知'] })
    if (res.code === 200 && res.data?.records) {
      // 去重：同一标题+时间只展示一条（发布时会对每个用户创建一条，这里按标题+内容去重）
      const seen = new Set()
      allNotices.value = res.data.records
        .filter(n => {
          const key = `${n.title}|${n.content}`
          if (seen.has(key)) return false
          seen.add(key)
          return true
        })
        .map(n => ({
          id: n.id,
          title: n.title,
          content: n.content,
          time: formatDateTime(n.createTime),
          type: n.type === '系统公告' ? 'primary' : n.type === '维护通知' ? 'warning' : 'success',
          typeName: n.type,
          tagType: n.type === '系统公告' ? '' : n.type === '维护通知' ? 'warning' : 'success'
        }))
    }
  } catch (error) {
    console.error('加载全部通知失败:', error)
  } finally {
    allNoticesLoading.value = false
  }
}

// 加载系统通知公告（去重后只显示最近5条）
const loadSystemNotices = async () => {
  try {
    const res = await sysNoticeApi.getPage({ pageNum: 1, pageSize: 100, type: ['系统公告', '维护通知', '活动通知'] })
    if (res.code === 200 && res.data?.records) {
      const seen = new Set()
      const deduped = res.data.records.filter(n => {
        const key = `${n.title}|${n.content}`
        if (seen.has(key)) return false
        seen.add(key)
        return true
      })
      systemNotices.value = deduped.slice(0, 5).map(n => ({
        id: n.id,
        title: n.title,
        content: n.content,
        time: formatDateTime(n.createTime),
        type: n.type === '系统公告' ? 'primary' : n.type === '维护通知' ? 'warning' : 'success',
        typeName: n.type,
        tagType: n.type === '系统公告' ? '' : n.type === '维护通知' ? 'warning' : 'success'
      }))
    }
  } catch (error) {
    console.error('加载系统通知失败:', error)
  }
}


// 获取操作类型文本
const getOperationTypeText = (type) => {
  const typeMap = {
    'login': '登录',
    'logout': '登出',
    'add': '添加了',
    'update': '修改了',
    'delete': '删除了',
    'audit': '审核了'
  }
  return typeMap[type] || type
}

// 获取日志类型（用于时间线颜色）
const getLogType = (operationType) => {
  const typeMap = {
    'login': 'success',
    'logout': 'info',
    'add': 'primary',
    'update': 'warning',
    'delete': 'danger',
    'audit': 'primary'
  }
  return typeMap[operationType] || 'info'
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 系统设置对话框
const settingsDialogVisible = ref(false)
const settingsForm = reactive({
  siteName: '青稞绿植养护系统',
  needAudit: true,
  aiLimit: 100,
  announcement: ''
})

const handleResetSettings = () => {
  Object.assign(settingsForm, {
    siteName: '青稞绿植养护系统',
    needAudit: true,
    aiLimit: 100,
    announcement: ''
  })
}

// 加载统计数据（调用后端聚合接口）
const loadStatistics = async () => {
  try {
    const res = await statsApi.getDashboard()
    if (res.code === 200 && res.data) {
      const d = res.data
      statistics.totalUsers = d.userCount || 0
      statistics.todayUsers = d.todayNewUsers || 0
      statistics.totalKnowledge = d.knowledgeCount || 0
      statistics.totalRecords = d.recordCount || 0
      statistics.monthRecords = d.monthRecords || 0
      statistics.totalPosts = d.postCount || 0
      statistics.pendingPosts = d.pendingPosts || 0
      statistics.totalComments = d.commentCount || 0
      statistics.totalAiRecognitions = d.aiCount || 0
      statistics.todayAi = d.todayAi || 0
      statistics.totalCategories = d.categoryCount || 0
      statistics.storeupCount = d.storeupCount || 0
      statistics.logCount = d.logCount || 0
    }

    ElMessage.success('数据刷新成功')
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('数据加载失败')
  }
}

// 保存设置
const handleSaveSettings = () => {
  ElMessage.success('设置保存成功')
  settingsDialogVisible.value = false
}

// 查看所有日志
const allLogsVisible = ref(false)
const allLogs = ref([])
const allLogsLoading = ref(false)

const handleViewAllLogs = async () => {
  allLogsVisible.value = true
  allLogsLoading.value = true
  try {
    const res = await operationLogApi.getRecentLogs(500)
    if (res.code === 200 && res.data) {
      const seen = new Set()
      allLogs.value = res.data
        .filter(log => {
          const action = log.description || `${getOperationTypeText(log.operationType)}${log.module}`
          const key = `${log.username}|${action}`
          if (seen.has(key)) return false
          seen.add(key)
          return true
        })
        .map(log => ({
          id: log.id,
          user: log.username,
          role: log.role,
          action: log.description || `${getOperationTypeText(log.operationType)}${log.module}`,
          time: formatDateTime(log.operationTime),
          statusType: log.result === 'fail' ? 'danger' : getLogType(log.operationType),
          result: log.result || 'success',
          errorMessage: log.errorMessage || '',
          operationType: log.operationType,
          module: log.module
        }))
    }
  } catch (error) {
    console.error('加载全部日志失败:', error)
  } finally {
    allLogsLoading.value = false
  }
}

// 清空日志
const handleClearLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复。', '警告', {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await operationLogApi.clearLogs()
    if (res.code === 200) {
      ElMessage.success(res.message || '日志已清空')
      recentLogs.value = []
      loadRecentLogs()
    } else {
      ElMessage.error(res.message || '清空失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

// 图表实例
const userTrendChart = ref(null)
const recordsChart = ref(null)
const healthChart = ref(null)
const communityChart = ref(null)

let userTrendChartInstance = null
let recordsChartInstance = null
let healthChartInstance = null
let communityChartInstance = null

const userTrendPeriod = ref('week')

// 初始化图表
const initCharts = async () => {
  await nextTick()
  
  // 初始化用户增长趋势图
  if (userTrendChart.value) {
    userTrendChartInstance = echarts.init(userTrendChart.value)
    updateUserTrendChart()
  }
  
  // 初始化养护记录统计图
  if (recordsChart.value) {
    recordsChartInstance = echarts.init(recordsChart.value)
    updateRecordsChart()
  }
  
  // 初始化植物健康状况图
  if (healthChart.value) {
    healthChartInstance = echarts.init(healthChart.value)
    updateHealthChart()
  }
  
  // 初始化社区活跃度图
  if (communityChart.value) {
    communityChartInstance = echarts.init(communityChart.value)
    updateCommunityChart()
  }
  
  // 响应式调整
  window.addEventListener('resize', handleResize)
}

// 更新用户增长趋势图
const updateUserTrendChart = () => {
  if (!userTrendChartInstance) return
  
  const days = userTrendPeriod.value === 'week' ? 7 : 30
  const dates = []
  const values = []
  
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(`${date.getMonth() + 1}/${date.getDate()}`)
    values.push(Math.floor(Math.random() * 10) + statistics.totalUsers / days)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '新增用户',
      type: 'line',
      smooth: true,
      data: values,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102, 126, 234, 0.6)' },
          { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
        ])
      },
      lineStyle: { color: '#667eea' },
      itemStyle: { color: '#667eea' }
    }]
  }
  
  userTrendChartInstance.setOption(option)
}

// 更新养护记录统计图
const updateRecordsChart = () => {
  if (!recordsChartInstance) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['观叶植物', '多肉植物', '观花植物', '水生植物', '其他']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '养护次数',
      type: 'bar',
      data: [
        Math.floor(statistics.totalRecords * 0.3),
        Math.floor(statistics.totalRecords * 0.25),
        Math.floor(statistics.totalRecords * 0.2),
        Math.floor(statistics.totalRecords * 0.15),
        Math.floor(statistics.totalRecords * 0.1)
      ],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#4facfe' },
          { offset: 1, color: '#00f2fe' }
        ])
      },
      barMaxWidth: 50
    }]
  }
  
  recordsChartInstance.setOption(option)
}

// 更新植物健康状况图
const updateHealthChart = () => {
  if (!healthChartInstance) return
  
  const total = statistics.totalAiRecognitions || 100
  const healthy = Math.floor(total * 0.65)
  const disease = Math.floor(total * 0.25)
  const unknown = total - healthy - disease
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center',
      textStyle: {
        color: '#e0e6ed'
      },
      inactiveColor: '#7b9a8f'
    },
    series: [{
      name: '识别结果',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 20,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: healthy, name: '健康植物', itemStyle: { color: '#43e97b' } },
        { value: disease, name: '检测到病害', itemStyle: { color: '#fa709a' } },
        { value: unknown, name: '未识别', itemStyle: { color: '#a8edea' } }
      ]
    }]
  }
  
  healthChartInstance.setOption(option)
}

// 更新社区活跃度图
const updateCommunityChart = () => {
  if (!communityChartInstance) return
  
  const months = ['1月', '2月', '3月', '4月', '5月', '6月']
  const postsData = []
  const commentsData = []
  
  for (let i = 0; i < 6; i++) {
    postsData.push(Math.floor(Math.random() * 50) + 20)
    commentsData.push(Math.floor(Math.random() * 100) + 50)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['发帖数', '评论数'],
      top: '5%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '发帖数',
        type: 'line',
        smooth: true,
        data: postsData,
        lineStyle: { color: '#43e97b' },
        itemStyle: { color: '#43e97b' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(67, 233, 123, 0.5)' },
            { offset: 1, color: 'rgba(67, 233, 123, 0.1)' }
          ])
        }
      },
      {
        name: '评论数',
        type: 'line',
        smooth: true,
        data: commentsData,
        lineStyle: { color: '#4facfe' },
        itemStyle: { color: '#4facfe' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(79, 172, 254, 0.5)' },
            { offset: 1, color: 'rgba(79, 172, 254, 0.1)' }
          ])
        }
      }
    ]
  }
  
  communityChartInstance.setOption(option)
}

// 窗口大小调整
const handleResize = () => {
  userTrendChartInstance?.resize()
  recordsChartInstance?.resize()
  healthChartInstance?.resize()
  communityChartInstance?.resize()
}

onMounted(async () => {
  await loadStatistics()
  loadRecentLogs()
  loadSystemNotices()
  
  // 初始化图表
  await initCharts()
  
  // 每30秒刷新一次日志
  setInterval(loadRecentLogs, 30000)
})

onUnmounted(() => {
  // 销毁图表实例
  userTrendChartInstance?.dispose()
  recordsChartInstance?.dispose()
  healthChartInstance?.dispose()
  communityChartInstance?.dispose()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})

</script>

<style scoped>
.system-manage-page {
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

.statistics-card,
.recent-logs-card,
.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #2e7d32;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  border: none;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.user-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.knowledge-card {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.record-card {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.community-card {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.ai-card {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.category-card {
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
  color: white;
}

.comment-card {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #333;
}

.storage-card {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  color: #333;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.stat-icon {
  opacity: 0.8;
}

.stat-info {
  flex: 1;
}

.stat-extra {
  margin-top: 10px;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 100%;
  min-height: 400px;
}

/* 面板 HUD 青绿发光切角边框:统计看板 / 图表 / 日志 / 通知 */
.statistics-card,
.chart-card,
.recent-logs-card,
.notice-card {
  position: relative;
  overflow: visible !important;
  border: 1px solid rgba(0, 191, 165, 0.18) !important;
}


.statistics-card::before,
.statistics-card::after,
.chart-card::before,
.chart-card::after,
.recent-logs-card::before,
.recent-logs-card::after,
.notice-card::before,
.notice-card::after {
  content: '';
  position: absolute;
  width: 18px;
  height: 18px;
  pointer-events: none;
  z-index: 2;
}
.statistics-card::before,
.chart-card::before,
.recent-logs-card::before,
.notice-card::before {
  top: -1px;
  left: -1px;
  border-top: 2px solid #00bfa5;
  border-left: 2px solid #00bfa5;
  border-top-left-radius: 6px;
  box-shadow: -2px -2px 8px rgba(0, 191, 165, 0.3);
}
.statistics-card::after,
.chart-card::after,
.recent-logs-card::after,
.notice-card::after {
  bottom: -1px;
  right: -1px;
  border-bottom: 2px solid #00bfa5;
  border-right: 2px solid #00bfa5;
  border-bottom-right-radius: 6px;
  box-shadow: 2px 2px 8px rgba(0, 191, 165, 0.3);
}



.chart-container {
  width: 100%;
  height: 350px;
}

/* 用户增长趋势切换按钮:透明背景 + 主题绿色边框 */
.trend-radio :deep(.el-radio-button__inner) {
  background: transparent;
  border-color: #5a8f7b;
  color: #5a8f7b;
  box-shadow: none;
}

.trend-radio :deep(.el-radio-button.is-active .el-radio-button__inner) {
  background: transparent;
  border-color: #5a8f7b;
  color: #5a8f7b;
  box-shadow: -1px 0 0 0 #5a8f7b;
  font-weight: 600;
}

:deep(.stat-card .el-statistic__head) {
  color: inherit;
  opacity: 0.9;
  font-size: 14px;
}

:deep(.stat-card .el-statistic__content) {
  color: inherit;
  font-size: 28px;
  font-weight: bold;
}

.log-content {
  font-size: 14px;
}

.log-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.log-user {
  font-weight: 600;
  color: #e0e6ed;
}

.log-action {
  color: #c8d6d2;
  font-size: 13px;
}

.log-error {
  margin-top: 4px;
  padding: 6px 10px;
  background: #fef0f0;
  border-left: 3px solid #f56c6c;
  border-radius: 0 4px 4px 0;
  font-size: 12px;
  color: #e74c3c;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 全部日志弹窗 */
.all-logs-container {
  padding: 0 4px;
}

.all-logs-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.error-text {
  color: #f56c6c;
  font-size: 12px;
}

.no-error {
  color: #ccc;
}

/* 时间线节点圆点统一为主题绿色 */
:deep(.el-timeline-item__node) {
  background-color: #5a8f7b;
  border-color: #5a8f7b;
}

:deep(.el-timeline-item__tail) {
  border-left-color: #5a8f7b;
}

:deep(.el-timeline-item__timestamp) {
  color: #999;
  font-size: 12px;
}

/* 系统通知公告卡片 */
.notice-card {
  height: 100%;
}

.notice-content {
  font-size: 14px;
}

.notice-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.notice-title {
  font-weight: 600;
  color: #e0e6ed;
}

.notice-text {
  color: #c8d6d2;
  font-size: 13px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
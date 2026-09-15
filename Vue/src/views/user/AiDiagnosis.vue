<template>
  <div class="ai-diagnosis-page">
    <div class="page-header">
      <h2>AI 病害诊断</h2>
      <p>上传绿植图片，智能识别病害并提供治疗建议</p>
    </div>

    <el-row :gutter="30">
      <!-- 左侧：上传识别区域 -->
      <el-col :span="12">
        <el-card class="upload-card">
          <template #header>
            <div class="card-header">
              <span>上传图片</span>
              <div style="display:flex;align-items:center;gap:8px;">
                <el-tag type="success" size="small">智谱AI识别</el-tag>
                <el-tag :type="aiCishu > 3 ? 'info' : aiCishu > 0 ? 'warning' : 'danger'" size="small">
                  剩余次数：{{ aiCishu }}
                </el-tag>
              </div>
            </div>
          </template>
          
          <div class="upload-area">
            <el-upload
              class="image-uploader"
              :show-file-list="false"
              :before-upload="handleBeforeUpload"
              :http-request="handleUpload"
              accept="image/*"
              drag
            >
              <div v-if="!imageUrl" class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">上传图片</div>
                <div class="upload-hint">支持 JPG、PNG 格式</div>
              </div>
              <el-image v-else
              :src="imageUrl" fit="cover" class="uploaded-image" />
            </el-upload>
          </div>
          
          <div class="action-buttons">
            <el-button type="primary" :loading="recognizing" @click="handleRecognize" :disabled="!imageUrl">
              <el-icon><MagicStick /></el-icon>
              开始识别
            </el-button>
            <el-button @click="handleClear">
              <el-icon><RefreshLeft /></el-icon>
              重新上传
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：识别结果区域 -->
      <el-col :span="12">
        <el-card class="result-card">
          <template #header>
            <span>识别结果</span>
          </template>
          
          <div v-if="!recognitionResult" class="empty-result">
            <el-icon class="empty-icon"><Document /></el-icon>
            <p>上传图片后点击"开始识别"查看结果</p>
          </div>
          
          <div v-else class="result-content">
            <div class="result-item">
              <h4><el-icon><Cpu /></el-icon> 植物名称</h4>
              <p class="plant-name">{{ parsedResult.plantName }}</p>
              <div class="confidence">
                置信度：{{ (parsedResult.confidence * 100).toFixed(1) }}%
              </div>
            </div>
            
            <el-divider />
            
            <div class="result-item">
              <h4><el-icon><Reading /></el-icon> 植物介绍</h4>
              <p class="intro-text">{{ parsedResult.plantIntro }}</p>
            </div>
            
            <el-divider />
            
            <div class="result-item">
              <h4>
                <el-icon :class="parsedResult.hasDisease ? 'warning-icon' : 'success-icon'">
                  <Warning />
                </el-icon>
                病害诊断
              </h4>
              <el-alert
                :title="parsedResult.diseaseName"
                :type="parsedResult.hasDisease ? 'warning' : 'success'"
                :closable="false"
                class="disease-alert"
              >
                <p>{{ parsedResult.diseaseDesc }}</p>
              </el-alert>
            </div>
            
            <el-divider />
            
            <div class="result-item">
              <h4><el-icon><Document /></el-icon> 治疗建议</h4>
              <div class="treatment-text">
                <div
                  v-for="(line, i) in treatmentLines"
                  :key="i"
                  style="margin-bottom: 4px;"
                >{{ line }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 识别历史 -->
    <el-card class="history-card">
      <template #header>
        <span>识别历史</span>
      </template>
      
      <el-table 
        :data="historyList" 
        stripe 
        @row-click="handleViewHistory"
        :row-style="{ cursor: 'pointer' }"
      >
        <el-table-column prop="plantName" label="植物名称" width="120" />
        <el-table-column prop="diseaseName" label="病害诊断" width="120" />
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.imgUrl"
              :src="row.imgUrl"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="plantIntro" label="植物介绍" show-overflow-tooltip />
        <el-table-column prop="treatSuggest" label="治疗建议" show-overflow-tooltip />
        <el-table-column prop="createTime" label="识别时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" plain @click.stop="handleDeleteHistory(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 识别历史详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :close-on-click-modal="true"
      width="900px"
      draggable
      class="history-detail-dialog resizable-dialog user-cartoon-dialog history-detail-body"
    >
      <template #header>
        <div class="dialog-title">
          <el-icon :size="20"><Reading /></el-icon>
          <span>{{ currentHistory.plantName || '识别详情' }}</span>
          <span class="dialog-subtitle">识别详情</span>
        </div>
      </template>

      <div class="detail-content" v-if="currentHistory">
        <div class="detail-layout">
          <!-- 左侧：图片 -->
          <div class="detail-image-col">
            <div class="detail-image-box">
              <el-image
                :src="currentHistory.imgUrl"
                fit="cover"
                class="detail-image"
              >
                <template #error>
                  <div class="image-error-placeholder">
                    <el-icon :size="48"><Document /></el-icon>
                    <p>暂无图片</p>
                  </div>
                </template>
              </el-image>
            </div>
          </div>

          <!-- 右侧：信息（可滚动） -->
          <div class="detail-info-col">
            <div class="detail-info-scroll">
              <!-- 植物名称 -->
              <div class="detail-section">
                <div class="section-label">
                  <el-icon><Cpu /></el-icon> 植物名称
                </div>
                <div class="section-value plant-name-lg">
                  {{ currentHistory.plantName || '未知' }}
                </div>
              </div>

              <!-- 病害诊断 -->
              <div class="detail-section">
                <div class="section-label">
                  <el-icon><Warning /></el-icon> 病害诊断
                </div>
                <el-alert
                  :title="currentHistory.diseaseName || '未诊断'"
                  :type="isDisease(currentHistory.diseaseName) ? 'warning' : 'success'"
                  :closable="false"
                  class="section-alert"
                >
                  <p>{{ currentHistory.diseaseDesc || '暂无病害描述' }}</p>
                </el-alert>
              </div>

              <!-- 治疗建议 -->
              <div class="detail-section">
                <div class="section-label">
                  <el-icon><Document /></el-icon> 治疗建议
                </div>
                <div class="treatment-box">
                  {{ currentHistory.treatSuggest || '暂无治疗建议' }}
                </div>
              </div>

              <!-- 植物介绍 -->
              <div class="detail-section">
                <div class="section-label">
                  <el-icon><Reading /></el-icon> 植物介绍
                </div>
                <p class="intro-text-detail">
                  {{ currentHistory.plantIntro || '暂无植物介绍' }}
                </p>
              </div>

              <!-- 识别时间 -->
              <div class="detail-section">
                <div class="section-label">
                  <el-icon><RefreshLeft /></el-icon> 识别时间
                </div>
                <p class="time-text">
                  {{ formatDateTime(currentHistory.createTime) }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button type="danger" plain @click="handleDeleteFromDialog">
            删除此记录
          </el-button>
          <el-button type="primary" @click="dialogVisible = false">
            关闭
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, MagicStick, RefreshLeft, Document, Cpu, Reading, Warning } from '@element-plus/icons-vue'
import { aiApi, userApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const imageUrl = ref('')
const imageFile = ref(null)
const recognizing = ref(false)
const recognitionResult = ref(null)

// 安全解析：如果 diseaseDesc 意外包含 JSON 字符串，自动提取真实数据
const parsedResult = computed(() => {
  const r = recognitionResult.value
  if (!r) return null

  // 检测 diseaseDesc 是否是未解析的 JSON 字符串
  if (r.diseaseDesc && typeof r.diseaseDesc === 'string') {
    const text = r.diseaseDesc.trim()
    if (text.startsWith('{') || text.startsWith('"json') || text.includes('"植物名称"')) {
      try {
        let jsonStr = text
        if (jsonStr.includes('```json')) {
          jsonStr = jsonStr.substring(jsonStr.indexOf('```json') + 7, jsonStr.lastIndexOf('```'))
        } else if (jsonStr.includes('```')) {
          jsonStr = jsonStr.substring(jsonStr.indexOf('```') + 3, jsonStr.lastIndexOf('```'))
        } else if (!jsonStr.startsWith('{')) {
          jsonStr = jsonStr.substring(jsonStr.indexOf('{'), jsonStr.lastIndexOf('}') + 1)
        }
        const parsed = JSON.parse(jsonStr)
        return {
          plantName: parsed['植物名称'] || r.plantName,
          plantIntro: parsed['植物介绍'] || r.plantIntro,
          confidence: parsed['置信度'] || r.confidence,
          diseaseName: parsed['病害名称'] || r.diseaseName,
          diseaseDesc: parsed['病害描述'] || r.diseaseDesc,
          treatment: Array.isArray(parsed['治疗建议']) ? parsed['治疗建议'].join('\n') : (parsed['治疗建议'] || r.treatment),
          hasDisease: !(parsed['病害名称'] || '').includes('无明显病害')
        }
      } catch (e) {
        console.warn('前端 JSON 解析失败:', e)
      }
    }
  }
  return r
})

// 治疗建议按行拆分，每行独立展示
const treatmentLines = computed(() => {
  const t = parsedResult.value?.treatment || ''
  return t.split('\n').filter(line => line.trim())
})
const historyList = ref([])
const aiCishu = ref(0)

// 弹窗相关
const dialogVisible = ref(false)
const currentHistory = ref(null)

const loadUserAiCishu = async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) return
  try {
    const res = await userApi.getUserInfo(userInfo.id)
    if (res.code === 200 && res.data) {
      // 积分兑换机制：每10积分兑换1次AI识别次数
      aiCishu.value = Math.floor((res.data.points || 0) / 10)
    }
  } catch (e) {
    // ignore
  }
}

const compressImage = (file, maxWidth = 800, quality = 0.7) => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width
        let height = img.height

        if (width > maxWidth) {
          height = Math.round((height * maxWidth) / width)
          width = maxWidth
        }

        canvas.width = width
        canvas.height = height
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)
        resolve(canvas.toDataURL('image/jpeg', quality))
      }
      img.src = e.target.result
    }
    reader.readAsDataURL(file)
  })
}

// 判断是否有病害
const isDisease = (name) => {
  if (!name) return false
  return !name.includes('无明显病害') && !name.includes('健康') && name !== '未诊断'
}

// 查看历史详情
const handleViewHistory = (row) => {
  currentHistory.value = row
  dialogVisible.value = true
}

// 从弹窗中删除
const handleDeleteFromDialog = () => {
  dialogVisible.value = false
  if (currentHistory.value) {
    handleDeleteHistory(currentHistory.value.id)
  }
}

const handleBeforeUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  
  imageFile.value = file
  
  const compressed = await compressImage(file)
  imageUrl.value = compressed
  
  return false
}

const handleUpload = () => {
  // 自定义上传逻辑（这里不需要实际上传到服务器）
  return false
}

const handleRecognize = async () => {
  if (!imageUrl.value) {
    ElMessage.warning('请先上传图片')
    return
  }
  
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (aiCishu.value <= 0) {
    ElMessage.warning('AI识别次数不足，请通过每日登录获取积分后兑换次数')
    return
  }
  
  recognizing.value = true
  try {
    const res = await aiApi.recognize({
      image: imageUrl.value,
      userid: userInfo.id,
      type: 'disease'
    })
    
    if (res.code === 200) {
      recognitionResult.value = res.data
      aiCishu.value = Math.max(0, aiCishu.value - 1)
      ElMessage.success('识别完成')
      getHistory()
    } else {
      ElMessage.error(res.message || '识别失败')
      if (res.message && res.message.includes('次数不足')) {
        aiCishu.value = 0
      }
    }
  } catch (error) {
    console.error('识别失败:', error)
    ElMessage.error('识别失败，请稍后重试')
  } finally {
    recognizing.value = false
  }
}

const handleClear = () => {
  imageUrl.value = ''
  imageFile.value = null
  recognitionResult.value = null
}

const getHistory = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) return
    
    const res = await aiApi.getHistoryList(userInfo.id)
    if (res.code === 200) {
      historyList.value = res.data || []
    }
  } catch (error) {
    console.error('获取历史记录失败:', error)
  }
}

const handleDeleteHistory = (id) => {
  ElMessageBox.confirm('确定要删除这条识别记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await aiApi.deleteRecord(id)
      ElMessage.success('删除成功')
      getHistory()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  getHistory()
  loadUserAiCishu()
})
</script>

<style scoped>
.ai-diagnosis-page {
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
}

.page-header p {
  font-size: 16px;
  color: #666;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-card, .result-card, .history-card {
  margin-bottom: 20px;
}

.upload-area {
  margin-bottom: 20px;
}

.image-uploader {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
  min-height: 300px;
  max-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background-color: #f9f9f9;
}

.upload-placeholder {
  text-align: center;
}

.upload-icon {
  font-size: 80px;
  color: #66bb6a;
  margin-bottom: 20px;
}

.upload-text {
  font-size: 18px;
  color: #333;
  margin-bottom: 10px;
}

.upload-hint {
  font-size: 14px;
  color: #999;
}

/* 上传区域图片填满容器 */
.uploaded-image {
  width: 100%;
  height: 300px;
  object-fit: cover;
  object-position: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.image-uploader .el-image__inner {
  width: 100%;
  height: 300px;
  object-fit: cover;
}
.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.empty-result {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.empty-icon {
  font-size: 80px;
  color: #ddd;
  margin-bottom: 20px;
}

.result-content {
  padding: 10px;
}

.result-item {
  margin-bottom: 20px;
}

.result-item h4 {
  font-size: 16px;
  color: #2e7d32;
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.plant-name {
  font-size: 24px;
  font-weight: 600;
  color: #2e7d32;
  margin: 10px 0;
}

.confidence {
  font-size: 14px;
  color: #666;
}

.intro-text {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  margin: 0;
}

.disease-alert {
  margin-top: 10px;
}

.disease-alert p {
  margin: 10px 0 0 0;
  line-height: 1.6;
}

.treatment-text {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}

.warning-icon {
  color: #ff9800;
}

.success-icon {
  color: #4caf50;
}

.history-card {
  margin-top: 30px;
}

/* ========== 识别历史详情弹窗样式 ========== */

.dialog-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #2e7d32;
}

.dialog-subtitle {
  font-size: 13px;
  color: #2e7d32;
  font-weight: 400;
  margin-left: 4px;
}

.image-error-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #ccc;
}

.image-error-placeholder p {
  margin-top: 10px;
  font-size: 14px;
}

.detail-info-scroll {
  height: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-label {
  font-size: 14px;
  color: #2e7d32;
  font-weight: 500;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.plant-name-lg {
  font-size: 22px;
  font-weight: 600;
  color: #2e7d32;
}

.section-alert {
  margin-top: 0;
}

.section-alert p {
  margin: 8px 0 0 0;
  line-height: 1.7;
  font-size: 14px;
  color: #555;
}

.treatment-box {
  background: #f5f5f5;
  padding: 14px 16px;
  border-radius: 0 8px 8px 0;
  border-left: 4px solid #2e7d32;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}

.intro-text-detail {
  font-size: 14px;
  line-height: 1.8;
  color: #555;
  margin: 0;
}

.time-text {
  font-size: 14px;
  color: #888;
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 历史详情弹窗 body 自适应高度 */
.history-detail-body .el-dialog__body {
  flex: 1 1 auto !important;
  min-height: 0 !important;
  max-height: 70vh !important;
  overflow: hidden !important;
  display: flex !important;
  flex-direction: column !important;
}

.detail-content {
  padding: 4px 0;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.detail-layout {
  display: flex;
  gap: 24px;
  flex: 1;
  min-height: 0;
  align-items: stretch;
}

.detail-image-col {
  flex-shrink: 0;
  width: 320px;
  min-height: 0;
}

.detail-image-box {
  width: 100%;
  height: 100%;
  min-height: 300px;
  background: #f9f9f9;
  border-radius: 12px;
  overflow: hidden;
}

.detail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info-col {
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow-y: auto;
  padding-right: 8px;
}
</style>
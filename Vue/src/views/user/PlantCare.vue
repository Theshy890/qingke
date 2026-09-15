<template>
  <div class="plant-care-page">
    <div class="page-header">
      <h1>🌱 我的养护</h1>
      <el-button type="success" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加养护记录
      </el-button>
    </div>

    <div class="filter-bar">
      <el-input 
        v-model="searchQuery" 
        placeholder="搜索植物名称"
        style="width: 300px"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select v-model="filterType" placeholder="选择种类" clearable style="width: 200px" @change="loadPlantList">
        <el-option label="全部" value="" />
        <el-option label="观叶植物" value="观叶植物" />
        <el-option label="多肉植物" value="多肉植物" />
        <el-option label="开花植物" value="开花植物" />
        <el-option label="蔷薇植物" value="蔷薇植物" />
        <el-option label="香草植物" value="香草植物" />
      </el-select>
    </div>

    <div class="plant-grid" v-loading="loading">
      <el-empty v-if="!loading && filteredPlantList.length === 0" description="暂无养护记录">
        <el-button type="success" @click="openAddDialog">添加第一条记录</el-button>
      </el-empty>
      
      <div v-for="item in filteredPlantList" :key="item.id" class="plant-card">
       
        <img 
          :src="item.imgUrl || '/src/assets/img/ims.jpg'" 
          :alt="item.plantName" 
          class="plant-image"
          @error="handleImageError"
        >
       
        <div class="plant-info">
          <h3>{{ item.plantName }}</h3>
          <el-tag size="small" type="success">{{ item.categoryName }}</el-tag>
          <p class="status">
            <el-icon><Checked /></el-icon>
            状态：{{ item.growStatus }}
          </p>
          <p class="next-care">
            <el-icon><Clock /></el-icon>
            下次养护：{{ formatDate(item.nextMaintainTime) || '未设置' }}
          </p>
          <p class="care-cycle">
            <el-icon><Calendar /></el-icon>
            养护周期：{{ item.maintainCycle || '未设置' }}
          </p>
          <div class="plant-actions">
            <el-button size="small" type="primary" plain @click="viewDetail(item)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button size="small" type="success" @click="recordCare(item)">
              <el-icon><Edit /></el-icon>
              记录养护
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加养护记录对话框 -->
    <el-dialog v-model="showAddDialog" :title="editingId ? '编辑养护记录' : '添加养护记录'" width="600px" :close-on-click-modal="false" draggable class="resizable-dialog user-cartoon-dialog">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="绿植名称" prop="plantName">
          <el-input v-model="formData.plantName" placeholder="请输入绿植名称" />
        </el-form-item>
        <el-form-item label="绿植种类" prop="categoryName">
          <el-select v-model="formData.categoryName" placeholder="请选择种类" style="width: 100%">
            <el-option label="观叶植物" value="观叶植物" />
            <el-option label="多肉植物" value="多肉植物" />
            <el-option label="开花植物" value="开花植物" />
            <el-option label="蔷薇植物" value="蔷薇植物" />
            <el-option label="香草植物" value="香草植物" />
          </el-select>
        </el-form-item>
        <el-form-item label="绿植图片">
          <el-upload
            class="plant-img-uploader"
            :show-file-list="false"
            :before-upload="handlePlantImgUpload"
            :http-request="() => false"
            accept="image/*"
          >
            <el-image
              v-if="formData.imgUrl"
              :src="formData.imgUrl"
              fit="cover"
              style="width: 120px; height: 120px; border-radius: 8px; display: block;"
            />
            <div v-else class="plant-img-placeholder">
              <el-icon :size="28"><Plus /></el-icon>
              <div style="font-size: 12px; margin-top: 6px; color: #999;">点击上传</div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="生长状态" prop="growStatus">
          <el-input v-model="formData.growStatus" placeholder="如：生长良好、叶片翠绿" />
        </el-form-item>
        <el-form-item label="养护记录" prop="maintainContent">
          <el-input 
            v-model="formData.maintainContent" 
            type="textarea" 
            :rows="4"
            placeholder="记录养护内容，如：浇水500ml、施肥、修剪等"
          />
        </el-form-item>
        <el-form-item label="养护日期" prop="maintainDate">
          <el-date-picker
            v-model="formData.maintainDate"
            type="date"
            placeholder="选择养护日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="养护周期">
          <el-input v-model="formData.maintainCycle" placeholder="如：每7天、每月" />
        </el-form-item>
        <el-form-item label="下次养护">
          <el-date-picker
            v-model="formData.nextMaintainTime"
            type="datetime"
            placeholder="选择下次养护时间"
            :disabled-date="disabledNextDate"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button @click="resetForm">重置</el-button>
        <el-button type="primary" @click="handleAdd" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="养护记录详情" width="700px" draggable class="user-cartoon-dialog detail-dialog">
      <div v-if="currentPlant" class="detail-content">
        <div class="detail-image">
          <img 
            :src="currentPlant.imgUrl || 'https://via.placeholder.com/600x400?text=No+Image'" 
            :alt="currentPlant.plantName"
          >
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="绿植名称">{{ currentPlant.plantName }}</el-descriptions-item>
          <el-descriptions-item label="绿植种类">{{ currentPlant.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="生长状态">{{ currentPlant.growStatus }}</el-descriptions-item>
          <el-descriptions-item label="养护周期">{{ currentPlant.maintainCycle || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="养护日期">{{ currentPlant.maintainDate }}</el-descriptions-item>
          <el-descriptions-item label="下次养护">{{ formatDate(currentPlant.nextMaintainTime) || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="养护记录" :span="2">
            <div style="white-space: pre-wrap;">{{ currentPlant.maintainContent }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button type="primary" @click="editPlant(currentPlant)">编辑</el-button>
        <el-button type="danger" @click="deletePlant(currentPlant.id)">删除</el-button>
      </template>
    </el-dialog>

    <!-- 记录养护对话框 -->
    <el-dialog v-model="showRecordDialog" title="记录养护" width="600px" :close-on-click-modal="false" class="user-cartoon-dialog">
      <el-form :model="recordFormData" ref="recordFormRef" label-width="100px">
        <el-form-item label="绿植名称">
          <el-input v-model="recordFormData.plantName" disabled />
        </el-form-item>
        <el-form-item label="养护日期">
          <el-date-picker
            v-model="recordFormData.maintainDate"
            type="date"
            placeholder="选择养护日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="生长状态">
          <el-input v-model="recordFormData.growStatus" placeholder="更新当前生长状态" />
        </el-form-item>
        <el-form-item label="养护记录">
          <el-input 
            v-model="recordFormData.maintainContent" 
            type="textarea" 
            :rows="5"
            placeholder="记录本次养护内容..."
          />
        </el-form-item>
        <el-form-item label="下次养护">
          <el-date-picker
            v-model="recordFormData.nextMaintainTime"
            type="datetime"
            placeholder="设置下次养护时间"
            :disabled-date="disabledNextDate"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRecordDialog = false">取消</el-button>
        <el-button @click="resetRecordForm">重置</el-button>
        <el-button type="primary" @click="handleRecord" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { plantMaintainRecordApi } from '../../api/index.js'

const searchQuery = ref('')
const filterType = ref('')
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const showRecordDialog = ref(false)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const recordFormRef = ref(null)
const plantList = ref([])
const currentPlant = ref(null)
const editingId = ref(null)

// 限制下次养护时间：只能选当前时间 ~ 2100年
const disabledNextDate = (time) => {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  const maxDate = new Date('2100-12-31')
  return time.getTime() < now.getTime() || time.getTime() > maxDate.getTime()
}

// 表单数据
const formData = reactive({
  plantName: '',
  categoryName: '',
  imgUrl: '',
  growStatus: '',
  maintainContent: '',
  maintainDate: new Date().toISOString().split('T')[0],
  maintainCycle: '',
  nextMaintainTime: ''
})

// 记录养护表单
const recordFormData = reactive({
  id: null,
  plantName: '',
  categoryName: '',
  imgUrl: '',
  growStatus: '',
  maintainContent: '',
  maintainDate: new Date().toISOString().split('T')[0],
  nextMaintainTime: ''
})

const resetRecordForm = () => {
  recordFormData.growStatus = ''
  recordFormData.maintainContent = ''
  recordFormData.maintainDate = new Date().toISOString().split('T')[0]
  recordFormData.nextMaintainTime = ''
  recordFormRef.value?.clearValidate()
}

// 表单验证规则
const formRules = {
  plantName: [{ required: true, message: '请输入绿植名称', trigger: 'blur' }],
  categoryName: [{ required: true, message: '请选择绿植种类', trigger: 'change' }],
  growStatus: [{ required: true, message: '请输入生长状态', trigger: 'blur' }],
  maintainContent: [{ required: true, message: '请输入养护记录', trigger: 'blur' }],
  maintainDate: [{ required: true, message: '请选择养护日期', trigger: 'change' }]
}

// 过滤后的列表
const filteredPlantList = computed(() => {
  let list = plantList.value
  
  // 按种类过滤
  if (filterType.value) {
    list = list.filter(item => item.categoryName === filterType.value)
  }
  
  // 按名称搜索
  if (searchQuery.value) {
    list = list.filter(item => 
      item.plantName.includes(searchQuery.value)
    )
  }
  
  return list
})

// 加载养护记录列表
const loadPlantList = async () => {
  try {
    loading.value = true
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const zhanghao = userInfo.zh || userInfo.zhanghao
    
    if (!zhanghao) {
      ElMessage.warning('请先登录')
      return
    }
    
    const res = await plantMaintainRecordApi.getList(zhanghao)
    if (res.code === 200) {
      plantList.value = res.data || []
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载养护记录失败:', error)
    ElMessage.error('加载失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  // computed 会自动过滤
}

// 查看详情
const viewDetail = (item) => {
  currentPlant.value = item
  showDetailDialog.value = true
}

// 记录养护
const recordCare = (item) => {
  Object.assign(recordFormData, {
    id: item.id,
    plantName: item.plantName,
    categoryName: item.categoryName,
    imgUrl: item.imgUrl,
    growStatus: item.growStatus,
    maintainContent: item.maintainContent || '',
    maintainDate: new Date().toISOString().split('T')[0],
    maintainCycle: item.maintainCycle,
    nextMaintainTime: item.nextMaintainTime || ''
  })
  showRecordDialog.value = true
}

// 提交养护记录（区分添加/编辑）
const handleAdd = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const zhanghao = userInfo.zh || userInfo.zhanghao
    
    const data = {
      plantName: formData.plantName,
      categoryName: formData.categoryName,
      imgUrl: formData.imgUrl,
      growStatus: formData.growStatus,
      maintainContent: formData.maintainContent,
      maintainDate: formData.maintainDate,
      maintainCycle: formData.maintainCycle,
      nextMaintainTime: formData.nextMaintainTime,
      userAccount: zhanghao,
      userId: userInfo.id
    }
    
    let res
    if (editingId.value) {
      data.id = editingId.value
      res = await plantMaintainRecordApi.update(data)
    } else {
      res = await plantMaintainRecordApi.add(data)
    }
    
    if (res.code === 200) {
      const msg = editingId.value ? '更新成功，提醒已同步至【我的提醒】' : '添加成功，提醒已同步至【我的提醒】'
      ElMessage.success(msg)
      showAddDialog.value = false
      resetForm()
      loadPlantList()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    submitting.value = false
  }
}

// 保存养护记录
const handleRecord = async () => {
  try {
    submitting.value = true
    
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const zhanghao = userInfo.zh || userInfo.zhanghao
    
    const data = {
      ...recordFormData,
      userAccount: zhanghao,
      userId: userInfo.id
    }
    
    const res = await plantMaintainRecordApi.update(data)
    if (res.code === 200) {
      ElMessage.success('保存成功，提醒已同步至【我的提醒】')
      showRecordDialog.value = false
      loadPlantList()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}


// 删除
const deletePlant = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条养护记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await plantMaintainRecordApi.deleteById(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      showDetailDialog.value = false
      loadPlantList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  editingId.value = null
  Object.assign(formData, {
    plantName: '',
    categoryName: '',
    imgUrl: '',
    growStatus: '',
    maintainContent: '',
    maintainDate: new Date().toISOString().split('T')[0],
    maintainCycle: '',
    nextMaintainTime: ''
  })
  formRef.value?.clearValidate()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

// 初始化
onMounted(() => {
  loadPlantList()
})

// 打开添加对话框
const openAddDialog = () => {
  editingId.value = null
  resetForm()
  showAddDialog.value = true
}

// 编辑养护记录（从查看详情中点击编辑）
const editPlant = (item) => {
  editingId.value = item.id
  Object.assign(formData, {
    plantName: item.plantName || '',
    categoryName: item.categoryName || '',
    imgUrl: item.imgUrl || '',
    growStatus: item.growStatus || '',
    maintainContent: item.maintainContent || '',
    maintainDate: item.maintainDate || new Date().toISOString().split('T')[0],
    maintainCycle: item.maintainCycle || '',
    nextMaintainTime: item.nextMaintainTime || ''
  })
  showDetailDialog.value = false
  showAddDialog.value = true
}

const compressImage = (file, maxWidth = 800, quality = 0.75) => {
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
        canvas.getContext('2d').drawImage(img, 0, 0, width, height)
        resolve(canvas.toDataURL('image/jpeg', quality))
      }
      img.src = e.target.result
    }
    reader.readAsDataURL(file)
  })
}

const handlePlantImgUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) { ElMessage.error('只能上传图片文件！'); return false }
  if (!isLt5M) { ElMessage.error('图片大小不能超过 5MB！'); return false }
  formData.imgUrl = await compressImage(file)
  return false
}

</script>

<style scoped>
.plant-care-page {
  padding: 20px;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.plant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  min-height: 400px;
}

.plant-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
}

.plant-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.plant-image {
  width: 100%;
  height: 220px;
  object-fit: cover;
}

.plant-info {
  padding: 20px;
}

.plant-info h3 {
  margin: 0 0 12px 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.el-tag {
  margin-bottom: 12px;
}

.status, .next-care, .care-cycle {
  font-size: 14px;
  color: #666;
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.plant-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.plant-actions .el-button {
  flex: 1;
}

.detail-content {
  padding: 0 0 20px 0;
}

.detail-image {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.detail-image img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
}

.plant-img-placeholder {
  width: 120px;
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #8c939d;
  transition: border-color 0.3s;
  background: #fafafa;
}
.plant-img-placeholder:hover {
  border-color: #7c8cf8;
  color: #7c8cf8;
}

/* 重置 el-upload 内部触发区域，确保点击区域与占位块对齐 */
.plant-img-uploader :deep(.el-upload) {
  display: block;
  line-height: 0;
}

/* 养护记录详情对话框：图片顶到上边框 */
:global(.detail-dialog.el-dialog .el-dialog__body) {
  padding: 0 !important;
}

.detail-dialog .detail-content {
  padding: 0 0 20px 0;
}

.detail-dialog .detail-image {
  margin-bottom: 0;
  border-radius: 12px 12px 0 0;
}

.detail-dialog .detail-image img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
  display: block;
}

.detail-dialog .el-descriptions {
  margin: 0 20px;
}

:global(.detail-dialog.el-dialog .el-dialog__footer) {
  padding: 20px !important;
}
</style>

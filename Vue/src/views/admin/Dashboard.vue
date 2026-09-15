<template>
  <div class="dashboard-page" v-loading="loading">
    <!-- 粒子背景 -->
    <vue-particles id="tsparticles" class="particles-bg" :options="particleOptions" />

    <!-- 网格叠加层 -->
    <div class="grid-overlay"></div>

    <!-- 扫描线 -->
    <div class="scan-line"></div>

    <!-- 顶部标题横幅 -->
    <div class="top-banner">
      <div class="banner-deco-left"></div>
      <div class="banner-title">青稞绿植养护管理系统</div>
      <div class="banner-deco-right"></div>
    </div>

    <!-- 快捷操作栏 -->
    <div class="dash-topbar">
      <div class="quick-actions">
        <button class="qa-btn" @click="openNotificationDialog">
          <span class="qa-icon">🔔</span><span>发布通知</span>
        </button>
        <button class="qa-btn" @click="handleQuickAction('category')">
          <span class="qa-icon">🌱</span><span>添加绿植</span>
        </button>
        <button class="qa-btn" @click="handleQuickAction('record')">
          <span class="qa-icon">📋</span><span>查询记录</span>
        </button>
        <button class="qa-btn" @click="handleQuickAction('knowledge')">
          <span class="qa-icon">📖</span><span>添加知识</span>
        </button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="dash-body">

      <!-- 中层：系统状态 + 异常监控 -->
      <div class="dash-mid-row">
        <!-- 系统状态面板 -->
        <div class="panel health-panel hud-panel">
          <div class="panel-hud-tl"></div>
          <div class="panel-hud-br"></div>
          <div class="panel-banner">
            <div class="banner-flag"></div>
            <span class="banner-text">系统状态</span>
          </div>
          <div class="health-row">
            <div class="health-item">
              <span class="status-dot" :class="healthStatus.server ? 'dot-success' : 'dot-danger'"></span>
              <span class="h-label">服务器</span>
              <span class="h-value" :class="healthStatus.server ? 'text-success' : 'text-danger'">{{ healthStatus.server ? '运行中' : '异常' }}</span>
            </div>
            <div class="health-item">
              <span class="status-dot" :class="healthStatus.database ? 'dot-success' : 'dot-danger'"></span>
              <span class="h-label">数据库</span>
              <span class="h-value" :class="healthStatus.database ? 'text-success' : 'text-danger'">{{ healthStatus.database ? '已连接' : '断开' }}</span>
            </div>
            <div class="health-item">
              <span class="h-label">响应</span>
              <span class="h-value data-glow">{{ systemInfo.responseTime }}</span>
            </div>
            <div class="health-item">
              <span class="h-label">运行</span>
              <span class="h-value data-glow">{{ systemInfo.uptime }}</span>
            </div>
          </div>
          <!-- 六边形 CPU / 内存 / 磁盘 -->
          <div class="hex-row">
            <div class="hex-unit">
              <div class="hex-outer">
                <div class="hex-inner-bg"></div>
                <div class="hex-content">
                  <div class="hex-name">CPU</div>
                  <div class="hex-num" :class="getUsageClass(systemInfo.cpuUsage)">{{ systemInfo.cpuUsage }}<small>%</small></div>
                </div>
              </div>
              <div class="hex-bar"><div class="hex-bar-fill" :class="getUsageClass(systemInfo.cpuUsage)" :style="{ width: systemInfo.cpuUsage + '%' }"></div></div>
            </div>
            <div class="hex-unit">
              <div class="hex-outer">
                <div class="hex-inner-bg"></div>
                <div class="hex-content">
                  <div class="hex-name">内存</div>
                  <div class="hex-num" :class="getUsageClass(systemInfo.memoryUsage)">{{ systemInfo.memoryUsage }}<small>%</small></div>
                </div>
              </div>
              <div class="hex-bar"><div class="hex-bar-fill" :class="getUsageClass(systemInfo.memoryUsage)" :style="{ width: systemInfo.memoryUsage + '%' }"></div></div>
              <div class="hex-detail">{{ systemInfo.memoryUsedMB }}MB / {{ systemInfo.memoryTotalMB }}MB</div>
            </div>
            <div class="hex-unit">
              <div class="hex-outer">
                <div class="hex-inner-bg"></div>
                <div class="hex-content">
                  <div class="hex-name">磁盘</div>
                  <div class="hex-num" :class="getUsageClass(systemInfo.diskUsage)">{{ systemInfo.diskUsage }}<small>%</small></div>
                </div>
              </div>
              <div class="hex-bar"><div class="hex-bar-fill" :class="getUsageClass(systemInfo.diskUsage)" :style="{ width: systemInfo.diskUsage + '%' }"></div></div>
              <div class="hex-detail">{{ systemInfo.diskUsedGB }}GB / {{ systemInfo.diskTotalGB }}GB</div>
            </div>
          </div>
        </div>

        <!-- 异常监控 -->
        <div class="panel error-panel hud-panel">
          <div class="panel-hud-tl"></div>
          <div class="panel-hud-br"></div>
          <div class="panel-banner banner-red">
            <div class="banner-flag red"></div>
            <span class="banner-text">⚠ 异常监控</span>
          </div>
          <div class="error-body">
            <div class="error-big-num" :class="statistics.todayErrors > 0 ? 'glow-red' : 'glow-green'">{{ statistics.todayErrors }}</div>
            <div class="error-sub-label">今日异常</div>
            <div class="error-divider"></div>
            <div class="error-big-num">{{ statistics.totalErrors }}</div>
            <div class="error-sub-label">异常总数</div>
          </div>
          <button class="detail-btn" @click="loadRecentErrors">查看异常详情 →</button>
        </div>
      </div>

    </div>

    <!-- ===== 弹窗（保持原样） ===== -->
    <el-dialog v-model="userDialogVisible" title="添加用户" width="600px" :close-on-click-modal="false" draggable>
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-row :gutter="20"><el-col :span="12"><el-form-item label="账号" prop="zh"><el-input v-model="userForm.zh" placeholder="请输入账号" clearable /></el-form-item></el-col><el-col :span="12"><el-form-item label="姓名" prop="name"><el-input v-model="userForm.name" placeholder="请输入姓名" clearable /></el-form-item></el-col></el-row>
        <el-row :gutter="20"><el-col :span="12"><el-form-item label="密码" prop="password"><el-input v-model="userForm.password" type="password" placeholder="请输入密码" clearable /></el-form-item></el-col><el-col :span="12"><el-form-item label="性别" prop="gender"><el-select v-model="userForm.gender" placeholder="请选择性别" style="width:100%"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item></el-col></el-row>
        <el-row :gutter="20"><el-col :span="12"><el-form-item label="手机号" prop="phone"><el-input v-model="userForm.phone" placeholder="请输入手机号" clearable /></el-form-item></el-col><el-col :span="12"><el-form-item label="邮箱" prop="email"><el-input v-model="userForm.email" placeholder="请输入邮箱" clearable /></el-form-item></el-col></el-row>
      </el-form>
      <template #footer><el-button @click="userDialogVisible=false">取消</el-button><el-button @click="resetUserForm">重置</el-button><el-button type="primary" @click="submitUser" :loading="submitting">确定</el-button></template>
    </el-dialog>
    <el-dialog v-model="categoryDialogVisible" title="添加绿植种类" width="600px" :close-on-click-modal="false" draggable>
      <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef" label-width="100px">
        <el-form-item label="种类名称" prop="zhongleimingcheng"><el-input v-model="categoryForm.zhongleimingcheng" placeholder="请输入种类名称" clearable /></el-form-item>
        <el-form-item label="图片URL" prop="tupian"><el-input v-model="categoryForm.tupian" placeholder="请输入图片URL" clearable /></el-form-item>
        <el-form-item label="生长习性" prop="shengzhangxixing"><el-input v-model="categoryForm.shengzhangxixing" type="textarea" :rows="3" placeholder="请输入生长习性" /></el-form-item>
        <el-form-item label="养护要点" prop="yanghueyaodian"><el-input v-model="categoryForm.yanghueyaodian" type="textarea" :rows="3" placeholder="请输入养护要点" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="categoryDialogVisible=false">取消</el-button><el-button @click="resetCategoryForm">重置</el-button><el-button type="primary" @click="submitCategory" :loading="submitting">确定</el-button></template>
    </el-dialog>
    <el-dialog v-model="knowledgeDialogVisible" title="添加养护知识" width="700px" :close-on-click-modal="false" draggable>
      <el-form :model="knowledgeForm" :rules="knowledgeRules" ref="knowledgeFormRef" label-width="100px">
        <el-row :gutter="20"><el-col :span="12"><el-form-item label="植物名称" prop="plantName"><el-input v-model="knowledgeForm.plantName" placeholder="请输入植物名称" clearable /></el-form-item></el-col><el-col :span="12"><el-form-item label="绿植种类" prop="categoryName"><el-select v-model="knowledgeForm.categoryName" placeholder="请选择" style="width:100%"><el-option label="观叶植物" value="观叶植物" /><el-option label="多肉植物" value="多肉植物" /><el-option label="观花植物" value="观花植物" /><el-option label="水生植物" value="水生植物" /></el-select></el-form-item></el-col></el-row>
        <el-form-item label="图片URL" prop="imgUrl"><el-input v-model="knowledgeForm.imgUrl" placeholder="请输入图片URL" clearable /></el-form-item>
        <el-form-item label="养护教程" prop="maintainTutorial"><el-input v-model="knowledgeForm.maintainTutorial" type="textarea" :rows="5" placeholder="请输入养护教程" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="knowledgeDialogVisible=false">取消</el-button><el-button @click="resetKnowledgeForm">重置</el-button><el-button type="primary" @click="submitKnowledge" :loading="submitting">确定</el-button></template>
    </el-dialog>
    <el-dialog v-model="notificationDialogVisible" title="发布系统通知" width="600px" draggable :close-on-click-modal="false">
      <el-form :model="notificationForm" label-width="100px">
        <el-form-item label="标题"><el-input v-model="notificationForm.title" maxlength="50" show-word-limit /></el-form-item>
        <el-form-item label="类型"><el-select v-model="notificationForm.type" style="width:100%"><el-option label="系统公告" value="系统公告" /><el-option label="维护通知" value="维护通知" /><el-option label="活动通知" value="活动通知" /></el-select></el-form-item>
        <el-form-item label="内容"><el-input v-model="notificationForm.content" type="textarea" :rows="6" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button class="transparent-btn" @click="notificationDialogVisible=false">取消</el-button><el-button class="transparent-btn" @click="resetNotificationForm">重置</el-button><el-button type="primary" :loading="publishingNotification" @click="handlePublishNotification">发布</el-button></template>
    </el-dialog>
    <el-dialog v-model="errorLogVisible" width="800px" draggable>
      <template #header>
        <div style="display:flex;align-items:center;justify-content:space-between;width:100%">
          <span style="font-size:16px;font-weight:600;color:#00bfa5">系统异常日志</span>
          <el-button type="danger" plain size="small" @click="handleClearErrors">
            <el-icon><Delete /></el-icon> 清空日志
          </el-button>
        </div>
      </template>
      <div v-loading="errorLogLoading" class="error-log-scroll"><div v-if="!errorLogLoading && errorLogs.length===0" style="text-align:center;color:#7b8fa3;padding:60px 0;font-size:14px">暂无异常</div><div v-for="e in errorLogs" :key="e.id" style="padding:12px 0;border-bottom:1px solid rgba(0,191,165,0.1)"><div style="display:flex;gap:8px;align-items:center;margin-bottom:4px"><el-tag type="danger" size="small">{{ e.errorType }}</el-tag><b>{{ e.module }}</b><span style="margin-left:auto;font-size:12px;color:#7b8fa3">{{ e.createTime }}</span></div><div v-if="e.requestUrl" style="font-size:12px;color:#7b8fa3;margin-bottom:4px"><el-tag size="small" type="info">{{ e.requestMethod }}</el-tag> {{ e.requestUrl }}</div><div style="color:#ff5252;font-size:13px">{{ e.errorMessage }}</div><el-collapse v-if="e.stackTrace"><el-collapse-item title="堆栈"><pre style="font-size:11px;color:#7b8fa3;white-space:pre-wrap;max-height:150px;overflow:auto">{{ e.stackTrace }}</pre></el-collapse-item></el-collapse></div></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, GoodsFilled, DocumentCopy, Reading, Plus, Search, Monitor, Bell, Delete } from '@element-plus/icons-vue'
import { userApi, categoryApi, plantKnowledgeApi, plantMaintainRecordApi, sysNoticeApi, operationLogApi, statsApi } from '../../api'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const statistics = reactive({ userCount: 0, categoryCount: 0, recordCount: 0, knowledgeCount: 0, todayNewUsers: 0, monthRecords: 0, postCount: 0, pendingPosts: 0, commentCount: 0, aiCount: 0, todayAi: 0, storeupCount: 0, logCount: 0, todayErrors: 0, totalErrors: 0 })
const healthStatus = reactive({ server: true, database: true })
const systemInfo = reactive({ uptime: '计算中...', responseTime: '--', cpuUsage: 0, memoryUsage: 0, memoryUsedMB: 0, memoryTotalMB: 0, diskUsage: 0, diskUsedGB: 0, diskTotalGB: 0 })
const getUsageClass = (v) => v >= 80 ? 'u-danger' : v >= 60 ? 'u-warn' : 'u-safe'
const errorLogVisible = ref(false), errorLogLoading = ref(false), errorLogs = ref([])
let uptimeTimer = null, serverStartTime = null
const calculateUptime = () => { if (!serverStartTime) { systemInfo.uptime = '计算中...'; return }; const d = Date.now() - serverStartTime; systemInfo.uptime = `${Math.floor(d/864e5)}天${Math.floor(d%864e5/36e5)}时${Math.floor(d%36e5/6e4)}分` }
const userDialogVisible = ref(false), categoryDialogVisible = ref(false), knowledgeDialogVisible = ref(false)
const userFormRef = ref(null), categoryFormRef = ref(null), knowledgeFormRef = ref(null)
const userForm = reactive({ zh: '', password: '', name: '', gender: '', phone: '', email: '' })
const userRules = { zh: [{ required: true, message: '请输入账号', trigger: 'blur' }, { min: 3, max: 20, message: '3-20字符', trigger: 'blur' }], password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '6-20字符', trigger: 'blur' }], name: [{ required: true, message: '请输入姓名', trigger: 'blur' }], phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }], email: [{ type: 'email', message: '邮箱格式错误', trigger: 'blur' }] }
const categoryForm = reactive({ zhongleimingcheng: '', tupian: '', shengzhangxixing: '', yanghueyaodian: '' })
const categoryRules = { zhongleimingcheng: [{ required: true, message: '请输入种类名称', trigger: 'blur' }] }
const knowledgeForm = reactive({ plantName: '', categoryName: '', imgUrl: '', maintainTutorial: '' })
const knowledgeRules = { plantName: [{ required: true, message: '请输入植物名称', trigger: 'blur' }], categoryName: [{ required: true, message: '请选择种类', trigger: 'change' }], maintainTutorial: [{ required: true, message: '请输入教程', trigger: 'blur' }] }
const notifications = ref([])
const loadNotifications = async () => { try { const r = await sysNoticeApi.getPage({ pageNum: 1, pageSize: 5 }); if (r.code===200 && r.data?.records) notifications.value = r.data.records.map(i => ({ id: i.id, title: i.title, content: i.content, createTime: i.remindTime||i.createTime, read: i.isRead===1 })) } catch(e) {} }
const notificationDialogVisible = ref(false), publishingNotification = ref(false), notificationForm = reactive({ title: '', content: '', type: '系统公告' })
const openNotificationDialog = () => { notificationForm.title=''; notificationForm.content=''; notificationForm.type='系统公告'; notificationDialogVisible.value=true }
const resetNotificationForm = () => { notificationForm.title=''; notificationForm.content=''; notificationForm.type='系统公告' }
const handlePublishNotification = async () => { if (!notificationForm.title.trim()) { ElMessage.warning('请输入标题'); return }; if (!notificationForm.content.trim()) { ElMessage.warning('请输入内容'); return }; publishingNotification.value=true; try { const u=await userApi.getAllUsers(); if (u.code!==200||!u.data) { ElMessage.error('获取用户失败'); return }; const brief=notificationForm.content.substring(0,50)+(notificationForm.content.length>50?'...':''); await Promise.all(u.data.map(x=>sysNoticeApi.add({userId:x.id,type:notificationForm.type,title:notificationForm.title,content:notificationForm.content,brief,isRead:0}))); try { await operationLogApi.addLog({username:'admin',operationType:'add',module:'系统通知',description:`发布通知「${notificationForm.title}」，推送${u.data.length}位用户`,result:'success'}) } catch(e) {}; ElMessage.success(`已推送${u.data.length}位用户`); notificationDialogVisible.value=false; loadNotifications() } catch(e) { ElMessage.error('发布失败') } finally { publishingNotification.value=false } }
const formatDate = (d) => { const dt=new Date(d); return `${dt.getFullYear()}-${String(dt.getMonth()+1).padStart(2,'0')}-${String(dt.getDate()).padStart(2,'0')} ${String(dt.getHours()).padStart(2,'0')}:${String(dt.getMinutes()).padStart(2,'0')}` }
const loadStatistics = async () => { loading.value=true; const t=performance.now(); try { const r=await statsApi.getDashboard(); if (r.code===200 && r.data) { const d=r.data; Object.keys(statistics).forEach(k=>{statistics[k]=d[k]||0}); systemInfo.cpuUsage=d.cpuUsage||0; systemInfo.memoryUsage=d.memoryUsage||0; systemInfo.memoryUsedMB=d.memoryUsedMB||0; systemInfo.memoryTotalMB=d.memoryTotalMB||0; systemInfo.diskUsage=d.diskUsage||0; systemInfo.diskUsedGB=d.diskUsedGB||0; systemInfo.diskTotalGB=d.diskTotalGB||0; healthStatus.server=true; healthStatus.database=true; if (d.serverStartTime) { serverStartTime=d.serverStartTime; calculateUptime(); if (!uptimeTimer) uptimeTimer=setInterval(calculateUptime,60000) } }; systemInfo.responseTime=`${Math.round(performance.now()-t)}ms` } catch(e) { healthStatus.server=false } finally { loading.value=false } }
const loadRecentErrors = async () => { errorLogVisible.value=true; errorLogLoading.value=true; try { const r=await statsApi.getRecentErrors(50); if (r.code===200) errorLogs.value=r.data||[] } catch(e) {} finally { errorLogLoading.value=false } }
const handleClearErrors = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有异常日志吗？此操作不可恢复。', '警告', { confirmButtonText: '确定清空', cancelButtonText: '取消', type: 'warning' })
    errorLogLoading.value=true
    const r = await statsApi.clearErrors()
    if (r.code===200) { ElMessage.success(r.message||'清空成功'); errorLogs.value=[] }
    else ElMessage.error(r.message||'清空失败')
  } catch(e) { if (e!=='cancel') ElMessage.error('清空失败') } finally { errorLogLoading.value=false }
}
const navigateTo = (p) => router.push(p)
const handleQuickAction = (t) => { if (t==='category') { Object.assign(categoryForm,{zhongleimingcheng:'',tupian:'',shengzhangxixing:'',yanghueyaodian:''}); categoryFormRef.value?.clearValidate(); categoryDialogVisible.value=true } else if (t==='record') { router.push('/admin/plant-maintain-record') } else if (t==='knowledge') { Object.assign(knowledgeForm,{plantName:'',categoryName:'',imgUrl:'',maintainTutorial:''}); knowledgeFormRef.value?.clearValidate(); knowledgeDialogVisible.value=true } }
const resetUserForm = () => { Object.assign(userForm,{zh:'',password:'',name:'',gender:'',phone:'',email:''}); userFormRef.value?.clearValidate() }
const submitUser = async () => { try { await userFormRef.value.validate(); submitting.value=true; const r=await userApi.addUser(userForm); if (r.code===200) { ElMessage.success('添加成功'); userDialogVisible.value=false; loadStatistics() } else ElMessage.error(r.msg||'失败') } catch(e) { if (e!==false) ElMessage.error('添加失败') } finally { submitting.value=false } }
const resetCategoryForm = () => { Object.assign(categoryForm,{zhongleimingcheng:'',tupian:'',shengzhangxixing:'',yanghueyaodian:''}); categoryFormRef.value?.clearValidate() }
const submitCategory = async () => { try { await categoryFormRef.value.validate(); submitting.value=true; const r=await categoryApi.addCategory(categoryForm); if (r.code===200) { ElMessage.success('添加成功'); categoryDialogVisible.value=false; loadStatistics() } else ElMessage.error(r.msg||'失败') } catch(e) { if (e!==false) ElMessage.error('添加失败') } finally { submitting.value=false } }
const resetKnowledgeForm = () => { Object.assign(knowledgeForm,{plantName:'',categoryName:'',imgUrl:'',maintainTutorial:''}); knowledgeFormRef.value?.clearValidate() }
const submitKnowledge = async () => { try { await knowledgeFormRef.value.validate(); submitting.value=true; const r=await plantKnowledgeApi.addKnowledge(knowledgeForm); if (r.code===200) { ElMessage.success('添加成功'); knowledgeDialogVisible.value=false; loadStatistics() } else ElMessage.error(r.msg||'失败') } catch(e) { if (e!==false) ElMessage.error('添加失败') } finally { submitting.value=false } }

// 粒子配置
const particleOptions = {
  fullScreen: { enable: false },
  background: { color: { value: 'transparent' } },
  fpsLimit: 60,
  particles: {
    color: { value: ['#00bfa5', '#ff6a00', '#00e676'] },
    links: { enable: true, color: '#00bfa5', distance: 150, opacity: 0.15, width: 1 },
    move: { enable: true, speed: 0.5, direction: 'none', outModes: 'bounce' },
    number: { value: 40, density: { enable: true, area: 800 } },
    opacity: { value: { min: 0.1, max: 0.4 } },
    shape: { type: 'circle' },
    size: { value: { min: 1, max: 3 } }
  },
  detectRetina: true
}

onMounted(async () => { loadStatistics(); loadNotifications() })
onUnmounted(() => { if (uptimeTimer) clearInterval(uptimeTimer) })
</script>

<style scoped>
/* ===== 粒子背景 ===== */
.particles-bg { position: absolute; inset: 0; z-index: 0; pointer-events: none; }

/* ===== 网格叠加 ===== */
.grid-overlay { position: absolute; inset: 0; z-index: 1; pointer-events: none;
  background-image:
    repeating-linear-gradient(0deg, rgba(0,191,165,0.03) 0px, rgba(0,191,165,0.03) 1px, transparent 1px, transparent 40px),
    repeating-linear-gradient(90deg, rgba(0,191,165,0.03) 0px, rgba(0,191,165,0.03) 1px, transparent 1px, transparent 40px);
}

/* ===== 扫描线 ===== */
.scan-line { position: fixed; left: 0; right: 0; height: 2px; z-index: 998; pointer-events: none;
  background: linear-gradient(90deg, transparent 0%, rgba(0,191,165,0.1) 20%, rgba(0,191,165,0.8) 50%, rgba(0,191,165,0.1) 80%, transparent 100%);
  box-shadow: 0 0 15px rgba(0,191,165,0.5), 0 0 30px rgba(0,191,165,0.2);
  animation: scanMove 5s linear infinite; }
@keyframes scanMove { 0%{top:56px;opacity:0} 3%{opacity:1} 97%{opacity:1} 100%{top:100vh;opacity:0} }

.dashboard-page { position: relative; width: 100%; height: 100vh; z-index: 2; display: flex; flex-direction: column; overflow: hidden; }

/* ===== 顶部横幅 ===== */
.top-banner { display: flex; align-items: center; justify-content: center; gap: 20px; padding: 8px 0 4px; margin-bottom: 4px; }
.banner-title { font-size: 20px; font-weight: 700; letter-spacing: 6px; color: var(--admin-text); text-shadow: 0 0 12px rgba(0,191,165,0.4); }
.banner-deco-left, .banner-deco-right { flex: 1; height: 1px; max-width: 200px; }
.banner-deco-left { background: linear-gradient(90deg, transparent, var(--admin-data)); }
.banner-deco-right { background: linear-gradient(90deg, var(--admin-data), transparent); }

/* ===== 快捷操作 ===== */
.dash-topbar { display: flex; justify-content: center; padding: 6px 0 14px; margin-bottom: 12px; border-bottom: 1px solid rgba(0,191,165,0.12); position: relative; }
.dash-topbar::after { content:''; position:absolute; bottom:-1px; left:50%; transform:translateX(-50%); width:180px; height:1px; background:linear-gradient(90deg,transparent,var(--admin-accent),transparent); }
.quick-actions { display: flex; gap: 12px; }
.qa-btn { display:flex; align-items:center; gap:7px; padding:8px 20px; border:1px solid rgba(0,191,165,0.2); background:linear-gradient(180deg,rgba(0,191,165,0.1) 0%,rgba(0,191,165,0.03) 100%); color:var(--admin-text); font-size:13px; cursor:pointer; transition:all 0.3s; clip-path:polygon(6px 0,100% 0,calc(100% - 6px) 100%,0 100%); }
.qa-btn:hover { background:linear-gradient(180deg,rgba(0,191,165,0.2) 0%,rgba(0,191,165,0.05) 100%); border-color:var(--admin-data); color:var(--admin-data); box-shadow:0 0 15px rgba(0,191,165,0.2); }
.qa-icon { font-size: 14px; }

/* ===== 面板通用 ===== */
.hud-panel { position:relative; background:linear-gradient(160deg,rgba(6,25,55,0.92) 0%,rgba(10,30,65,0.88) 100%); border:1px solid rgba(0,191,165,0.12); padding:0 20px 16px; }
.panel-hud-tl, .panel-hud-br { position:absolute; width:20px; height:20px; pointer-events:none; z-index:3; }
.panel-hud-tl { top:-1px; left:-1px; border-top:2px solid var(--admin-data); border-left:2px solid var(--admin-data); }
.panel-hud-br { bottom:-1px; right:-1px; border-bottom:2px solid var(--admin-data); border-right:2px solid var(--admin-data); }

/* 横幅标题 */
.panel-banner { display:flex; align-items:center; gap:10px; padding:10px 16px; margin:0 -20px 14px; background:linear-gradient(90deg,#0d47a1 0%,#00bfa5 60%,rgba(0,191,165,0.1) 100%); clip-path:polygon(0 0,calc(100% - 24px) 0,100% 100%,0 100%); position:relative; }
.panel-banner.banner-red { background:linear-gradient(90deg,#e53935 0%,rgba(229,57,53,0.7) 60%,rgba(229,57,53,0.1) 100%); }
.banner-flag { width:4px; height:20px; background:#fff; border-radius:2px; opacity:0.8; }
.banner-flag.red { background:#ffcdd2; }
.banner-text { font-size:14px; font-weight:700; color:#fff; letter-spacing:3px; text-shadow:0 1px 4px rgba(0,0,0,0.4); }
.panel-link { margin-left:auto; background:none; border:1px solid rgba(255,255,255,0.3); color:#fff; padding:3px 12px; font-size:11px; cursor:pointer; transition:all 0.3s; border-radius:2px; }
.panel-link:hover { background:rgba(255,255,255,0.15); }

/* ===== 布局 ===== */
.dash-body { display:flex; flex-direction:column; gap:14px; flex:1; min-height:0; }
.dash-mid-row { display:flex; gap:16px; flex:1; min-height:0; }

/* ===== 系统健康 ===== */
.health-panel { flex:1; display:flex; flex-direction:column; }
.health-row { display:grid; grid-template-columns:repeat(4,1fr); gap:10px; margin-bottom:14px; flex-shrink:0; }
.health-item { display:flex; flex-direction:column; align-items:center; gap:4px; padding:10px 6px; background:rgba(0,191,165,0.04); border:1px solid rgba(0,191,165,0.08); }
.h-label { font-size:11px; color:var(--admin-text-secondary); letter-spacing:1px; }
.h-value { font-size:13px; font-weight:600; color:var(--admin-text); }
.data-glow { font-family:'Orbitron','Consolas',monospace; color:var(--admin-data); text-shadow:0 0 6px rgba(0,191,165,0.5); }
.status-dot { width:10px; height:10px; border-radius:50%; }
.dot-success { background:var(--admin-success); box-shadow:0 0 8px var(--admin-success),0 0 20px rgba(0,230,118,0.3); }
.dot-danger { background:var(--admin-danger); box-shadow:0 0 8px var(--admin-danger),0 0 20px rgba(255,82,82,0.3); animation:pulse 1.5s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.4} }
.text-success { color:var(--admin-success)!important; }
.text-danger { color:var(--admin-danger)!important; }

/* ===== 六边形监控 ===== */
.hex-row { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; flex:1; min-height:0; align-content:center; }
.hex-unit { text-align:center; display:flex; flex-direction:column; align-items:center; justify-content:center; }
.hex-outer { width:clamp(110px,12vw,180px); height:clamp(120px,13vw,200px); margin:0 auto 8px; position:relative; display:flex; align-items:center; justify-content: center; clip-path:polygon(50% 0%,100% 25%,100% 75%,50% 100%,0% 75%,0% 25%); background:linear-gradient(135deg,rgba(0,191,165,0.25),rgba(0,191,165,0.08)); }
.hex-inner-bg { position:absolute; inset:2px; clip-path:polygon(50% 0%,100% 25%,100% 75%,50% 100%,0% 75%,0% 25%); background:rgba(6,25,55,0.92); }
.hex-content { position:relative; z-index:1; }
.hex-name { font-size:11px; color:var(--admin-text-secondary); letter-spacing:2px; margin-bottom:2px; }
.hex-num { font-family:'Orbitron','Consolas',monospace; font-size:clamp(30px,3.5vw,52px); font-weight:700; color:var(--admin-data); text-shadow:0 0 8px rgba(0,191,165,0.6),0 0 20px rgba(0,191,165,0.2); }
.hex-num small { font-size:14px; opacity:0.7; }
.hex-bar { height:4px; background:rgba(0,191,165,0.1); border-radius:2px; overflow:hidden; }
.hex-bar-fill { height:100%; border-radius:2px; transition:width 0.8s; }
.hex-detail { font-size:10px; color:var(--admin-text-secondary); margin-top:4px; }
.u-safe { color:var(--admin-success); background:linear-gradient(90deg,var(--admin-success),rgba(0,230,118,0.4)); }
.u-warn { color:var(--admin-warning); background:linear-gradient(90deg,var(--admin-warning),rgba(255,171,0,0.4)); }
.u-danger { color:var(--admin-danger); background:linear-gradient(90deg,var(--admin-danger),rgba(255,82,82,0.4)); }

/* ===== 异常面板 ===== */
.error-panel { width:280px; flex-shrink:0; display:flex; flex-direction:column; }
.error-body { display:flex; flex-direction:column; align-items:center; gap:4px; padding:16px 0; flex:1; justify-content:center; }
.error-big-num { font-family:'Orbitron','Consolas',monospace; font-size:clamp(52px,6vw,80px); font-weight:700; color:var(--admin-data); text-shadow:0 0 10px rgba(0,191,165,0.7),0 0 30px rgba(0,191,165,0.3); line-height:1; }
.error-sub-label { font-size:12px; color:var(--admin-text-secondary); letter-spacing:1px; }
.error-divider { width:60px; height:1px; background:linear-gradient(90deg,transparent,rgba(0,191,165,0.3),transparent); margin:8px 0; }
.glow-red { color:var(--admin-danger)!important; text-shadow:0 0 10px rgba(255,82,82,0.7),0 0 30px rgba(255,82,82,0.3)!important; }
.glow-green { color:var(--admin-success)!important; text-shadow:0 0 10px rgba(0,230,118,0.7),0 0 30px rgba(0,230,118,0.3)!important; }
.detail-btn { width:100%; padding:10px; border:1px solid rgba(255,106,0,0.3); background:rgba(255,106,0,0.06); color:var(--admin-accent); font-size:12px; cursor:pointer; transition:all 0.3s; margin-top:8px; }
.detail-btn:hover { background:rgba(255,106,0,0.15); color:#fff; box-shadow:0 0 15px rgba(255,106,0,0.2); }


/* ===== 异常日志弹窗内容自适应 ===== */
.error-log-scroll {
  height: 100%;
  overflow-y: auto;
}
</style>

<template>
  <div class="newbie-guide-page">
    <div class="guide-header">
      <h1>🌱 新手引导</h1>
      <p>跟随以下步骤，快速上手青稞绿植养护系统</p>
    </div>

    <div class="guide-body">
      <div class="steps-sidebar">
        <el-steps :active="currentStep" direction="vertical" finish-status="success">
          <el-step v-for="(step, index) in steps" :key="index" :title="step.title" />
        </el-steps>
      </div>

      <div class="content-area">
        <transition name="fade-slide" mode="out-in">
          <el-card :key="currentStep" class="step-card" shadow="always">
            <div class="step-header">
              <div class="step-number">{{ currentStep + 1 }}</div>
              <div class="step-title">
                <el-icon :size="28" color="#52c41a">
                  <component :is="steps[currentStep].icon" />
                </el-icon>
                <h2>{{ steps[currentStep].title }}</h2>
              </div>
            </div>

            <div class="step-desc">
              <p>{{ steps[currentStep].desc }}</p>
            </div>

            <div class="step-features">
              <div v-for="feature in steps[currentStep].features" :key="feature" class="feature-item">
                <el-icon color="#67c23a"><Check /></el-icon>
                <span>{{ feature }}</span>
              </div>
            </div>

            <el-divider />

            <div class="step-actions">
              <el-button v-if="currentStep > 0" @click="prevStep">
                <el-icon><ArrowLeft /></el-icon> 上一步
              </el-button>

              <el-button v-if="currentStep < steps.length - 1" type="primary" @click="nextStep">
                下一步 <el-icon><ArrowRight /></el-icon>
              </el-button>

              <el-button v-else type="success" @click="finishGuide">
                <el-icon><CircleCheck /></el-icon> 完成引导
              </el-button>

              <el-button type="info" plain @click="goTry(steps[currentStep].link)">
                <el-icon><Pointer /></el-icon> {{ steps[currentStep].actionText }}
              </el-button>
            </div>
          </el-card>
        </transition>

        <el-progress
          :percentage="progressPercent"
          :stroke-width="8"
          color="#52c41a"
          class="progress-bar"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  House, Camera, Reading, Management, Bell, ChatDotRound,
  Check, ArrowLeft, ArrowRight, CircleCheck, Pointer
} from '@element-plus/icons-vue'

const router = useRouter()

const steps = [
  {
    title: '认识青稞',
    icon: 'House',
    desc: '青稞绿植养护系统是一款面向家庭绿植爱好者的智能养护平台，集绿植识别、养护知识、日常记录、智能提醒和社区互动于一体。',
    features: [
      '上传照片即可AI识别植物种类和病害',
      '丰富的养护知识库，随时查阅',
      '记录浇水、施肥等日常养护活动',
      '智能提醒，再也不怕忘记浇水',
      '社区交流，与其他绿植爱好者分享经验'
    ],
    actionText: '去首页看看',
    link: '/user/index'
  },
  {
    title: 'AI识别绿植',
    icon: 'Camera',
    desc: '遇到不认识的植物或发现绿植出现异常情况？使用AI识别功能，拍照上传即可获取植物名称、可能的病害分析及治疗建议。',
    features: [
      '支持JPG、PNG格式图片上传',
      '智谱 GLM-4.6V-Flash 一步完成植物识别与病害分析',
      '识别结果自动保存到个人记录',
      '可查看历史识别记录'
    ],
    actionText: '去识别',
    link: '/user/ai-diagnosis'
  },
  {
    title: '学习养护知识',
    icon: 'Reading',
    desc: '系统内置了丰富的绿植养护知识库，涵盖常见家庭绿植的养护要点、病虫害防治、季节养护建议等内容。',
    features: [
      '按绿植种类筛选知识',
      '支持关键词搜索',
      '收藏感兴趣的知识文章',
      '查看知识点击排行和收藏排行'
    ],
    actionText: '去看知识',
    link: '/user/knowledge'
  },
  {
    title: '添加养护记录',
    icon: 'Management',
    desc: '记录每一次浇水、施肥、修剪、换盆等养护活动，形成完整的养护历史，帮助你更好地了解植物的生长规律。',
    features: [
      '选择绿植种类和养护类型',
      '记录养护时间和详细说明',
      '查看历史养护记录时间线',
      '分析养护频率，优化养护计划'
    ],
    actionText: '去记录',
    link: '/user/plant-care'
  },
  {
    title: '设置养护提醒',
    icon: 'Bell',
    desc: '为每株绿植设置个性化的养护提醒，系统会在设定时间通过通知栏提醒你进行浇水、施肥等养护操作。',
    features: [
      '自定义提醒类型（浇水/施肥/修剪等）',
      '设置提醒周期和具体时间',
      '系统定时检测并弹出提醒',
      '标记已完成的提醒'
    ],
    actionText: '去设置',
    link: '/user/reminders'
  },
  {
    title: '加入社区',
    icon: 'ChatDotRound',
    desc: '在社区中与其他绿植爱好者交流养护心得、分享绿植美照、求助病害问题，共同进步，让养植变得更有趣。',
    features: [
      '发布帖子分享养护经验',
      '点赞、评论、互动',
      '管理员发布官方养护技巧',
      '搜索感兴趣的话题'
    ],
    actionText: '去社区',
    link: '/user/community'
  }
]

const currentStep = ref(0)

const progressPercent = computed(() => {
  return Math.round(((currentStep.value + 1) / steps.length) * 100)
})

const nextStep = () => {
  if (currentStep.value < steps.length - 1) {
    currentStep.value++
    saveProgress()
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
    saveProgress()
  }
}

const saveProgress = () => {
  localStorage.setItem('newbieGuideStep', currentStep.value.toString())
}

const goTry = (link) => {
  router.push(link)
}

const finishGuide = () => {
  localStorage.setItem('newbieGuideFinished', 'true')
  localStorage.removeItem('newbieGuideStep')
  ElMessage.success('恭喜完成新手引导！快去体验吧~')
  router.push('/user/index')
}

const restoreProgress = () => {
  const finished = localStorage.getItem('newbieGuideFinished')
  if (finished === 'true') {
    currentStep.value = 0
    return
  }
  const savedStep = localStorage.getItem('newbieGuideStep')
  if (savedStep) {
    currentStep.value = parseInt(savedStep)
  }
}

restoreProgress()
</script>

<style scoped>
.newbie-guide-page {
  min-height: 100%;
  padding: 24px;
}

.guide-header {
  text-align: center;
  margin-bottom: 32px;
}

.guide-header h1 {
  font-size: 28px;
  color: #2e7d32;
  margin: 12px 0 8px;
}

.guide-header p {
  color: #666;
  font-size: 14px;
}

.guide-body {
  display: flex;
  gap: 32px;
  max-width: 1200px;
  margin: 0 auto;
}

.steps-sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.content-area {
  flex: 1;
}

.step-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

.step-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.step-title h2 {
  margin: 0;
  color: #1a1a1a;
  font-size: 20px;
}

.step-desc p {
  color: #555;
  line-height: 1.8;
  font-size: 15px;
  margin-bottom: 20px;
}

.step-features {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #444;
  font-size: 14px;
}

.step-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.progress-bar {
  margin-top: 8px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

@media (max-width: 768px) {
  .guide-body {
    flex-direction: column;
  }

  .steps-sidebar {
    width: 100%;
  }
}
</style>
<template>
  <div class="community-page">
    <div class="page-header">
      <h2>绿植社区</h2>
      <p>分享交流绿植养护经验</p>
      <el-button type="primary" class="post-btn" @click="handleShowPostDialog">
        <el-icon><Edit /></el-icon>
        发帖
      </el-button>
    </div>

    <!-- 社区帖子列表 -->
    <div v-loading="loading" class="community-list">
      <el-empty v-if="!loading && communityList.length === 0" description="暂无帖子" />
      
      <div class="post-list">
        <div
          v-for="item in communityList"
          :key="item.id"
          class="post-card"
          @click="handleViewDetail(item)"
        >
          <div class="post-header">
            <div class="post-author" @click.stop="goToUserProfile(item.userId)">
              <el-avatar :size="40" :src="item.avatarUrl">{{ item.userAccount?.charAt(0) || 'U' }}</el-avatar>
              <div class="author-info">
                <div class="author-name">{{ item.userAccount }}</div>
                <div class="post-time">{{ formatDateTime(item.publishTime) }}</div>
              </div>
            </div>
            <template v-if="item.userId === currentUserId">
              <el-tag v-if="item.auditStatus === 1" type="success" size="small">已审核</el-tag>
              <el-tag v-else-if="item.auditStatus === 2" type="danger" size="small">审核不通过</el-tag>
              <el-tag v-else type="warning" size="small">待审核</el-tag>
            </template>
          </div>
          
          <div class="post-content">
            <h3 class="post-title">{{ item.title }}</h3>
            <div class="post-text">{{ truncateText(item.content, 150) }}</div>
            <div v-if="item.imgUrl" class="post-img-list">
              <el-image
                v-for="(img, i) in item.imgUrl.split('|').filter(s => s)"
                :key="i"
                :src="img"
                fit="cover"
                class="post-image"
              />
            </div>
          </div>
          
          <div class="post-footer">
            <div class="post-stats">
              <span class="stat-item" @click.stop="handleThumbsup(item)">
                 <span style="font-size: 15px;">👍</span>
                {{ item.likeCount || 0 }}
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ item.commentCount || 0 }}
              </span>
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
        layout="total, sizes, prev, pager, next"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 发帖对话框 -->
    <el-dialog
      v-model="postDialogVisible"
      title="发布新帖"
      width="700px"
      draggable
      class="resizable-dialog user-cartoon-dialog"
    >
      <el-form :model="postForm" :rules="postRules" ref="postFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="neirong">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="8"
            placeholder="分享你的绿植养护经验..."
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="图片">
          <div class="img-upload-list">
            <div
              v-for="(img, index) in postForm.imgUrlList"
              :key="index"
              class="img-upload-item"
            >
              <el-image :src="img" fit="cover" class="img-preview" />
              <div class="img-delete-btn" @click.stop="removePostImg(index)">
                <el-icon :size="14"><Close /></el-icon>
              </div>
            </div>
            <el-upload
              v-if="postForm.imgUrlList.length < 3"
              class="community-img-uploader"
              :show-file-list="false"
              :before-upload="handlePostImgUpload"
              :http-request="() => false"
              accept="image/*"
            >
              <div class="community-img-placeholder">
                <el-icon :size="28"><Plus /></el-icon>
                <div style="font-size: 12px; margin-top: 6px; color: #999;">
                  {{ postForm.imgUrlList.length }}/3
                </div>
              </div>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="postDialogVisible = false">取消</el-button>
        <el-button @click="resetPostForm">重置</el-button>
        <el-button type="primary" @click="handleSubmitPost">发布</el-button>
      </template>
    </el-dialog>

    <!-- 帖子详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentPost.title"
      width="900px"
      draggable
      class="detail-dialog user-cartoon-dialog"
    >
      <div v-if="currentPost.id" class="post-detail">
        <div class="detail-header">
          <div class="post-author" @click.stop="goToUserProfile(currentPost.userId)">
            <el-avatar :size="50" :src="currentPost.avatarUrl">{{ currentPost.userAccount?.charAt(0) || 'U' }}</el-avatar>
            <div class="author-info">
              <div class="author-name">{{ currentPost.userAccount }}</div>
              <div class="post-time">{{ formatDateTime(currentPost.publishTime) }}</div>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="detail-content">
          <div class="content-text">{{ currentPost.content }}</div>
          <div v-if="currentPost.imgUrl" class="detail-img-list">
            <el-image
              v-for="(img, i) in currentPost.imgUrl.split('|').filter(s => s)"
              :key="i"
              :src="img"
              fit="contain"
              class="content-image"
              :preview-src-list="currentPost.imgUrl.split('|').filter(s => s)"
              :initial-index="i"
            />
          </div>
        </div>
        
         <div class="detail-actions">
          <el-button :type="isThumbsuped ? 'warning' : 'default'" @click="handleThumbsup(currentPost)">
            <span style="font-size: 16px; margin-right: 4px;">{{ isThumbsuped ? '👍' : '👍🏻' }}</span>
            {{ isThumbsuped ? '已点赞' : '点赞' }} {{ currentPost.likeCount || 0 }}
          </el-button>
          <el-button :type="isCommunityStored ? 'warning' : 'primary'" @click="handleStoreupCommunity">
            <el-icon><Star /></el-icon>
            {{ isCommunityStored ? '取消收藏' : '收藏' }} {{ currentPost.collectCount || 0 }}
          </el-button>
        </div>
        
        <el-divider />
        
        <!-- 评论列表 -->
        <div class="comment-section">
          <h4>评论 ({{ commentTreeList.length }})</h4>

          <!-- 顶部发表评论输入框 -->
          <div class="comment-input">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="发表你的看法..."
              maxlength="500"
              show-word-limit
            />
            <div class="comment-input-actions">
              <el-button type="primary" class="submit-comment-btn" @click="handleSubmitComment">
                发表评论
              </el-button>
            </div>
          </div>

          <div class="comment-list">
            <div v-for="comment in commentTreeList" :key="comment.id" class="comment-item">
              <el-avatar :size="36" :src="comment.avatarUrl" class="comment-avatar clickable-avatar" @click.stop="goToUserProfile(comment.userId)">{{ comment.nickname?.charAt(0) || 'U' }}</el-avatar>
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-author comment-author-link" @click.stop="goToUserProfile(comment.userId)">{{ comment.nickname || '匿名用户' }}</span>
                  <span v-if="comment.userId === currentPost.userId" class="author-tag">作者</span>
                  <span class="comment-time">{{ formatRelativeTime(comment.createTime) }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
                <div v-if="comment.reply" class="comment-reply">
                  <el-icon><Warning /></el-icon>
                  管理员回复：{{ comment.reply }}
                </div>
                <div class="comment-actions">
                  <span
                    class="action-btn"
                    :class="{ 'action-btn-active': isCommentLiked(comment.id) }"
                    @click="handleThumbsupComment(comment)"
                  >
                    <span class="action-icon">{{ isCommentLiked(comment.id) ? '👍' : '👍' }}</span>
                    {{ comment.likeCount || 0 }}
                  </span>
                  <span class="action-btn" @click="handleReplyComment(comment)">
                    <el-icon><ChatDotRound /></el-icon>
                    回复
                  </span>
                </div>

                <!-- 回复列表 -->
                <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                  <div v-for="reply in getVisibleReplies(comment)" :key="reply.id" class="reply-item">
                    <el-avatar :size="28" :src="reply.avatarUrl" class="reply-avatar clickable-avatar" @click.stop="goToUserProfile(reply.userId)">{{ reply.nickname?.charAt(0) || 'U' }}</el-avatar>
                    <div class="reply-content">
                      <div class="reply-header">
                        <span class="reply-author comment-author-link" @click.stop="goToUserProfile(reply.userId)">{{ reply.nickname || '匿名用户' }}</span>
                        <span v-if="reply.replyToUserName" class="reply-to">回复 <span class="reply-to-name">@{{ reply.replyToUserName }}</span></span>
                        <span v-if="reply.userId === currentPost.userId" class="author-tag">作者</span>
                      </div>
                      <div class="reply-text">{{ reply.content }}</div>
                      <div class="reply-footer">
                        <span class="reply-time">{{ formatRelativeTime(reply.createTime) }}</span>
                        <span class="action-btn-small" @click="handleReplyToReply(comment, reply)">回复</span>
                      </div>
                    </div>
                  </div>

                  <!-- 展开/折叠回复 -->
                  <div v-if="comment.replyCount > 1" class="expand-replies" @click="handleExpandReplies(comment)">
                    <span v-if="expandedComments.has(comment.id)">收起回复</span>
                    <span v-else>展开更多回复 ({{ comment.replyCount - 1 }}条)</span>
                  </div>
                </div>

                <!-- 就近弹出的回复输入框 -->
                <div v-if="replyToComment && replyToComment.id === comment.id" class="reply-input-popup">
                  <el-input
                    v-model="replyContent"
                    type="textarea"
                    :rows="2"
                    :placeholder="replyPlaceholder"
                    maxlength="500"
                    show-word-limit
                    ref="replyInputRef"
                    autofocus
                  />
                  <div class="reply-input-actions">
                    <el-button size="small" @click="cancelReply">取消</el-button>
                    <el-button size="small" type="primary" @click="handleSubmitReply">回复</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Edit, StarFilled, ChatDotRound, Star, Warning, Plus, Close } from '@element-plus/icons-vue'
import { communityPostApi, communityCommentApi, userCollectApi } from '../../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const communityList = ref([])

const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserId = currentUserInfo.id || null

const postDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentPost = ref({})
const commentTreeList = ref([])
const commentContent = ref('')
const isCommunityStored = ref(false)
const isThumbsuped = ref(false)
const likedCommentIds = ref([])
const postFormRef = ref(null)

// 回复相关状态
const replyToComment = ref(null)  // 当前回复的主评论
const replyToUser = ref(null)     // 当前回复的目标用户
const replyPlaceholder = ref('')  // 输入框占位符
const replyContent = ref('')      // 回复内容
const visibleReplyCount = ref(1)  // 默认显示的回复数量（超过1条自动折叠）
const replyPopupPosition = ref({ top: 0, left: 0 })  // 回复弹窗位置
const showReplyPopup = ref(false)  // 是否显示回复弹窗
const expandedComments = ref(new Set())  // 已展开回复的评论ID集合
const postForm = reactive({
  title: '',
  content: '',
  imgUrlList: []
})

const resetPostForm = () => {
  postForm.title = ''
  postForm.content = ''
  postForm.imgUrlList = []
  postFormRef.value?.clearValidate()
}

const postRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ]
}

const getCommunityList = async () => {
  loading.value = true
  try {
    const res = await communityPostApi.getCommunityList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      auditStatus: 1
    })
    if (res.code === 200 && res.data) {
      communityList.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取社区列表失败')
    }
  } catch (error) {
    console.error('获取社区列表错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => {
  getCommunityList()
}

const handleCurrentChange = () => {
  getCommunityList()
}

const handleShowPostDialog = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) {
    ElMessage.warning('请先登录')
    return
  }
  postDialogVisible.value = true
}

const handleSubmitPost = async () => {
  if (!postFormRef.value) return
  await postFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        const data = {
          title: postForm.title,
          content: postForm.content,
          imgUrl: postForm.imgUrlList.join('|'),
          userId: userInfo.id,
          userAccount: userInfo.zh || userInfo.name,
          avatarUrl: userInfo.avatarUrl || ''
        }
        await communityPostApi.addCommunity(data)
        ElMessage.success('发布成功，等待管理员审核')
        postDialogVisible.value = false
        postForm.title = ''
        postForm.content = ''
        postForm.imgUrlList = []
        getCommunityList()
      } catch (error) {
        console.error('发布失败:', error)
        ElMessage.error('发布失败')
      }
    }
  })
}

const handleViewDetail = async (item) => {
  try {
    const res = await communityPostApi.getCommunityById(item.id)
    if (res.code === 200) {
      currentPost.value = res.data
      detailDialogVisible.value = true
      getCommentTree(item.id)
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      if (userInfo.id) {
        const [storeRes, thumbRes] = await Promise.all([
          userCollectApi.checkStoreup(userInfo.id, item.id, 'community_post'),
          userCollectApi.checkStoreup(userInfo.id, item.id, 'community_post_thumbsup')
        ])
        isCommunityStored.value = storeRes.data || false
        isThumbsuped.value = thumbRes.data || false
      }
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

const getCommentTree = async (postId) => {
  try {
    const res = await communityCommentApi.getCommentTree(postId)
    if (res.code === 200) {
      commentTreeList.value = res.data || []
      loadLikedComments()
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

// 获取可见的回复列表（未展开显示1条，展开后显示全部）
const getVisibleReplies = (comment) => {
  if (!comment.replies) return []
  if (expandedComments.value.has(comment.id)) {
    return comment.replies
  }
  return comment.replies.slice(0, 1)
}

// 切换展开/折叠回复
const handleExpandReplies = async (comment) => {
  if (expandedComments.value.has(comment.id)) {
    // 折叠
    expandedComments.value.delete(comment.id)
  } else {
    // 展开 - 加载全部回复
    try {
      const res = await communityCommentApi.getReplies(comment.id, 1, comment.replyCount)
      if (res.code === 200) {
        comment.replies = res.data || []
        expandedComments.value.add(comment.id)
      }
    } catch (error) {
      console.error('展开回复失败:', error)
    }
  }
}

// 回复主评论
const handleReplyComment = (comment) => {
  replyToComment.value = comment
  replyToUser.value = null
  replyPlaceholder.value = `回复 @${comment.nickname}：`
}

// 回复回复
const handleReplyToReply = (parentComment, reply) => {
  replyToComment.value = parentComment
  replyToUser.value = reply
  replyPlaceholder.value = `回复 @${reply.nickname}：`
}

// 取消回复
const cancelReply = () => {
  replyToComment.value = null
  replyToUser.value = null
  replyPlaceholder.value = ''
  replyContent.value = ''
}

// 提交回复（就近弹出）
const handleSubmitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) {
      ElMessage.warning('请先登录')
      return
    }
    const data = {
      postId: currentPost.value.id,
      userId: userInfo.id,
      nickname: userInfo.name || userInfo.zh,
      avatarUrl: userInfo.avatarUrl || '',
      content: replyContent.value,
      parentId: replyToComment.value.id,
      replyToUserId: replyToUser.value ? replyToUser.value.userId : null
    }
    await communityCommentApi.replyComment(data)
    ElMessage.success('回复成功')
    cancelReply()
    getCommentTree(currentPost.value.id)
    currentPost.value.commentCount = (currentPost.value.commentCount || 0) + 1
    const listItem = communityList.value.find(i => i.id === currentPost.value.id)
    if (listItem) listItem.commentCount = currentPost.value.commentCount
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  }
}

const loadLikedComments = () => {
  const stored = localStorage.getItem('likedComments')
  likedCommentIds.value = stored ? JSON.parse(stored) : []
}
const isCommentLiked = (commentId) => {
  return likedCommentIds.value.includes(commentId)
}
const saveLikedComments = () => {
  localStorage.setItem('likedComments', JSON.stringify(likedCommentIds.value))
}
const handleSubmitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) {
      ElMessage.warning('请先登录')
      return
    }

    if (replyToComment.value) {
      // 回复评论
      const data = {
        postId: currentPost.value.id,
        userId: userInfo.id,
        nickname: userInfo.name || userInfo.zh,
        avatarUrl: userInfo.avatarUrl || '',
        content: commentContent.value,
        parentId: replyToComment.value.id,
        replyToUserId: replyToUser.value ? replyToUser.value.userId : null
      }
      await communityCommentApi.replyComment(data)
      ElMessage.success('回复成功')
      cancelReply()
    } else {
      // 发表评论
      const data = {
        postId: currentPost.value.id,
        userId: userInfo.id,
        nickname: userInfo.name || userInfo.zh,
        avatarUrl: userInfo.avatarUrl || '',
        content: commentContent.value
      }
      await communityCommentApi.addDiscuss(data)
      ElMessage.success('评论成功，等待审核')
    }

    commentContent.value = ''
    getCommentTree(currentPost.value.id)
    currentPost.value.commentCount = (currentPost.value.commentCount || 0) + 1
    const listItem = communityList.value.find(i => i.id === currentPost.value.id)
    if (listItem) listItem.commentCount = currentPost.value.commentCount
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败')
  }
}

const handleStoreupCommunity = async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isCommunityStored.value) {
      await userCollectApi.cancelStoreup(userInfo.id, currentPost.value.id, 'community_post')
      await communityPostApi.decreaseStoreupnum(currentPost.value.id)
      isCommunityStored.value = false
      currentPost.value.collectCount = (currentPost.value.collectCount || 1) - 1
      ElMessage.success('取消收藏成功')
    } else {
      await userCollectApi.addStoreup({
        userId: userInfo.id,
        targetId: currentPost.value.id,
        targetTable: 'community_post',
        targetName: currentPost.value.title,
        imgUrl: currentPost.value.imgUrl,
        targetType: '2'
      })
      await communityPostApi.increaseStoreupnum(currentPost.value.id)
      isCommunityStored.value = true
      currentPost.value.collectCount = (currentPost.value.collectCount || 0) + 1
      ElMessage.success('收藏成功')
    }
    const listItem = communityList.value.find(i => i.id === currentPost.value.id)
    if (listItem) listItem.collectCount = currentPost.value.collectCount
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const handleThumbsup = async (item) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isThumbsuped.value) {
      await userCollectApi.cancelStoreup(userInfo.id, item.id, 'community_post_thumbsup')
      await communityPostApi.thumbsdownCommunity(item.id)
      isThumbsuped.value = false
      item.likeCount = Math.max(0, (item.likeCount || 1) - 1)
      ElMessage.success('已取消点赞')
    } else {
      await userCollectApi.addStoreup({
        userId: userInfo.id,
        targetId: item.id,
        targetTable: 'community_post_thumbsup',
        targetName: item.title,
        imgUrl: item.imgUrl,
        targetType: '3'
      })
      await communityPostApi.thumbsupCommunity(item.id)
      isThumbsuped.value = true
      item.likeCount = (item.likeCount || 0) + 1
      ElMessage.success('点赞成功')
    }
    if (currentPost.value.id === item.id) {
      currentPost.value.likeCount = item.likeCount
    }
    const listItem = communityList.value.find(i => i.id === item.id)
    if (listItem) listItem.likeCount = item.likeCount
  } catch (error) {
    if (error.response?.data?.message?.includes('已经')) {
      ElMessage.warning('您已经点过赞了')
    } else {
      ElMessage.error('操作失败')
    }
  }
}

const handleThumbsupComment = async (comment) => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isCommentLiked(comment.id)) {
      likedCommentIds.value = likedCommentIds.value.filter(id => id !== comment.id)
      saveLikedComments()
      comment.likeCount = Math.max(0, (comment.likeCount || 1) - 1)
      ElMessage.info('已取消点赞')
    } else {
      await communityCommentApi.thumbsupDiscuss(comment.id)
      likedCommentIds.value.push(comment.id)
      saveLikedComments()
      comment.likeCount = (comment.likeCount || 0) + 1
      ElMessage.success('点赞成功 👍')
    }
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('操作失败')
  }
}

const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 相对时间格式化（抖音风格）
const formatRelativeTime = (date) => {
  if (!date) return ''
  const now = new Date()
  const d = new Date(date)
  const diff = Math.floor((now - d) / 1000) // 秒数

  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  if (diff < 2592000) return `${Math.floor(diff / 86400)}天前`
  if (diff < 31536000) return `${Math.floor(diff / 2592000)}个月前`
  return `${Math.floor(diff / 31536000)}年前`
}

const compressImage = (file, maxWidth = 800, quality = 0.75) => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width, height = img.height
        if (width > maxWidth) { height = Math.round((height * maxWidth) / width); width = maxWidth }
        canvas.width = width; canvas.height = height
        canvas.getContext('2d').drawImage(img, 0, 0, width, height)
        resolve(canvas.toDataURL('image/jpeg', quality))
      }
      img.src = e.target.result
    }
    reader.readAsDataURL(file)
  })
}

const handlePostImgUpload = async (file) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片文件！'); return false }
  if (file.size / 1024 / 1024 >= 5) { ElMessage.error('图片大小不能超过 5MB！'); return false }
  if (postForm.imgUrlList.length >= 3) { ElMessage.warning('最多上传3张图片'); return false }
  const base64 = await compressImage(file)
  postForm.imgUrlList.push(base64)
  return false
}

const removePostImg = (index) => {
  postForm.imgUrlList.splice(index, 1)
}

// 跳转到用户主页（新标签页打开）
const goToUserProfile = (userId) => {
  if (!userId) {
    ElMessage.warning('用户信息不存在')
    return
  }
  const routeUrl = router.resolve(`/user/profile/${userId}`)
  window.open(routeUrl.href, '_blank')
}

// 打开帖子详情的通用方法
const openPostById = async (postId) => {
  if (!postId) return
  try {
    const res = await communityPostApi.getCommunityById(postId)
    if (res.code === 200 && res.data) {
      currentPost.value = res.data
      detailDialogVisible.value = true
      getCommentTree(postId)
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      if (userInfo.id) {
        const [storeRes, thumbRes] = await Promise.all([
          userCollectApi.checkStoreup(userInfo.id, postId, 'community_post'),
          userCollectApi.checkStoreup(userInfo.id, postId, 'community_post_thumbsup')
        ])
        isCommunityStored.value = storeRes.data || false
        isThumbsuped.value = thumbRes.data || false
      }
    }
  } catch (error) {
    console.error('加载帖子详情失败:', error)
  }
}

onMounted(async () => {
  await getCommunityList()
  // 支持从收藏页跳转：自动打开指定帖子详情
  if (route.query.id) {
    await nextTick()
    openPostById(route.query.id)
  }
})

// 监听路由参数变化（组件已挂载时再次导航）
watch(() => route.query.id, async (newId) => {
  if (newId) {
    await nextTick()
    openPostById(newId)
  }
})
</script>

<style scoped>
.community-page {
  min-height: calc(100vh - 120px);
  padding: 30px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
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

.post-btn {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
  border: none;
}

.community-list {
  max-width: 900px;
  margin: 0 auto;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

.post-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.post-author {
  display: flex;
  gap: 12px;
  align-items: center;
  cursor: pointer !important;
  transition: opacity 0.3s;
  position: relative;
  z-index: 10;
  pointer-events: auto !important;
}

.post-author * {
  cursor: pointer !important;
  pointer-events: auto !important;
}

.post-author:hover {
  opacity: 0.8;
}

.post-author:hover .author-name {
  color: #66bb6a;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-weight: 600;
  color: #2e7d32;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-content {
  margin-bottom: 15px;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
}

.post-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.post-img-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.post-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
}

.post-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}

.post-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #888;
  cursor: pointer;
  transition: color 0.3s;
}

.stat-item:hover {
  color: #4caf50;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.img-upload-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: flex-start;
}

.img-upload-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: visible;
}

.img-preview {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  display: block;
}

.img-delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 22px;
  height: 22px;
  background: #ff4d4f;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  z-index: 10;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
  transition: background 0.2s;
}

.img-delete-btn:hover {
  background: #cf1322;
}

.community-img-placeholder {
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

.community-img-placeholder:hover {
  border-color: #7c8cf8;
  color: #7c8cf8;
}

.community-img-uploader :deep(.el-upload) {
  display: block;
  line-height: 0;
}

.post-detail {
  padding: 10px;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-content {
  margin-bottom: 20px;
}

.content-text {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  margin-bottom: 20px;
}

.detail-img-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.content-image {
  width: calc(33.33% - 8px);
  min-width: 120px;
  border-radius: 8px;
  cursor: zoom-in;
}

.detail-actions {
  text-align: center;
  margin: 20px 0;
}

.comment-section h4 {
  font-size: 18px;
  color: #2e7d32;
  margin-bottom: 15px;
}

.comment-input {
  margin-bottom: 20px;
}

.submit-comment-btn {
  margin-top: 10px;
  width: 100%;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: #2e7d32;
}

.comment-author-link {
  cursor: pointer;
}

.comment-author-link:hover {
  text-decoration: underline;
}

.comment-avatar {
  cursor: pointer;
  flex-shrink: 0;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-reply {
  background: #fff3e0;
  padding: 10px;
  border-radius: 6px;
  font-size: 13px;
  color: #f57c00;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  gap: 15px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #888;
  cursor: pointer;
  transition: color 0.3s;
}

.action-btn:hover {
  color: #4caf50;
}

.action-btn-active {
  color: #4caf50;
}

/* 作者标识 */
.author-tag {
  display: inline-block;
  padding: 2px 6px;
  background: #e8f5e9;
  color: #2e7d32;
  font-size: 11px;
  border-radius: 4px;
  margin-left: 6px;
}

/* 回复列表 */
.reply-list {
  margin-top: 12px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
}

.reply-item {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-avatar {
  flex-shrink: 0;
  cursor: pointer;
}

.reply-content {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}

.reply-author {
  font-weight: 600;
  color: #2e7d32;
  font-size: 13px;
}

.reply-to {
  font-size: 12px;
  color: #999;
}

.reply-to-name {
  color: #4caf50;
  cursor: pointer;
}

.reply-to-name:hover {
  text-decoration: underline;
}

.reply-text {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 4px;
}

.reply-footer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reply-time {
  font-size: 11px;
  color: #999;
}

.action-btn-small {
  font-size: 12px;
  color: #888;
  cursor: pointer;
  transition: color 0.3s;
}

.action-btn-small:hover {
  color: #4caf50;
}

/* 展开更多回复 */
.expand-replies {
  margin-top: 8px;
  padding: 6px 12px;
  background: #e8f5e9;
  border-radius: 16px;
  display: inline-block;
  cursor: pointer;
  transition: background 0.3s;
}

.expand-replies:hover {
  background: #c8e6c9;
}

.expand-replies span {
  font-size: 12px;
  color: #2e7d32;
}

/* 就近弹出的回复输入框 */
.reply-input-popup {
  margin-top: 10px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.reply-input-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 8px;
}

/* 评论输入框操作区 */
.comment-input-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  justify-content: flex-end;
}

.submit-comment-btn {
  min-width: 100px;
}

/* 可点击头像 */
.clickable-avatar {
  cursor: pointer;
  transition: transform 0.2s;
}

.clickable-avatar:hover {
  transform: scale(1.08);
}
</style>
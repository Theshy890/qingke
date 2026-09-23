import request from '../utils/request'

export const userApi = {
  // 获取用户列表（分页）
  getUserList: (params) => {
    return request({
      url: '/api/sys-user/page',
      method: 'get',
      params
    })
  },

  // 获取单个用户
  getUserInfo: (id) => {
    return request({
      url: `/api/sys-user/${id}`,
      method: 'get'
    })
  },

  // 添加用户
  addUser: (data) => {
    return request({
      url: '/api/sys-user/add',
      method: 'post',
      data
    })
  },

  // 更新用户信息
  updateUser: (data) => {
    return request({
      url: '/api/sys-user/update',
      method: 'put',
      data
    })
  },

  // 修改密码（需验证旧密码）
  changePassword: (data) => {
    return request({
      url: '/api/sys-user/change-password',
      method: 'post',
      data
    })
  },

  // 删除用户
  deleteUser: (id) => {
    return request({
      url: `/api/sys-user/${id}`,
      method: 'delete'
    })
  },

  // 批量删除用户
  deleteUserBatch: (ids) => {
    return request({
      url: '/api/sys-user/batch',
      method: 'delete',
      data: ids
    })
  },

  // 更新用户状态
  updateUserStatus: (id, status) => {
    return request({
      url: `/api/sys-user/${id}/status`,
      method: 'put',
      params: { status }
    })
  },

  // 更新隐私设置
  updatePrivacySettings: (data) => {
    return request({
      url: '/api/sys-user/privacySettings',
      method: 'put',
      data
    })
  }
}

// 绿植种类相关API
export const categoryApi = {
  // 获取绿植种类列表（分页）
  getCategoryList: (params) => {
    return request({
      url: '/api/plant-category/page',
      method: 'get',
      params
    })
  },

  // 添加绿植种类
  addCategory: (data) => {
    return request({
      url: '/api/plant-category/add',
      method: 'post',
      data
    })
  },

  // 更新绿植种类
  updateCategory: (data) => {
    return request({
      url: '/api/plant-category/update',
      method: 'put',
      data
    })
  },

  // 删除绿植种类
  deleteCategory: (id) => {
    return request({
      url: `/api/plant-category/${id}`,
      method: 'delete'
    })
  },

  // 批量删除绿植种类
  deleteCategoryBatch: (ids) => {
    return request({
      url: '/api/plant-category/batch',
      method: 'delete',
      data: ids
    })
  }
}

// 养护知识相关API
export const plantKnowledgeApi = {
  // 获取养护知识列表（分页）
  getKnowledgeList: (params) => {
    return request({
      url: '/api/plant-knowledge/page',
      method: 'get',
      params
    })
  },

  // 获取单个养护知识详情
  getKnowledgeById: (id) => {
    return request({
      url: `/api/plant-knowledge/${id}`,
      method: 'get'
    })
  },

  // 添加养护知识
  addKnowledge: (data) => {
    return request({
      url: '/api/plant-knowledge/add',
      method: 'post',
      data
    })
  },

  // 更新养护知识
  updateKnowledge: (data) => {
    return request({
      url: '/api/plant-knowledge/update',
      method: 'put',
      data
    })
  },

  // 删除养护知识
  deleteKnowledge: (id) => {
    return request({
      url: `/api/plant-knowledge/${id}`,
      method: 'delete'
    })
  },

  // 批量删除养护知识
  deleteKnowledgeBatch: (ids) => {
    return request({
      url: '/api/plant-knowledge/batch',
      method: 'delete',
      data: ids
    })
  },

  // 增加收藏数量
  increaseStoreupnum: (id) => {
    return request({
      url: `/api/plant-knowledge/${id}/storeup`,
      method: 'put'
    })
  },

  // 减少收藏数量
  decreaseStoreupnum: (id) => {
    return request({
      url: `/api/plant-knowledge/${id}/unstoreup`,
      method: 'put'
    })
  }
}

// 养护记录相关API
export const plantMaintainRecordApi = {
  // 获取所有养护记录（管理员用）
  getAll: () => {
    return request({
      url: '/api/plant-maintain-record/all',
      method: 'get'
    })
  },

  // 添加养护记录
  add: (data) => {
    return request({
      url: '/api/plant-maintain-record/add',
      method: 'post',
      data
    })
  },

  // 查询用户的所有养护记录
  getList: (userAccount) => {
    return request({
      url: '/api/plant-maintain-record/list',
      method: 'get',
      params: { userAccount }
    })
  },

  // 更新养护记录
  update: (data) => {
    return request({
      url: '/api/plant-maintain-record/update',
      method: 'put',
      data
    })
  },

  // 删除养护记录
  deleteById: (id) => {
    return request({
      url: `/api/plant-maintain-record/${id}`,
      method: 'delete'
    })
  },

  // 批量删除
  deleteBatch: (ids) => {
    return request({
      url: '/api/plant-maintain-record/batch',
      method: 'delete',
      data: ids
    })
  }
}

// 收藏相关API
export const userCollectApi = {
  // 添加收藏
  addStoreup: (data) => {
    return request({
      url: '/api/user-collect/add',
      method: 'post',
      data
    })
  },

  // 取消收藏
  cancelStoreup: (userid, spid, tablename) => {
    return request({
      url: '/api/user-collect/cancel',
      method: 'delete',
      params: { userid, spid, tablename }
    })
  },

  // 检查是否已收藏
  checkStoreup: (userid, spid, tablename) => {
    return request({
      url: '/api/user-collect/check',
      method: 'get',
      params: { userid, spid, tablename }
    })
  },

  // 获取用户所有收藏
  getStoreupList: (userid) => {
    return request({
      url: '/api/user-collect/list',
      method: 'get',
      params: { userid }
    })
  },

  // 根据表名获取收藏列表
  getStoreupByTable: (userid, tablename) => {
    return request({
      url: '/api/user-collect/list-by-table',
      method: 'get',
      params: { userid, tablename }
    })
  }
}

// 社区互动相关API
export const communityPostApi = {
  // 获取社区互动列表（分页）
  getCommunityList: (params) => {
    return request({
      url: '/api/community-post/page',
      method: 'get',
      params
    })
  },

  // 获取所有社区互动
  getAllCommunity: () => {
    return request({
      url: '/api/community-post/list',
      method: 'get'
    })
  },

  // 获取社区互动详情
  getCommunityById: (id) => {
    return request({
      url: `/api/community-post/${id}`,
      method: 'get'
    })
  },

  // 添加社区互动
  addCommunity: (data) => {
    return request({
      url: '/api/community-post/add',
      method: 'post',
      data
    })
  },

  // 删除社区互动
  deleteCommunity: (id) => {
    return request({
      url: `/api/community-post/${id}`,
      method: 'delete'
    })
  },

  // 批量删除
  deleteCommunityBatch: (ids) => {
    return request({
      url: '/api/community-post/batch',
      method: 'delete',
      data: ids
    })
  },

  // 点赞
  thumbsupCommunity: (id) => {
    return request({
      url: `/api/community-post/${id}/thumbsup`,
      method: 'put'
    })
  },

  // 取消点赞
  thumbsdownCommunity: (id) => {
    return request({
      url: `/api/community-post/${id}/thumbsdown`,
      method: 'put'
    })
  },

  // 增加收藏数
  increaseStoreupnum: (id) => {
    return request({
      url: `/api/community-post/${id}/storeup`,
      method: 'put'
    })
  },

  // 减少收藏数
  decreaseStoreupnum: (id) => {
    return request({
      url: `/api/community-post/${id}/unstoreup`,
      method: 'put'
    })
  },

  // 获取用户发布的帖子（作品Tab）
  getUserPosts: (userId) => {
    return request({
      url: `/api/community-post/user/${userId}`,
      method: 'get'
    })
  },

  // 审核
  auditCommunity: (id, auditStatus, auditReply) => {
    return request({
      url: `/api/community-post/${id}/audit`,
      method: 'put',
      params: { auditStatus, auditReply }
    })
  }
}

// 社区评论相关API
export const communityCommentApi = {
  // 获取评论树（主评论+回复列表）
  getCommentTree: (postId) => {
    return request({
      url: `/api/community-comment/tree/${postId}`,
      method: 'get'
    })
  },

  // 获取某个主评论下的回复列表（分页）
  getReplies: (parentId, pageNum = 1, pageSize = 3) => {
    return request({
      url: `/api/community-comment/replies/${parentId}`,
      method: 'get',
      params: { pageNum, pageSize }
    })
  },

  // 添加评论
  addDiscuss: (data) => {
    return request({
      url: '/api/community-comment/add',
      method: 'post',
      data
    })
  },

  // 回复评论
  replyComment: (data) => {
    return request({
      url: '/api/community-comment/reply',
      method: 'post',
      data
    })
  },

  // 点赞评论
  thumbsupDiscuss: (id) => {
    return request({
      url: `/api/community-comment/${id}/thumbsup`,
      method: 'post'
    })
  }
}

// AI识别相关API
export const aiApi = {
  // AI识别绿植病害
  recognize: (data) => {
    return request({
      url: '/api/plant-recognize/recognize',
      method: 'post',
      data
    })
  },

  // 获取识别历史列表
  getHistoryList: (userid) => {
    return request({
      url: '/api/plant-recognize/list',
      method: 'get',
      params: { userid }
    })
  },

  // 获取所有识别记录（管理员用）
  getAllRecords: () => {
    return request({
      url: '/api/plant-recognize/all',
      method: 'get'
    })
  },

  // 删除识别记录
  deleteRecord: (id) => {
    return request({
      url: `/api/plant-recognize/${id}`,
      method: 'delete'
    })
  },

  // 批量删除识别记录
  deleteBatch: (ids) => {
    return request({
      url: '/api/plant-recognize/batch',
      method: 'delete',
      data: ids
    })
  }
}

// 操作日志相关API
export const operationLogApi = {
  // 获取最近的操作日志
  getRecentLogs: (limit = 10) => {
    return request({
      url: '/api/operation-log/recent',
      method: 'get',
      params: { limit }
    })
  },

  // 清空所有操作日志
  clearLogs: () => {
    return request({
      url: '/api/operation-log/clear',
      method: 'delete'
    })
  }
}

// 提醒相关API
export const sysNoticeApi = {
  // 分页查询提醒
  getPage: (params) => {
    return request({
      url: '/api/sys-notice/page',
      method: 'get',
      params
    })
  },

  // 获取未读提醒数量
  getUnreadCount: (userid) => {
    return request({
      url: `/api/sys-notice/unread/${userid}`,
      method: 'get'
    })
  },

  // 添加提醒
  add: (data) => {
    return request({
      url: '/api/sys-notice/add',
      method: 'post',
      data
    })
  },

  // 删除提醒
  deleteById: (id) => {
    return request({
      url: `/api/sys-notice/${id}`,
      method: 'delete'
    })
  },

  // 批量删除（所有用户的相同通知）
  deleteBatchAllUsers: (ids) => {
    return request({
      url: '/api/sys-notice/batch-all-users',
      method: 'delete',
      data: ids
    })
  },

  // 标记为已读
  markAsRead: (id) => {
    return request({
      url: `/api/sys-notice/read/${id}`,
      method: 'put'
    })
  },

  // 全部标记为已读
  markAllAsRead: (userid) => {
    return request({
      url: `/api/sys-notice/read/all/${userid}`,
      method: 'put'
    })
  }
}

// 统计相关 API
export const statsApi = {
  // 仪表盘聚合统计
  getDashboard: () => {
    return request({
      url: '/api/stats/dashboard',
      method: 'get'
    })
  },
  // 查询最近错误日志
  getRecentErrors: (limit = 20) => {
    return request({
      url: '/api/stats/errors',
      method: 'get',
      params: { limit }
    })
  },
  // 清空所有错误日志
  clearErrors: () => {
    return request({
      url: '/api/stats/errors/clear',
      method: 'delete'
    })
  }
}

// 用户主页相关 API
export const userProfileApi = {
  // 获取用户主页信息
  getUserProfile: (userId) => {
    return request({
      url: `/api/userProfile/${userId}`,
      method: 'get'
    })
  },

  // 获取用户的养护记录（分页）
  getMaintainRecords: (userId, pageNum = 1, pageSize = 10) => {
    return request({
      url: `/api/userProfile/${userId}/maintainRecords`,
      method: 'get',
      params: { pageNum, pageSize }
    })
  },

  // 获取用户的收藏内容（分页）
  getCollections: (userId, pageNum = 1, pageSize = 10) => {
    return request({
      url: `/api/userProfile/${userId}/collections`,
      method: 'get',
      params: { pageNum, pageSize }
    })
  }
}

// 用户关注相关 API
export const userFollowApi = {
  // 关注用户
  follow: (followerId, followingId) => {
    return request({
      url: '/api/userFollow/follow',
      method: 'post',
      params: { followerId, followingId }
    })
  },

  // 取消关注
  unfollow: (followerId, followingId) => {
    return request({
      url: '/api/userFollow/unfollow',
      method: 'delete',
      params: { followerId, followingId }
    })
  },

  // 获取关注列表
  getFollowingList: (userId, pageNum = 1, pageSize = 10) => {
    return request({
      url: `/api/userFollow/following/${userId}`,
      method: 'get',
      params: { pageNum, pageSize }
    })
  },

  // 获取粉丝列表
  getFollowersList: (userId, pageNum = 1, pageSize = 10) => {
    return request({
      url: `/api/userFollow/followers/${userId}`,
      method: 'get',
      params: { pageNum, pageSize }
    })
  },

  // 获取关注/粉丝统计
  getStats: (userId, currentUserId) => {
    return request({
      url: `/api/userFollow/stats/${userId}`,
      method: 'get',
      params: { currentUserId }
    })
  }
}

// 访客记录相关 API
export const userVisitorApi = {
  // 记录访问
  recordVisit: (userId, visitorId) => {
    return request({
      url: '/api/userVisitor/record',
      method: 'post',
      params: { userId, visitorId }
    })
  },
  // 获取访客列表
  getVisitorList: (userId, pageNum = 1, pageSize = 20) => {
    return request({
      url: '/api/userVisitor/list',
      method: 'get',
      params: { userId, pageNum, pageSize }
    })
  },
  // 标记访客记录已读
  markAsRead: (userId) => {
    return request({
      url: '/api/userVisitor/markRead',
      method: 'put',
      params: { userId }
    })
  },
  // 获取未读访客数量
  getUnreadCount: (userId) => {
    return request({
      url: '/api/userVisitor/unreadCount',
      method: 'get',
      params: { userId }
    })
  }
}

// 私信相关 API
export const userMessageApi = {
  // 发送私信
  sendMessage: (senderId, receiverId, content, quoteContent) => {
    return request({
      url: '/api/userMessage/send',
      method: 'post',
      params: { senderId, receiverId, content, quoteContent }
    })
  },
  // 撤回消息
  revokeMessage: (messageId, userId) => {
    return request({
      url: '/api/userMessage/revoke',
      method: 'put',
      params: { messageId, userId }
    })
  },
  // 删除消息（单边）
  deleteMessage: (messageId, userId) => {
    return request({
      url: '/api/userMessage/delete',
      method: 'delete',
      params: { messageId, userId }
    })
  },
  // 获取会话列表
  getConversations: (userId) => {
    return request({
      url: '/api/userMessage/conversations',
      method: 'get',
      params: { userId }
    })
  },
  // 获取与某用户的聊天记录
  getChatHistory: (userId, targetUserId, pageNum = 1, pageSize = 50) => {
    return request({
      url: '/api/userMessage/detail',
      method: 'get',
      params: { userId, targetUserId, pageNum, pageSize }
    })
  },
  // 标记与某用户的消息已读
  markAsRead: (userId, targetUserId) => {
    return request({
      url: '/api/userMessage/markRead',
      method: 'put',
      params: { userId, targetUserId }
    })
  },
  // 获取未读私信总数
  getUnreadCount: (userId) => {
    return request({
      url: '/api/userMessage/unreadCount',
      method: 'get',
      params: { userId }
    })
  }
}

// 消息中心相关 API
export const messageCenterApi = {
  // 获取消息中心未读总数
  getUnreadTotal: (userId) => {
    return request({
      url: '/api/messageCenter/unreadTotal',
      method: 'get',
      params: { userId }
    })
  }
}

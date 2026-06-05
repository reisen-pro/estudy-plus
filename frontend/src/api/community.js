import request from '../utils/request'

// 帖子分页
export function getPostPage(params) {
  return request.get('/community/post/page', { params })
}

// 帖子详情
export function getPostDetail(id) {
  return request.get(`/community/post/${id}`)
}

// 发帖
export function createPost(data) {
  return request.post('/community/post', data)
}

// 删除帖子
export function deletePost(id) {
  return request.delete(`/community/post/${id}`)
}

// 发评论
export function createComment(data) {
  return request.post('/community/comment', data)
}

// 帖子评论
export function getPostComments(postId) {
  return request.get(`/community/post/${postId}/comments`)
}

// 点赞
export function toggleLike(targetType, targetId) {
  return request.post('/community/like', null, { params: { targetType, targetId } })
}

// 通知列表
export function getNotifications(params) {
  return request.get('/community/notification/page', { params })
}

// 未读数
export function getUnreadCount() {
  return request.get('/community/notification/unread-count')
}

// 标记已读
export function markRead(id) {
  return request.put(`/community/notification/${id}/read`)
}

// 全部已读
export function markAllRead() {
  return request.put('/community/notification/read-all')
}

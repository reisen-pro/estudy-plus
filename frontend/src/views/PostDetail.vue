<template>
  <div v-loading="loading">
    <el-page-header @back="$router.back()" style="margin-bottom: 20px">
      <template #content><span style="font-size: 18px">{{ post.title }}</span></template>
    </el-page-header>

    <!-- 帖子正文 -->
    <el-card style="margin-bottom: 20px">
      <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 16px">
        <el-avatar :size="36" style="background: #409EFF">{{ post.authorName?.[0] || 'U' }}</el-avatar>
        <div>
          <div style="font-weight: bold">{{ post.authorName }}</div>
          <div style="color: #999; font-size: 12px">{{ post.createTime }}</div>
        </div>
        <el-tag size="small" style="margin-left: 8px">{{ post.category }}</el-tag>
        <el-tag v-if="post.isTop" type="danger" size="small">置顶</el-tag>
      </div>
      <div style="line-height: 1.8; white-space: pre-wrap">{{ post.content }}</div>
      <el-divider />
      <div style="display: flex; gap: 24px">
        <el-button :type="post.liked ? 'primary' : 'default'" text @click="handleLike">
          <el-icon><Star /></el-icon> {{ post.liked ? '已赞' : '点赞' }} ({{ post.likeCount }})
        </el-button>
        <span style="color: #999"><el-icon><View /></el-icon> {{ post.viewCount }} 浏览</span>
        <span style="color: #999"><el-icon><ChatDotSquare /></el-icon> {{ post.commentCount }} 评论</span>
      </div>
    </el-card>

    <!-- 评论区 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>评论 ({{ post.commentCount }})</span>
        </div>
      </template>

      <!-- 发评论 -->
      <div style="margin-bottom: 20px; display: flex; gap: 12px">
        <el-input v-model="commentContent" placeholder="写下你的评论..." />
        <el-button type="primary" @click="handleComment">发送</el-button>
      </div>

      <!-- 评论列表 -->
      <div v-for="c in comments" :key="c.id" style="margin-bottom: 16px">
        <div style="display: flex; gap: 10px">
          <el-avatar :size="32" style="background: #67C23A; flex-shrink: 0">{{ c.userName?.[0] || 'U' }}</el-avatar>
          <div style="flex: 1">
            <div style="font-weight: bold; font-size: 14px">{{ c.userName }} <span style="color: #999; font-weight: normal; font-size: 12px">{{ c.createTime }}</span></div>
            <div style="margin: 6px 0">{{ c.content }}</div>
            <div style="display: flex; gap: 16px; color: #999; font-size: 12px">
              <el-button text size="small" @click="handleReply(c)"><el-icon><ChatDotSquare /></el-icon> 回复</el-button>
              <el-button text size="small" @click="handleCommentLike(c)"><el-icon><Star /></el-icon> {{ c.likeCount }}</el-button>
            </div>

            <!-- 子评论 -->
            <div v-for="child in c.children" :key="child.id" style="margin-top: 12px; padding: 10px; background: #f9f9f9; border-radius: 8px">
              <div style="font-weight: bold; font-size: 13px">
                {{ child.userName }}
                <span v-if="child.replyToUserName" style="color: #999; font-weight: normal"> 回复 {{ child.replyToUserName }}</span>
                <span style="color: #999; font-weight: normal; font-size: 12px; margin-left: 8px">{{ child.createTime }}</span>
              </div>
              <div style="margin: 4px 0; font-size: 14px">{{ child.content }}</div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!comments.length" description="暂无评论，快来抢沙发" />
    </el-card>

    <!-- 回复弹窗 -->
    <el-dialog v-model="showReplyDialog" title="回复评论" width="400px">
      <el-input v-model="replyContent" type="textarea" :rows="3" />
      <template #footer>
        <el-button @click="showReplyDialog = false">取消</el-button>
        <el-button type="primary" @click="handleReplySubmit">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPostDetail, getPostComments, createComment, toggleLike } from '../api/community'
import { ElMessage } from 'element-plus'

const route = useRoute()
const postId = route.params.id
const loading = ref(false)
const post = ref({})
const comments = ref([])
const commentContent = ref('')
const showReplyDialog = ref(false)
const replyContent = ref('')
const replyTarget = ref({})

async function loadData() {
  loading.value = true
  try {
    const [postRes, commentRes] = await Promise.all([
      getPostDetail(postId),
      getPostComments(postId)
    ])
    post.value = postRes.data
    comments.value = commentRes.data || []
  } finally {
    loading.value = false
  }
}

async function handleComment() {
  if (!commentContent.value.trim()) return
  await createComment({ postId: Number(postId), content: commentContent.value })
  ElMessage.success('评论成功')
  commentContent.value = ''
  loadData()
}

function handleReply(comment) {
  replyTarget.value = comment
  replyContent.value = ''
  showReplyDialog.value = true
}

async function handleReplySubmit() {
  if (!replyContent.value.trim()) return
  await createComment({
    postId: Number(postId),
    content: replyContent.value,
    parentId: replyTarget.value.id,
    replyToUserId: replyTarget.value.userId
  })
  ElMessage.success('回复成功')
  showReplyDialog.value = false
  loadData()
}

async function handleLike() {
  const res = await toggleLike(1, Number(postId))
  post.value.liked = res.data.liked
  post.value.likeCount += res.data.liked ? 1 : -1
}

async function handleCommentLike(comment) {
  await toggleLike(2, comment.id)
  comment.likeCount++
}

onMounted(loadData)
</script>

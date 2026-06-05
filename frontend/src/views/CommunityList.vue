<template>
  <div>
    <!-- 搜索栏 -->
    <div :class="['search-bar', { 'search-bar--mobile': isMobile }]">
      <el-input v-model="query.keyword" placeholder="搜索帖子" :style="{ width: isMobile ? '100%' : '240px' }" clearable @keyup.enter="loadData">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.category" placeholder="分类" clearable @change="loadData" :style="{ width: isMobile ? '100%' : '120px' }">
        <el-option label="讨论" value="讨论" />
        <el-option label="分享" value="分享" />
        <el-option label="提问" value="提问" />
        <el-option label="公告" value="公告" />
      </el-select>
      <el-button v-if="!isMobile" type="primary" @click="showPostDialog = true"><el-icon><EditPen /></el-icon> 发帖</el-button>
      <el-button v-if="isMobile" type="primary" circle @click="showPostDialog = true"><el-icon><EditPen /></el-icon></el-button>
    </div>

    <!-- PC版帖子列表 -->
    <template v-if="!isMobile">
      <el-card v-for="post in posts" :key="post.id" shadow="hover" style="margin-bottom: 12px; cursor: pointer" @click="$router.push(`/community/post/${post.id}`)">
        <div style="display: flex; justify-content: space-between; align-items: flex-start">
          <div style="flex: 1">
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 6px">
              <el-tag v-if="post.isTop" type="danger" size="small">置顶</el-tag>
              <el-tag size="small">{{ post.category }}</el-tag>
              <h3 style="margin: 0; display: inline">{{ post.title }}</h3>
            </div>
            <div style="color: #999; font-size: 13px">
              <span>{{ post.authorName }}</span>
              <span style="margin: 0 8px">·</span>
              <span>{{ post.createTime }}</span>
            </div>
          </div>
          <div style="display: flex; gap: 16px; color: #999; font-size: 13px">
            <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
            <span><el-icon><ChatDotSquare /></el-icon> {{ post.commentCount }}</span>
            <span :style="{ color: post.liked ? '#409EFF' : '' }"><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
          </div>
        </div>
      </el-card>
    </template>

    <!-- 移动端帖子列表 -->
    <template v-if="isMobile">
      <el-card v-for="post in posts" :key="post.id" shadow="hover" style="margin-bottom: 8px; cursor: pointer" @click="$router.push(`/community/post/${post.id}`)">
        <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 6px; flex-wrap: wrap">
          <el-tag v-if="post.isTop" type="danger" size="small">置顶</el-tag>
          <el-tag size="small">{{ post.category }}</el-tag>
          <span style="font-weight: bold; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; min-width: 0">{{ post.title }}</span>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div style="color: #999; font-size: 12px">
            <span>{{ post.authorName }}</span>
            <span style="margin: 0 4px">·</span>
            <span>{{ post.createTime?.slice(5, 16) }}</span>
          </div>
          <div style="display: flex; gap: 12px; color: #999; font-size: 12px">
            <span>{{ post.viewCount }}</span>
            <span>{{ post.commentCount }}</span>
            <span :style="{ color: post.liked ? '#409EFF' : '' }">★{{ post.likeCount }}</span>
          </div>
        </div>
      </el-card>
    </template>

    <el-empty v-if="!posts.length" description="暂无帖子" />

    <el-pagination
      v-model:current-page="query.pageNum"
      :page-size="query.pageSize"
      :total="total"
      :layout="isMobile ? 'prev, next' : 'prev, pager, next'"
      @current-change="loadData"
      style="margin-top: 16px; justify-content: center"
    />

    <!-- 发帖弹窗 -->
    <el-dialog v-model="showPostDialog" title="发帖" :width="isMobile ? '92%' : '600px'">
      <el-form :model="newPost" :label-width="isMobile ? '50px' : '60px'">
        <el-form-item label="标题"><el-input v-model="newPost.title" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="newPost.category" style="width: 100%">
            <el-option label="讨论" value="讨论" />
            <el-option label="分享" value="分享" />
            <el-option label="提问" value="提问" />
            <el-option label="公告" value="公告" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="newPost.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getPostPage, createPost } from '../api/community'
import { ElMessage } from 'element-plus'

const posts = ref([])
const total = ref(0)
const showPostDialog = ref(false)
const isMobile = ref(false)
const query = ref({ keyword: '', category: '', pageNum: 1, pageSize: 10 })
const newPost = ref({ title: '', content: '', category: '讨论' })

function checkMobile() { isMobile.value = window.innerWidth < 768 }

async function loadData() {
  const res = await getPostPage(query.value)
  posts.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function handlePost() {
  if (!newPost.value.title || !newPost.value.content) return ElMessage.warning('请填写标题和内容')
  await createPost(newPost.value)
  ElMessage.success('发布成功')
  showPostDialog.value = false
  newPost.value = { title: '', content: '', category: '讨论' }
  loadData()
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadData()
})
onUnmounted(() => window.removeEventListener('resize', checkMobile))
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
.search-bar--mobile {
  flex-wrap: wrap;
  gap: 8px;
}
</style>

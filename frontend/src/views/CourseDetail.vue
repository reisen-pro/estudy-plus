<template>
  <div v-loading="loading" class="course-detail">
    <el-page-header @back="$router.back()" style="margin-bottom: 16px">
      <template #content><span style="font-size: 18px">{{ course.title }}</span></template>
    </el-page-header>

    <!-- 移动端：顶部课时切换 -->
    <div v-if="isMobile" style="margin-bottom: 12px">
      <el-button size="small" @click="showChapterDrawer = true" style="width: 100%">
        📖 课程目录 ({{ course.chapters?.flatMap(ch => ch.lessons || []).length || 0 }}课时)
      </el-button>
    </div>

    <el-row :gutter="isMobile ? 0 : 20">
      <!-- 主内容区 -->
      <el-col :xs="24" :sm="24" :md="18">

        <!-- 视频播放器 -->
        <el-card v-if="currentLesson?.lessonType === 1 && currentLesson?.mediaUrl" class="player-card" style="margin-bottom: 12px; padding: 0">
          <div class="video-wrap">
            <video
              ref="videoPlayer"
              :src="currentLesson.mediaUrl"
              controls
              preload="metadata"
              style="width: 100%; display: block"
              @timeupdate="onTimeUpdate"
              @ended="onVideoEnded"
            >您的浏览器不支持视频播放</video>
          </div>
          <div class="player-meta">
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap">
              <el-tag type="primary" size="small">📹 视频</el-tag>
              <span style="font-weight: bold; font-size: 14px">{{ currentLesson.title }}</span>
              <span v-if="currentLesson.duration" style="color: #999; font-size: 12px">{{ formatDuration(currentLesson.duration) }}</span>
            </div>
            <el-button v-if="!currentLesson.completed" type="primary" size="small" @click="markComplete(currentLesson)">标记已学</el-button>
            <el-tag v-else type="success" size="small">已学完</el-tag>
          </div>
        </el-card>

        <!-- 文档查看器 -->
        <el-card v-else-if="currentLesson?.lessonType === 2" class="player-card" style="margin-bottom: 12px">
          <div class="player-meta" style="margin-bottom: 10px">
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap">
              <el-tag type="success" size="small">📄 文档</el-tag>
              <span style="font-weight: bold; font-size: 14px">{{ currentLesson.title }}</span>
              <span style="color: #999; font-size: 12px">需阅读 {{ formatDuration(docRequired) }}</span>
              <span v-if="docTimer > 0" style="color: #67C23A; font-size: 12px">已读 {{ formatDuration(docTimer) }}</span>
            </div>
            <div style="display: flex; gap: 8px; align-items: center; margin-top: 8px">
              <el-button v-if="!currentLesson.completed && docTimer >= docRequired" type="primary" size="small" @click="markComplete(currentLesson)">标记已学</el-button>
              <el-tooltip v-else-if="!currentLesson.completed" :content="`还需阅读 ${formatDuration(docRequired - docTimer)}`">
                <el-button type="primary" size="small" disabled>标记已学</el-button>
              </el-tooltip>
              <el-tag v-else type="success" size="small">已学完</el-tag>
            </div>
          </div>
          <div v-if="currentLesson.mediaUrl" class="doc-viewer">
            <iframe :src="currentLesson.mediaUrl" style="width: 100%; height: 100%; border: none" />
          </div>
          <div v-else class="doc-placeholder">
            <el-icon style="font-size: 36px; color: #C0C4CC"><Document /></el-icon>
            <p style="color: #999; margin-top: 8px; font-size: 13px">文档内容加载中...</p>
            <p style="color: #E6A23C; font-size: 12px">请保持此页面打开，阅读计时将自动记录</p>
          </div>
        </el-card>

        <!-- 链接课时 -->
        <el-card v-else-if="currentLesson?.lessonType === 3" style="margin-bottom: 12px">
          <div style="display: flex; justify-content: space-between; align-items: center">
            <div>
              <el-tag type="warning" size="small">🔗 外链</el-tag>
              <span style="margin-left: 8px; font-weight: bold">{{ currentLesson.title }}</span>
            </div>
            <el-button v-if="currentLesson.mediaUrl" type="primary" size="small" @click="window.open(currentLesson.mediaUrl)">打开链接</el-button>
          </div>
        </el-card>

        <!-- 未选课时 -->
        <el-card v-else-if="currentLesson" style="margin-bottom: 12px">
          <el-empty description="请从目录选择课时" :image-size="80" />
        </el-card>

        <!-- 课程信息 -->
        <el-card>
          <p style="color: #666; line-height: 1.8; font-size: 14px">{{ course.description || '暂无描述' }}</p>

          <el-divider content-position="left" style="margin: 12px 0">课程目录</el-divider>

          <!-- PC端章节列表 -->
          <el-collapse v-if="!isMobile" v-model="activeChapters">
            <el-collapse-item v-for="ch in course.chapters || []" :key="ch.id" :title="ch.title" :name="ch.id">
              <div
                v-for="le in ch.lessons || []"
                :key="le.id"
                :class="['lesson-item', { 'lesson-active': currentLesson?.id === le.id }]"
                @click="playLesson(le)"
              >
                <div style="display: flex; justify-content: space-between; align-items: center; width: 100%">
                  <div style="display: flex; align-items: center; gap: 6px; min-width: 0">
                    <el-icon v-if="le.lessonType === 1" color="#409EFF" :size="14"><VideoPlay /></el-icon>
                    <el-icon v-else-if="le.lessonType === 2" color="#67C23A" :size="14"><Document /></el-icon>
                    <el-icon v-else color="#E6A23C" :size="14"><Link /></el-icon>
                    <span style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ le.title }}</span>
                  </div>
                  <div style="display: flex; align-items: center; gap: 6px; flex-shrink: 0">
                    <el-tag v-if="le.lessonType === 1" size="small" type="primary">视频</el-tag>
                    <el-tag v-else-if="le.lessonType === 2" size="small" type="success">文档</el-tag>
                    <el-tag v-else size="small" type="warning">链接</el-tag>
                    <span v-if="le.duration" style="color: #999; font-size: 12px">{{ formatDuration(le.duration) }}</span>
                    <el-tag v-if="le.completed" type="success" size="small">已学</el-tag>
                  </div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>

          <!-- 移动端章节列表（平铺） -->
          <div v-if="isMobile">
            <div
              v-for="(le, idx) in allLessons"
              :key="le.id"
              :class="['lesson-item', { 'lesson-active': currentLesson?.id === le.id }]"
              @click="playLesson(le); showChapterDrawer = false"
              style="padding: 10px 12px"
            >
              <div style="display: flex; align-items: center; gap: 8px">
                <span style="color: #999; font-size: 12px; width: 20px">{{ idx + 1 }}</span>
                <el-icon v-if="le.lessonType === 1" color="#409EFF" :size="14"><VideoPlay /></el-icon>
                <el-icon v-else-if="le.lessonType === 2" color="#67C23A" :size="14"><Document /></el-icon>
                <el-icon v-else color="#E6A23C" :size="14"><Link /></el-icon>
                <span style="flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px">{{ le.title }}</span>
                <el-tag v-if="le.completed" type="success" size="small">已学</el-tag>
                <el-tag v-if="le.duration" size="small" type="info">{{ formatDuration(le.duration) }}</el-tag>
              </div>
            </div>
          </div>

          <el-empty v-if="!course.chapters?.length" description="暂无章节" />
        </el-card>
      </el-col>

      <!-- 侧边栏（PC） -->
      <el-col v-if="!isMobile" :md="6" :sm="24" :xs="24">
        <el-card class="sidebar-card">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="分类">{{ course.categoryName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="讲师">
              <div style="display: flex; align-items: center; gap: 6px">
                <el-avatar :size="24">{{ course.teacherName?.[0] }}</el-avatar>
                <span>{{ course.teacherName || '未知' }}</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="学习人数">{{ course.studyCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="course.status === 1 ? 'success' : 'info'" size="small">{{ course.status === 1 ? '已发布' : '草稿' }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <el-divider />
          <div style="text-align: center">
            <div style="color: #999; font-size: 12px; margin-bottom: 6px">觉得课程有帮助？打赏讲师</div>
            <el-button type="warning" size="small" @click="showTipDialog = true" :icon="Present" round>打赏</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 移动端底部打赏栏 -->
    <div v-if="isMobile" class="mobile-reward-bar">
      <div style="display: flex; align-items: center; gap: 8px; flex: 1; min-width: 0">
        <el-avatar :size="28">{{ course.teacherName?.[0] }}</el-avatar>
        <div style="min-width: 0">
          <div style="font-size: 13px; font-weight: bold; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ course.teacherName || '未知' }}</div>
          <div style="font-size: 11px; color: #999">{{ course.studyCount || 0 }}人学习</div>
        </div>
      </div>
      <el-button type="warning" size="small" @click="showTipDialog = true" round>打赏</el-button>
    </div>

    <!-- 移动端目录抽屉 -->
    <el-drawer v-if="isMobile" v-model="showChapterDrawer" title="课程目录" direction="ltr" size="80%">
      <div v-for="(le, idx) in allLessons" :key="le.id" :class="['lesson-item', { 'lesson-active': currentLesson?.id === le.id }]" @click="playLesson(le); showChapterDrawer = false" style="padding: 10px 12px; cursor: pointer">
        <div style="display: flex; align-items: center; gap: 8px">
          <span style="color: #999; font-size: 12px; width: 20px">{{ idx + 1 }}</span>
          <el-icon v-if="le.lessonType === 1" color="#409EFF"><VideoPlay /></el-icon>
          <el-icon v-else-if="le.lessonType === 2" color="#67C23A"><Document /></el-icon>
          <el-icon v-else color="#E6A23C"><Link /></el-icon>
          <span style="flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ le.title }}</span>
          <el-tag v-if="le.completed" type="success" size="small">已学</el-tag>
        </div>
      </div>
    </el-drawer>

    <!-- 打赏弹窗 -->
    <el-dialog v-model="showTipDialog" title="打赏讲师" :width="isMobile ? '90%' : '400px'">
      <div style="text-align: center; margin-bottom: 16px">
        <span>打赏给 </span>
        <b>{{ course.teacherName || '讲师' }}</b>
      </div>
      <el-form label-width="80px">
        <el-form-item label="打赏积分">
          <el-input-number v-model="tipAmount" :min="0.5" :max="100" :step="0.5" :precision="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <div style="color: #999; font-size: 13px; text-align: center">你的积分余额：{{ myPoints }}</div>
      <template #footer>
        <el-button @click="showTipDialog = false">取消</el-button>
        <el-button type="warning" @click="handleTip" :loading="tipLoading">确认打赏</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCourseDetail, saveProgress } from '../api/course'
import { getPointAccount, tipCourse } from '../api/point'
import { completeLearn } from '../api/task'
import { ElMessage } from 'element-plus'
import { Present } from '@element-plus/icons-vue'

const route = useRoute()
const course = ref({})
const loading = ref(false)
const activeChapters = ref([])
const currentLesson = ref(null)
const videoPlayer = ref(null)
const showTipDialog = ref(false)
const tipAmount = ref(1)
const tipLoading = ref(false)
const myPoints = ref(0)
const docTimer = ref(0)
const docRequired = ref(180)
let docTimerInterval = null
const taskId = ref(null)
const isMobile = ref(false)
const showChapterDrawer = ref(false)

const allLessons = computed(() =>
  (course.value.chapters || []).flatMap(ch => ch.lessons || [])
)

function checkMobile() { isMobile.value = window.innerWidth < 768 }

function formatDuration(seconds) {
  if (!seconds) return ''
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return m > 0 ? `${m}分${s > 0 ? s + '秒' : ''}` : `${s}秒`
}

async function loadDetail() {
  loading.value = true
  try {
    const res = await getCourseDetail(route.params.id)
    course.value = res.data
    activeChapters.value = (res.data.chapters || []).map(c => c.id)
    for (const ch of res.data.chapters || []) {
      for (const le of ch.lessons || []) {
        if (le.lessonType === 1 && le.mediaUrl) { currentLesson.value = le; return }
      }
    }
    for (const ch of res.data.chapters || []) {
      if (ch.lessons?.length) { currentLesson.value = ch.lessons[0]; return }
    }
  } finally {
    loading.value = false
  }
}

function playLesson(lesson) {
  currentLesson.value = lesson
  docTimer.value = 0
  if (docTimerInterval) { clearInterval(docTimerInterval); docTimerInterval = null }
  if (lesson.lessonType === 2) {
    docRequired.value = lesson.duration || 180
    docTimerInterval = setInterval(() => {
      docTimer.value++
      if (docTimer.value % 30 === 0) {
        saveProgress({
          courseId: course.value.id,
          lessonId: lesson.id,
          learnDuration: docTimer.value,
          progress: Math.min(100, Math.floor(docTimer.value / docRequired.value * 100))
        })
      }
    }, 1000)
  }
  if (lesson.lessonType === 1 && lesson.mediaUrl) {
    setTimeout(() => { if (videoPlayer.value) { videoPlayer.value.load(); videoPlayer.value.play().catch(() => {}) } }, 100)
  }
}

function onTimeUpdate(e) {
  const video = e.target
  if (video.duration > 0) { watchedPercent = video.currentTime / video.duration }
}
let watchedPercent = 0

function onVideoEnded() {
  if (currentLesson.value && !currentLesson.value.completed) { markComplete(currentLesson.value) }
}

async function markComplete(lesson) {
  await saveProgress({
    courseId: course.value.id,
    lessonId: lesson.id,
    status: 1,
    learnDuration: lesson.lessonType === 2 ? docTimer.value : undefined
  })
  lesson.completed = true
  ElMessage.success('已标记完成')
  if (taskId.value) {
    const allDone = allLessons.value.every(le => le.completed || le.id === lesson.id)
    if (allDone) {
      try {
        await completeLearn(taskId.value)
        ElMessage.success('🎉 学习任务已完成！')
      } catch {}
    }
  }
}

async function loadMyPoints() {
  try { const res = await getPointAccount(); myPoints.value = res.data?.balance || 0 } catch {}
}

async function handleTip() {
  if (tipAmount.value <= 0) return ElMessage.warning('打赏积分必须大于0')
  if (tipAmount.value > myPoints.value) return ElMessage.warning('积分不足')
  tipLoading.value = true
  try {
    await tipCourse({ courseId: course.value.id, amount: tipAmount.value })
    ElMessage.success('打赏成功')
    showTipDialog.value = false
    loadMyPoints()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '打赏失败')
  } finally {
    tipLoading.value = false
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  taskId.value = route.query.taskId || null
  loadDetail()
  loadMyPoints()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  if (videoPlayer.value) videoPlayer.value.pause()
  if (docTimerInterval) clearInterval(docTimerInterval)
})
</script>

<style scoped>
.course-detail { padding-bottom: 60px; }
@media (min-width: 768px) {
  .course-detail { padding-bottom: 0; }
}
.video-wrap {
  background: #000;
  border-radius: 8px 8px 0 0;
  overflow: hidden;
}
.player-card :deep(.el-card__body) { padding: 0; }
.player-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 12px;
}
.doc-viewer {
  height: 50vh;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  overflow: auto;
}
.doc-placeholder {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
}
.sidebar-card :deep(.el-card__body) {
  padding: 14px;
}
.lesson-item {
  padding: 10px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}
.lesson-item:hover { background: #f5f7fa; }
.lesson-active {
  background: #ecf5ff;
  border-left: 3px solid #409EFF;
}
.mobile-reward-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  z-index: 998;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.06);
}
</style>

<template>
  <div>
    <el-tabs v-model="activeTab">
      <!-- 待完成任务 -->
      <el-tab-pane name="pending">
        <template #label>
          待完成 <el-badge v-if="pendingTasks.length" :value="pendingTasks.length" class="tab-badge" />
        </template>

        <div v-for="task in pendingTasks" :key="task.id" style="margin-bottom: 12px">
          <el-card shadow="hover" :class="['task-card', { 'task-card--mobile': isMobile }]">
            <!-- 标题行 -->
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px; flex-wrap: wrap">
              <el-tag :type="taskTypeColor(task.taskType)" size="small">{{ taskTypeLabel(task.taskType) }}</el-tag>
              <h3 style="margin: 0; font-size: 15px">{{ task.title }}</h3>
            </div>

            <!-- 描述 -->
            <p v-if="task.description && !isMobile" style="color: #999; font-size: 13px; margin: 4px 0">{{ task.description }}</p>

            <!-- 标签信息 -->
            <div :class="['task-meta', { 'task-meta--mobile': isMobile }]">
              <span v-if="task.courseTitle">📚 {{ task.courseTitle }}</span>
              <span v-if="task.paperTitle">📝 {{ task.paperTitle }}</span>
              <span v-if="task.deadline">⏰ {{ isMobile ? formatDate(task.deadline) : '截止: ' + task.deadline }}</span>
            </div>

            <!-- PC：步骤条 -->
            <div v-if="!isMobile" style="margin-top: 12px">
              <el-steps :active="getStepActive(task)" :space="200" finish-status="success" simple>
                <el-step v-if="task.taskType !== 2" title="学习课程" :status="task.learnStatus === 2 ? 'success' : task.learnStatus === 1 ? 'process' : 'wait'" />
                <el-step v-if="task.taskType !== 3" :title="task.examStatus === 3 ? '考试未通过' : '参加考试'" :status="task.examStatus === 2 ? 'success' : task.examStatus === 3 ? 'error' : 'process'" />
                <el-step title="完成" />
              </el-steps>
            </div>

            <!-- 移动端：步骤简化显示 -->
            <div v-if="isMobile" style="margin-top: 8px; display: flex; gap: 6px; flex-wrap: wrap">
              <el-tag :type="task.learnStatus === 2 ? 'success' : 'info'" size="small">
                {{ task.taskType === 2 ? '' : (task.learnStatus === 2 ? '✅ 学习完成' : task.learnStatus === 1 ? '📖 学习中' : '📖 待学习') }}
              </el-tag>
              <el-tag v-if="task.taskType !== 3" :type="task.examStatus === 2 ? 'success' : task.examStatus === 3 ? 'danger' : 'info'" size="small">
                {{ task.examStatus === 2 ? '✅ 考试通过' : task.examStatus === 3 ? '❌ 未通过' : '📝 待考试' }}
              </el-tag>
            </div>

            <!-- 操作按钮 -->
            <div :class="['task-actions', { 'task-actions--mobile': isMobile }]">
              <el-button v-if="task.taskType !== 2 && task.learnStatus !== 2" type="primary" size="small" @click="goLearn(task)">
                {{ task.learnStatus === 1 ? '继续学习' : '开始学习' }}
              </el-button>
              <el-tag v-if="task.taskType !== 2 && task.learnStatus === 2" type="success" size="small" style="margin-right: 8px">✅ 学习完成</el-tag>
              <el-button v-if="task.taskType !== 3 && canExam(task)" type="warning" size="small" @click="goExam(task)">
                {{ task.examStatus === 3 ? '重新考试' : '开始考试' }}
              </el-button>
              <el-tooltip v-if="task.taskType === 1 && !canExam(task) && task.learnStatus !== 2" content="请先完成学习才能参加考试">
                <el-button type="warning" size="small" disabled>开始考试</el-button>
              </el-tooltip>
            </div>
          </el-card>
        </div>

        <el-empty v-if="!pendingTasks.length" description="暂无待完成任务" />
      </el-tab-pane>

      <!-- 已完成任务 -->
      <el-tab-pane label="已完成" name="done">
        <div v-for="task in doneTasks" :key="task.id" style="margin-bottom: 12px">
          <el-card shadow="hover">
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap">
              <el-tag type="success" size="small">✅ 已完成</el-tag>
              <el-tag :type="taskTypeColor(task.taskType)" size="small">{{ taskTypeLabel(task.taskType) }}</el-tag>
              <h3 style="margin: 0; font-size: 15px">{{ task.title }}</h3>
            </div>
          </el-card>
        </div>
        <el-empty v-if="!doneTasks.length" description="暂无已完成任务" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyTasks, canStartExam, getTaskPaperId } from '../api/task'
import { startExam, getPaperDetail, getPaperQuestions } from '../api/exam'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const activeTab = ref('pending')
const tasks = ref([])
const isMobile = ref(false)

function checkMobile() { isMobile.value = window.innerWidth < 768 }

const pendingTasks = computed(() => tasks.value.filter(t => t.taskStatus !== 2))
const doneTasks = computed(() => tasks.value.filter(t => t.taskStatus === 2))

async function loadTasks() {
  const res = await getMyTasks()
  tasks.value = res.data || []
}

function canExam(task) {
  if (task.taskType === 2) return true
  return task.learnStatus === 2
}

function goLearn(task) {
  router.push(`/course/${task.courseId}?taskId=${task.id}`)
}

async function goExam(task) {
  if (task.taskType !== 2) {
    const checkRes = await canStartExam(task.id)
    if (!checkRes.data) { ElMessage.warning('请先完成学习才能参加考试'); return }
  }
  try {
    await ElMessageBox.confirm('确认开始考试？开始后计时不可暂停。', '开始考试', { type: 'warning' })
    let paperId = task.paperId
    if (!paperId) { const res = await getTaskPaperId(task.id); paperId = res.data }
    if (!paperId) { ElMessage.error('未找到关联试卷'); return }
    const res = await startExam(paperId)
    const recordId = res.data.id || res.data.recordId
    let paper = { id: paperId, title: task.title, duration: 60 }
    let questions = []
    try {
      const [paperRes, qRes] = await Promise.all([getPaperDetail(paperId), getPaperQuestions(paperId)])
      if (paperRes?.data) paper = { ...paper, ...paperRes.data }
      if (qRes?.data) questions = qRes.data
    } catch {}
    const endTime = Date.now() + (paper.duration || 60) * 60 * 1000
    sessionStorage.setItem(`exam_${recordId}`, JSON.stringify({ paper, questions, endTime, taskId: task.id }))
    ElMessage.success('考试开始')
    router.push(`/exam/take/${recordId}`)
  } catch {}
}

function taskTypeLabel(type) { return { 1: '学习+考试', 2: '纯考试', 3: '纯学习' }[type] || '未知' }
function taskTypeColor(type) { return { 1: 'warning', 2: 'danger', 3: 'success' }[type] || 'info' }

function getStepActive(task) {
  if (task.taskStatus === 2) return task.taskType === 2 ? 2 : 3
  if (task.examStatus === 2) return task.taskType === 2 ? 2 : 3
  if (task.learnStatus === 2 && task.taskType !== 2) return 1
  return 0
}

function formatDate(dt) {
  if (!dt) return ''
  return dt.slice(0, 10)
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadTasks()
})
onUnmounted(() => window.removeEventListener('resize', checkMobile))
</script>

<style scoped>
.tab-badge :deep(.el-badge__content) { top: 8px; }

.task-meta {
  display: flex;
  gap: 16px;
  color: #666;
  font-size: 13px;
  margin-top: 8px;
}
.task-meta--mobile {
  gap: 8px;
  font-size: 12px;
  flex-wrap: wrap;
}

.task-actions {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.task-actions--mobile {
  flex-wrap: wrap;
}

.task-card :deep(.el-card__body) {
  padding: 14px;
}
.task-card--mobile :deep(.el-card__body) {
  padding: 10px 12px;
}
</style>

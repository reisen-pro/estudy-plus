<template>
  <div>
    <el-alert type="info" :closable="false" style="margin-bottom: 12px">
      <template #title>
        <span style="font-size: 13px">考试为推送制，只有部门推送的考试任务才会显示</span>
      </template>
    </el-alert>

    <el-tabs v-model="activeTab">
      <!-- 待考任务 -->
      <el-tab-pane name="pending">
        <template #label>
          待完成 <el-badge v-if="examTasks.length" :value="examTasks.length" class="tab-badge" />
        </template>

        <!-- PC：卡片网格 -->
        <el-row v-if="!isMobile" :gutter="16">
          <el-col :sm="12" :md="8" v-for="task in examTasks" :key="task.id">
            <el-card shadow="hover" style="margin-bottom: 16px">
              <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 12px">
                <el-tag :type="task.taskType === 2 ? 'danger' : 'warning'" size="small">
                  {{ task.taskType === 2 ? '纯考试' : '学习+考试' }}
                </el-tag>
                <h3 style="margin: 0; font-size: 15px">{{ task.title }}</h3>
              </div>
              <div v-if="task.paperTitle" style="color: #666; font-size: 13px; margin-bottom: 8px">
                📝 试卷: {{ task.paperTitle }}
              </div>
              <div v-if="task.deadline" style="color: #E6A23C; font-size: 13px; margin-bottom: 12px">
                ⏰ 截止: {{ task.deadline }}
              </div>
              <div v-if="task.taskType === 1 && task.learnStatus !== 2" style="margin-bottom: 8px">
                <el-tag type="info" size="small">需先完成学习</el-tag>
              </div>
              <el-button
                v-if="task.taskType === 2 || task.learnStatus === 2"
                type="primary"
                style="width: 100%"
                @click="handleStart(task)"
              >
                {{ task.examStatus === 3 ? '重新考试' : '开始考试' }}
              </el-button>
              <el-tooltip v-else content="请先在任务中心完成学习">
                <el-button type="primary" style="width: 100%" disabled>开始考试</el-button>
              </el-tooltip>
            </el-card>
          </el-col>
        </el-row>

        <!-- 移动端：列表模式 -->
        <div v-if="isMobile">
          <el-card v-for="task in examTasks" :key="task.id" shadow="hover" style="margin-bottom: 10px">
            <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 8px; flex-wrap: wrap">
              <el-tag :type="task.taskType === 2 ? 'danger' : 'warning'" size="small">
                {{ task.taskType === 2 ? '纯考试' : '学习+考试' }}
              </el-tag>
              <span style="font-weight: bold; font-size: 14px">{{ task.title }}</span>
            </div>
            <div style="display: flex; gap: 8px; color: #999; font-size: 12px; margin-bottom: 8px; flex-wrap: wrap">
              <span v-if="task.paperTitle">📝 {{ task.paperTitle }}</span>
              <span v-if="task.deadline">⏰ {{ task.deadline.slice(0,10) }}</span>
            </div>
            <div v-if="task.taskType === 1 && task.learnStatus !== 2" style="margin-bottom: 8px">
              <el-tag type="info" size="small">需先完成学习</el-tag>
            </div>
            <el-button
              v-if="task.taskType === 2 || task.learnStatus === 2"
              type="primary"
              size="small"
              style="width: 100%"
              @click="handleStart(task)"
            >
              {{ task.examStatus === 3 ? '重新考试' : '开始考试' }}
            </el-button>
            <el-button v-else type="primary" size="small" style="width: 100%" disabled>开始考试</el-button>
          </el-card>
        </div>

        <el-empty v-if="!examTasks.length" description="暂无待考任务" />
      </el-tab-pane>

      <!-- 我的成绩 -->
      <el-tab-pane label="我的成绩" name="result">
        <!-- PC表格 -->
        <el-table v-if="!isMobile" :data="records" stripe>
          <el-table-column prop="paperTitle" label="试卷" />
          <el-table-column prop="score" label="得分" width="100" />
          <el-table-column prop="totalScore" label="满分" width="100" />
          <el-table-column label="结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.passed ? 'success' : 'danger'">{{ row.passed ? '通过' : '未通过' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="timeUsed" label="用时" width="120" />
          <el-table-column prop="createTime" label="考试时间" width="180" />
        </el-table>

        <!-- 移动端：卡片列表 -->
        <div v-if="isMobile">
          <el-card v-for="r in records" :key="r.id" shadow="hover" style="margin-bottom: 10px">
            <div style="display: flex; justify-content: space-between; align-items: center">
              <div style="min-width: 0; flex: 1">
                <div style="font-weight: bold; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ r.paperTitle || '考试' }}</div>
                <div style="color: #999; font-size: 12px; margin-top: 4px">{{ r.createTime }}</div>
              </div>
              <div style="text-align: right; flex-shrink: 0; margin-left: 12px">
                <div style="font-size: 18px; font-weight: bold" :style="{ color: r.passed ? '#67C23A' : '#F56C6C' }">{{ r.score }}<span style="font-size: 12px; color: #999">/{{ r.totalScore }}</span></div>
                <el-tag :type="r.passed ? 'success' : 'danger'" size="small">{{ r.passed ? '通过' : '未通过' }}</el-tag>
              </div>
            </div>
          </el-card>
        </div>

        <el-empty v-if="!records.length" description="暂无考试记录" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getMyTasks, canStartExam, getTaskPaperId } from '../api/task'
import { getMyRecords, startExam, getPaperDetail, getPaperQuestions } from '../api/exam'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const activeTab = ref('pending')
const tasks = ref([])
const records = ref([])
const isMobile = ref(false)

function checkMobile() { isMobile.value = window.innerWidth < 768 }

const examTasks = computed(() =>
  tasks.value.filter(t => t.taskType !== 3 && t.taskStatus !== 2 && t.examStatus !== 2)
)

async function loadTasks() {
  const res = await getMyTasks()
  tasks.value = res.data || []
}

async function loadRecords() {
  const res = await getMyRecords()
  records.value = res.data || []
}

async function handleStart(task) {
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

watch(activeTab, (v) => { if (v === 'pending') loadTasks(); else loadRecords() })

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadTasks()
  loadRecords()
})

onUnmounted(() => window.removeEventListener('resize', checkMobile))
</script>

<style scoped>
.tab-badge :deep(.el-badge__content) { top: 8px; }
</style>

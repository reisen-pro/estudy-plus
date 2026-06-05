<template>
  <div v-loading="loading">
    <el-card style="margin-bottom: 16px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <h2 style="margin: 0">{{ paper.title }}</h2>
        <div style="font-size: 20px; font-weight: bold; color: #E6A23C">
          剩余 {{ remainingTime }}
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="18">
        <el-card v-for="(q, idx) in questions" :key="q.id" :id="`q-${q.id}`" style="margin-bottom: 12px">
          <div style="margin-bottom: 12px">
            <el-tag size="small" style="margin-right: 8px">{{ q.questionType === 1 ? '单选' : q.questionType === 2 ? '多选' : '判断' }}</el-tag>
            <span style="font-weight: bold">{{ idx + 1 }}. {{ q.content }}</span>
            <span style="color: #999; margin-left: 8px">（{{ q.score }}分）</span>
          </div>

          <!-- 单选/判断 -->
          <el-radio-group v-if="q.questionType === 1 || q.questionType === 3" v-model="answers[q.id]">
            <el-radio v-for="opt in parseOptions(q.options)" :key="opt.key" :value="opt.key" style="display: block; margin-bottom: 8px">
              {{ opt.key }}. {{ opt.value }}
            </el-radio>
          </el-radio-group>

          <!-- 多选 -->
          <el-checkbox-group v-if="q.questionType === 2" v-model="answers[q.id]">
            <el-checkbox v-for="opt in parseOptions(q.options)" :key="opt.key" :value="opt.key" style="display: block; margin-bottom: 8px">
              {{ opt.key }}. {{ opt.value }}
            </el-checkbox>
          </el-checkbox-group>
        </el-card>
      </el-col>

      <!-- 答题卡 -->
      <el-col :span="6">
        <el-card style="position: sticky; top: 20px">
          <template #header>答题卡</template>
          <div style="display: flex; flex-wrap: wrap; gap: 8px">
            <el-button v-for="(q, idx) in questions" :key="q.id" :type="(Array.isArray(answers[q.id]) ? answers[q.id].length > 0 : !!answers[q.id]) ? 'primary' : 'default'" circle size="small" @click="scrollTo(q.id)">
              {{ idx + 1 }}
            </el-button>
          </div>
          <el-divider />
          <el-button type="success" style="width: 100%" @click="handleSubmit">交 卷</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { submitAnswer, submitExam } from '../api/exam'

const route = useRoute()
const router = useRouter()
const recordId = route.params.recordId
const loading = ref(true)
const paper = ref({})
const questions = ref([])
const answers = ref({})
const remainingTime = ref('')
let timer = null
let endTime = 0

function parseOptions(json) {
  try {
    return JSON.parse(json || '[]')
  } catch { return [] }
}

function scrollTo(qId) {
  document.getElementById(`q-${qId}`)?.scrollIntoView({ behavior: 'smooth' })
}

function updateTimer() {
  const diff = Math.max(0, endTime - Date.now())
  if (diff <= 0) {
    clearInterval(timer)
    ElMessage.warning('考试时间到，自动交卷')
    doSubmit()
    return
  }
  const m = Math.floor(diff / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  remainingTime.value = `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

async function handleSubmit() {
  const total = questions.value.length
  const answered = Object.values(answers.value).filter(v => v && (Array.isArray(v) ? v.length > 0 : true)).length
  try {
    await ElMessageBox.confirm(`已答 ${answered}/${total} 题，确认交卷？`, '交卷确认', { type: 'warning' })
    await doSubmit()
  } catch {}
}

async function doSubmit() {
  // 提交所有答案
  for (const [qId, answer] of Object.entries(answers.value)) {
    const q = questions.value.find(x => x.id === Number(qId))
    if (!q || !answer) continue
    await submitAnswer({
      recordId: Number(recordId),
      questionId: Number(qId),
      userAnswer: Array.isArray(answer) ? answer.join(',') : answer
    })
  }
  // 交卷
  const res = await submitExam(recordId)
  clearInterval(timer)
  ElMessage.success('交卷成功')
  router.push('/exam/result')
}

onMounted(async () => {
  try {
    // 从 exam record 获取试卷和题目信息
    // 这里简化处理：从 route query 取（实际应从API获取）
    const data = JSON.parse(sessionStorage.getItem(`exam_${recordId}`) || '{}')
    paper.value = data.paper || {}
    questions.value = data.questions || []
    endTime = data.endTime || Date.now() + 60 * 60 * 1000

    // 初始化答案对象
    for (const q of questions.value) {
      answers.value[q.id] = q.questionType === 2 ? [] : ''
    }

    updateTimer()
    timer = setInterval(updateTimer, 1000)
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

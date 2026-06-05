<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>上传课程</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-steps :active="step" align-center style="margin-bottom: 30px">
        <el-step title="基本信息" />
        <el-step title="章节目录" />
        <el-step title="发布" />
      </el-steps>

      <!-- Step 1: 基本信息 -->
      <el-form v-show="step === 0" :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 600px; margin: 0 auto">
        <el-form-item label="课程标题" prop="title">
          <el-input v-model="form.title" placeholder="输入课程标题" />
        </el-form-item>
        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="form.subtitle" placeholder="输入副标题（可选）" />
        </el-form-item>
        <el-form-item label="课程分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程类型">
          <el-radio-group v-model="form.courseType">
            <el-radio :value="0">免费</el-radio>
            <el-radio :value="1">付费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.courseType === 1" label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="课程描述" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="nextStep">下一步</el-button>
        </el-form-item>
      </el-form>

      <!-- Step 2: 章节目录 -->
      <div v-show="step === 1">
        <div v-for="(ch, ci) in chapters" :key="ci" style="margin-bottom: 16px; border: 1px solid #ebeef5; border-radius: 8px; padding: 16px">
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 12px">
            <el-tag>第{{ ci + 1 }}章</el-tag>
            <el-input v-model="ch.title" placeholder="章节标题" style="flex: 1" />
            <el-button type="danger" size="small" @click="chapters.splice(ci, 1)">删除章节</el-button>
          </div>
          <div v-for="(le, li) in ch.lessons" :key="li" style="margin-left: 20px; margin-bottom: 8px; display: flex; align-items: center; gap: 8px">
            <el-tag type="info" size="small">课时</el-tag>
            <el-input v-model="le.title" placeholder="课时标题" style="width: 200px" />
            <el-select v-model="le.lessonType" style="width: 100px">
              <el-option label="视频" :value="1" />
              <el-option label="文档" :value="2" />
              <el-option label="链接" :value="3" />
            </el-select>
            <el-input v-model="le.mediaUrl" placeholder="视频/文档URL" style="flex: 1" />
            <el-input-number v-model="le.duration" :min="0" placeholder="秒" style="width: 100px" />
            <el-button type="danger" size="small" @click="ch.lessons.splice(li, 1)">删除</el-button>
          </div>
          <el-button size="small" style="margin-left: 20px" @click="ch.lessons.push({ title: '', lessonType: 1, mediaUrl: '', duration: 0 })">+ 添加课时</el-button>
        </div>
        <el-button type="primary" @click="chapters.push({ title: '', lessons: [] })">+ 添加章节</el-button>
        <div style="margin-top: 20px; text-align: center">
          <el-button @click="step = 0">上一步</el-button>
          <el-button type="primary" @click="nextStep">下一步</el-button>
        </div>
      </div>

      <!-- Step 3: 确认发布 -->
      <div v-show="step === 2" style="text-align: center; padding: 40px">
        <el-result icon="info" title="确认发布课程？" sub-title="发布后学员即可看到此课程">
          <template #extra>
            <el-button @click="step = 1">上一步</el-button>
            <el-button type="primary" @click="submitCourse" :loading="submitting">确认创建</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createCourse } from '../api/course'
import { getCategoryTree } from '../api/course'
import { ElMessage } from 'element-plus'

const router = useRouter()
const step = ref(0)
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])

const form = ref({
  title: '',
  subtitle: '',
  categoryId: null,
  courseType: 0,
  price: 0,
  description: ''
})

const chapters = ref([
  { title: '', lessons: [{ title: '', lessonType: 1, mediaUrl: '', duration: 0 }] }
])

const rules = {
  title: [{ required: true, message: '请输入课程标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

async function nextStep() {
  if (step.value === 0) {
    try {
      await formRef.value.validate()
    } catch {
      return
    }
  }
  step.value++
}

async function submitCourse() {
  submitting.value = true
  try {
    const courseId = await createCourse(form.value)
    ElMessage.success('课程创建成功')
    router.push('/course')
  } catch (e) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } catch {}
})
</script>

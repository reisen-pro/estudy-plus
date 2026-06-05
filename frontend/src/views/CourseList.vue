<template>
  <div>
    <!-- 搜索栏 -->
    <div :class="['search-bar', { 'search-bar--mobile': isMobile }]">
      <el-input v-model="query.keyword" placeholder="搜索课程" :style="{ width: isMobile ? '100%' : '240px' }" clearable @clear="loadData" @keyup.enter="loadData">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.categoryId" placeholder="课程分类" clearable @change="loadData" :style="{ width: isMobile ? '100%' : '160px' }">
        <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
      </el-select>
      <el-button v-if="hasUploadPerm && !isMobile" type="primary" @click="$router.push('/course/upload')"><el-icon><Upload /></el-icon> 上传课程</el-button>
      <el-button v-if="hasUploadPerm && isMobile" type="primary" circle @click="$router.push('/course/upload')"><el-icon><Upload /></el-icon></el-button>
    </div>

    <!-- 课程卡片 -->
    <el-row :gutter="isMobile ? 8 : 16">
      <el-col :xs="12" :sm="8" :md="6" v-for="course in courses" :key="course.id">
        <el-card shadow="hover" :class="['course-card', { 'course-card--mobile': isMobile }]" @click="$router.push(`/course/${course.id}`)">
          <div class="course-card__cover">
            <el-icon style="font-size: 40px; color: #fff"><Reading /></el-icon>
          </div>
          <h3 class="course-card__title">{{ course.title }}</h3>
          <div class="course-card__meta">
            <span>{{ course.teacherName || '未知' }}</span>
            <span>{{ course.viewCount || 0 }}人</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!courses.length && !loading" description="暂无课程" />

    <!-- 新建课程弹窗 -->
    <el-dialog v-model="showAddDialog" title="新建课程" :width="isMobile ? '90%' : '500px'">
      <el-form :model="newCourse" :label-width="isMobile ? '60px' : '80px'">
        <el-form-item label="课程名称"><el-input v-model="newCourse.title" /></el-form-item>
        <el-form-item label="课程分类"><el-select v-model="newCourse.categoryId" placeholder="选择分类" style="width: 100%">
          <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select></el-form-item>
        <el-form-item label="课程描述"><el-input v-model="newCourse.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getCourseList, getCategoryTree, createCourse } from '../api/course'
import { ElMessage } from 'element-plus'

const courses = ref([])
const categories = ref([])
const loading = ref(false)
const showAddDialog = ref(false)
const isMobile = ref(false)
const query = ref({ keyword: '', categoryId: null })
const hasUploadPerm = ref(false)
const newCourse = ref({ title: '', categoryId: null, description: '' })

function checkMobile() { isMobile.value = window.innerWidth < 768 }

function checkUploadPerm() {
  const perms = JSON.parse(localStorage.getItem('userPermissions') || '[]')
  hasUploadPerm.value = perms.includes('course:upload')
}

function flattenTree(nodes = []) {
  let result = []
  for (const n of nodes) {
    result.push(n)
    if (n.children?.length) result = result.concat(flattenTree(n.children))
  }
  return result
}

async function loadData() {
  loading.value = true
  try {
    const [courseRes, catRes] = await Promise.all([
      getCourseList(query.value),
      getCategoryTree()
    ])
    const pageData = courseRes.data
    courses.value = pageData?.records || pageData || []
    categories.value = flattenTree(catRes.data || [])
  } finally {
    loading.value = false
  }
}

async function handleAdd() {
  if (!newCourse.value.title) return ElMessage.warning('请输入课程名称')
  await createCourse(newCourse.value)
  ElMessage.success('创建成功')
  showAddDialog.value = false
  newCourse.value = { title: '', categoryId: null, description: '' }
  loadData()
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadData()
  checkUploadPerm()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
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

.course-card {
  margin-bottom: 16px;
  cursor: pointer;
}
.course-card--mobile {
  margin-bottom: 8px;
}
.course-card :deep(.el-card__body) {
  padding: 12px;
}
.course-card--mobile :deep(.el-card__body) {
  padding: 8px;
}

.course-card__cover {
  height: 100px;
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}
.course-card--mobile .course-card__cover {
  height: 72px;
}
.course-card--mobile .course-card__cover .el-icon {
  font-size: 28px;
}

.course-card__title {
  margin: 0 0 6px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.course-card--mobile .course-card__title {
  font-size: 13px;
  margin-bottom: 4px;
}

.course-card__meta {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 12px;
}
</style>

<template>
  <div>
    <el-card>
      <el-empty v-if="!records.length" description="暂无考试记录" />
      <div v-for="r in records" :key="r.id" style="padding: 16px; border-bottom: 1px solid #f0f0f0">
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h3 style="margin: 0">{{ r.paperTitle }}</h3>
          <el-tag :type="r.passed ? 'success' : 'danger'" size="large">
            {{ r.score }}分 / {{ r.totalScore }}分 — {{ r.passed ? '通过' : '未通过' }}
          </el-tag>
        </div>
        <div style="color: #999; margin-top: 8px">用时：{{ r.timeUsed }} | 考试时间：{{ r.createTime }}</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyRecords } from '../api/exam'

const records = ref([])

onMounted(async () => {
  const res = await getMyRecords()
  records.value = res.data || []
})
</script>

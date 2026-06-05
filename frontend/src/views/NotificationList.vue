<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0">消息通知</h2>
      <el-button text type="primary" @click="handleReadAll">全部已读</el-button>
    </div>

    <el-card v-for="n in notifications" :key="n.id" shadow="hover" style="margin-bottom: 8px; cursor: pointer" :style="{ background: n.isRead ? '#fff' : '#f0f7ff' }" @click="handleRead(n)">
      <div style="display: flex; align-items: center; gap: 12px">
        <el-icon :size="20" :color="typeColor(n.type)">
          <Bell v-if="n.type === 'system'" />
          <ChatDotSquare v-else-if="n.type === 'comment'" />
          <Star v-else-if="n.type === 'like'" />
          <ChatRound v-else />
        </el-icon>
        <div style="flex: 1">
          <div style="display: flex; align-items: center; gap: 8px">
            <span style="font-weight: bold">{{ n.title }}</span>
            <el-tag v-if="!n.isRead" type="danger" size="small">未读</el-tag>
          </div>
          <div style="color: #999; font-size: 13px; margin-top: 4px">{{ n.content }}</div>
        </div>
        <div style="color: #ccc; font-size: 12px">{{ n.createTime }}</div>
      </div>
    </el-card>

    <el-empty v-if="!notifications.length" description="暂无通知" />

    <el-pagination
      v-model:current-page="pageNum"
      :page-size="10"
      :total="total"
      layout="prev, pager, next"
      @current-change="loadData"
      style="margin-top: 16px; justify-content: center"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNotifications, markRead, markAllRead } from '../api/community'
import { ElMessage } from 'element-plus'

const notifications = ref([])
const pageNum = ref(1)
const total = ref(0)

function typeColor(type) {
  const map = { like: '#E6A23C', comment: '#409EFF', reply: '#67C23A', system: '#909399' }
  return map[type] || '#909399'
}

async function loadData() {
  const res = await getNotifications({ pageNum: pageNum.value, pageSize: 10 })
  notifications.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function handleRead(n) {
  if (n.isRead) return
  await markRead(n.id)
  n.isRead = 1
}

async function handleReadAll() {
  await markAllRead()
  ElMessage.success('已全部标记已读')
  loadData()
}

onMounted(loadData)
</script>
